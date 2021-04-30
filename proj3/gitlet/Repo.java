package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/** Gitlet repository that handles all commands.
 *  @author Colby Chang
 */
public class Repo implements Serializable {

    /** Creates repository.
     *  @param folder folder in which to save repo
     */
    public Repo(File folder) {
        _gitletfolder = new File(folder.getAbsolutePath());
        _head = Utils.join(_gitletfolder, "HEAD");
        _stagingarea = Utils.join(_gitletfolder, "STAGINGAREA");
        _refs = Utils.join(_gitletfolder, "refs");
        _heads = Utils.join(_refs, "heads");
        _blobs = Utils.join(_refs, "blobs");
        _commits = Utils.join(_refs, "commits");
        _remotes = new HashMap<String, String>();
    }
    /** Initializes repository. */
    public void init() {
        Commit initCommit = new Commit("initial commit", null, null);
        _refs.mkdir();
        _heads.mkdir();
        _blobs.mkdir();
        _commits.mkdir();
        initCommit.saveCommit(_commits);
        new Branch("master", initCommit, _commits, _heads).saveBranch();
        new StagingArea().saveStagingArea();
        try {
            _head.createNewFile();
            _stagingarea.createNewFile();
        } catch (IOException io) {
            System.exit(0);
        }
        Utils.writeContents(_head, "refs/heads/master");
        saveRepo();
    }

    /** Stages file for addition.
     *  @param file file to be added
     */
    public void add(File file) {
        Blob blob = new Blob(file);
        blob.saveBlob(_blobs);
        Commit headCommit = getCurrentBranch().getHead();
        StagingArea area = StagingArea.fromFile(_gitletfolder);
        if (!headCommit.isTracking(blob)) {
            area.stageForAddition(blob.getName(), Utils.sha1(Utils.serialize(
                    blob)));
        } else if (area.isStagedForAddition(blob.getName())) {
            area.unstageAddition(blob.getName());
        } else if (area.isStagedForRemoval(blob.getName())) {
            area.unstageRemoval(blob.getName());
        }
        area.saveStagingArea();
    }

    /** Creates commit based on staging area.
     *  @param message message of commit
     */
    public void createCommit(String message) {
        StagingArea area = StagingArea.fromFile(_gitletfolder);
        Branch currentBranch = getCurrentBranch();
        Commit parent = currentBranch.getHead();
        HashMap<String, String> blobs = new HashMap<String, String>();
        if (parent.hasBlobs()) {
            for (String fileName : parent.getFileNames()) {
                if (!area.isStagedForRemoval(fileName)) {
                    blobs.put(fileName, parent.getBlobHash(fileName));
                }
            }
        }
        for (String fileName : area.filesStagedForAddition()) {
            blobs.put(fileName, area.getFromAddition(fileName));
        }
        Commit newCommit = new Commit(message, parent, blobs);
        newCommit.saveCommit(_commits);
        currentBranch.updateBranchHead(newCommit);
        area.clear();
        area.saveStagingArea();
    }

    /** Stages file for removal.
     *  @param file file to be removed
     */
    public void remove(File file) {
        String fileName = file.getName();
        Commit headCommit = getCurrentBranch().getHead();
        StagingArea area = StagingArea.fromFile(_gitletfolder);
        if (!(area.isStagedForAddition(fileName)
                || headCommit.isTrackingFile(file))) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        } else if (area.isStagedForAddition(fileName)) {
            area.unstageAddition(fileName);
        }
        if (headCommit.isTrackingFile(file)) {
            area.stageForRemoval(fileName);
            Utils.restrictedDelete(fileName);
        }
        area.saveStagingArea();
    }

    /** Prints all commits in current branch, in order from most recent. */
    public void printLog() {
        Commit commit = getCurrentBranch().getHead();
        while (commit != null) {
            System.out.println("===");
            System.out.println("commit " + Utils.sha1(Utils.serialize(
                    commit)));
            if (commit.isMergeCommit()) {
                System.out.println("Merge: " + commit.getParent().
                        substring(0, 7) + " " + commit.getMergeParent().
                        substring(0, 7));
            }
            System.out.println("Date: " + commit.getTimestamp());
            System.out.println(commit.getMessage());
            System.out.println();
            if (commit.getParent() == null) {
                commit = null;
            } else {
                commit = Utils.readObject(Utils.join(_commits,
                        commit.getParent()), Commit.class);
            }
        }
    }

    /** Prints all commits in no particular order. */
    public void printGlobalLog() {
        for (File commitFile : _commits.listFiles()) {
            Commit commit = Utils.readObject(commitFile, Commit.class);
            System.out.println("===");
            System.out.println("commit " + Utils.sha1(Utils.serialize(
                    commit)));
            if (commit.isMergeCommit()) {
                System.out.println("Merge: " + commit.getParent().
                        substring(0, 7) + " " + commit.getMergeParent().
                        substring(0, 7));
            }
            System.out.println("Date: " + commit.getTimestamp());
            System.out.println(commit.getMessage());
            System.out.println();
        }
    }

    /** Prints hashes of all commits with given commit message.
     *  @param message message of commit
     */
    public void find(String message) {
        boolean found  = false;
        for (File commitFile : _commits.listFiles()) {
            Commit commit = Utils.readObject(commitFile, Commit.class);
            if (commit.getMessage().equals(message)) {
                found = true;
                System.out.println(Utils.sha1(Utils.serialize(commit)));
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
        System.out.println();
    }

    /** Prints all branches, indicating which one is current branch,
     *  staged files, removed files, modifications that are not staged for
     *  commit, and untracked files.
     */
    public void getStatus() {
        System.out.println("=== Branches ===");
        listBranches();
        listStagedFiles();
        listRemovedFiles();
        listModFiles();
        listUntrackedFiles();
    }

    /** Prints names of all branches, using an '*' to denote current branch. */
    public void listBranches() {
        ArrayList<String> branchNames = new ArrayList<String>();
        File[] p = _heads.listFiles();
        for (File branchFile :p) {
            branchNames.add(Utils.readObject(branchFile, Branch.class).
                    getName());
        }
        Collections.sort(branchNames);
        String currentBranchName = getCurrentBranch().getName();
        for (String branchName : branchNames) {
            if (branchName.equals(currentBranchName)) {
                System.out.println("*" + branchName);
            } else {
                System.out.println(branchName);
            }
        }
        System.out.println();
    }

    /** Prints names of all files staged for addition. */
    public void listStagedFiles() {
        StagingArea area = StagingArea.fromFile(_gitletfolder);
        ArrayList<String> stagedFiles = new ArrayList<String>();
        for (String fileName : area.filesStagedForAddition()) {
            stagedFiles.add(fileName);
            File file = new File(fileName);
        }
        Collections.sort(stagedFiles);
        System.out.println("=== Staged Files ===");
        for (String fileName : stagedFiles) {
            System.out.println(fileName);
        }
        System.out.println();
    }
    /** Prints names of all files staged for removal. */
    public void listRemovedFiles() {
        StagingArea area = StagingArea.fromFile(_gitletfolder);
        ArrayList<String> removedFiles = new ArrayList<String>();
        for (String fileName : area.filesStagedForRemoval()) {
            removedFiles.add(fileName);
        }
        Collections.sort(removedFiles);
        System.out.println("=== Removed Files ===");
        for (String fileName : removedFiles) {
            System.out.println(fileName);
        }
        System.out.println();
    }

    /** Prints names of all files that have been modified but not staged. */
    public void listModFiles() {
        StagingArea area = StagingArea.fromFile(_gitletfolder);
        ArrayList<String> modFiles = new ArrayList<String>();
        Commit currentCommit = getCurrentBranch().getHead();
        for (String fileName : area.filesStagedForAddition()) {
            File file = new File(fileName);
            if (!file.exists()) {
                modFiles.add(fileName + " (deleted)");
            } else if (!Utils.readObject(Utils.join(_blobs,
                    area.getFromAddition(fileName)), Blob.class).getContents().
                    equals(Utils.readContentsAsString(file))) {
                modFiles.add(fileName + " (modified)");
            }
        }
        if (currentCommit.hasBlobs()) {
            for (String fileName : currentCommit.getFileNames()) {
                File file = new File(fileName);
                if (!file.exists()) {
                    if (!area.isStaged(fileName)) {
                        modFiles.add(fileName + " (deleted)");
                    }
                } else {
                    if (!currentCommit.getBlob(fileName, _blobs).getContents().
                            equals(Utils.readContentsAsString(file))
                            && !area.isStaged(fileName)) {
                        modFiles.add(fileName + " (modified)");
                    }
                }
            }
        }
        Collections.sort(modFiles);
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String fileName : modFiles) {
            System.out.println(fileName);
        }
        System.out.println();
    }

    /** Prints names of all untracked files. */
    public void listUntrackedFiles() {
        StagingArea area = StagingArea.fromFile(_gitletfolder);
        Commit currentCommit = getCurrentBranch().getHead();
        ArrayList<String> untrackedFiles = new ArrayList<String>();
        for (File file : new File(".").listFiles()) {
            if (!file.isDirectory() && (!currentCommit.isTrackingFile(file)
                    && !area.isStagedForAddition(file.getName())
                    || area.isStagedForRemoval(file.getName()))) {
                untrackedFiles.add(file.getName());
            }
        }
        Collections.sort(untrackedFiles);
        System.out.println("=== Untracked Files ===");
        for (String fileName : untrackedFiles) {
            System.out.println(fileName);
        }
        System.out.println();
    }

    /** Makes file in the working directory the same as the version in the
     *  head commit.
     *  @param fileName file to be checked out
     */
    public void checkoutFile(String fileName) {
        Commit commit = getCurrentBranch().getHead();
        File file = new File(fileName);
        writeFromCommit(commit, file);
    }

    /** Makes file in the working directory the same as the version in the
     *  given commit.
     *  @param commitFile file of commit to be checked out from
     *  @param fileName file to be checked out
     */
    public void checkoutFileFromCommit(File commitFile,
                                              String fileName) {
        Commit commit = Utils.readObject(commitFile, Commit.class);
        File file = new File(fileName);
        writeFromCommit(commit, file);
    }

    /** Makes all files in the working directory the same as the version in the
     *  given branch. Also makes the given branch the current one.
     *  @param branchFile file of branch to be checked out
     */
    public void checkoutBranch(File branchFile) {
        Branch branch = Utils.readObject(branchFile, Branch.class);
        Branch currentBranch = getCurrentBranch();
        if (branch.getName().equals(currentBranch.getName())
                && branch.getDirectory().equals(currentBranch.
                getDirectory())) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        Commit commit = branch.getHead();
        removeExtraFiles(commit);
        if (!currentBranch.getName().equals(branch.getName())) {
            StagingArea area = StagingArea.fromFile(_gitletfolder);
            area.clear();
            area.saveStagingArea();
        }
        writeAllFromCommit(commit);
        Utils.writeContents(_head, branchFile.getPath().substring(
                branchFile.getPath().indexOf("refs/heads/")));
    }

    /** Creates a new branch at the current head commit.
     *  @param name name of new branch
     */
    public void createBranch(String name) {
        Branch currentBranch = getCurrentBranch();
        new Branch(name, currentBranch.getHead(), _commits, _heads).
                saveBranch();
    }

    /** Removes an existing branch.
     *  @param name name of branch to be removed
     */
    public void removeBranch(String name) {
        Branch currentBranch = getCurrentBranch();
        if (currentBranch.getName().equals(name)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        Utils.join(_heads, name).delete();
    }

    /** Reverts working directory to a different version dictated by commit.
     *  @param commitFile file of commit to be reverted to
     */
    public void resetToCommit(File commitFile) {
        Commit commit = Utils.readObject(commitFile, Commit.class);
        Branch currentBranch = getCurrentBranch();
        removeExtraFiles(commit);
        writeAllFromCommit(commit);
        currentBranch.updateBranchHead(commit);
        StagingArea area = StagingArea.fromFile(_gitletfolder);
        area.clear();
        area.saveStagingArea();
    }

    /** Merges given branch into current branch, if they can be merged, into
     *  a new commit.
     *  @param branchFile file of branch to be merged from
     */
    public void mergeBranch(File branchFile) {
        StagingArea area = StagingArea.fromFile(_gitletfolder);
        if (!area.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        Branch currentBranch = getCurrentBranch();
        Branch givenBranch = Utils.readObject(branchFile, Branch.class);
        if (givenBranch.getName().equals(currentBranch.getName())
                && givenBranch.getDirectory().equals(currentBranch.
                        getDirectory())) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        Commit splitPoint = findSplitPoint(currentBranch, givenBranch);
        if (splitPoint.equals(givenBranch.getHead())) {
            System.out.println("Given branch is an ancestor of the current"
                    + " branch.");
            System.exit(0);
        } else if (splitPoint.equals(currentBranch.getHead())) {
            checkoutBranch(branchFile);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        Commit givenCommit = givenBranch.getHead();
        Commit currentCommit = currentBranch.getHead();
        if (givenCommit.hasBlobs()) {
            for (String fileName : givenCommit.getFileNames()) {
                File file = new File(fileName);
                if (file.exists() && !currentCommit.isTrackingFile(file)
                        && !currentCommit.checkSameVersion(file.getName(),
                        givenCommit)) {
                    System.out.println("There is an untracked file in "
                            + "the way; delete it, or add and commit it "
                            + "first.");
                    System.exit(0);
                }
            }
        }
        boolean conflict = mergeFiles(currentCommit, givenCommit, splitPoint);
        if (givenBranch.getDirectory().equals(currentBranch.getDirectory())) {
            createMergeCommit("Merged " + givenBranch.getName() + " into "
                    + currentBranch.getName() + ".", givenCommit);
        } else {
            createMergeCommit("Merged " + givenBranch.getDirectory().getName()
                    + "/" + givenBranch.getName() + " into "
                    + currentBranch.getName() + ".", givenCommit);
        }
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /** Returns the splitpoint of the current branch and a given branch.
     *  @param cBranch current branch
     *  @param gBranch given branch
     *  @return split point of two branches
     */
    public Commit findSplitPoint(Branch cBranch, Branch gBranch) {
        HashSet<String> givenParents = new HashSet<String>();
        Commit givenParent = gBranch.getHead();
        HashSet<Commit> prevParents = new HashSet<Commit>();
        prevParents.add(givenParent);
        givenParents.add(Utils.sha1(Utils.serialize(givenParent)));
        while (!prevParents.isEmpty()) {
            HashSet<Commit> nextParents = new HashSet<Commit>();
            for (Commit child : prevParents) {
                if (child.getParent() != null) {
                    nextParents.add(Utils.readObject(Utils.join(_commits,
                            child.getParent()), Commit.class));
                    if (child.isMergeCommit()) {
                        nextParents.add(Utils.readObject(Utils.join(_commits,
                                child.getMergeParent()), Commit.class));
                    }
                }
            }
            for (Commit parent : nextParents) {
                givenParents.add(Utils.sha1(Utils.serialize(parent)));
            }
            prevParents = nextParents;
        }
        HashSet<Commit> parents = new HashSet<Commit>();
        Commit currentParent = cBranch.getHead();
        parents.add(currentParent);
        while (true) {
            HashSet<String> ids = new HashSet<String>();
            for (Commit parent : parents) {
                ids.add(Utils.sha1(Utils.serialize(parent)));
            }
            for (String id : ids) {
                if (givenParents.contains(id)) {
                    return Utils.readObject(Utils.join(_commits, id),
                            Commit.class);
                }
            }
            HashSet<Commit> newParents = new HashSet<Commit>();
            for (Commit parent : parents) {
                newParents.add(Utils.readObject(Utils.join(_commits,
                        parent.getParent()), Commit.class));
                if (parent.isMergeCommit()) {
                    newParents.add(Utils.readObject(Utils.join(_commits,
                            parent.getMergeParent()), Commit.class));
                }
            }
            parents = newParents;
        }
    }

    /** Stages all files present in the merge commit of the current and given
     *  commits.
     *  @param currentCommit current commit
     *  @param givenCommit given commit
     *  @param splitPoint split point between branches
     *  @return true if there is a merge conflict
     */
    public boolean mergeFiles(Commit currentCommit, Commit givenCommit,
                                     Commit splitPoint) {
        boolean conflict = false;
        if (handleFilesInCurrent(currentCommit, givenCommit, splitPoint)) {
            conflict = true;
        }
        if (handleFilesInGiven(currentCommit, givenCommit, splitPoint)) {
            conflict = true;
        }
        return conflict;
    }

    /** Stages files for merged commit that are in the current commit.
     * @param currentCommit current commit
     * @param givenCommit given commit
     * @param splitPoint split point between branches
     * @return true if there is a merge conflict
     */
    public boolean handleFilesInCurrent(Commit currentCommit,
                                               Commit givenCommit,
                                               Commit splitPoint) {
        boolean conflict = false;
        StagingArea area = StagingArea.fromFile(_gitletfolder);
        for (String fileName : currentCommit.getFileNames()) {
            File file = new File(fileName);
            if (splitPoint.isTrackingFile(file)) {
                if (splitPoint.checkSameVersion(fileName, currentCommit)
                        && !splitPoint.checkSameVersion(fileName,
                        givenCommit)) {
                    if (givenCommit.isTrackingFile(file)) {
                        writeFromCommit(givenCommit, file);
                        area.stageForAddition(fileName, givenCommit.
                                getBlobHash(fileName));
                    } else {
                        Utils.restrictedDelete(file);
                        area.stageForRemoval(fileName);
                    }
                } else if (!splitPoint.checkSameVersion(fileName,
                        currentCommit) && !splitPoint.checkSameVersion(
                        fileName, givenCommit) && !currentCommit.
                        checkSameVersion(fileName, givenCommit)) {
                    conflict = true;
                    String currContents = "";
                    if (currentCommit.isTrackingFile(file)) {
                        currContents = currentCommit.getBlob(fileName, _blobs).
                                getContents();
                    }
                    String givenContents = "";
                    if (givenCommit.isTrackingFile(file)) {
                        givenContents = givenCommit.getBlob(fileName, _blobs).
                                getContents();
                    }
                    Utils.writeContents(file, "<<<<<<< HEAD\n" + currContents
                            + "=======\n" + givenContents + ">>>>>>>\n");
                    Blob blob = new Blob(file);
                    blob.saveBlob(_blobs);
                    area.stageForAddition(fileName, Utils.sha1(Utils.serialize(
                            blob)));
                }
            } else if (givenCommit.isTrackingFile(file)
                    && !currentCommit.checkSameVersion(fileName,
                    givenCommit)) {
                conflict = true;
                String currContents = currentCommit.getBlob(fileName, _blobs).
                        getContents();
                String givenContents = givenCommit.getBlob(fileName, _blobs).
                        getContents();
                Utils.writeContents(file, "<<<<<<< HEAD\n" + currContents
                        + "=======\n" + givenContents + ">>>>>>>\n");
                Blob blob = new Blob(file);
                blob.saveBlob(_blobs);
                area.stageForAddition(fileName, Utils.sha1(
                        Utils.serialize(blob)));
            }
        }
        area.saveStagingArea();
        return conflict;
    }

    /** Stages files for merged commit that are in the given commit.
     * @param currentCommit current commit
     * @param givenCommit given commit
     * @param splitPoint split point between branches
     * @return true if there is a merge conflict
     */
    public boolean handleFilesInGiven(Commit currentCommit,
                                             Commit givenCommit,
                                             Commit splitPoint) {
        boolean conflict = false;
        StagingArea area = StagingArea.fromFile(_gitletfolder);
        for (String fileName : givenCommit.getFileNames()) {
            File file = new File(fileName);
            if (splitPoint.isTrackingFile(file)) {
                if (!currentCommit.isTrackingFile(file)
                        && !givenCommit.checkSameVersion(
                        fileName, splitPoint)) {
                    conflict = true;
                    Utils.writeContents(file, "<<<<<<< HEAD\n" + "=======\n"
                            + givenCommit.getBlob(fileName, _blobs).
                            getContents() + ">>>>>>>\n");
                    Blob blob = new Blob(file);
                    blob.saveBlob(_blobs);
                    area.stageForAddition(fileName, Utils.sha1(
                            Utils.serialize(blob)));
                }
            } else {
                if (!currentCommit.isTrackingFile(file)) {
                    writeFromCommit(givenCommit, file);
                    area.stageForAddition(fileName, givenCommit.getBlobHash(
                            fileName));
                }
            }
        }
        area.saveStagingArea();
        return conflict;
    }

    /** Creates a commit based on staging area.
     *  @param message message of commit
     *  @param mergeParent commit's second parent from merging
     */
    public void createMergeCommit(String message, Commit mergeParent) {
        StagingArea area = StagingArea.fromFile(_gitletfolder);
        Branch currentBranch = getCurrentBranch();
        Commit parent = currentBranch.getHead();
        HashMap<String, String> blobs = new HashMap<String, String>();
        if (parent.hasBlobs()) {
            for (String fileName : parent.getFileNames()) {
                if (!area.isStagedForRemoval(fileName)) {
                    blobs.put(fileName, parent.getBlobHash(fileName));
                }
            }
        }
        for (String fileName : area.filesStagedForAddition()) {
            blobs.put(fileName, area.getFromAddition(fileName));
        }
        Commit newCommit = new Commit(message, parent, mergeParent, blobs);
        newCommit.saveCommit(_commits);
        currentBranch.updateBranchHead(newCommit);
        area.clear();
        area.saveStagingArea();
    }

    /** Retrieve the current branch from its file.
     *  @return current branch
     */
    public Branch getCurrentBranch() {
        return Utils.readObject(Utils.join(
                _gitletfolder, Utils.readContentsAsString(
                        _head)), Branch.class);
    }

    /** Overwrites a previous file from a given commit, and creates a new file
     *  if file does not exist.
     *  @param commit commit to be written from
     *  @param file file to be written over
     */
    public void writeFromCommit(Commit commit, File file) {
        if (!commit.isTrackingFile(file)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException io) {
                System.exit(0);
            }
        }
        Utils.writeContents(file, commit.getBlob(file.getName(), _blobs).
                getContents());
    }

    /** Writes all the files from the previous commit.
     *  @param commit commit to be written from.
     */
    public void writeAllFromCommit(Commit commit) {
        if (commit.hasBlobs()) {
            for (String fileName : commit.getFileNames()) {
                writeFromCommit(commit, new File(fileName));
            }
        }
    }

    /** Removes the files that are tracked by the current commit, but not by
     *  the given one.
     *  @param commit given commit
     */
    public void removeExtraFiles(Commit commit) {
        Branch currentBranch = getCurrentBranch();
        if (commit.hasBlobs()) {
            for (String fileName : commit.getFileNames()) {
                File file = new File(fileName);
                if (file.exists() && !currentBranch.
                        getHead().isTrackingFile(file)
                        && !currentBranch.getHead().checkSameVersion(
                                file.getName(), commit)) {
                    System.out.println("There is an untracked file in "
                            + "the way; delete it, or add and commit it "
                            + "first.");
                    System.exit(0);
                }
            }
        }
        if (currentBranch.getHead().hasBlobs()) {
            for (String fileName : currentBranch.getHead().
                    getFileNames()) {
                if (!commit.isTrackingFile(new File(fileName))) {
                    Utils.restrictedDelete(fileName);
                }
            }
        }
    }

    /** Creates a new remote.
     * @param name remote name
     * @param folder folder in which remote exists
     */
    public void createRemote(String name, File folder) {
        _remotes.put(name, folder.getPath());
        saveRepo();
    }

    /** Removes an existing remote.
     *  @param name remote name
     */
    public void removeRemote(String name) {
        deleteAllFiles(new File(_remotes.get(name)));
        _remotes.remove(name);
        saveRepo();
    }

    /** Deletes all files in directory and directory itself.
     *  @param dir directory to be deleted
     */
    public void deleteAllFiles(File dir) {
        if (dir.exists()) {
            if (dir.isDirectory()) {
                for (File f : dir.listFiles()) {
                    deleteAllFiles(f);
                }
            }
            dir.delete();
        }
    }

    /** Appends current branch's commits to the end of the given branch in the
     *  remote with given name.
     *  @param name remote name
     *  @param branchName branch name
     */
    public void pushToRemoteBranch(String name, String branchName) {
        File remoteFile = new File(_remotes.get(name));
        if (!remoteFile.exists()) {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }
        Repo remote = Repo.fromFile(remoteFile);
        File remoteBranchFile = Utils.join(remote._heads, branchName);
        Commit currentCommit = getCurrentBranch().getHead();
        if (remoteBranchFile.exists()) {
            Branch remoteBranch = Utils.readObject(remoteBranchFile,
                    Branch.class);
            if (headInHistory(remoteBranch)) {
                Commit remoteHead = remoteBranch.getHead();
                remoteBranch.updateBranchHead(currentCommit);
                while (!Utils.sha1(Utils.serialize(remoteHead)).equals(
                        Utils.sha1(Utils.serialize(currentCommit)))) {
                    currentCommit.saveCommit(remote._commits);
                    for (String fileName : currentCommit.getFileNames()) {
                        currentCommit.getBlob(fileName, _blobs).saveBlob(
                                remote._blobs);
                    }
                    currentCommit = Utils.readObject(Utils.join(_commits,
                            currentCommit.getParent()), Commit.class);
                }
            } else {
                System.out.println("Please pull down remote changes before "
                        + "pushing.");
                System.exit(0);
            }
        } else {
            try {
                remoteBranchFile.createNewFile();
            } catch (IOException io) {
                System.exit(0);
            }
            new Branch(branchName, currentCommit, remote._commits,
                    remote._heads).saveBranch();
            currentCommit.saveCommit(remote._commits);
            if (currentCommit.hasBlobs()) {
                for (String fileName : currentCommit.getFileNames()) {
                    currentCommit.getBlob(fileName, _blobs).saveBlob(
                            remote._blobs);
                }
                while (currentCommit.getParent() != null) {
                    currentCommit = Utils.readObject(Utils.join(_commits,
                            currentCommit.getParent()), Commit.class);
                    currentCommit.saveCommit(remote._commits);
                    for (String fileName : currentCommit.getFileNames()) {
                        currentCommit.getBlob(fileName, _blobs).saveBlob(
                                remote._blobs);
                    }
                }
            }
        }
    }

    /** Checks if the given branch's head is in history of the current branch.
     *  @param remoteBranch branch in remote repository
     *  @return true if the head is in this repository's current branch history
     */
    public boolean headInHistory(Branch remoteBranch) {
        Commit currentCommit = getCurrentBranch().getHead();
        Commit remoteHead = remoteBranch.getHead();
        while (currentCommit.getParent() != null) {
            if (Utils.sha1(Utils.serialize(remoteHead)).equals(Utils.sha1(
                    Utils.serialize(currentCommit)))) {
                return true;
            }
            currentCommit = Utils.readObject(Utils.join(_commits,
                    currentCommit.getParent()), Commit.class);
        }
        return Utils.sha1(Utils.serialize(remoteHead)).equals(Utils.sha1(
                Utils.serialize(currentCommit)));
    }

    /** Copies commits from the remote repository into the local repository.
     *  @param name remote name
     *  @param branchName branch name
     */
    public void fetchFromRemoteBranch(String name, String branchName) {
        File remoteFile = new File(_remotes.get(name));
        if (!remoteFile.exists()) {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }
        Repo remote = Repo.fromFile(remoteFile);
        File remoteBranchFile = Utils.join(remote._heads, branchName);
        if (remoteBranchFile.exists()) {
            Branch remoteBranch = Utils.readObject(remoteBranchFile,
                    Branch.class);
            File remoteHeads = Utils.join(_heads, name);
            File branchFile = Utils.join(remoteHeads, branchName);
            Commit currentBranchCommit = remoteBranch.getHead();
            if (!branchFile.exists()) {
                createFromRemoteBranch(remote, branchName, remoteHeads,
                        currentBranchCommit);
            } else {
                updateFromRemoteBranch(remote, branchFile, currentBranchCommit);
            }
        } else {
            System.out.println("That remote does not have that branch.");
            System.exit(0);
        }
    }

    /** Creates a new branch from a given remote branch.
     *  @param remote remote repository
     *  @param branchName name of branch from remote
     *  @param remoteHeads directory that contains branches from remote
     *  @param currentBranchCommit remote branch head
     */
    public void createFromRemoteBranch(Repo remote, String branchName,
                                       File remoteHeads,
                                       Commit currentBranchCommit) {
        if (!remoteHeads.exists()) {
            remoteHeads.mkdir();
        }
        new Branch(branchName, currentBranchCommit,
                _commits, remoteHeads).saveBranch();
        currentBranchCommit.saveCommit(_commits);
        if (currentBranchCommit.hasBlobs()) {
            for (String fileName : currentBranchCommit.
                    getFileNames()) {
                currentBranchCommit.getBlob(fileName, remote._blobs).
                        saveBlob(_blobs);
            }
        }
        while (currentBranchCommit.getParent() != null) {
            currentBranchCommit = Utils.readObject(Utils.join(
                    remote._commits, currentBranchCommit.
                            getParent()), Commit.class);
            currentBranchCommit.saveCommit(_commits);
            if (currentBranchCommit.hasBlobs()) {
                for (String fileName : currentBranchCommit.
                        getFileNames()) {
                    currentBranchCommit.getBlob(fileName,
                            remote._blobs).saveBlob(_blobs);
                }
            }
        }
    }

    /** Updates a branch from a given remote branch.
     *  @param remote remote repository
     *  @param branchFile file where branch to be updated is stored
     *  @param currentBranchCommit remote branch head
     */
    public void updateFromRemoteBranch(Repo remote, File branchFile,
                                       Commit currentBranchCommit) {
        Branch branch = Utils.readObject(branchFile, Branch.class);
        Commit head = branch.getHead();
        branch.updateBranchHead(currentBranchCommit);
        while (!Utils.sha1(Utils.serialize(head)).equals(
                Utils.sha1(Utils.serialize(currentBranchCommit)))) {
            currentBranchCommit.saveCommit(_commits);
            if (currentBranchCommit.hasBlobs()) {
                for (String fileName : currentBranchCommit.
                        getFileNames()) {
                    currentBranchCommit.getBlob(fileName,
                            remote._blobs).saveBlob(_blobs);
                }
            }
            currentBranchCommit = Utils.readObject(Utils.join(_commits,
                    currentBranchCommit.getParent()), Commit.class);
        }
    }

    /** Fetches given branch from remote and merges with current branch.
     * @param name remote name
     * @param branchName branch name
     */
    public void pullFromRemoteBranch(String name, String branchName) {
        fetchFromRemoteBranch(name, branchName);
        mergeBranch(Utils.join(_heads, name + "/" + branchName));
    }

    /** Saves repository to the repo file to be accessed later. */
    public void saveRepo() {
        Utils.writeObject(Utils.join(_gitletfolder, "repo"), this);
    }

    /** Retrieves the repository from the repo file.
     *  @param folder directory in which repository is stored
     *  @return repository being worked with
     */
    public static Repo fromFile(File folder) {
        return Utils.readObject(Utils.join(folder, "repo"),
                Repo.class);
    }

    /** Returns _gitletfolder. */
    public File getGitletFolder() {
        return _gitletfolder;
    }

    /** Returns _head. */
    public File getHead() {
        return _head;
    }

    /** Returns _stagingarea. */
    public File getStagingArea() {
        return _stagingarea;
    }

    /** Returns _refs. */
    public File getRefs() {
        return _refs;
    }

    /** Returns _heads. */
    public File getHeads() {
        return _heads;
    }

    /** Returns _head. */
    public File getBlobs() {
        return _blobs;
    }

    /** Returns _head. */
    public File getCommits() {
        return _commits;
    }

    /** Returns _remotes. */
    public Set<String> getRemoteNames() {
        return _remotes.keySet();
    }

    /** Folder that contains all of files. */
    private File _gitletfolder;

    /** File that contains file path to current head. */
    private File _head;

    /** File that contains the staging area. */
    private File _stagingarea;

    /** Folder that contains all references. */
    private File _refs;

    /** Folder that contains heads of all branches. */
    private File _heads;

    /** Folder that contains all blobs that exist in the repository. */
    private File _blobs;

    /** Folder that contains all the commits made in the repository. */
    private File _commits;

    /** Folder that contains all the remotes made. */
    private HashMap<String, String> _remotes;
}

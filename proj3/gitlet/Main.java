package gitlet;

import java.io.File;
import java.util.regex.Pattern;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Colby Chang
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        handleCases(args);
    }

    /** Handles cases for arguments.
     *  @param args inputs from terminal
     */
    public static void handleCases(String[] args) {
        switch (args[0]) {
        case "init":
            initGitlet(args);
            break;
        case "add":
            add(args);
            break;
        case "commit":
            createCommit(args);
            break;
        case "rm":
            remove(args);
            break;
        case "log":
            printLog(args);
            break;
        case "global-log":
            printGlobalLog(args);
            break;
        case "find":
            find(args);
            break;
        case "status":
            getStatus(args);
            break;
        case "checkout":
            checkout(args);
            break;
        case "branch":
            createBranch(args);
            break;
        case "rm-branch":
            removeBranch(args);
            break;
        case "reset":
            resetToCommit(args);
            break;
        case "merge":
            mergeBranch(args);
            break;
        case "add-remote":
            createRemote(args);
            break;
        case "rm-remote":
            removeRemote(args);
            break;
        case "push":
            push(args);
            break;
        case "fetch":
            fetch(args);
            break;
        case "pull":
            pull(args);
            break;
        default:
            noCommandExit();
        }
    }

    /** Initializes gitlet, if not already initialized.
     *  @param args command that starts init
     */
    public static void initGitlet(String[] args) {
        validateNumArgs(args, 1);
        if (!GITLET_FOLDER.exists()) {
            GITLET_FOLDER.mkdir();
            new Repo(GITLET_FOLDER).init();
        } else {
            System.out.println("A Gitlet version-control system already "
                    + "exists in the current directory.");
            System.exit(0);
        }
    }

    /** Stages given file to be added in staging area.
     *  @param args command that includes file to be added
     */
    public static void add(String[] args) {
        checkInitialized();
        validateNumArgs(args, 2);
        File file = new File(args[1]);
        if (file.exists()) {
            Repo.fromFile(GITLET_FOLDER).add(file);
        } else {
            System.out.println("File does not exist.");
            System.exit(0);
        }
    }

    /** Creates a commit, if there is a new commit to be made.
     *  @param args command that includes the commit message
     */
    public static void createCommit(String[] args) {
        checkInitialized();
        if (args.length == 1 || args[1].equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        validateNumArgs(args, 2);
        if (StagingArea.fromFile(GITLET_FOLDER).isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        Repo.fromFile(GITLET_FOLDER).createCommit(args[1]);
    }

    /** Stages given file to be removed in staging area.
     *  @param args command that includes file to be removed.
     */
    public static void remove(String[] args) {
        checkInitialized();
        validateNumArgs(args, 2);
        Repo.fromFile(GITLET_FOLDER).remove(new File(args[1]));
    }

    /** Prints out the commits, in order, of the current branch.
     *  @param args command that starts log
     */
    public static void printLog(String[] args) {
        checkInitialized();
        validateNumArgs(args, 1);
        Repo.fromFile(GITLET_FOLDER).printLog();
    }

    /** Prints out all commits, in no particular order.
     *  @param args command that starts global log
     */
    public static void printGlobalLog(String[] args) {
        checkInitialized();
        validateNumArgs(args, 1);
        Repo.fromFile(GITLET_FOLDER).printGlobalLog();
    }

    /** Prints ids of all commits with the given message.
     *  @param args command that includes the message of commits to be printed
     */
    public static void find(String[] args) {
        checkInitialized();
        validateNumArgs(args, 2);
        Repo.fromFile(GITLET_FOLDER).find(args[1]);
    }

    /** Displays current status of gitlet.
     *  @param args command that starts status.
     */
    public static void getStatus(String[] args) {
        checkInitialized();
        validateNumArgs(args, 1);
        Repo.fromFile(GITLET_FOLDER).getStatus();
    }

    /** Checks out a certain version of a file or checks out an entire branch.
     *  @param args command that includes what file or branch to check out
     */
    public static void checkout(String[] args) {
        checkInitialized();
        Repo repo = Repo.fromFile(GITLET_FOLDER);
        switch (args.length) {
        case 3:
            if (!args[1].equals("--")) {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
            repo.checkoutFile(args[2]);
            break;
        case 4:
            if (!args[2].equals("--")) {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
            String commit = checkAbbreviatedID(args[1]);
            File commitFile = Utils.join(repo.getCommits(), commit);
            if (commitFile.exists()) {
                repo.checkoutFileFromCommit(commitFile, args[3]);
            } else {
                System.out.println("No commit with that id exists.");
                System.exit(0);
            }
            break;
        case 2:
            File branchFile = Utils.join(repo.getHeads(), args[1]);
            if (branchFile.exists()) {
                repo.checkoutBranch(branchFile);
            } else {
                System.out.println("No such branch exists.");
                System.exit(0);
            }
            break;
        default:
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    /** Creates a new branch, if it does not already exist.
     *  @param args command that includes new branch name
     */
    public static void createBranch(String[] args) {
        checkInitialized();
        validateNumArgs(args, 2);
        Repo repo = Repo.fromFile(GITLET_FOLDER);
        if (Utils.join(repo.getHeads(), args[1]).exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        } else {
            repo.createBranch(args[1]);
        }
    }

    /** Removes a branch, if it exists.
     *  @param args command that includes name of branch to be removed
     */
    public static void removeBranch(String[] args) {
        checkInitialized();
        validateNumArgs(args, 2);
        Repo repo = Repo.fromFile(GITLET_FOLDER);
        File branchFile = Utils.join(repo.getHeads(), args[1]);
        if (!branchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        } else {
            repo.removeBranch(args[1]);
        }
    }

    /** Checks out all files in a certain commit.
     *  @param args command that includes the hash of the commit to be reset to
     */
    public static void resetToCommit(String[] args) {
        checkInitialized();
        validateNumArgs(args, 2);
        Repo repo = Repo.fromFile(GITLET_FOLDER);
        File commitFile = Utils.join(repo.getCommits(), checkAbbreviatedID(
                args[1]));
        if (commitFile.exists()) {
            repo.resetToCommit(commitFile);
        } else {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
    }

    /** Merges files from given branch into current branch.
     *  @param args command that includes the branch to merge from
     */
    public static void mergeBranch(String[] args) {
        checkInitialized();
        validateNumArgs(args, 2);
        Repo repo = Repo.fromFile(GITLET_FOLDER);
        File branchFile = Utils.join(repo.getHeads(), args[1]);
        if (!branchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        } else {
            repo.mergeBranch(branchFile);
        }
    }

    /** Creates a new remote.
     *  @param args command that includes remote name and directory
     */
    public static void createRemote(String[] args) {
        checkInitialized();
        validateNumArgs(args, 3);
        if (!Pattern.matches("(.+/)+[.]gitlet", args[2])) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        Repo repo = Repo.fromFile(GITLET_FOLDER);
        if (repo.getRemoteNames().contains(args[1])) {
            System.out.println("A remote with that name already exists.");
            System.exit(0);
        } else {
            String[] path = args[2].split("[/]");
            File remoteFolder = new File(".");
            for (int i = 0; i < path.length; i++) {
                remoteFolder = Utils.join(remoteFolder, path[i]);
            }
            repo.createRemote(args[1], remoteFolder);
        }
    }

    /** Removes a remote, if it exists.
     * @param args command that includes the name of remote to be removed
     */
    public static void removeRemote(String[] args) {
        checkInitialized();
        validateNumArgs(args, 2);
        Repo repo = Repo.fromFile(GITLET_FOLDER);
        if (repo.getRemoteNames().contains(args[1])) {
            repo.removeRemote(args[1]);
        } else {
            System.out.println("A remote with that name does not exist.");
            System.exit(0);
        }
    }

    /** Pushes updated changes to a given remote branch.
     *  @param args command that includes the name of remote and name of branch
     */
    public static void push(String[] args) {
        checkInitialized();
        validateNumArgs(args, 3);
        Repo repo = Repo.fromFile(GITLET_FOLDER);
        if (repo.getRemoteNames().contains(args[1])) {
            repo.pushToRemoteBranch(args[1], args[2]);
        } else {
            System.out.println("Remote directory not found.");
        }
    }

    /** Fetches updated changes from a given remote branch.
     *  @param args command that includes the name of remote and name of branch
     */
    public static void fetch(String[] args) {
        checkInitialized();
        validateNumArgs(args, 3);
        Repo repo = Repo.fromFile(GITLET_FOLDER);
        if (repo.getRemoteNames().contains(args[1])) {
            repo.fetchFromRemoteBranch(args[1], args[2]);
        } else {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }
    }

    /** Pulls updated changes from a given remote branch.
     *  @param args command that includes the name of remote and name of branch
     */
    public static void pull(String[] args) {
        checkInitialized();
        validateNumArgs(args, 3);
        Repo repo = Repo.fromFile(GITLET_FOLDER);
        if (repo.getRemoteNames().contains(args[1])) {
            repo.pullFromRemoteBranch(args[1], args[2]);
        } else {
            System.out.println("Remote directory not found.");
        }
    }

    /** Checks for an abbreviated ID.
     *  @param id potentially abbreviated id
     *  @return full length hash if unique, otherwise returns id
     */
    public static String checkAbbreviatedID(String id) {
        String commit = null;
        Repo repo = Repo.fromFile(GITLET_FOLDER);
        if (id.length() < SHA1_LENGTH) {
            for (File f : repo.getCommits().listFiles()) {
                if (f.getName().substring(0, id.length()).equals(
                        id)) {
                    if (commit != null) {
                        commit = id;
                        break;
                    }
                    commit = f.getName();
                }
            }
        } else {
            commit = id;
        }
        return commit;
    }

    /** Checks if the repository has already been initialized. */
    public static void checkInitialized() {
        if (!GITLET_FOLDER.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    /** Checks the number of arguments versus the expected number,
     *  exits if they do not match.
     *  @param args Argument array from command line
     *  @param n Number of expected arguments
     */
    public static void validateNumArgs(String[] args, int n) {
        if (args.length != n) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    /** Print case for no command that exists. */
    public static void noCommandExit() {
        System.out.println("No command with that name exists.");
        System.exit(0);
    }

    /** Main metadata folder. */
    static final File GITLET_FOLDER = Utils.join(".", ".gitlet");

    /** Appropriate sha1 hash length. */
    static final int SHA1_LENGTH = 40;
}

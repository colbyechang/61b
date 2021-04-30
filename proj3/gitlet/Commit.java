package gitlet;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.text.SimpleDateFormat;
import java.io.File;

/** Commit that tracks relevant files present at the time of committing.
 *  @author Colby Chang
 */
public class Commit implements Serializable {

    /** Creates a new Commit.
     *  @param message commit message
     *  @param parent parent commit
     *  @param blobs mapping of file names to blob hashes
     */
    public Commit(String message, Commit parent,
                  HashMap<String, String> blobs) {
        _message = message;
        if (parent == null) {
            _timestamp = new SimpleDateFormat("EEE LLL d HH':'mm':'ss yyyy Z").
                    format(new Date(0));
        } else {
            _timestamp = new SimpleDateFormat("EEE LLL d HH':'mm':'ss yyyy Z").
                    format(new Date(System.currentTimeMillis()));
            _parent = Utils.sha1(Utils.serialize(parent));
            _mergeParent = null;
        }
        _blobs = blobs;
    }

    /** Creates a new Commit from a merge.
     *  @param message commit message
     *  @param parent parent commit
     *  @param mergeParent second parent commit from merge
     *  @param blobs mapping of file names to blob hashes
     */
    public Commit(String message, Commit parent, Commit mergeParent,
                  HashMap<String, String> blobs) {
        _message = message;
        _timestamp = new SimpleDateFormat("EEE LLL d HH':'mm':'ss yyyy Z").
                format(new Date(System.currentTimeMillis()));
        _parent = Utils.sha1(Utils.serialize(parent));
        _mergeParent = Utils.sha1(Utils.serialize(mergeParent));
        _blobs = blobs;
    }

    /** Returns this commit's message.
     *  @return commit's message
     */
    public String getMessage() {
        return _message;
    }

    /** Returns this commit's timestamp.
     *  @return commit's timestamp
     */
    public String getTimestamp() {
        return _timestamp;
    }

    /** Returns this commit's parent.
     *  @return commit's parent
     */
    public String getParent() {
        return _parent;
    }

    /** Returns this commit's parent from merge.
     *  @return commit's parent from merge
     */
    public String getMergeParent() {
        return _mergeParent;
    }

    /** Checks if this is a commit resulting from a merge.
     *  @return true if this has a non-null merge parent
     */
    public boolean isMergeCommit() {
        return _mergeParent != null;
    }

    /** Checks if this and commit have the same sha1 hash.
     *  @param commit commit to be checked
     *  @return true if this and commit have the same sha1hash
     */
    public boolean equals(Commit commit) {
        return Utils.sha1(Utils.serialize(this)).equals(
                Utils.sha1(Utils.serialize(commit)));
    }

    /** Returns the blob associated with fileName.
     *  @param fileName file name
     *  @param blobs directory in which blobs are stored
     *  @return blob associated with file name
     */
    public Blob getBlob(String fileName, File blobs) {
        return Utils.readObject(Utils.join(blobs, _blobs.get(fileName)),
                Blob.class);
    }

    /** Checks if this and commit have the same blob for a given file name.
     *  @param fileName file name to be checked
     *  @param commit commit ot be checked
     *  @return true if the blobs of fileName match in this and commit
     */
    public boolean checkSameVersion(String fileName, Commit commit) {
        if (isTrackingFile(new File(fileName))) {
            return commit.isTrackingFile(new File(fileName))
                    && getBlobHash(fileName).equals(commit.getBlobHash(
                            fileName));
        } else {
            return !commit.isTrackingFile(new File(fileName));
        }
    }

    /** Returns the hash of the blob associated with fileName.
     *  @param fileName file name
     *  @return hash of the blob associated with fileName
     */
    public String getBlobHash(String fileName) {
        return _blobs.get(fileName);
    }

    /** Returns true if blobs is not null.
     *  @return true if blobs is not null
     */
    public boolean hasBlobs() {
        return _blobs != null;
    }

    /** Returns a set of file names of all files being tracked.
     *  @return set of file names
     */
    public Set<String> getFileNames() {
        return _blobs.keySet();
    }

    /** Checks if commit is tracking blob.
     *  @param blob blob to be checked
     *  @return true if blob is being tracked
     */
    public boolean isTracking(Blob blob) {
        return _blobs != null && _blobs.containsValue(Utils.sha1(
                Utils.serialize(blob)));
    }

    /** Checks if commit is tracking file.
     *  @param file file to be checked
     *  @return true if file is being tracked
     */
    public boolean isTrackingFile(File file) {
        return _blobs != null && _blobs.containsKey(file.getName());
    }

    /** Saves commit to be accessed later.
     *  @param commits directory in which to save commit
     */
    public void saveCommit(File commits) {
        Utils.writeObject(Utils.join(commits, Utils.sha1(
                Utils.serialize(this))), this);
    }

    /** Commit message. */
    private String _message;

    /** Timestamp of creation. */
    private String _timestamp;

    /** Parent commit hash. */
    private String _parent;

    /** Second parent commit from merge hash. */
    private String _mergeParent;

    /** Mapping of file names to blob hashes. */
    private HashMap<String, String> _blobs;
}

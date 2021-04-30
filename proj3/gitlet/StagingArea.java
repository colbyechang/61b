package gitlet;

import java.io.Serializable;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.io.File;

/** Staging Area that tracks files to be added and to be removed in the next
 *  commit.
 *  @author Colby Chang
 */
public class StagingArea implements Serializable {

    /** Creates a new, empty staging area. */
    public StagingArea() {
        _add = new HashMap<String, String>();
        _remove = new HashSet<String>();
    }

    /** Stages a file for addition.
     *  @param fileName name of file to be staged
     *  @param blobHash hashcode of blob associated
     */
    public void stageForAddition(String fileName, String blobHash) {
        _add.put(fileName, blobHash);
    }

    /** Unstages a file for removal.
     *  @param fileName name of file to be staged
     */
    public void stageForRemoval(String fileName) {
        _remove.add(fileName);
    }

    /** Checks if a file is staged for addition.
     *  @param fileName name of file to check
     *  @return true if file is staged for addition
     */
    public boolean isStagedForAddition(String fileName) {
        return _add.containsKey(fileName);
    }

    /** Checks if a file is staged for removal.
     *  @param fileName name of file to check
     *  @return true if file is staged for removal
     */
    public boolean isStagedForRemoval(String fileName) {
        return _remove.contains(fileName);
    }

    /** Checks if a file is staged for either addition or removal.
     *  @param fileName name of file to check
     *  @return true if file is staged for removal or addition
     */
    public boolean isStaged(String fileName) {
        return isStagedForAddition(fileName) || isStagedForRemoval(fileName);
    }

    /** Unstages a file for addition.
     *  @param fileName name of file to be unstaged
     */
    public void unstageAddition(String fileName) {
        _add.remove(fileName);
    }

    /** Unstages a file for removal.
     *  @param fileName name of file to be unstaged
     */
    public void unstageRemoval(String fileName) {
        _remove.remove(fileName);
    }

    /** Retrieves names of all files staged for addition.
     *  @return set of file names
     */
    public Set<String> filesStagedForAddition() {
        return _add.keySet();
    }

    /** Retrieves names of all files staged for removal.
     *  @return set of file names
     */
    public Set<String> filesStagedForRemoval() {
        return _remove;
    }

    /** Gets the blob associated with a certain file name.
     *  @param fileName name of file
     *  @return hash of blob associated with file name
     */
    public String getFromAddition(String fileName) {
        return _add.get(fileName);
    }

    /** Checks if staging area is empty.
     *  @return true if staging area is empty.
     */
    public boolean isEmpty() {
        return _remove.isEmpty() && _add.isEmpty();
    }

    /** Clears staging area. */
    public void clear() {
        _remove.clear();
        _add.clear();
    }

    /** Saves staging area to the STAGINGAREA file to be accessed later. */
    public void saveStagingArea() {
        Utils.writeObject(Utils.join(".gitlet", "STAGINGAREA"), this);
    }

    /** Retrieves the staging area from the STAGINGAREA file.
     *  @param folder directory in which staging area is stored
     *  @return staging area being worked with
     */
    public static StagingArea fromFile(File folder) {
        return Utils.readObject(Utils.join(folder, "STAGINGAREA"),
                StagingArea.class);
    }

    /** Mapping of file names of files to be added to the hashcodes of blobs
     * associated.
     */
    private HashMap<String, String> _add;

    /** Set of file names of files to be removed. **/
    private HashSet<String> _remove;
}

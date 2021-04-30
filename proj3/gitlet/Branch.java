package gitlet;

import java.io.Serializable;
import java.io.File;

/** Branch that tracks its name and head at all times.
 *  @author Colby Chang
 */
public class Branch implements Serializable {

    /** Creates a new Branch.
     *  @param name name of this branch
     *  @param head head of this branch
     *  @param commits directory where branch commits are stored
     *  @param heads directory that contains this branch
     */
    public Branch(String name, Commit head, File commits, File heads) {
        _name = name;
        _head = Utils.sha1(Utils.serialize(head));
        _commits = commits;
        _heads = heads;
    }

    /** Changes the head of this branch to newHead.
     *  @param newHead new head commit
     */
    public void updateBranchHead(Commit newHead) {
        _head = Utils.sha1(Utils.serialize(newHead));
        saveBranch();
    }

    /** Gets the head of this branch.
     *  @return head commit
     */
    public Commit getHead() {
        return Utils.readObject(Utils.join(_commits, _head), Commit.class);
    }

    /** Gets the name of this branch.
     *  @return name of this branch
     */
    public String getName() {
        return _name;
    }

    /** Saves branch to be accessed later. */
    public void saveBranch() {
        Utils.writeObject(Utils.join(_heads, _name), this);
    }

    /** Gets the directory this branch is stored in.
     *  @return directory this branch is stored in
     */
    public File getDirectory() {
        return _heads;
    }

    /** Name of this branch. */
    private String _name;

    /** Sha1 hash of this branch's head. */
    private String _head;

    /** Directory that contains commits of this branch. */
    private File _commits;

    /** Directory that contains this branch. */
    private File _heads;
}

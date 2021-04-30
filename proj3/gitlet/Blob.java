package gitlet;

import java.io.IOException;
import java.io.Serializable;
import java.io.File;

/** Blob that tracks the contents of a file and that file's name.
 *  @author Colby Chang
 */
public class Blob implements Serializable {

    /** Creates a new Blob.
     *  @param file file this blob represents
     */
    public Blob(File file) {
        _contents = Utils.readContentsAsString(file);
        _name = file.getName();
    }

    /** Returns the name of the file this blob represents.
     *  @return name of the file this blob represents
     */
    public String getName() {
        return _name;
    }

    /** Returns the contents of this blob.
     *  @return the contents of this blob
     */
    public String getContents() {
        return _contents;
    }

    /** Saves blob to be accessed later.
     *  @param blobs directory in which to save blob
     */
    public void saveBlob(File blobs) {
        File blobFile = Utils.join(blobs, Utils.sha1(Utils.serialize(
               this)));
        if (!blobFile.exists()) {
            try {
                blobFile.createNewFile();
                Utils.writeObject(blobFile, this);
            } catch (IOException io) {
                System.exit(0);
            }
        }
    }

    /** Contents of this blob. */
    private String _contents;

    /** File name of the file this blob represents. */
    private String _name;
}

package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a reflector in the enigma.
 *  @author Colby Chang
 */
class Reflector extends FixedRotor {

    /** A non-moving rotor named NAME whose permutation at the 0 setting
     * is PERM. */
    Reflector(String name, Permutation perm) {
        super(name, perm);
        if (!perm.derangement()) {
            throw new EnigmaException(
                    "Reflector permutation not a derangement");
        }
        _setting = 0;
    }

    @Override
    int setting() {
        return _setting;
    }

    @Override
    boolean reflecting() {
        return true;
    }

    @Override
    void set(int posn) {
        if (posn != 0) {
            throw new EnigmaException("reflector has only one position");
        }
    }

    /** Setting for reflector. */
    private final int _setting;
}

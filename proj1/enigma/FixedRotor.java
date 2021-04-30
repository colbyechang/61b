package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a rotor that has no ratchet and does not advance.
 *  @author Colby Chang
 */
class FixedRotor extends Rotor {

    /** A non-moving rotor named NAME whose permutation at the 0 setting
     * is given by PERM. */
    FixedRotor(String name, Permutation perm) {
        super(name, perm);
    }

    /** Returns false because fixed rotors do not rotate. */
    boolean rotates() {
        return false;
    }
}

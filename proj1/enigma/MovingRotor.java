package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author Colby Chang
 */
class MovingRotor extends Rotor {

    /** A rotor named NAME whose permutation in its default setting is
     *  PERM, and whose notches are at the positions indicated in NOTCHES.
     *  The Rotor is initally in its 0 setting (first character of its
     *  alphabet).
     */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        for (char c : notches.toCharArray()) {
            if (!alphabet().contains(c)) {
                throw new EnigmaException("Notch not in alphabet");
            }
        }
        _notches = notches;
    }

    @Override
    boolean rotates() {
        return true;
    }

    @Override
    boolean atNotch() {
        return _notches.contains(alphabet().toChar(
                permutation().wrap(setting())) + "");
    }

    @Override
    void advance() {
        set(setting() + 1);
    }

    /** String that contains the notches of the moving rotor. **/
    private String _notches;

}

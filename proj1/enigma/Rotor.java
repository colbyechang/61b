package enigma;

import static enigma.EnigmaException.*;

/** Superclass that represents a rotor in the enigma machine.
 *  @author Colby Chang
 */
class Rotor {

    /** A rotor named NAME whose permutation is given by PERM. */
    Rotor(String name, Permutation perm) {
        _name = name;
        _permutation = perm;
        _setting = 0;
        _ringSetting = 0;
    }

    /** Return my name. */
    String name() {
        return _name;
    }

    /** Return my alphabet. */
    Alphabet alphabet() {
        return _permutation.alphabet();
    }

    /** Return my permutation. */
    Permutation permutation() {
        return _permutation;
    }

    /** Return the size of my alphabet. */
    int size() {
        return _permutation.size();
    }

    /** Return true iff I have a ratchet and can move. */
    boolean rotates() {
        return false;
    }

    /** Return true iff I reflect. */
    boolean reflecting() {
        return false;
    }

    /** Return my current setting. */
    int setting() {
        return _setting;
    }

    /** Set setting() to POSN.  */
    void set(int posn) {
        posn = posn % size();
        if (posn < 0) {
            posn += size();
        }
        _setting = posn;
    }

    /** Set setting() to character CPOSN. */
    void set(char cposn) {
        if (!alphabet().contains(cposn)) {
            throw new EnigmaException("Not in Alphabet");
        }
        set(alphabet().toInt(cposn));
    }

    /** Return my current ringSetting. */
    int ringSetting() {
        return _ringSetting;
    }

    /** Set ringSetting() to POSN.  */
    void setRing(int posn) {
        posn = posn % size();
        if (posn < 0) {
            posn += size();
        }
        _ringSetting = posn;
    }

    /** Set setting() to character CPOSN. */
    void setRing(char cposn) {
        if (!alphabet().contains(cposn)) {
            throw new EnigmaException("Not in Alphabet");
        }
        setRing(alphabet().toInt(cposn));
    }

    /** Return the conversion of P (an integer in the range 0..size()-1)
     *  according to my permutation. */
    int convertForward(int p) {
        int pO = (_permutation.permute(p + setting() - ringSetting())
                - setting() + ringSetting()) % size();
        if (pO < 0) {
            pO += size();
        }
        return pO;
    }

    /** Return the conversion of E (an integer in the range 0..size()-1)
     *  according to the inverse of my permutation. */
    int convertBackward(int e) {
        int eO = (_permutation.invert(e + setting() - ringSetting())
                - setting() + ringSetting()) % size();
        if (eO < 0) {
            eO += size();
        }
        return eO;
    }

    /** Returns true iff I am positioned to allow the rotor to my left
     *  to advance. */
    boolean atNotch() {
        return false;
    }

    /** Advance me one position, if possible. By default, does nothing. */
    void advance() {
    }

    @Override
    public String toString() {
        return "Rotor " + _name;
    }

    /** My name. */
    private final String _name;

    /** The permutation implemented by this rotor in its 0 position. */
    private Permutation _permutation;

    /** The setting of this rotor. */
    private int _setting;

    /** The setting of this rotor's ring. */
    private int _ringSetting;

}

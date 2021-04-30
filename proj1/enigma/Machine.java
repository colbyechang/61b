package enigma;

import java.util.HashMap;
import java.util.Collection;
import java.util.ArrayList;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Colby Chang
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        if (pawls >= numRotors || numRotors <= 0 || pawls < 0) {
            throw new EnigmaException("Improper number of numRotors or pawls");
        }
        if (allRotors == null) {
            throw new EnigmaException("No rotors");
        }
        _numRotors = numRotors;
        _numPawls = pawls;
        _allRotors = new HashMap<String, Rotor>();
        for (Rotor r : allRotors) {
            _allRotors.put(r.name(), r);
        }
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _numPawls;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        if (rotors.length != numRotors()) {
            throw new EnigmaException("Incorrect number of rotors");
        } else {
            for (int i = 0; i < rotors.length; i++) {
                for (int j = i + 1; j < rotors.length; j++) {
                    if (rotors[i].equals(rotors[j])) {
                        throw new EnigmaException("Duplicate rotors");
                    }
                }
            }
            _ROTORS = new ArrayList<Rotor>();
            String setting = "";
            for (int i = 0; i < rotors.length; i++) {
                if (!_allRotors.containsKey(rotors[i])) {
                    throw new EnigmaException(
                            "Rotor " + rotors[i] + " not in rotors");
                } else if (i == 0 && !_allRotors.get(rotors[i]).reflecting()) {
                    throw new EnigmaException("First rotor not a reflector");
                } else if (i < numRotors() - numPawls()
                        && _allRotors.get(rotors[i]) instanceof MovingRotor) {
                    throw new EnigmaException("Rotor " + i
                            + " must be a fixed rotor");
                } else if (i >= numRotors() - numPawls()
                        && _allRotors.get(rotors[i]) instanceof FixedRotor) {
                    throw new EnigmaException("Rotor " + i
                            + " must be a moving rotor");
                } else {
                    _ROTORS.add(_allRotors.get(rotors[i]));
                    setting = setting + _alphabet.toChar(0);
                }
            }
            setRotors(setting.substring(1));
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        if (setting.length() < numRotors() - 1) {
            throw new EnigmaException("Wheel settings too short");
        } else if (setting.length() > numRotors() - 1) {
            throw new EnigmaException("Wheel settings too long");
        }
        for (int i = 0; i < setting.length(); i++) {
            _ROTORS.get(i + 1).set(setting.charAt(i));
        }
    }

    /** Set my rotors according to RINGSETTING which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotorRings(String ringSetting) {
        if (ringSetting.length() != numRotors() - 1) {
            throw new EnigmaException("Too many ring settings.");
        }
        for (int i = 0; i < ringSetting.length(); i++) {
            _ROTORS.get(i + 1).setRing(ringSetting.charAt(i));
        }
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        c = _plugboard.permute(c);
        for (int i = numRotors() - 1; i >= 0; i--) {
            c = _ROTORS.get(i).convertForward(c);
        }
        for (int i = 1; i < numRotors(); i++) {
            c = _ROTORS.get(i).convertBackward(c);
        }
        c = _plugboard.invert(c);
        return c;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        String result = "";
        for (char c : msg.toCharArray()) {
            if (!_alphabet.contains(c)) {
                throw new EnigmaException("character '"
                        + c + "' not in alphabet");
            }
            boolean[] rotate = new boolean[numRotors()];
            for (int i = 1; i < rotate.length; i++) {
                rotate[i] = false;
            }
            for (int i = 1; i < numRotors() - 1; i++) {
                if (_ROTORS.get(i).rotates() && _ROTORS.get(i + 1).atNotch()
                        || _ROTORS.get(i - 1).rotates()
                        && _ROTORS.get(i).atNotch()) {
                    rotate[i] = true;
                }
            }
            for (int i = 0; i < numRotors(); i++) {
                if (rotate[i]) {
                    _ROTORS.get(i).advance();
                }
            }
            if (_ROTORS.get(numRotors() - 1).rotates()) {
                _ROTORS.get(numRotors() - 1).advance();
            }
            result += _alphabet.toChar(convert(_alphabet.toInt(c)));
        }
        return result;
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** Number of rotor slots. */
    private final int _numRotors;

    /** Number of pawls/moving rotors. */
    private final int _numPawls;

    /** HashMap mapping rotor name to its rotor. Contains all rotors. */
    private HashMap<String, Rotor> _allRotors;

    /** ArrayList representing rotor slots. */
    private ArrayList<Rotor> _ROTORS;

    /** Plugboard of machine. */
    private Permutation _plugboard;
}

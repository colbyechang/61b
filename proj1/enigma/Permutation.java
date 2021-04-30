package enigma;
import java.util.ArrayList;

import static enigma.EnigmaException.*;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Colby Chang
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        _string = cycles;
        while (cycles.contains(") ")) {
            int i = cycles.indexOf(") ");
            cycles = cycles.substring(0, i + 1) + cycles.substring(i + 2);
        }
        while (cycles.contains(" (")) {
            int i = cycles.indexOf(" (");
            cycles = cycles.substring(0, i) + cycles.substring(i + 1);
        }
        if (!cycles.isEmpty()) {
            if (cycles.contains("()")) {
                throw new EnigmaException("Invalid Permutation");
            }
            if (cycles.charAt(0) == '('
                    && cycles.charAt(cycles.length() - 1) == ')') {
                cycles = cycles.substring(1, cycles.length() - 1);
            } else {
                throw new EnigmaException("Invalid Permutation");
            }
            for (char c : cycles.toCharArray()) {
                if (c != '(' && c != ')'
                        && cycles.substring(cycles.indexOf(c) + 1).indexOf(c)
                                > -1) {
                    throw new EnigmaException("Repeat in Permutations");
                }
            }
            int i = 0;
            ArrayList<String> lst = new ArrayList<String>();
            lst.add("");
            while (!cycles.isEmpty()) {
                if (cycles.charAt(0) == ')') {
                    if (cycles.length() != 1) {
                        if (cycles.charAt(1) == '(') {
                            cycles = cycles.substring(2);
                            lst.add("");
                            i++;
                        } else {
                            throw new EnigmaException("Invalid Permutation");
                        }
                    } else {
                        cycles = cycles.substring(1);
                    }
                } else if (cycles.charAt(0) == '(' || cycles.charAt(0) == ' '
                        || !alphabet.contains(cycles.charAt(0))) {
                    throw new EnigmaException("Invalid Permutation");
                } else {
                    lst.set(i, lst.get(i) + cycles.charAt(0));
                    cycles = cycles.substring(1);
                }
            }
            _cycles = new ArrayList<ArrayList<Character>>();
            for (String s : lst) {
                addCycle(s);
            }
        } else {
            _cycles = new ArrayList<ArrayList<Character>>();
        }
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        ArrayList<Character> lst = new ArrayList<Character>();
        for (char c : cycle.toCharArray()) {
            lst.add(c);
        }
        _cycles.add(lst);
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        char ch = _alphabet.toChar(wrap(p));
        int i = 0;
        ArrayList lst = null;
        while (i < _cycles.size()) {
            if (_cycles.get(i).contains(ch)) {
                lst = _cycles.get(i);
                break;
            }
            i++;
        }
        if (lst == null) {
            return wrap(p);
        }
        int permuteI = (lst.indexOf(ch) + 1) % lst.size();
        return _alphabet.toInt((char) lst.get(permuteI));
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        char ch = _alphabet.toChar(wrap(c));
        int i = 0;
        ArrayList lst = null;
        while (i < _cycles.size()) {
            if (_cycles.get(i).contains(ch)) {
                lst = _cycles.get(i);
                break;
            }
            i++;
        }
        if (lst == null) {
            return wrap(c);
        }
        int permuteI = (lst.indexOf(ch) - 1) % lst.size();
        if (permuteI < 0) {
            permuteI += lst.size();
        }
        return _alphabet.toInt((char) lst.get(permuteI));
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        if (!_alphabet.contains(p)) {
            throw new EnigmaException("Not In Alphabet");
        }
        return _alphabet.toChar(permute(_alphabet.toInt(p)));
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        if (!_alphabet.contains(c)) {
            throw new EnigmaException("Not In Alphabet");
        }
        return _alphabet.toChar(invert(_alphabet.toInt(c)));
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        for (ArrayList<Character> cycle : _cycles) {
            if (cycle.size() == 1) {
                return false;
            }
        }
        for (int i = 0; i < _alphabet.size(); i++) {
            if (!_string.contains("" + _alphabet.toChar(i))) {
                return false;
            }
        }
        return true;
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /** Permutations in the form of ArrayLists. */
    private ArrayList<ArrayList<Character>> _cycles;

    /** Original cycles string, used to check for derangement. */
    private String _string;
}

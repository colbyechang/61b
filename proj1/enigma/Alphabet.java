package enigma;
import java.util.ArrayList;

/** An alphabet of encodable characters.  Provides a mapping from characters
 *  to and from indices into the alphabet.
 *  @author Colby Chang
 */
class Alphabet {
    /** List of letters in the alphabet.
     */
    private ArrayList<Character> _alphabet = new ArrayList<Character>();

    /** A new alphabet containing CHARS.  Character number #k has index
     *  K (numbering from 0). No character may be duplicated. */
    Alphabet(String chars) {
        if (chars.contains("(") || chars.contains(")")
                || chars.contains("*") || chars.contains(" ")) {
            throw new EnigmaException(
                    "Invalid alphabet format: Contains (, ), *, or spaces");
        }
        for (int i = 0; i < chars.length() - 1; i++) {
            if (chars.substring(i + 1).contains(("" + chars.charAt(i)))) {
                throw new EnigmaException(
                        "Invalid alphabet format: Duplicate letters");
            }
        }
        for (char character : chars.toCharArray()) {
            _alphabet.add(character);
        }
    }

    /** A default alphabet of all upper-case characters. */
    Alphabet() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /** Returns the size of the alphabet. */
    int size() {
        return _alphabet.size();
    }

    /** Returns true if CH is in this alphabet. */
    boolean contains(char ch) {
        return _alphabet.contains(ch);
    }

    /** Returns character number INDEX in the alphabet, where
     *  0 <= INDEX < size(). */
    char toChar(int index) {
        return _alphabet.get(index);
    }

    /** Returns the index of character CH which must be in
     *  the alphabet. This is the inverse of toChar(). */
    int toInt(char ch) {
        return _alphabet.indexOf(ch);
    }

}

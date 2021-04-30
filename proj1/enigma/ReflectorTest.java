package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

import static enigma.TestUtils.*;

/** The suite of all JUnit tests for the Reflector class.
 *  @author Colby Chang
 */
public class ReflectorTest {
    /**
     * For this lab, you must use this to get a new Permutation,
     * the equivalent to:
     * new Permutation(cycles, alphabet)
     * @return a Permutation with cycles as its cycles and alphabet as
     * its alphabet
     * @see Permutation for description of the Permutation conctructor
     */
    Permutation getNewPermutation(String cycles, Alphabet alphabet) {
        return new Permutation(cycles, alphabet);
    }

    /**
     * For this lab, you must use this to get a new Alphabet,
     * the equivalent to:
     * new Alphabet(chars)
     * @return an Alphabet with chars as its characters
     * @see Alphabet for description of the Alphabet constructor
     */
    Alphabet getNewAlphabet(String chars) {
        return new Alphabet(chars);
    }

    /**
     * For this lab, you must use this to get a new Alphabet,
     * the equivalent to:
     * new Alphabet()
     * @return a default Alphabet with characters ABCD...Z
     * @see Alphabet for description of the Alphabet constructor
     */
    Alphabet getNewAlphabet() {
        return new Alphabet();
    }

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    @Test
    public void testSetIntValid() {
        Reflector r = new Reflector("reflector", getNewPermutation("(BACD)",
                getNewAlphabet("ABCD")));
        r.set(0);
        assertEquals(0, r.setting());
    }
    @Test(expected = EnigmaException.class)
    public void testSetIntInvalid() {
        Reflector r = new Reflector("reflector", getNewPermutation("(BACD)",
                getNewAlphabet("ABCD")));
        r.set(7);
    }

    @Test
    public void testSetCharValid() {
        Reflector r = new Reflector("reflector", getNewPermutation("(BACD)",
                getNewAlphabet("ABCD")));
        r.set('A');
        assertEquals(0, r.setting());
    }

    @Test(expected = EnigmaException.class)
    public void testSetCharInvalid() {
        Reflector r = new Reflector("reflector", getNewPermutation("(BACD)",
                getNewAlphabet("ABCD")));
        r.set('B');
    }

    @Test
    public void testConvertForward() {
        Reflector r = new Reflector("reflector", getNewPermutation("(BACD)",
                getNewAlphabet("ABCD")));
        assertEquals(0, r.convertForward(1));
        assertEquals(1, r.convertForward(3));
        r = new Reflector("reflector", getNewPermutation("(BAE) (CD)",
                getNewAlphabet("ABCDE")));
        assertEquals(1, r.convertForward(4));
        assertEquals(3, r.convertForward(2));
    }

    @Test
    public void testConvertBackward() {
        Reflector r = new Reflector("reflector", getNewPermutation("(BACD)",
                getNewAlphabet("ABCD")));
        assertEquals(3, r.convertBackward(1));
        assertEquals(2, r.convertBackward(3));
        r = new Reflector("reflector", getNewPermutation("(BAE) (CD)",
                getNewAlphabet("ABCDE")));
        assertEquals(1, r.convertBackward(5));
        assertEquals(2, r.convertBackward(3));
    }

    @Test(expected = EnigmaException.class)
    public void testInvalidPermutation() {
        Reflector r = new Reflector("reflector",
                getNewPermutation("(BA) (CD) (E)",
                        getNewAlphabet("ABCDE")));
    }
}


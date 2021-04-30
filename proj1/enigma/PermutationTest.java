package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

import static enigma.TestUtils.*;

/** The suite of all JUnit tests for the Permutation class.
 *  @author Colby Chang
 */
public class PermutationTest {

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

    /* ***** TESTING UTILITIES ***** */

    private Permutation perm;
    private String alpha = UPPER_STRING;

    /** Check that PERM has an ALPHABET whose size is that of
     *  FROMALPHA and TOALPHA and that maps each character of
     *  FROMALPHA to the corresponding character of FROMALPHA, and
     *  vice-versa. TESTID is used in error messages. */
    private void checkPerm(String testId,
                           String fromAlpha, String toAlpha,
                           Permutation p, Alphabet a) {
        int N = fromAlpha.length();
        assertEquals(testId + " (wrong length)", N, p.size());
        for (int i = 0; i < N; i += 1) {
            char c = fromAlpha.charAt(i), e = toAlpha.charAt(i);
            assertEquals(msg(testId, "wrong translation of '%c'", c),
                    e, p.permute(c));
            assertEquals(msg(testId, "wrong inverse of '%c'", e),
                    c, p.invert(e));
            int ci = a.toInt(c), ei = a.toInt(e);
            assertEquals(msg(testId, "wrong translation of %d", ci),
                    ei, p.permute(ci));
            assertEquals(msg(testId, "wrong inverse of %d", ei),
                    ci, p.invert(ei));
        }
    }

    /* ***** TESTS ***** */
    @Test(expected = EnigmaException.class)
    public void checkErrorCasesOpenParen() {
        perm = new Permutation("", new Alphabet("abcde("));
    }
    @Test(expected = EnigmaException.class)
    public void checkErrorCasesCloseParen() {
        perm = new Permutation("", new Alphabet("abc)de"));
    }
    @Test(expected = EnigmaException.class)
    public void checkErrorCasesStar() {
        perm = new Permutation("", new Alphabet("a*bcde"));
    }

    @Test
    public void testInvertChar() {
        Permutation p = getNewPermutation("(BACD)", getNewAlphabet("ABCD"));
        assertEquals('B', p.invert('A'));
        p = getNewPermutation("(BA) (CD)", getNewAlphabet("ABCDE"));
        assertEquals('B', p.invert('A'));
        assertEquals('A', p.invert('B'));
        assertEquals('C', p.invert('D'));
        assertEquals('E', p.invert('E'));
        p = getNewPermutation("(BA) (CD) (E)", getNewAlphabet("ABCDE"));
        assertEquals('E', p.invert('E'));
        p = getNewPermutation("(BAE) (CD)", getNewAlphabet("ABCDE"));
        assertEquals('B', p.invert('A'));
        assertEquals('A', p.invert('E'));
        assertEquals('C', p.invert('D'));
        p = getNewPermutation("([]Aa)(}/sd)(12)",
                getNewAlphabet("ABCD[]{}/?:;'.1234asdfg"));
        assertEquals(']', p.invert('A'));
        assertEquals('1', p.invert('2'));
        assertEquals('A', p.invert('a'));
    }

    @Test
    public void testInvertInt() {
        Permutation p = getNewPermutation("(BACD)", getNewAlphabet("ABCD"));
        assertEquals(1, p.invert(0));
        p = getNewPermutation("(BA) (CD)", getNewAlphabet("ABCDE"));
        assertEquals(1, p.invert(5));
        assertEquals(4, p.invert(-1));
        assertEquals(1, p.invert(1000));
        assertEquals(0, p.invert(1));
        assertEquals(2, p.invert(3));
        assertEquals(4, p.invert(4));
        p = getNewPermutation("(BA) (CD) (E)", getNewAlphabet("ABCDE"));
        assertEquals(4, p.invert(4));
        p = getNewPermutation("(BAE) (CD)", getNewAlphabet("ABCDE"));
        assertEquals(1, p.invert(0));
        assertEquals(0, p.invert(4));
        assertEquals(2, p.invert(3));
        p = getNewPermutation("([]Aa)(}/sd)(12)",
                getNewAlphabet("ABCD[]{}/?:;'.1234asdfg"));
        assertEquals(20, p.invert(7));
        assertEquals(14, p.invert(15));
        assertEquals(0, p.invert(18));
    }

    @Test
    public void testPermuteChar() {
        Permutation p = getNewPermutation("(BACD)", getNewAlphabet("ABCD"));
        assertEquals('A', p.permute('B'));
        p = getNewPermutation("(BA) (CD)", getNewAlphabet("ABCDE"));
        assertEquals('A', p.permute('B'));
        assertEquals('C', p.permute('D'));
        assertEquals('E', p.permute('E'));
        p = getNewPermutation("(BA) (CD) (E)", getNewAlphabet("ABCDE"));
        assertEquals('E', p.permute('E'));
        p = getNewPermutation("(BAE) (CD)", getNewAlphabet("ABCDE"));
        assertEquals('A', p.permute('B'));
        assertEquals('C', p.permute('D'));
        assertEquals('B', p.permute('E'));
        p = getNewPermutation("([]Aa)(}/sd)(12)",
                getNewAlphabet("ABCD[]{}/?:;'.1234asdfg"));
        assertEquals('A', p.permute(']'));
        assertEquals('2', p.permute('1'));
        assertEquals('a', p.permute('A'));
    }

    @Test
    public void testPermuteInt() {
        Permutation p = getNewPermutation("(BACD)", getNewAlphabet("ABCD"));
        assertEquals(0, p.permute(1));
        p = getNewPermutation("(BA) (CD)", getNewAlphabet("ABCDE"));
        assertEquals(0, p.permute(6));
        assertEquals(4, p.permute(-1));
        assertEquals(0, p.permute(1));
        assertEquals(1, p.permute(1000));
        assertEquals(2, p.permute(3));
        assertEquals(4, p.permute(4));
        p = getNewPermutation("(BA) (CD) (E)", getNewAlphabet("ABCDE"));
        assertEquals(4, p.permute(4));
        p = getNewPermutation("(BAE) (CD)", getNewAlphabet("ABCDE"));
        assertEquals(0, p.permute(1));
        assertEquals(2, p.permute(3));
        assertEquals(1, p.permute(4));
        p = getNewPermutation("([]Aa)(}/sd)(12)",
                getNewAlphabet("ABCD[]{}/?:;'.1234asdfg"));
        assertEquals(20, p.invert(7));
        assertEquals(15, p.invert(14));
        assertEquals(0, p.invert(18));
    }

    @Test
    public void testSize() {
        Permutation p = getNewPermutation("", getNewAlphabet(""));
        assertEquals(0, p.size());
        p = getNewPermutation("(BACD)", getNewAlphabet("ABCD"));
        assertEquals(4, p.size());
        p = getNewPermutation("(BA) (CD)", getNewAlphabet("ABCDE"));
        assertEquals(5, p.size());
    }

    @Test
    public void testAlphabet() {
        Alphabet a  = getNewAlphabet("ABCD");
        Permutation p = getNewPermutation("(BACD)", a);
        assertEquals(a, p.alphabet());
        p = getNewPermutation("(BA) (CD)", a);
        assertEquals(a, p.alphabet());
    }

    @Test
    public void testDerangement() {
        Permutation p = getNewPermutation("", getNewAlphabet(""));
        assertEquals(true, p.derangement());
        p = getNewPermutation("(BA) (CD)", getNewAlphabet("ABCDE"));
        assertEquals(false, p.derangement());
        p = getNewPermutation("(BA) (CD)", getNewAlphabet("ABCD"));
        assertEquals(true, p.derangement());
        p = getNewPermutation("(A)", getNewAlphabet("A"));
        assertEquals(false, p.derangement());
        p = getNewPermutation("", getNewAlphabet(""));
        assertEquals(true, p.derangement());
    }

    @Test(expected = EnigmaException.class)
    public void testInvalidPermutation1() {
        Permutation p = getNewPermutation("(ABCD", getNewAlphabet("ABCD"));
        p.invert('A');
    }

    @Test(expected = EnigmaException.class)
    public void testInvalidPermutation2() {
        Permutation p = getNewPermutation("(ABC)D", getNewAlphabet("ABCD"));
        p.invert('A');
    }

    @Test(expected = EnigmaException.class)
    public void testInvalidPermutation3() {
        Permutation p = getNewPermutation("ABCD", getNewAlphabet("ABCD"));
        p.invert('A');
    }

    @Test(expected = EnigmaException.class)
    public void testInvalidPermutation4() {
        Permutation p = getNewPermutation("(ABCDE)", getNewAlphabet("ABCD"));
        p.invert('A');
    }

    @Test(expected = EnigmaException.class)
    public void testInvalidPermutation5() {
        Permutation p = getNewPermutation("(ABC) (CDE)",
                getNewAlphabet("ABCDE"));
        p.invert('A');
    }

    @Test(expected = EnigmaException.class)
    public void testInvalidPermutation6() {
        Permutation p = getNewPermutation("()",
                getNewAlphabet("ABCDE"));
        p.invert('A');
    }

    @Test(expected = EnigmaException.class)
    public void testInvalidPermutation7() {
        Permutation p = getNewPermutation("(AB C)",
                getNewAlphabet("ABCDE"));
        p.invert('A');
    }

    @Test(expected = EnigmaException.class)
    public void testNotInAlphabet() {
        Permutation p = getNewPermutation("(BACD)", getNewAlphabet("ABCD"));
        p.invert('F');
    }

    @Test(expected = EnigmaException.class)
    public void invalidAlphabet1() {
        Permutation p = getNewPermutation("(BACD)", getNewAlphabet("ABCD*"));
        p.invert('F');
    }

    @Test(expected = EnigmaException.class)
    public void invalidAlphabet2() {
        Permutation p = getNewPermutation("(BACD)", getNewAlphabet("ABCDA"));
        p.invert('F');
    }

    @Test
    public void checkSymbols() {
        Permutation p = getNewPermutation("(:;{) (][)",
                getNewAlphabet("[]{}/:;'"));
        checkPerm("nonalphanumerics1", "[]{}/:;'", "][:}/;{'", p,
                getNewAlphabet("[]{}/:;'"));
        p = getNewPermutation("(-&^)     ($[)", getNewAlphabet("-&^$['"));
        checkPerm("nonalphanumerics2", "-&^$['", "&^-[$'", p,
                getNewAlphabet("-&^$['"));
    }

    @Test
    public void checkIdTransform() {
        Alphabet a = getNewAlphabet();
        Permutation p = getNewPermutation("", a);
        checkPerm("identity", UPPER_STRING, UPPER_STRING, p, a);
    }
}

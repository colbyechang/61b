package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;
import java.util.ArrayList;

/** The suite of all JUnit tests for the Rotor class.
 *  @author Colby Chang
 */
public class MachineTest {
    /** Gets a new Alphabet. */
    Alphabet getNewAlphabet() {
        return new Alphabet();
    }

    /** Gets a new Permutation. */
    Permutation getNewPermutation(String cycles, Alphabet alphabet) {
        return new Permutation(cycles, alphabet);
    }

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    @Test
    public void testMachine() {
        Rotor r = new Rotor("rotor", getNewPermutation("(BACD)",
                getNewAlphabet()));
        ArrayList<Rotor> lst = new ArrayList<Rotor>();
        lst.add(r);
        Machine m = new Machine(getNewAlphabet(), 5, 4, lst);
        assertEquals(5, m.numRotors());
        assertEquals(4, m.numPawls());
    }
}

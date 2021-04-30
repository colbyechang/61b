package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Colby Chang
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            new Main(args).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Check ARGS and open the necessary files (see comment on main). */
    Main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            throw error("Only 1, 2, or 3 command-line arguments allowed");
        }

        _config = getInput(args[0]);

        if (args.length > 1) {
            _input = getInput(args[1]);
        } else {
            _input = new Scanner(System.in);
        }

        if (args.length > 2) {
            _output = getOutput(args[2]);
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        Machine machine = readConfig();
        if (!_input.hasNext("[*]")) {
            throw new EnigmaException("Input does not begin with a *");
        }
        while (_input.hasNextLine()) {
            String msg = _input.nextLine();
            Scanner sc = new Scanner(msg);
            if (sc.hasNext("[*]")) {
                sc.next();
                readInput(machine, sc);
            } else {
                msg = msg.replaceAll(" ", "");
                printMessageLine(machine.convert(msg));
            }
        }
    }

    /** Sets up machine using from the contents of input file _input.
     *
     * @param machine machine being used
     * @param sc      scanner for line of input
     */
    private void readInput(Machine machine, Scanner sc) {
        String[] rotors = new String[machine.numRotors()];
        for (int i = 0; i < rotors.length; i++) {
            if (sc.hasNext()) {
                rotors[i] = sc.next();
            } else {
                throw new EnigmaException("Not enough rotors");
            }
        }
        machine.insertRotors(rotors);
        String settings;
        if (sc.hasNext()) {
            settings = sc.next();
        } else {
            throw new EnigmaException("missing settings");
        }
        String ringSettings = "";
        if (!sc.hasNext() || sc.hasNext("[(].+[)]")) {
            for (int i = 0; i < machine.numRotors() - 1; i++) {
                ringSettings += _alphabet.toChar(0);
            }
        } else {
            ringSettings = sc.next();
        }
        setUp(machine, settings, ringSettings);
        String cycles = "";
        while (sc.hasNext("[(].+[)]")) {
            cycles += sc.next();
        }
        machine.setPlugboard(new Permutation(cycles, _alphabet));
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            _alphabet = new Alphabet(_config.next());
            int numRotors = _config.nextInt();
            int numPawls = _config.nextInt();
            ArrayList<Rotor> allRotors = new ArrayList<Rotor>();
            while (_config.hasNext()) {
                allRotors.add(readRotor());
            }
            return new Machine(_alphabet, numRotors, numPawls, allRotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            String name = _config.next();
            if (name.contains(")") || name.contains("(")) {
                throw new EnigmaException("Improper name");
            }
            String type = _config.next();
            String cycles = "";
            while (_config.hasNext("[(].+[)]")) {
                cycles += _config.next();
            }
            Permutation p = new Permutation(cycles, _alphabet);
            Rotor r;
            if (type.equals("R")) {
                r = new Reflector(name, p);
            } else if (type.equals("N")) {
                r = new FixedRotor(name, p);
            } else if (type.charAt(0) == 'M') {
                r = new MovingRotor(name, p, type.substring(1));
            } else {
                throw new EnigmaException("Improper typing of rotor");
            }
            return r;
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment.
     *
     * @param M             machine to be used
     * @param settings      settings to be used
     * @param ringSettings  ring settings to be used
     */
    private void setUp(Machine M, String settings, String ringSettings) {
        M.setRotors(settings);
        M.setRotorRings(ringSettings);
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        while (msg.length() > 5) {
            _output.print(msg.substring(0, 5) + " ");
            msg = msg.substring(5);
        }
        _output.println(msg);
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;
}

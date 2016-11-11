package name.skylarismy;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class Conductor {

    public static Conductor conductor;

    public static volatile int currentNote = 0;

    public static List<BellNote> sheetmusic = new ArrayList<>();

    public static final AudioFormat AF
            = new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, false);

    private static final List<Player> PLAYERS = new ArrayList<>();

    public static final Note[] NOTES = Note.values();

    public static SourceDataLine line;

    private static NoteLength stringToLength(String s) throws NumberFormatException {
        switch (s) {
            case "1":
                return NoteLength.WHOLE;
            case "2":
                return NoteLength.HALF;
            case "4":
                return NoteLength.QUARTER;
            case "8":
                return NoteLength.EIGTH;
            case "16":
                return NoteLength.SIXTEENTH;
            case "32":
                return NoteLength.THIRTYSECOND;
            case "0":
                return NoteLength.ZERO;
            default:
                throw new NumberFormatException("Invalid note length.");
        }
    }

    /**
     * Display an error message (using GUI if available) and suicides with the
     * given exit code, unless recoverable is true.
     *
     * @param message Human-readable error message
     * @param exitCode System.exit(exitCode)
     * @param recoverable If execution may continue after the error.
     */
    public static void showError(String message, int exitCode, boolean recoverable) {
        System.err.println(message);
        if (recoverable) {
            if (!GraphicsEnvironment.isHeadless()) {
                message += "\nQuit application or attempt to continue?";
                String[] options = {"Quit", "Continue"};
                int result = JOptionPane.showOptionDialog(null, message, "Error", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, options, "Exit");
                if (result == JOptionPane.YES_OPTION) {
                    System.exit(exitCode);
                }
            } else {
                System.err.print("\nQuit application? (Y/n): ");
                Scanner sc = new Scanner(System.in);
                if (sc.hasNext()) {
                    String yn = sc.nextLine();
                    if (!yn.toLowerCase().startsWith("n")) {
                        System.exit(exitCode);
                    }
                }
            }
        } else {
            if (!GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
            }
            System.exit(exitCode);
        }
    }

    /**
     * Display an error message (using GUI if available) and suicides with the
     * given exit code.
     *
     * @param message Human-readable error message
     * @param exitCode System.exit(exitCode)
     */
    public static void showError(String message, int exitCode) {
        showError(message, exitCode, false);
    }

    /**
     * Build a stacktrace for error output.
     *
     * @param file The filename with the error
     * @param line The line number in the file
     * @param data The content of the line
     * @param message The actual error text
     * @param recoverable If execution can continue after the error.
     * @return The built stacktrace
     */
    public static String buildErrorTrace(String file, int line, String data, String message, boolean recoverable) {
        String out = "";
        out += "Error: In " + file + "\n";
        out += "  At line " + line + ":\n";
        out += "    " + data + "\n";
        out += "  " + message + "\n";
        return out;
    }

    /**
     * Build a stacktrace for error output.
     *
     * @param file The filename with the error
     * @param line The line number in the file
     * @param data The content of the line
     * @param message The actual error text
     * @return The built stacktrace
     */
    public static String buildErrorTrace(String file, int line, String data, String message) {
        return buildErrorTrace(file, line, data, message, false);
    }

    public static void playNote(BellNote bn) {
        final int ms = Math.min(bn.length.timeMs(), (int) (Note.MEASURE_LENGTH_SEC * 1000));
        final int length = Note.SAMPLE_RATE * ms / 1000;
        line.write(bn.note.sample(), 0, length);
        line.write(Note.REST.sample(), 0, 50);
    }

    public Conductor(String filename) throws LineUnavailableException {
        line = AudioSystem.getSourceDataLine(Conductor.getAudioFormat());
        line.open();
        line.start();
        conductor = this;
        System.out.println("Reading...");
        // Read in the file to a List
        List<String> lines = null;
        try {
            lines = Files.readAllLines(Paths.get(filename));
        } catch (IOException ex) {
            showError("Error: Cannot load music sheet: " + ex.getMessage(), 1);
        }

        System.out.println("Parsing...");
        // Start reading/parsing lines
        int firstLine = 0;
        for (int i = 0; i < lines.size(); i++) {
            // Get the current line into a String
            String line = lines.get(i);

            // Handle blank lines
            if (line.equals("")) {
                if (i == firstLine) {
                    firstLine++;
                }
                continue;
            }
            // Handle commented lines
            if (line.startsWith("#") || line.startsWith("//") || line.startsWith(";")) {
                if (i == firstLine) {
                    firstLine++;
                }
                continue;
            }

            // Check if the line is valid
            if (line.matches("(([A-G][0-9]S?|REST) ([0-9]+))") == false) {
                showError(buildErrorTrace(filename, i + 1, line, "Invalid syntax."), 2, true);
                continue;
            }

            //([01248]|16|32)
            String note = line.split(" ")[0];
            String leng = line.split(" ")[1];
            try {
                sheetmusic.add(new BellNote(Note.valueOf(note), stringToLength(leng)));
            } catch (NumberFormatException ex) {
                showError(buildErrorTrace(filename, i + 1, line, "Invalid note length."), 3, true);
            } catch (IllegalArgumentException ex) {
                showError(buildErrorTrace(filename, i + 1, line, "Note " + note + " does not exist."), 4, true);
            }
        } // End reading lines

        System.out.println("Compiling...");
        // Setup all the PLAYERS and assign NOTES
        BellNote[] noteArray = sheetmusic.toArray(new BellNote[sheetmusic.size()]);
        for (int i = 0; i < NOTES.length; i++) {
            PLAYERS.add(new Player(Note.values()[i], noteArray));
        }

        // Make them all start playing
        System.out.println("Playing...");
        PLAYERS.stream().forEach((player) -> {
            player.start();
        });

        // Wait for someone to die
        try {
            PLAYERS.get(PLAYERS.size() - 1).join();
        } catch (InterruptedException e) {

        }
        System.out.println("Finished.");
        line.drain();
    }

    public static void main(String args[]) throws LineUnavailableException {
        String filename = "";
        // Handle no file supplied:
        // If we're able to pop a dialog, we'll ask the user for a song.
        // If there *is* a file supplied as the first argument, we'll use that.
        if (args.length < 1) {
            if (GraphicsEnvironment.isHeadless()) {
                showError("Error: No input file specified.  Please pass a filename.", 1);
            } else {
                final JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Open a song");
                fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
                fc.setDialogType(JFileChooser.OPEN_DIALOG);
                fc.setMultiSelectionEnabled(false);
                int returnVal = fc.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    try {
                        filename = fc.getSelectedFile().getCanonicalPath();
                    } catch (IOException ex) {

                    }
                } else {
                    showError("No input file specified.", 1);
                }
            }
        } else {
            filename = args[0];
        }

        new Conductor(filename);
    }

    /**
     * Get the AudioFormat.
     *
     * @return AudioFormat the format of the audio.
     */
    public static AudioFormat getAudioFormat() {
        return AF;
    }
}

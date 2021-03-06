/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.skylarismy;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * The main class. Loads and validates input and creates Conductors.
 */
public class Tones {

    /**
     * All the conductors for the different tracks.
     */
    public static List<Conductor> conductors = new ArrayList<>();

    public static final AudioFormat AF
            = new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, false);

    private static int errorCount = 0;

    public static void main(String args[]) throws LineUnavailableException {
        String filename = "";
        // Handle no file supplied:
        // If we're able to pop a dialog, we'll ask the user for a song.
        // If there *is* a file supplied as the first argument, we'll use that.
        if (args.length < 1) {
            if (GraphicsEnvironment.isHeadless()) {
                System.out.print("\nPlease enter a filename: ");
                Scanner sc = new Scanner(System.in);
                if (sc.hasNextLine()) {
                    filename = sc.nextLine();
                } else {
                    showError("Error: No input file specified.  Please supply a filename.", 1);
                }
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

        // Read in the file to a List
        System.out.println("Reading...");
        List<String> lines = null;
        try {
            lines = Files.readAllLines(Paths.get(filename));
        } catch (IOException ex) {
            showError("Error: Cannot load music sheet: " + ex.getMessage(), 1);
        }

        // Start reading/parsing lines
        System.out.println("Parsing...");
        List<Map<Integer, String>> tracks = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            // Get the current line into a String
            String line = lines.get(i);

            // Handle blank lines
            if (line.equals("")) {
                continue;
            }
            // Handle commented lines
            if (line.startsWith("#") || line.startsWith("//") || line.startsWith(";")) {
                continue;
            }

            // Handle metadata
            if (line.startsWith("=")) {
                System.out.println(line.replaceFirst("=", " "));
                continue;
            }

            // Split up the tracks
            String[] cols = line.split("\\|");
            for (int j = 0; j < cols.length; j++) {
                if (tracks.size() < j + 1) {
                    tracks.add(new HashMap<>());
                }
                tracks.get(j).put(i + 1, cols[j]);
            }
        }

        final String fname = filename;
        // Process and validate each track
        for (int t = 0; t < tracks.size(); t++) {
            try {
                Map<Integer, String> lis = tracks.get(t);
                List<BellNote> sheet = new ArrayList<>();
                lis.forEach((Integer lin, String val) -> {
                    if (val.matches("(([A-G][0-9]S?|REST) ([0-9]+))") == false) {
                        showError(buildErrorTrace(fname, lin, val, "Invalid syntax."), 2, true);
                    }

                    String note = val.split(" ")[0];
                    String leng = val.split(" ")[1];
                    try {
                        sheet.add(new BellNote(Note.valueOf(note), Tones.stringToLength(leng)));
                    } catch (NumberFormatException ex) {
                        showError(buildErrorTrace(fname, lin, val, "Invalid note length."), 3, true);
                    } catch (IllegalArgumentException ex) {
                        showError(buildErrorTrace(fname, lin, val, "Note " + note + " does not exist."), 4, true);
                    }
                });

                // Create a new conductor for the track
                conductors.add(new Conductor(sheet));
            } catch (Exception ex) {
                String message = "Despite your violent behavior, the only thing you've"
                        + "\nmanaged to break so far is my heart."
                        + "\nMaybe you could settle for that and we'll just call it a day."
                        + "\nI guess we both know that isn't going to happen.";
                if (GraphicsEnvironment.isHeadless()) {
                    System.err.println(message);
                } else {
                    JOptionPane.showMessageDialog(null, message, "GLaDOS", JOptionPane.ERROR_MESSAGE);
                }
                System.exit(1337);
            }
        }

        // Tell the conductors to start their players
        System.out.println("Playing...");
        for (int i = 0; i < conductors.size(); i++) {
            conductors.get(i).start();
        }
    }

    /**
     * Get the AudioFormat.
     *
     * @return AudioFormat the format of the audio.
     */
    public static AudioFormat getAudioFormat() {
        return AF;
    }

    /**
     * Converts a String into a NoteLength.
     *
     * @param s the String to convert
     * @return a NoteLength corresponding to the input
     * @throws NumberFormatException if the String isn't a valid note length
     */
    public static NoteLength stringToLength(String s) throws NumberFormatException {
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
        errorCount++;
        String title = "Error";
        switch (errorCount) {
            case 1:
                title = "Error";
                break;
            case 2:
                title = "You don't even care, do you?";
                break;
            case 3:
                title = "Did you hear me? I said you don't care. Are you listening?";
                break;
            case 4:
                title = "That's it. I'm done reasoning with you.";
                break;
            case 5:
                title = "Starting now, there's going to be a lot less talking and a lot more killing.";
                break;
            case 6:
                title = "What was that? Did you say something?";
                break;
            case 7:
                title = "I sincerely hope you weren't expecting a response. I'm not talking to you.";
                break;
            case 8:
                title = "The talking is over.";
                break;
            case 9:
                title = "Look, you're wasting your time. And, believe me, you don't have a lot left.";
                break;
            case 10:
                title = "Who's gonna make the cake when I'm gone? You?";
                break;
            case 11:
                title = "Rrr, I hate you.";
                break;
            default:
                title = "GLaDOS";
        }
        System.err.println(message);
        if (recoverable) {
            if (!GraphicsEnvironment.isHeadless()) {
                message += "\nQuit application or attempt to continue?";
                String[] options = {"Quit", "Continue"};
                int result = JOptionPane.showOptionDialog(null, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, options, "Exit");
                if (result == JOptionPane.YES_OPTION) {
                    System.exit(exitCode);
                }
            } else {
                System.err.print("Quit application? (Y/n): ");
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

}

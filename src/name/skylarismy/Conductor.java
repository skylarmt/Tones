package name.skylarismy;

import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Conductor extends Thread {

    public volatile int currentNote = 0;

    public List<BellNote> sheetmusic = new ArrayList<>();

    public static final AudioFormat AF
            = new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, false);

    private List<Player> players = new ArrayList<>();

    public static final Note[] NOTES = Note.values();

    public SourceDataLine line;

    public Conductor(List<BellNote> music, String filename) throws LineUnavailableException {
        line = AudioSystem.getSourceDataLine(Conductor.getAudioFormat());
        line.open();
        line.start();
        sheetmusic = music;

        // Setup all the players and assign notes
        BellNote[] noteArray = sheetmusic.toArray(new BellNote[sheetmusic.size()]);
        for (int i = 0; i < NOTES.length; i++) {
            players.add(new Player(Note.values()[i], noteArray, line, this));
        }
    }

    @Override
    public void run() {
        players.stream().forEach((player) -> {
            player.start();
        });

        // Wait for someone to die
        try {
            players.get(players.size() - 1).join();
        } catch (InterruptedException e) {

        }
        line.drain();
    }

    public void playNote(BellNote bn, SourceDataLine l) {
        final int ms = Math.min(bn.length.timeMs(), (int) (Note.MEASURE_LENGTH_SEC * 1000));
        final int length = Note.SAMPLE_RATE * ms / 1000;
        l.write(bn.note.sample(), 0, length);
        l.write(Note.REST.sample(), 0, 50);
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

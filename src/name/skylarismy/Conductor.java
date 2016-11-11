package name.skylarismy;

import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Conductor extends Thread {

    /**
     * Index of the current note.
     */
    public volatile int currentNote = 0;

    /**
     * The sheet music for this track.
     */
    public List<BellNote> sheetmusic = new ArrayList<>();

    /**
     * The musicians.
     */
    private final List<Player> players = new ArrayList<>();

    /**
     * All the notes.
     */
    public static final Note[] NOTES = Note.values();

    /**
     * Audio output line.
     */
    public SourceDataLine line;

    /**
     * Create a new Conductor. Automatically configures Players.
     *
     * @param music The song to play.
     * @throws LineUnavailableException if something is horribly wrong.
     */
    public Conductor(List<BellNote> music) throws LineUnavailableException {
        line = AudioSystem.getSourceDataLine(Tones.getAudioFormat());
        line.open();
        line.start();
        sheetmusic = music;

        // Setup all the players and assign notes
        BellNote[] noteArray = sheetmusic.toArray(new BellNote[sheetmusic.size()]);
        for (int i = 0; i < NOTES.length; i++) {
            players.add(new Player(Note.values()[i], noteArray, this));
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

    /**
     * Play a note.
     *
     * @param bn the BellNote to play
     */
    public void playNote(BellNote bn) {
        final int ms = Math.min(bn.length.timeMs(), (int) (Note.MEASURE_LENGTH_SEC * 1000));
        final int length = Note.SAMPLE_RATE * ms / 1000;
        line.write(bn.note.sample(), 0, length);
        line.write(Note.REST.sample(), 0, 50);
    }
}

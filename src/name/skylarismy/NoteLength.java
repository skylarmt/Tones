package name.skylarismy;

enum NoteLength {
    WHOLE(1.0f),
    HALF(0.5f),
    QUARTER(0.25f),
    EIGTH(0.125f),
    SIXTEENTH(0.0625f),
    THIRTYSECOND(0.03125f),
    ZERO(0.00001f);

    private final int timeMs;

    private NoteLength(float length) {
        timeMs = (int) (length * Note.MEASURE_LENGTH_SEC * 1000);
    }

    public int timeMs() {
        return timeMs;
    }
}

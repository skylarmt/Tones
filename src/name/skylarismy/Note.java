package name.skylarismy;

enum Note {
    // REST Must be the first 'Note'
    REST,
    A2,
    A2S,
    B2,
    C2,
    C2S,
    D2,
    D2S,
    E2,
    F2,
    F2S,
    G2,
    G2S,
    A3,
    A3S,
    B3,
    C3,
    C3S,
    D3,
    D3S,
    E3,
    F3,
    F3S,
    G3,
    G3S,
    A4,
    A4S,
    B4,
    C4,
    C4S,
    D4,
    D4S,
    E4,
    F4,
    F4S,
    G4,
    G4S,
    A5,
    A5S,
    B5,
    C5,
    C5S,
    D5,
    D5S,
    E5,
    F5,
    F5S,
    G5,
    G5S;

    public static final int SAMPLE_RATE = 48 * 1024; // ~48KHz
    public static final double MEASURE_LENGTH_SEC = 1.5;

    // Circumference of a circle divided by # of samples
    private static final double step_alpha = (2.0d * Math.PI) / SAMPLE_RATE;

    private final double FREQUENCY_A_HZ = 440.0d;
    private final double MAX_VOLUME = 100.0d;

    private final byte[] sinSample = new byte[(int) (MEASURE_LENGTH_SEC * SAMPLE_RATE)];

    private Note() {
        int n = this.ordinal();
        if (n > 0) {
            // Calculate the frequency!
            double halfStepUpFromA = n;
//            if (n < 37) {
//                halfStepUpFromA = n - 73;
//            }
            final double exp = halfStepUpFromA / 12.0d;
            final double freq = FREQUENCY_A_HZ * Math.pow(2.0d, exp);

            final double step = freq * step_alpha;
            for (int i = 0; i < sinSample.length; i++) {
                //if (n < 37) {
                //    sinSample[i] = (byte) (Math.tan(i * step) * MAX_VOLUME / 2);
                //} else {
                sinSample[i] = (byte) (Math.sin(i * step) * MAX_VOLUME);
                //}
            }
        }
    }

    public byte[] sample() {
        return sinSample;
    }
}

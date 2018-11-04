public class Measure {
    private static long START_TIME = -1;

    public static void START() {
        START_TIME = System.nanoTime();
    }

    public static String STOP() {
        long endTime = System.nanoTime();
        if (START_TIME == -1)
            throw new RuntimeException("Stopping measure when not started yet");
        StringBuilder out = new StringBuilder(2);
        long elapsedTime = endTime - START_TIME;
        long ms = elapsedTime / 1000000;
        long s = ms / 1000;
        out.append("Elapsed time (nanoseconds): ").append(elapsedTime).append(" ns.").append("\n");
        out.append("Elapsed time (milliseconds): ").append(ms).append(" ms.").append("\n");
        out.append("Elapsed time (seconds): ").append(s).append(" s.").append("\n");
        if (s >= 60) {
            long m = s / 60;
            long seconds = s % 60;
            out.append("Elapsed time (m,s): ").append(m).append(" m")
                    .append(seconds).append(" s").append("\n");
            if (m >= 60) {
                long h = m / 60;
                long minutes = m % 60;
                out.append("Elapsed time (h,m,s): ").append(h).append(" h")
                        .append(minutes).append(" m").append(seconds).append(" s").append("\n");
            }
        }
        START_TIME = -1;
        return out.toString();
    }
}

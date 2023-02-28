package Tree;

public class Timer {
    private long start;
    private double secondsAllowed;

    public Timer(double secondsAllowed) {
        start = System.currentTimeMillis();
        this.secondsAllowed = secondsAllowed;
    }

    public boolean timeLeft() {
        return System.currentTimeMillis() - (long) (secondsAllowed * 1000) < start;
    }
}

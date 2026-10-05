package com.cntrident.fakevoidtrading;

/** Tick-based background cooldown; elapsed time alone never authorizes a return. */
public final class ReturnGate {
    private int deadline;
    private boolean started, storageFinished;

    public void start(int arrivalTick, double seconds) {
        deadline = arrivalTick + (int) Math.ceil(sanitizeSeconds(seconds) * 20.0);
        started = true;
        storageFinished = false;
    }
    public void finishStorage() { storageFinished = true; }
    public int remaining(int tick) { return Math.max(0, deadline - tick); }
    public boolean ready(int tick) { return started && storageFinished && remaining(tick) == 0; }
    public static double sanitizeSeconds(double seconds) {
        return Double.isFinite(seconds) ? Math.max(0.0, Math.min(60.0, seconds)) : 2.0;
    }
}

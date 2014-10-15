package com.micdm.transportlive.misc;

public class TimeChecker {

    private final int interval;
    private long lastCheck;

    public TimeChecker(int interval) {
        this.interval = interval;
    }

    public boolean check() {
        long now = System.currentTimeMillis();
        if (now - lastCheck > interval * 1000) {
            lastCheck = now;
            return true;
        }
        return false;
    }
}

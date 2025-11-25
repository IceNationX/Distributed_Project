package com.stocksim.core;

import java.io.Serializable;

// lamport clock for ordering events in distributed system
public class LamportClock implements Serializable {
    private static final long serialVersionUID = 1L;

    // local logical time
    private long time;

    // start clock at 0
    public LamportClock() {
        this.time = 0;
    }

    // internal event tick
    public synchronized void tick() {
        this.time++;
    }

    // outgoing message tick
    public synchronized long updateOnSend() {
        this.time++;
        return this.time;
    }

    // update clock on receiving timestamp
    public synchronized void updateOnReceive(long receivedTime) {
        this.time = Math.max(this.time, receivedTime) + 1;
    }

    // get current logical time
    public synchronized long getTime() {
        return this.time;
    }

    @Override
    public String toString() {
        return "LamportClock{" + "time=" + time + '}';
    }
}

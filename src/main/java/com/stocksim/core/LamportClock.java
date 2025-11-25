package com.stocksim.core;

import java.io.Serializable;

/**
 * Implements a Lamport logical clock for maintaining causal ordering of events
 * in a distributed system. Each event (local, send, receive) updates the clock.
 */
public class LamportClock implements Serializable {
    private static final long serialVersionUID = 1L;
    private long time;

    public LamportClock() {
        this.time = 0;
    }

    /**
     * Increments the local clock for an internal event.
     */
    public synchronized void tick() {
        this.time++;
    }

    /**
     * Increments the clock and returns the new value for an outgoing message.
     * This signifies a "send event".
     * @return The logical timestamp to be sent with a message.
     */
    public synchronized long updateOnSend() {
        this.time++;
        return this.time;
    }

    /**
     * Updates the local clock upon receiving a message.
     * The local clock is set to max(localTime, receivedTime) + 1.
     * This signifies a "receive event".
     * @param receivedTime The Lamport timestamp from the incoming message.
     */
    public synchronized void updateOnReceive(long receivedTime) {
        this.time = Math.max(this.time, receivedTime) + 1;
    }

    /**
     * Returns the current logical time of this clock.
     * @return The current Lamport timestamp.
     */
    public synchronized long getTime() {
        return this.time;
    }

    @Override
    public String toString() {
        return "LamportClock{" + "time=" + time + '}';
    }
}

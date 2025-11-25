package com.stocksim.data;

import java.io.Serializable;

public class TradeMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    // sender agent id
    private final String senderId;

    // receiver id (usually market)
    private final String receiverId;

    // type of message (ORDER or HEARTBEAT)
    private final MessageType type;

    // order attached (only if type = ORDER)
    private final Order order;

    // lamport timestamp attached to message
    private final long lamportTimestamp;

    // builds message object
    public TradeMessage(String senderId, String receiverId, MessageType type, Order order, long lamportTimestamp) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.type = type;
        this.order = order;
        this.lamportTimestamp = lamportTimestamp;
    }

    // getters for fields
    public String getSenderId() { return senderId; }
    public String getReceiverId() { return receiverId; }
    public MessageType getType() { return type; }
    public Order getOrder() { return order; }
    public long getLamportTimestamp() { return lamportTimestamp; }
}

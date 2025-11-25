package com.stocksim.data;

import java.io.Serializable;

public class TradeMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String senderId;
    private final String receiverId;
    private final MessageType type;
    private final Order order;
    private final long lamportTimestamp;

    public TradeMessage(String senderId, String receiverId, MessageType type, Order order, long lamportTimestamp) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.type = type;
        this.order = order;
        this.lamportTimestamp = lamportTimestamp;
    }

    public String getSenderId() { return senderId; }
    public String getReceiverId() { return receiverId; }
    public MessageType getType() { return type; }
    public Order getOrder() { return order; }
    public long getLamportTimestamp() { return lamportTimestamp; }
}

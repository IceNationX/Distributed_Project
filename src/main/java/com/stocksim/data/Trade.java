package com.stocksim.data;

import java.io.Serializable;

public class Trade implements Serializable {
    private static final long serialVersionUID = 1L;

    // unique id for the trade
    private final String tradeId;

    // agent who executed trade
    private final String agentId;

    // stock symbol
    private final String stockSymbol;

    // quantity traded
    private final int quantity;

    // price per share
    private final double price;

    // lamport timestamp when trade happened
    private final long lamportTimestamp;

    // actual system time in ms
    private final long systemTimeMillis;

    // builds trade object
    public Trade(String tradeId, String agentId, String stockSymbol, int quantity, double price, long lamportTimestamp, long systemTimeMillis) {
        this.tradeId = tradeId;
        this.agentId = agentId;
        this.stockSymbol = stockSymbol;
        this.quantity = quantity;
        this.price = price;
        this.lamportTimestamp = lamportTimestamp;
        this.systemTimeMillis = systemTimeMillis;
    }

    // getters
    public String getTradeId() { return tradeId; }
    public String getAgentId() { return agentId; }
    public String getStockSymbol() { return stockSymbol; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public long getLamportTimestamp() { return lamportTimestamp; }
    public long getSystemTimeMillis() { return systemTimeMillis; }
}

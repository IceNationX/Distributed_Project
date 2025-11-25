package com.stocksim.data;

import java.io.Serializable;

public class Trade implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String tradeId;
    private final String agentId;
    private final String stockSymbol;
    private final int quantity;
    private final double price;
    private final long lamportTimestamp;
    private final long systemTimeMillis;

    public Trade(String tradeId, String agentId, String stockSymbol, int quantity, double price, long lamportTimestamp, long systemTimeMillis) {
        this.tradeId = tradeId;
        this.agentId = agentId;
        this.stockSymbol = stockSymbol;
        this.quantity = quantity;
        this.price = price;
        this.lamportTimestamp = lamportTimestamp;
        this.systemTimeMillis = systemTimeMillis;
    }

    public String getTradeId() { return tradeId; }
    public String getAgentId() { return agentId; }
    public String getStockSymbol() { return stockSymbol; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public long getLamportTimestamp() { return lamportTimestamp; }
    public long getSystemTimeMillis() { return systemTimeMillis; }
}

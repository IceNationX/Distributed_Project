package com.stocksim.data;

import java.io.Serializable;

public class Order implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String agentId;
    private final String stockSymbol;
    private final int quantity;
    private final double price;
    private final OrderType type;

    public Order(String agentId, String stockSymbol, int quantity, double price, OrderType type) {
        this.agentId = agentId;
        this.stockSymbol = stockSymbol;
        this.quantity = quantity;
        this.price = price;
        this.type = type;
    }

    public String getAgentId() { return agentId; }
    public String getStockSymbol() { return stockSymbol; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public OrderType getType() { return type; }

    @Override
    public String toString() {
        return "Order{" + "agentId='" + agentId + "'" + ", type=" + type + '}' ;
    }
}

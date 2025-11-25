package com.stocksim.data;

import java.io.Serializable;

public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    // id of agent sending the order
    private final String agentId;

    // stock symbol (AAPL etc)
    private final String stockSymbol;

    // how many shares
    private final int quantity;

    // price per share
    private final double price;

    // buy or sell
    private final OrderType type;

    // builds order object
    public Order(String agentId, String stockSymbol, int quantity, double price, OrderType type) {
        this.agentId = agentId;
        this.stockSymbol = stockSymbol;
        this.quantity = quantity;
        this.price = price;
        this.type = type;
    }

    // getters for fields
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

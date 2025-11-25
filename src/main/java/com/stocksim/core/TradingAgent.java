package com.stocksim.core;

import com.stocksim.data.*;
import com.stocksim.metrics.Metrics;
import com.stocksim.net.MarketNodeRemote;

import java.rmi.RemoteException;
import java.util.Random;

/**
 * A TradingAgent runs as a separate thread, simulating a trader.
 * Sends orders and heartbeats to the MarketNode.
 */
public class TradingAgent implements Runnable {

    private static final int BASE_METRICS_PORT = 9090;

    private final String agentId;           // agent name
    private final MarketNodeRemote market;  // remote market node ref
    private final LamportClock clock;       // local lamport clock
    private final Random random = new Random();
    private final String[] stockSymbols = {"AAPL", "GOOG", "TSLA"}; // random stocks
    private final boolean simulateFailure;  // make agent die on purpose
    private int messageCount = 0;

    // agent constructor
    public TradingAgent(String agentId, MarketNodeRemote market, boolean simulateFailure) {
        this.agentId = agentId;
        this.market = market;
        this.clock = new LamportClock();
        this.simulateFailure = simulateFailure;

        // start metrics server for this agent
        try {
            int agentNumericId = Integer.parseInt(agentId.substring(agentId.lastIndexOf("-") + 1));
            int metricsPort = BASE_METRICS_PORT + agentNumericId;
            Metrics.startMetricsServer(metricsPort);
            Metrics.NODE_STATUS.labels(this.agentId).set(1); // agent UP
            tick(); // init lamport value
        } catch (NumberFormatException e) {
            System.err.println("Could not parse agent ID for metrics port: " + agentId);
        }
    }

    // tick lamport clock and update metric
    private long tick() {
        long timestamp = this.clock.updateOnSend();
        Metrics.LAMPORT_CLOCK.labels(this.agentId).set(timestamp);
        return timestamp;
    }

    // main agent loop
    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {

                // random delay between actions
                Thread.sleep(1000 + random.nextInt(2000));

                // simulate the agent dying after a few messages
                if (simulateFailure && messageCount > (5 + random.nextInt(5))) {
                    System.out.printf("!!! Agent %s is now SIMULATING FAILURE - stopping all messages. !!!%n", agentId);
                    Metrics.NODE_STATUS.labels(this.agentId).set(0); // mark DOWN
                    break;
                }

                // 70 percent chance send order, 30 percent heartbeat
                boolean sendOrder = random.nextDouble() > 0.3;
                if (sendOrder) {
                    sendOrderMessage();
                } else {
                    sendHeartbeatMessage();
                }

                messageCount++;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // thread stopped
        } catch (RemoteException e) {
            System.err.printf("Agent %s lost connection to MarketNode: %s%n", agentId, e.getMessage());
            Metrics.NODE_STATUS.labels(this.agentId).set(0); // mark failed
        }
    }

    // builds and sends an ORDER message
    private void sendOrderMessage() throws RemoteException {
        OrderType type = random.nextBoolean() ? OrderType.BUY : OrderType.SELL;
        String symbol = stockSymbols[random.nextInt(stockSymbols.length)];
        int quantity = 1 + random.nextInt(100);
        double price = 10.0 + (190.0 * random.nextDouble());
        Order order = new Order(agentId, symbol, quantity, price, type);

        // show clock before sending
        System.out.printf("Agent %s: Local clock before sending ORDER: %d%n", agentId, clock.getTime());

        long timestamp = tick(); // lamport tick
        System.out.printf("Agent %s: Local clock after tick (ORDER): %d. Sending timestamp: %d%n",
                agentId, clock.getTime(), timestamp);

        // create message object
        TradeMessage message = new TradeMessage(agentId, MarketNode.RMI_NAME, MessageType.ORDER, order, timestamp);

        // send to market
        market.submitMessage(message);

        // metrics update
        Metrics.MESSAGES_SENT_TOTAL.labels(this.agentId).inc();

        // debug log
        System.out.printf("[LT=%d] Agent %s -> Market: Sent ORDER %s %d %s @ %.2f%n",
                timestamp, agentId, type, quantity, symbol, price);
    }

    // builds and sends a HEARTBEAT message
    private void sendHeartbeatMessage() throws RemoteException {
        // log clock before sending
        System.out.printf("Agent %s: Local clock before sending HEARTBEAT: %d%n", agentId, clock.getTime());

        long timestamp = tick(); // tick
        System.out.printf("Agent %s: Local clock after tick (HEARTBEAT): %d. Sending timestamp: %d%n",
                agentId, clock.getTime(), timestamp);

        // heartbeat has no order attached
        TradeMessage message = new TradeMessage(agentId, MarketNode.RMI_NAME, MessageType.HEARTBEAT, null, timestamp);

        // send
        market.submitMessage(message);

        // metrics
        Metrics.HEARTBEATS_TOTAL.labels(this.agentId).inc();
        Metrics.MESSAGES_SENT_TOTAL.labels(this.agentId).inc();

        // log
        System.out.printf("[LT=%d] Agent %s -> Market: Sent HEARTBEAT%n", timestamp, agentId);
    }
}

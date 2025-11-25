package com.stocksim.core;

import com.stocksim.data.*;
import com.stocksim.metrics.Metrics;
import com.stocksim.net.HeartbeatMonitor;
import com.stocksim.net.MarketNodeRemote;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Main market server. Handles orders, heartbeats, trades, metrics.
 */
public class MarketNode extends UnicastRemoteObject implements MarketNodeRemote {

    public static final String RMI_NAME = "MarketNode";
    private static final int METRICS_PORT = 8080;
    private static final int MAX_TRADES_IN_STATE = 50;
    private static final long AGENT_TIMEOUT_MS = 10000; // timeout for failure

    private final String nodeId;
    private final LamportClock clock;               // lamport clock
    private final List<Trade> tradeLog;             // log of executed trades
    private final HeartbeatMonitor monitor;         // tracks agent heartbeats
    private final Map<String, String> agentStatuses;// ACTIVE or FAILED

    // constructor (starts metrics and failure detector)
    public MarketNode() throws RemoteException {
        super();
        this.nodeId = "market-node-01";
        this.clock = new LamportClock();
        this.tradeLog = new CopyOnWriteArrayList<>();
        this.monitor = new HeartbeatMonitor();
        this.agentStatuses = new ConcurrentHashMap<>();

        // start prometheus metrics on port 8080
        Metrics.startMetricsServer(METRICS_PORT);
        Metrics.NODE_STATUS.labels(this.nodeId).set(1); // market node is UP
        tick(); // update lamport clock metric

        // background failure detector thread
        Thread failureDetectorThread = new Thread(this::runFailureDetector);
        failureDetectorThread.setDaemon(true);
        failureDetectorThread.start();

        System.out.println("MarketNode initialized. Failure detector started.");
    }

    // local lamport tick + update metric
    private void tick() {
        this.clock.tick();
        Metrics.LAMPORT_CLOCK.labels(this.nodeId).set(this.clock.getTime());
    }

    // loops and checks for failed agents
    private void runFailureDetector() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(2000);

                // get latest statuses from heartbeat monitor
                Map<String, String> currentStatuses = monitor.getStatuses(AGENT_TIMEOUT_MS);

                for (Map.Entry<String, String> entry : currentStatuses.entrySet()) {
                    String agentId = entry.getKey();
                    String currentStatus = entry.getValue();
                    String previousStatus = agentStatuses.getOrDefault(agentId, "ACTIVE");

                    // agent newly detected as failed
                    if ("FAILED".equals(currentStatus) && "ACTIVE".equals(previousStatus)) {
                        System.out.printf("[FAULT DETECTOR] Agent %s has failed (no heartbeat). Marking as FAILED.%n", agentId);

                        // mark failure in metrics
                        Metrics.FAILURES_DETECTED_TOTAL.inc();
                        Metrics.NODE_STATUS.labels(agentId).set(0); // agent DOWN
                    }

                    // update status map
                    agentStatuses.put(agentId, currentStatus);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // stop thread
            }
        }
    }

    // main entry point when agents send messages
    @Override
    public synchronized void submitMessage(TradeMessage message) throws RemoteException {
        // metrics count of messages received
        Metrics.MESSAGES_RECEIVED_TOTAL.labels(this.nodeId).inc();

        // show clock before merge
        System.out.printf("MarketNode: Local clock before receiving message from %s (LT=%d): %d%n",
                message.getSenderId(), message.getLamportTimestamp(), clock.getTime());

        // sync lamport clock with message timestamp
        clock.updateOnReceive(message.getLamportTimestamp());
        Metrics.LAMPORT_CLOCK.labels(this.nodeId).set(this.clock.getTime());

        // show clock after merge
        System.out.printf("MarketNode: Local clock after updateOnReceive: %d%n", clock.getTime());

        // route message
        switch (message.getType()) {
            case ORDER:
                handleOrder(message);
                break;
            case HEARTBEAT:
                handleHeartbeat(message);
                break;
        }
    }

    // handles order messages
    private void handleOrder(TradeMessage message) {
        Order order = message.getOrder();
        if (order == null) return;

        // count this trade in metrics
        Metrics.TRADES_TOTAL.labels(order.getType().toString()).inc();

        // create executed trade log entry
        Trade executedTrade = new Trade(
                UUID.randomUUID().toString(),
                order.getAgentId(),
                order.getStockSymbol(),
                order.getQuantity(),
                order.getPrice(),
                clock.getTime(),
                System.currentTimeMillis()
        );

        tradeLog.add(executedTrade);

        System.out.printf("[LT=%d] MarketNode: Processed %s order from %s (Msg LT=%d)%n",
                clock.getTime(), order.getType(), order.getAgentId(), message.getLamportTimestamp());
    }

    // handles heartbeat messages from agents
    private void handleHeartbeat(TradeMessage message) {
        monitor.updateHeartbeat(message.getSenderId());

        // mark agent as UP (in case previously down)
        Metrics.NODE_STATUS.labels(message.getSenderId()).set(1);

        System.out.printf("[LT=%d] MarketNode: Received heartbeat from %s (Msg LT=%d)%n",
                clock.getTime(), message.getSenderId(), message.getLamportTimestamp());
    }

    // frontend UI calls this to get latest system snapshot
    @Override
    public SystemState getState() throws RemoteException {
        List<Trade> recentTrades = tradeLog.stream()
                .skip(Math.max(0, tradeLog.size() - MAX_TRADES_IN_STATE))
                .collect(Collectors.toList());

        return new SystemState(
                recentTrades,
                monitor.getStatuses(AGENT_TIMEOUT_MS),
                "UP"
        );
    }
}

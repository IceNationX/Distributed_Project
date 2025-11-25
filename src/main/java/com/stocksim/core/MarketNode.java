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
 * The MarketNode is the central server in the simulation. It implements the RMI remote
 * interface to receive messages from TradingAgents. It processes orders, maintains the
 * official trade log, and monitors agent liveness.
 */
public class MarketNode extends UnicastRemoteObject implements MarketNodeRemote {

    public static final String RMI_NAME = "MarketNode";
    private static final int METRICS_PORT = 8080;
    private static final int MAX_TRADES_IN_STATE = 50;
    private static final long AGENT_TIMEOUT_MS = 10000; // 10 seconds

    private final String nodeId;
    private final LamportClock clock;
    private final List<Trade> tradeLog;
    private final HeartbeatMonitor monitor;
    private final Map<String, String> agentStatuses;

    public MarketNode() throws RemoteException {
        super();
        this.nodeId = "market-node-01";
        this.clock = new LamportClock();
        this.tradeLog = new CopyOnWriteArrayList<>();
        this.monitor = new HeartbeatMonitor();
        this.agentStatuses = new ConcurrentHashMap<>();

        // --- Metrics Initialization ---
        Metrics.startMetricsServer(METRICS_PORT);
        Metrics.NODE_STATUS.labels(this.nodeId).set(1); // 1 for UP
        tick(); // Initialize clock metric
        // --------------------------

        Thread failureDetectorThread = new Thread(this::runFailureDetector);
        failureDetectorThread.setDaemon(true);
        failureDetectorThread.start();

        System.out.println("MarketNode initialized. Failure detector started.");
    }

    private void tick() {
        this.clock.tick();
        Metrics.LAMPORT_CLOCK.labels(this.nodeId).set(this.clock.getTime());
    }

    private void runFailureDetector() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(2000);
                Map<String, String> currentStatuses = monitor.getStatuses(AGENT_TIMEOUT_MS);
                for (Map.Entry<String, String> entry : currentStatuses.entrySet()) {
                    String agentId = entry.getKey();
                    String currentStatus = entry.getValue();
                    String previousStatus = agentStatuses.getOrDefault(agentId, "ACTIVE");

                    if ("FAILED".equals(currentStatus) && "ACTIVE".equals(previousStatus)) {
                        System.out.printf("[FAULT DETECTOR] Agent %s has failed (no heartbeat). Marking as FAILED.%n", agentId);
                        // --- Metrics Update ---
                        Metrics.FAILURES_DETECTED_TOTAL.inc();
                        Metrics.NODE_STATUS.labels(agentId).set(0); // 0 for FAILED
                        // --------------------
                    }
                    agentStatuses.put(agentId, currentStatus);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public synchronized void submitMessage(TradeMessage message) throws RemoteException {
        // --- Metrics Update ---
        Metrics.MESSAGES_RECEIVED_TOTAL.labels(this.nodeId).inc();
        // --------------------

        System.out.printf("MarketNode: Local clock before receiving message from %s (LT=%d): %d%n",
                message.getSenderId(), message.getLamportTimestamp(), clock.getTime());
        
        clock.updateOnReceive(message.getLamportTimestamp());
        Metrics.LAMPORT_CLOCK.labels(this.nodeId).set(this.clock.getTime()); // Update gauge after sync
        
        System.out.printf("MarketNode: Local clock after updateOnReceive: %d%n", clock.getTime());

        switch (message.getType()) {
            case ORDER:
                handleOrder(message);
                break;
            case HEARTBEAT:
                handleHeartbeat(message);
                break;
        }
    }

    private void handleOrder(TradeMessage message) {
        Order order = message.getOrder();
        if (order == null) return;

        // --- Metrics Update ---
        Metrics.TRADES_TOTAL.labels(order.getType().toString()).inc();
        // --------------------

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

    private void handleHeartbeat(TradeMessage message) {
        monitor.updateHeartbeat(message.getSenderId());
        // Set node status to UP when a heartbeat is received, in case it was marked down
        Metrics.NODE_STATUS.labels(message.getSenderId()).set(1);
        System.out.printf("[LT=%d] MarketNode: Received heartbeat from %s (Msg LT=%d)%n",
                clock.getTime(), message.getSenderId(), message.getLamportTimestamp());
    }

    @Override
    public SystemState getState() throws RemoteException {
        List<Trade> recentTrades = tradeLog.stream()
                .skip(Math.max(0, tradeLog.size() - MAX_TRADES_IN_STATE))
                .collect(Collectors.toList());
        return new SystemState(recentTrades, monitor.getStatuses(AGENT_TIMEOUT_MS), "UP");
    }
}

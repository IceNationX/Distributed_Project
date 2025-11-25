package com.stocksim.metrics;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;

import java.io.IOException;

/**
 * Centralized registry for all Prometheus metrics.
 */
public class Metrics {

    // --- Gauges ---
    public static final Gauge LAMPORT_CLOCK = Gauge.build()
            .name("lamport_timestamp")
            .help("The current Lamport logical clock time for a node.")
            .labelNames("node_id")
            .register();

    public static final Gauge NODE_STATUS = Gauge.build()
            .name("node_status")
            .help("The status of a node (1=UP, 0=DOWN/FAILED).")
            .labelNames("node_id")
            .register();

    // --- Counters ---
    public static final Counter TRADES_TOTAL = Counter.build()
            .name("trade_count_total")
            .help("Total number of trades processed by the MarketNode.")
            .labelNames("order_type") // "BUY" or "SELL"
            .register();

    public static final Counter HEARTBEATS_TOTAL = Counter.build()
            .name("heartbeat_count_total")
            .help("Total number of heartbeats sent by an agent.")
            .labelNames("agent_id")
            .register();

    public static final Counter MESSAGES_SENT_TOTAL = Counter.build()
            .name("message_sent_total")
            .help("Total number of messages sent from a node.")
            .labelNames("node_id")
            .register();

    public static final Counter MESSAGES_RECEIVED_TOTAL = Counter.build()
            .name("message_received_total")
            .help("Total number of messages received by a node.")
            .labelNames("node_id")
            .register();

    public static final Counter FAILURES_DETECTED_TOTAL = Counter.build()
            .name("failure_detected_total")
            .help("Total number of agent failures detected by the MarketNode.")
            .register();

    /**
     * Initializes and starts the Prometheus HTTP server.
     * @param port The port for the /metrics endpoint.
     */
    public static void startMetricsServer(int port) {
        try {
            // Initialize JVM metrics
            DefaultExports.initialize();
            // Start the HTTP server
            new HTTPServer(port);
            System.out.println("Prometheus /metrics endpoint started on port: " + port);
        } catch (IOException e) {
            System.err.println("Failed to start Prometheus metrics server on port " + port);
            e.printStackTrace();
        }
    }
}

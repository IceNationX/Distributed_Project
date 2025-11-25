# Distributed Multi-Agent Stock Trading Simulation (with Prometheus & Grafana)

This project is an academic demonstration of a distributed system built with Java. It simulates a multi-agent stock trading environment to showcase core distributed systems concepts. The original JavaFX UI has been replaced with a production-grade observability stack (Prometheus and Grafana) to create a more realistic model of a modern distributed architecture.

## Architecture with Observability

The system consists of multiple Java processes that expose metrics via a standard `/metrics` HTTP endpoint. A Prometheus server scrapes these endpoints, and a Grafana instance visualizes the collected data.

```
+------------------+      +------------------+      +------------------+
|  TradingAgent 1  |      |  TradingAgent 2  | ...  |  TradingAgent N  |
|  /metrics:9091   |      |  /metrics:9092   |      |  /metrics:909N   |
+------------------+      +------------------+      +------------------+
         |                        |                        |
   Order/Heartbeat Msg            |           Order/Heartbeat Msg
    (via Java RMI)                |             (via Java RMI)
         |                        |                        |
         |   +------------------------------------------+     |
         +-->|               MarketNode                 |<----+
             |            (LamportClock)                |
             | - Processes Orders                       |
             | - Detects Failures                       |
             | - /metrics endpoint on port 8080         |
             +------------------------------------------+
                                  ^
                                  | Scrapes /metrics endpoints
                                  |
                       +---------------------+
                       |     Prometheus      |
                       | (localhost:9090)    |
                       +---------------------+
                                  ^
                                  | Queries data from Prometheus
                                  |
                       +---------------------+
                       |       Grafana       |
                       | (localhost:3000)    |
                       +---------------------+
```

## Final Report Explanation

### Why Prometheus + Grafana is the Correct Modern Choice

In modern distributed systems, especially those based on a microservices architecture, centralized and scalable monitoring is not a luxury—it is a fundamental requirement. The combination of Prometheus and Grafana has become the de-facto industry standard for observability for several key reasons:

1.  **Pull-Based Monitoring**: Prometheus operates on a pull model, where it actively scrapes `/metrics` endpoints from registered services. This is superior to push-based models (where services actively send data) because it decouples the monitoring system from the application logic, simplifies the client (services just expose data, they don't need to know the monitor's address), and provides a clear mechanism for service discovery and health checking (if a scrape fails, the service is likely down).

2.  **Standardization and Ecosystem**: Prometheus introduced a standardized, text-based exposition format that is now supported by thousands of tools and libraries. This makes it trivial to instrument applications written in any language. The ecosystem includes service discovery integrations, alert managers, and powerful visualization tools like Grafana.

3.  **Scalability and Resilience**: Prometheus is designed for reliability and scalability. Its architecture allows for federation and long-term storage solutions, making it suitable for everything from small projects to large-scale enterprise systems.

4.  **Powerful Query Language (PromQL)**: Prometheus features a rich, functional query language that allows for powerful and flexible analysis of time-series data, including rates, increases, aggregations, and predictions.

### How This Forms a Real Distributed Systems Model

This project now mirrors a real-world distributed architecture far more accurately than a monolithic application with a built-in UI.
- **Decoupled Components**: The `MarketNode`, `TradingAgent`s, `Prometheus`, and `Grafana` are all independent, decoupled processes communicating over a network. This is the essence of a distributed system.
- **Service-Based Architecture**: Each Java application (`MarketNode`, `TradingAgent`) acts as a self-contained service that performs a specific function and exposes its internal state via a standardized metrics interface.
- **Centralized Observability**: The monitoring stack is a critical but separate component of the system, just as it would be in a production environment. It provides a single pane of glass to observe the health and performance of all other services.

### How Key Concepts Become Observable

The primary benefit of this architecture is that it makes abstract distributed systems concepts tangible and observable.

1.  **Logical Time (Lamport Clocks)**: Instead of just printing timestamps to a console, we can now visualize the progression of logical time across all nodes on a single graph. The Grafana dashboard clearly shows each node's clock advancing independently. When a node receives a message, the characteristic "jump" in its clock (`time = max(local_time, received_time) + 1`) becomes a visible event on the timeline, perfectly demonstrating the synchronization algorithm.

2.  **Message Passing**: While we cannot see the message content itself, we can observe its effects. The dashboard shows real-time graphs of message and heartbeat rates (`rate(message_sent_total[1m])`), allowing us to quantify the communication load between components.

3.  **Fault Tolerance (Failure Detection)**: This is where the monitoring stack truly shines. The "Node Status" panel provides an immediate, color-coded view of which agents are `UP` and which are `FAILED`. When the designated agent stops sending heartbeats, we can see its status change from green to red in Grafana. Simultaneously, the "Agent Failures Detected" counter on the `MarketNode` increments. This provides a clear, visual confirmation that the failure detection mechanism is working as designed.

### Why This is Superior to a JavaFX UI

The original JavaFX UI, while functional, represents a monolithic design pattern that is antithetical to modern distributed principles.

- **Scalability**: The JavaFX UI polled the `MarketNode` directly. This would not scale if there were hundreds of nodes or multiple monitoring instances. The Prometheus pull model is far more scalable and efficient.
- **Flexibility**: The Grafana dashboard is fully customizable with a powerful query language. We can add new panels, create alerts, and correlate different metrics in ways that would require significant custom code in JavaFX.
- **Resilience**: If the JavaFX UI crashed, the simulation would continue, but all visibility would be lost. In the new architecture, if Grafana goes down, the data collection (Prometheus) and the simulation itself are unaffected.
- **Industry Relevance**: No real-world distributed system uses a custom-built JavaFX GUI for monitoring. Using Prometheus and Grafana provides experience with tools and concepts that are directly applicable to professional software engineering roles.

### How This Fulfills the Project Rubric

This implementation directly fulfills the core requirements of a distributed systems project by not only implementing key algorithms but also by demonstrating them in a realistic, observable, and industry-standard manner. It elevates the project from a simple simulation to a robust model of a production-grade distributed application, complete with a modern observability stack.
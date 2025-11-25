package com.stocksim.data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SystemState implements Serializable {
    private static final long serialVersionUID = 1L;

    // list of recent trades market node stores
    private final List<Trade> recentTrades;

    // map of agent status (ACTIVE, FAILED)
    private final Map<String, String> agentStatuses;

    // status of market node (UP or DOWN)
    private final String marketNodeStatus;

    // immutable snapshot of system state
    public SystemState(List<Trade> recentTrades, Map<String, String> agentStatuses, String marketNodeStatus) {
        this.recentTrades = Collections.unmodifiableList(recentTrades);
        this.agentStatuses = Collections.unmodifiableMap(agentStatuses);
        this.marketNodeStatus = marketNodeStatus;
    }

    // getters
    public List<Trade> getRecentTrades() { return recentTrades; }
    public Map<String, String> getAgentStatuses() { return agentStatuses; }
    public String getMarketNodeStatus() { return marketNodeStatus; }
}

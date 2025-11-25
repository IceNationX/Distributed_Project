package com.stocksim.data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SystemState implements Serializable {
    private static final long serialVersionUID = 1L;
    private final List<Trade> recentTrades;
    private final Map<String, String> agentStatuses;
    private final String marketNodeStatus;

    public SystemState(List<Trade> recentTrades, Map<String, String> agentStatuses, String marketNodeStatus) {
        this.recentTrades = Collections.unmodifiableList(recentTrades);
        this.agentStatuses = Collections.unmodifiableMap(agentStatuses);
        this.marketNodeStatus = marketNodeStatus;
    }

    public List<Trade> getRecentTrades() { return recentTrades; }
    public Map<String, String> getAgentStatuses() { return agentStatuses; }
    public String getMarketNodeStatus() { return marketNodeStatus; }
}

package com.stocksim.net;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class HeartbeatMonitor {
    private final Map<String, Long> lastSeenTimestamps = new ConcurrentHashMap<>();

    public void updateHeartbeat(String agentId) {
        lastSeenTimestamps.put(agentId, System.currentTimeMillis());
    }

    public synchronized Map<String, String> getStatuses(long timeoutMillis) {
        Map<String, String> statuses = new HashMap<>();
        long now = System.currentTimeMillis();
        for (Map.Entry<String, Long> entry : lastSeenTimestamps.entrySet()) {
            if (now - entry.getValue() > timeoutMillis) {
                statuses.put(entry.getKey(), "FAILED");
            } else {
                statuses.put(entry.getKey(), "ACTIVE");
            }
        }
        return statuses;
    }

    public synchronized Map<String, Boolean> getFailureFlags(long timeoutMillis) {
        return getStatuses(timeoutMillis).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> "FAILED".equals(e.getValue())));
    }

    public synchronized boolean isAgentFailed(String agentId, long timeoutMillis) {
        long lastSeen = lastSeenTimestamps.getOrDefault(agentId, 0L);
        return (System.currentTimeMillis() - lastSeen) > timeoutMillis;
    }
}

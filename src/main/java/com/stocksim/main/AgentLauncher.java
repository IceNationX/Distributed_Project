package com.stocksim.main;

import com.stocksim.core.MarketNode;
import com.stocksim.core.TradingAgent;
import com.stocksim.net.MarketNodeRemote;

import java.rmi.Naming;

public class AgentLauncher {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java com.stocksim.main.AgentLauncher <numberOfAgents>");
            System.exit(1);
        }
        try {
            int numberOfAgents = Integer.parseInt(args[0]);
            String rmiUrl = "//localhost/" + MarketNode.RMI_NAME;
            MarketNodeRemote market = (MarketNodeRemote) Naming.lookup(rmiUrl);
            System.out.println("Successfully connected to MarketNode at " + rmiUrl);

            for (int i = 0; i < numberOfAgents; i++) {
                String agentId = "agent-" + (i + 1);
                boolean willFail = (i == 0); // First agent will simulate failure
                TradingAgent agent = new TradingAgent(agentId, market, willFail);
                new Thread(agent).start();
                if (willFail) {
                    System.out.printf("Agent %s will simulate failure.%n", agentId);
                }
            }
            System.out.printf("Started %d trading agents.%n", numberOfAgents);
        } catch (Exception e) {
            System.err.println("AgentLauncher exception: " + e.toString());
            e.printStackTrace();
        }
    }
}

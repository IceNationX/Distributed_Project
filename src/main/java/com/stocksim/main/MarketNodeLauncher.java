package com.stocksim.main;

import com.stocksim.core.MarketNode;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class MarketNodeLauncher {
    public static void main(String[] args) {
        try {
            try {
                LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
                System.out.println("RMI registry created.");
            } catch (Exception e) {
                System.out.println("RMI registry already running.");
            }
            MarketNode marketNode = new MarketNode();
            String rmiUrl = "//localhost/" + MarketNode.RMI_NAME;
            Naming.rebind(rmiUrl, marketNode);
            System.out.println("MarketNode is ready and bound to " + rmiUrl);
        } catch (Exception e) {
            System.err.println("MarketNodeLauncher exception: " + e.toString());
            e.printStackTrace();
        }
    }
}

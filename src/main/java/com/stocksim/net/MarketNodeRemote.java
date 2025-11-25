package com.stocksim.net;

import com.stocksim.data.SystemState;
import com.stocksim.data.TradeMessage;

import java.rmi.Remote;
import java.rmi.RemoteException;

// interface for remote market node communication
public interface MarketNodeRemote extends Remote {

    // sends a trade message
    void submitMessage(TradeMessage message) throws RemoteException;

    // returns full system state
    SystemState getState() throws RemoteException;
}

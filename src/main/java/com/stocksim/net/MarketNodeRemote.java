package com.stocksim.net;

import com.stocksim.data.SystemState;
import com.stocksim.data.TradeMessage;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MarketNodeRemote extends Remote {
    void submitMessage(TradeMessage message) throws RemoteException;
    SystemState getState() throws RemoteException;
}

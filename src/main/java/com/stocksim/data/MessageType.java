package com.stocksim.data;

import java.io.Serializable;

// message types sent between agents and market
public enum MessageType implements Serializable {
    ORDER,      // order message
    HEARTBEAT   // heartbeat message
}

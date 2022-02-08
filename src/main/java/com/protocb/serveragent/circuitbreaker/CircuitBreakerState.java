package com.protocb.serveragent.circuitbreaker;

public enum CircuitBreakerState {
    OPEN,
    CLOSED,
    HALF_OPEN,
    SUSPICION,
    NOT_CLOSED
}

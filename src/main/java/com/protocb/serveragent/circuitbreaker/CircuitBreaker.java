package com.protocb.serveragent.circuitbreaker;

import java.util.Map;

public interface CircuitBreaker {
    void registerSuccess();
    void registerFailure();
    boolean isCircuitBreakerOpen();
    void initialize(Map<String, Integer> parameters);
    void reset();
}

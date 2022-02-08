package com.protocb.serveragent.circuitbreaker;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ClosedCircuit implements CircuitBreaker {
    @Override
    public void registerSuccess() {

    }

    @Override
    public void registerFailure() {

    }

    @Override
    public boolean isCircuitBreakerOpen() {
        return false;
    }

    @Override
    public void initialize(Map<String, Integer> parameters) {

    }

    @Override
    public void reset() {

    }
}

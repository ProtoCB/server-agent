package com.protocb.serveragent.circuitbreaker;

import com.protocb.serveragent.circuitbreaker.gedcb.GEDCircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CircuitBreakerFactory {

    @Autowired
    private StaticCircuitBreaker staticCircuitBreaker;

    @Autowired
    private GEDCircuitBreaker gedCircuitBreaker;

    @Autowired
    private ClosedCircuit closedCircuit;

    public CircuitBreaker getCircuitBreaker(String type) {
        if(type.equals("Static")) {
            staticCircuitBreaker.reset();
            return staticCircuitBreaker;
        } else if(type.equals("GEDCB")) {
            gedCircuitBreaker.reset();
            return gedCircuitBreaker;
        } else {
            return closedCircuit;
        }
    }


}

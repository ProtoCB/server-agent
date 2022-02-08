package com.protocb.serveragent.circuitbreaker;

import com.protocb.serveragent.logger.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.protocb.serveragent.circuitbreaker.CircuitBreakerState.*;
import static com.protocb.serveragent.circuitbreaker.WindowSlot.*;

@Component
public class StaticCircuitBreaker implements CircuitBreaker {

    @Autowired
    private Logger logger;

    private CircuitBreakerState circuitBreakerState;

    private List<WindowSlot> window;

    private int failureThreshold;

    private int halfOpenFailureThreshold;

    private int halfOpenSuccessThreshold;

    private long openDuration;

    private long lastOpenAt;

    private int index;

    private String getCBWindow() {
        String w = "|";
        for(WindowSlot windowSlot:window) {
            if(windowSlot == EMPTY) {
                w += "E|";
            } else if(windowSlot == SUCCESS) {
                w += "S|";
            } else if(windowSlot == FAILURE) {
                w += "F|";
            }
        }
        return w;
    }

    private void changeCircuitBreakerState(CircuitBreakerState circuitBreakerState) {
        this.circuitBreakerState = circuitBreakerState;
        logger.log("CBCHANGE", circuitBreakerState.toString());
    }

    private void clearWindow() {
        for(int i = 0; i<window.size(); i++) {
            window.set(i, EMPTY);
        }
    }

    private void openCircuitBreaker() {
        changeCircuitBreakerState(OPEN);
        clearWindow();
        lastOpenAt = Instant.now().toEpochMilli() % 100000;
    }

    private void monitorForStateTransition() {
        int failures = 0;
        int successes = 0;
        for(int i = 0; i<window.size(); i++) {
            if(window.get(i) == FAILURE) failures++;
            if(window.get(i) == SUCCESS) successes++;
        }

        if(circuitBreakerState == CLOSED && failures > failureThreshold) {
            openCircuitBreaker();
        } else if(circuitBreakerState == HALF_OPEN) {
            if(failures > halfOpenFailureThreshold) {
                openCircuitBreaker();
            } else if(successes > halfOpenSuccessThreshold){
                changeCircuitBreakerState(CLOSED);
                clearWindow();
            }
        }
    }

    private void registerResponse(WindowSlot response) {
        window.set(index, response);
        index = (index + 1) % window.size();
        monitorForStateTransition();
    }

    @Override
    public void registerSuccess() {
        registerResponse(SUCCESS);
    }

    @Override
    public void registerFailure() {
        registerResponse(FAILURE);
    }

    @Override
    public boolean isCircuitBreakerOpen() {

        logger.log("CBSTATE", getCBWindow());

        if(circuitBreakerState == OPEN) {
            long timeElapsedSinceOpen = Instant.now().toEpochMilli() % 100000 - lastOpenAt;
            if(timeElapsedSinceOpen >= openDuration) {
                changeCircuitBreakerState(HALF_OPEN);
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void initialize(Map<String, Integer> parameters) {
        failureThreshold = parameters.get("FT");
        halfOpenFailureThreshold = parameters.get("HOFT");
        halfOpenSuccessThreshold = parameters.get("HOST");
        openDuration = parameters.get("OD");
        int windowSize = parameters.get("WS");
        lastOpenAt = 0;
        index = 0;
        window = Arrays.asList(new WindowSlot[windowSize]);
        clearWindow();
        circuitBreakerState = CLOSED;
    }

    @Override
    public void reset() {
        failureThreshold = 0;
        halfOpenSuccessThreshold = 0;
        halfOpenFailureThreshold = 0;
        openDuration = 0;
        lastOpenAt = 0;
        index = 0;
        window = new ArrayList<>();
        circuitBreakerState = CLOSED;
    }
}

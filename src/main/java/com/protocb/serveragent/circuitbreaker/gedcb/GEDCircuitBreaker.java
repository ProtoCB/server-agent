package com.protocb.serveragent.circuitbreaker.gedcb;

import com.protocb.serveragent.circuitbreaker.CircuitBreaker;
import com.protocb.serveragent.circuitbreaker.CircuitBreakerState;
import com.protocb.serveragent.circuitbreaker.WindowSlot;
import com.protocb.serveragent.logger.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.protocb.serveragent.circuitbreaker.WindowSlot.*;
import static com.protocb.serveragent.circuitbreaker.CircuitBreakerState.*;
import static com.protocb.serveragent.circuitbreaker.CircuitBreakerState.CLOSED;

@Component
public class GEDCircuitBreaker implements CircuitBreaker {

    @Autowired
    private Logger logger;

    @Autowired
    private GEDCBClientRegister gedcbClientRegister;

    private CircuitBreakerState circuitBreakerState;

    private List<WindowSlot> window;

    private int softFailureThreshold;

    private int hardFailureThreshold;

    private int suspicionSuccessThreshold;

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

    private void closeCircuitBreaker() {
        changeCircuitBreakerState(CLOSED);
        clearWindow();
        gedcbClientRegister.updateSelfOpinion(CLOSED);
    }

    private void monitorForStateTransition() {
        int failures = 0;
        int successes = 0;
        for(int i = 0; i<window.size(); i++) {
            if(window.get(i) == FAILURE) failures++;
            if(window.get(i) == SUCCESS) successes++;
        }

        if(circuitBreakerState == CLOSED && failures > softFailureThreshold) {

            changeCircuitBreakerState(SUSPICION);
            gedcbClientRegister.updateSelfOpinion(NOT_CLOSED);
            if(gedcbClientRegister.isConsensusOnSuspicion()) openCircuitBreaker();

        } else if(circuitBreakerState == SUSPICION) {

            if(failures > hardFailureThreshold || gedcbClientRegister.isConsensusOnSuspicion()) {
                openCircuitBreaker();
            } else if(successes > suspicionSuccessThreshold) {
                closeCircuitBreaker();
            }

        } else if(circuitBreakerState == HALF_OPEN) {

            if(failures > halfOpenFailureThreshold) {
                openCircuitBreaker();
            } else if(successes > halfOpenSuccessThreshold){
                closeCircuitBreaker();
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
        } else if(circuitBreakerState == SUSPICION && gedcbClientRegister.isConsensusOnSuspicion()) {
            openCircuitBreaker();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void initialize(Map<String, Integer> parameters) {
        softFailureThreshold = parameters.get("SFT");
        hardFailureThreshold = parameters.get("HFT");
        suspicionSuccessThreshold = parameters.get("SST");
        halfOpenFailureThreshold = parameters.get("HOFT");
        halfOpenSuccessThreshold = parameters.get("HOST");
        openDuration = parameters.get("OD");
        int windowSize = parameters.get("WS");
        lastOpenAt = 0;
        index = 0;
        window = Arrays.asList(new WindowSlot[windowSize]);
        clearWindow();
        circuitBreakerState = CLOSED;

        int maxAge = parameters.get("maxAge");
        int gossipPeriod = parameters.get("gossipPeriod");
        int gossipCount = parameters.get("gossipCount");
        boolean pushPullGossip = parameters.get("pushPullGossip") == 1;

        gedcbClientRegister.initialize(maxAge, gossipPeriod, gossipCount, pushPullGossip);

    }

    @Override
    public void reset() {
        softFailureThreshold = 0;
        hardFailureThreshold = 0;
        suspicionSuccessThreshold = 0;
        halfOpenSuccessThreshold = 0;
        halfOpenFailureThreshold = 0;
        openDuration = 0;
        lastOpenAt = 0;
        index = 0;
        window = new ArrayList<>();
        circuitBreakerState = CLOSED;
        gedcbClientRegister.reset();
    }
}

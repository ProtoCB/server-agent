package com.protocb.serveragent.circuitbreaker.gedcb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.protocb.serveragent.circuitbreaker.CircuitBreakerState;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.Map;

import static com.protocb.serveragent.circuitbreaker.CircuitBreakerState.CLOSED;

@Data
@Builder
public class GossipSetState {
    @NonNull
    @JsonProperty
    private Long version;

    @NonNull
    @JsonProperty
    private Map<String, CircuitBreakerState> opinion;

    @NonNull
    @JsonProperty
    private Map<String, Integer> age;

    @Override
    public String toString() {
        String s = "(" + version + ")";
        for(String clientId:opinion.keySet()) {
            s += "[" + clientId + ":(";
            if(this.opinion.get(clientId) == CLOSED) {
                s += "C,";
            } else {
                s += "S,";
            }
            s += this.age.get(clientId) + ")]";
        }
        return s;
    }
}

package com.protocb.serveragent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@ToString
public class ExperimentRecipe {

    @JsonProperty
    @NonNull
    private String experimentSession;

    @JsonProperty
    @NonNull
    private List<String> eventsToLog;

    @JsonProperty
    @NonNull
    private Float tfProbability;

    @JsonProperty
    @NonNull
    private String circuitBreakerType;

    @JsonProperty
    @NonNull
    private Map<String, Integer> circuitBreakerParameters;

    @JsonProperty
    @NonNull
    private Integer failureInferenceTime;

    @JsonProperty
    @NonNull
    private ExperimentSchedule experimentSchedule;

    @JsonProperty
    @NonNull
    private List<NetworkPartitionEvent> networkPartitionSchedule;

    @JsonProperty
    @NonNull
    private List<ActivityChangeEvent> serverAvailabilitySchedule;

}

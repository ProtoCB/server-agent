package com.protocb.serveragent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ServerRequestBody {

    @JsonProperty
    private String ip;

    @JsonProperty
    private int minLatency;

    @JsonProperty
    private int failureInferenceTime;

    @JsonProperty
    private long timestamp;

}

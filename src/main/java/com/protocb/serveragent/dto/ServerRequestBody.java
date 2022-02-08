package com.protocb.serveragent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static com.protocb.serveragent.config.EnvironmentVariables.AGENT_URL;

@Component
@Getter
@Setter
public class ServerRequestBody {

    @JsonProperty
    private String ip;

    @JsonProperty
    private int minLatency;

    @JsonProperty
    private long timestamp;

    @PostConstruct
    public void postConstruct() {
        ip = AGENT_URL;
        minLatency = 0;
        timestamp = 0;
    }
}

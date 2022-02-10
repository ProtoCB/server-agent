package com.protocb.serveragent.heartbeat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static com.protocb.serveragent.config.EnvironmentVariables.*;

@Component
@Getter
@Setter
public class HeartbeatPayload {

    @JsonProperty
    private String ip;

    @JsonProperty
    private String agentSecret;

    @JsonProperty
    private String experimentSession;

    @JsonProperty
    private String experimentStatus;

    @PostConstruct
    public void postConstruct() {
        ip = AGENT_HOST + AGENT_PORT;
        agentSecret = AGENT_SECRET;
        experimentSession = "Uninitialized";
        experimentStatus = "Uninitialized";
    }

}

package com.protocb.serveragent.heartbeat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.protocb.serveragent.config.EnvironmentVariables;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static com.protocb.serveragent.config.AgentConstants.*;

@Component
@Getter
@Setter
public class HeartbeatPayload {

    @Autowired
    private EnvironmentVariables environmentVariables;

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
        ip = environmentVariables.getAgentIp();
        agentSecret = environmentVariables.getAgentSecret();
        experimentSession = "Uninitialized";
        experimentStatus = "Uninitialized";
    }

}

package com.protocb.serveragent.proxy;

import com.protocb.serveragent.AgentState;
import com.protocb.serveragent.gedcb.pojo.SetRevisionMessage;
import com.protocb.serveragent.interaction.Observer;
import com.protocb.serveragent.logger.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class Proxy implements Observer {

    @Autowired
    private AgentState agentState;

    @Autowired
    private Logger logger;

    private boolean networkPartitioned;

    private List<String> allowList;

    private boolean serverAvailable;

    private float tfProbability;

    private int failureInferenceTime;

    @PostConstruct
    private void initialize() {
        allowList = new ArrayList<>();
        serverAvailable = false;
        networkPartitioned = false;
        tfProbability = 0;
        failureInferenceTime = 0;
        agentState.registerObserver(this);
    }

    @PreDestroy
    private void cleanUp() {
        agentState.removeObserver(this);
    }

    @Override
    public void update() {
        this.serverAvailable = agentState.isServerAvailable();
        this.networkPartitioned = agentState.isNetworkPartitioned();
        this.allowList = agentState.getPartitionMembers();
        this.tfProbability = agentState.getTfProbability();
        this.failureInferenceTime = agentState.getFailureInferenceTime();
    }

    public void sendGsrMessage(String clientUrl, SetRevisionMessage setRevisionMessage) {
        try {

            if(!serverAvailable) return;

            boolean shouldFailTransiently = Math.random() < tfProbability;
            boolean clientReachable = !networkPartitioned || allowList.contains(clientUrl);

            if(!shouldFailTransiently && clientReachable) {
                System.out.println("Sending GSR - " + clientUrl);
                WebClient.create(clientUrl)
                        .post()
                        .uri("/api/v1/gedcb/gsr")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(setRevisionMessage))
                        .retrieve()
                        .bodyToMono(String.class)
                        .timeout(Duration.ofMillis(failureInferenceTime))
                        .block();

            } else {
                Thread.sleep(failureInferenceTime);
            }

        } catch(Exception e) {
            logger.logErrorEvent("Failed to send GSR message");
        }
    }

}

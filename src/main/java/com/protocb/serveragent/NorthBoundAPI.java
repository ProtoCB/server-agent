package com.protocb.serveragent;

import com.protocb.serveragent.config.EnvironmentVariables;
import com.protocb.serveragent.dto.ExperimentRecipe;
import com.protocb.serveragent.scheduler.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.protocb.serveragent.config.AgentConstants.NORTHBOUND_ENDPOINT;

/*
* Rest Controller exposing NorthBoundAPI to ProtoCB Controller
* */
@RestController
@RequestMapping(NORTHBOUND_ENDPOINT)
public class NorthBoundAPI {

    @Autowired
    private EnvironmentVariables environmentVariables;

    @Autowired
    private AgentState agentState;

    @Autowired
    private NetworkPartitionScheduler networkPartitionScheduler;

    @Autowired
    private ExperimentScheduler experimentScheduler;

    @Autowired
    private ServerAvailabilityScheduler serverAvailabilityScheduler;

    @PostMapping("/schedule-experiment")
    public ResponseEntity scheduleExperiment(@RequestHeader("agent-secret") String secret, @RequestBody ExperimentRecipe experimentRecipe) {
        try {

            System.out.println("Scheduling experiment - " + experimentRecipe.getExperimentSession());

            if(!secret.equals(environmentVariables.getAgentSecret())) {
                System.out.println("Agent secret does not match");
                return ResponseEntity.status(401).body(null);
            }

            if(!agentState.getExperimentSession().equals("Uninitialized")) {
                System.out.println("Experiment already scheduled - not scheduling");
                return ResponseEntity.status(401).body(null);
            }

            agentState.setExperimentSession(experimentRecipe.getExperimentSession());
            agentState.setEventsToLog(experimentRecipe.getEventsToLog());
            agentState.setTfProbability(experimentRecipe.getTfProbability());
            agentState.setCircuitBreakerType(experimentRecipe.getCircuitBreakerType());
            agentState.setCircuitBreakerParameters(experimentRecipe.getCircuitBreakerParameters());
            agentState.setFailureInferenceTime(experimentRecipe.getFailureInferenceTime());
            agentState.notifyObservers();

            networkPartitionScheduler.scheduleExperiment(experimentRecipe.getNetworkPartitionSchedule());
            serverAvailabilityScheduler.scheduleExperiment(experimentRecipe.getServerAvailabilitySchedule());
            experimentScheduler.scheduleExperiment(experimentRecipe.getExperimentSchedule());

            System.out.println("Experiment Scheduled - " + experimentRecipe.getExperimentSession());

            return ResponseEntity.ok().body(null);

        } catch(Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PatchMapping("/reset-agent")
    public ResponseEntity resetAgent(@RequestHeader("agent-secret") String secret) {
        try {

            System.out.println("Received reset signal");

            if(!secret.equals(environmentVariables.getAgentSecret())) {
                return ResponseEntity.status(401).body(null);
            }

            experimentScheduler.cancelExperiment();
            networkPartitionScheduler.cancelExperiment();
            serverAvailabilityScheduler.cancelExperiment();

            agentState.resetAgent();

            return ResponseEntity.ok().body(null);

        } catch(Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
        }
    }

}

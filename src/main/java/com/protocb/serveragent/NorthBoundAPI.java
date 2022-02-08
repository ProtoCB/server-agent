package com.protocb.serveragent;

import com.protocb.serveragent.dto.ExperimentRecipe;
import com.protocb.serveragent.scheduler.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.protocb.serveragent.config.EnvironmentVariables.AGENT_SECRET;
import static com.protocb.serveragent.config.EnvironmentVariables.NORTHBOUND_ENDPOINT;

@RestController
@RequestMapping(NORTHBOUND_ENDPOINT)
public class NorthBoundAPI {

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

            if(!secret.equals(AGENT_SECRET)) {
                return ResponseEntity.status(401).body(null);
            }

            if(!agentState.getExperimentSession().equals("Uninitialized")) {
                return ResponseEntity.status(401).body(null);
            }

            System.out.println(experimentRecipe.toString());

            agentState.setExperimentSession(experimentRecipe.getExperimentSession());
            agentState.setEventsToLog(experimentRecipe.getEventsToLog());
            agentState.setTfProbability(experimentRecipe.getTfProbability());
            agentState.setCircuitBreakerType(experimentRecipe.getCircuitBreakerType());
            agentState.setCircuitBreakerParameters(experimentRecipe.getCircuitBreakerParameters());
            agentState.setFailureInferenceTime(experimentRecipe.getFailureInferenceTime());

            networkPartitionScheduler.scheduleExperiment(experimentRecipe.getNetworkPartitionSchedule());
            serverAvailabilityScheduler.scheduleExperiment(experimentRecipe.getServerAvailabilitySchedule());
            experimentScheduler.scheduleExperiment(experimentRecipe.getExperimentSchedule());

            return ResponseEntity.ok().body(null);

        } catch(Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PatchMapping("/reset-agent")
    public ResponseEntity resetAgent(@RequestHeader("agent-secret") String secret) {
        try {

            if(!secret.equals(AGENT_SECRET)) {
                return ResponseEntity.status(401).body(null);
            }

            experimentScheduler.cancelExperiment();
            networkPartitionScheduler.cancelExperiment();
            serverAvailabilityScheduler.cancelExperiment();

            agentState.resetAgent();

            return ResponseEntity.ok().body(null);

        } catch(Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

}

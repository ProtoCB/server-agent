package com.protocb.serveragent.scheduler;

import com.protocb.serveragent.AgentState;
import com.protocb.serveragent.dto.ExperimentSchedule;
import com.protocb.serveragent.logger.Logger;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Component
public class ExperimentScheduler {

    @Autowired
    private Logger logger;

    @Autowired
    private AgentState agentState;

    @Autowired
    private ScheduledExecutorService scheduledExecutorService;

    private List<ActivityState> activityStates;

    private List<ScheduledFuture> schedule;

    private int nextEventIndex;

    @NoArgsConstructor
    private class ExperimentSchedulingTask implements Runnable {
        @Override
        public void run() {
            ActivityState activityState = getNextState();
            if(activityState == ActivityState.ACTIVE) {
                logger.logSchedulingEvent("Experiment started");
                agentState.setExperimentUnderProgress(true);
            } else {
                agentState.setServerAvailable(false);
                agentState.setExperimentUnderProgress(false);
                logger.logSchedulingEvent("Experiment ended");
            }
        }
    }

    @PostConstruct
    public void initialize() {
        activityStates = new ArrayList<>();
        schedule = new ArrayList<>();
    }

    public void cancelExperiment() {

        for(ScheduledFuture scheduledEvent : schedule) {
            if(scheduledEvent != null) {
                scheduledEvent.cancel(false);
            }
        }

        schedule.clear();
        activityStates.clear();
        logger.setExperimentSession("Uninitialized");
    }

    public void scheduleExperiment(ExperimentSchedule schedule) {

        logger.setExperimentSession(agentState.getExperimentSession());
        logger.setEventsToLog(agentState.getEventsToLog());

        long delay = schedule.getStart() - Instant.now().getEpochSecond();

        if(delay <= 0) {
            logger.logErrorEvent("Experiment's start cannot be in past");
        } else {
            activityStates.add(ActivityState.ACTIVE);
            scheduledExecutorService.schedule(new ExperimentSchedulingTask(), delay, TimeUnit.SECONDS);
        }

        delay = schedule.getEnd() - Instant.now().getEpochSecond();

        if(delay <= 0) {
            logger.logErrorEvent("Experiment's end cannot be in past");
        } else {
            activityStates.add(ActivityState.INACTIVE);
            scheduledExecutorService.schedule(new ExperimentSchedulingTask(), delay, TimeUnit.SECONDS);
        }

        scheduledExecutorService.schedule(() -> {
                logger.shipExperimentSessionLog();
                agentState.setExperimentStatus("Logs Shipped");
        }, delay + 2, TimeUnit.SECONDS);

        nextEventIndex = 0;
    }

    private ActivityState getNextState() {

        if(nextEventIndex < 0) {
            logger.logErrorEvent("ExperimentScheduler - Trying to work on an empty schedule");
            return ActivityState.INACTIVE;
        }

        return activityStates.get(nextEventIndex++);

    }

}


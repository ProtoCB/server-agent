package com.protocb.serveragent.scheduler;

import com.protocb.serveragent.AgentState;
import com.protocb.serveragent.dto.NetworkPartitionEvent;
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
public class NetworkPartitionScheduler {

    @Autowired
    private Logger logger;

    @Autowired
    private AgentState agentState;

    @Autowired
    private ScheduledExecutorService scheduledExecutorService;

    private List<NetworkPartitionEvent> partitions;

    private List<ScheduledFuture> schedule;

    private int nextEventIndex;

    @NoArgsConstructor
    private class NetworkPartitionSchedulingTask implements Runnable {
        @Override
        public void run() {
            NetworkPartitionEvent networkPartitionEvent = getNextPartition();
            agentState.setNetworkPartition(networkPartitionEvent.isNetworkPartitioned(), networkPartitionEvent.getPartition());
            logger.logSchedulingEvent("Network Partition - " + networkPartitionEvent.isNetworkPartitioned());
        }
    }

    @PostConstruct
    public void initialize() {
        partitions = new ArrayList<>();
        schedule = new ArrayList<>();
    }

    public void cancelExperiment() {
        for(ScheduledFuture scheduledEvent : schedule) {
            if(scheduledEvent != null) {
                scheduledEvent.cancel(false);
            }
        }
        schedule.clear();
        partitions.clear();
    }

    public void scheduleExperiment(List<NetworkPartitionEvent> events) {
        for(NetworkPartitionEvent event : events) {
            long delay = event.getTime() - Instant.now().getEpochSecond();

            if(delay <= 0) {
                continue;
            }

            partitions.add(event);

            scheduledExecutorService.schedule( new NetworkPartitionSchedulingTask(), delay, TimeUnit.SECONDS);
        }
        nextEventIndex = partitions.size() != 0 ? 0 : -1;
    }

    private NetworkPartitionEvent getNextPartition() {

        if(nextEventIndex < 0) {
            logger.logErrorEvent("NetworkPartitionScheduler - Trying to work on an empty schedule");
            return NetworkPartitionEvent
                    .builder()
                    .networkPartitioned(false)
                    .partition(new ArrayList<String>())
                    .build();
        }

        return partitions.get(nextEventIndex++);

    }


}


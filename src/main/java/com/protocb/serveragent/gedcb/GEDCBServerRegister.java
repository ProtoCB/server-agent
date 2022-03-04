package com.protocb.serveragent.gedcb;

import com.protocb.serveragent.AgentState;
import com.protocb.serveragent.gedcb.pojo.SetRevisionMessage;
import com.protocb.serveragent.gedcb.pojo.ClientEntry;
import com.protocb.serveragent.interaction.Observer;
import com.protocb.serveragent.logger.Logger;
import com.protocb.serveragent.proxy.Proxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class GEDCBServerRegister implements Observer {

    @Autowired
    private AgentState agentState;

    @Autowired
    private Logger logger;

    @Autowired
    private Proxy proxy;

    @Autowired
    private ScheduledExecutorService scheduledExecutorService;

    private boolean enabled;

    private Long version;

    private int minSetSize;

    private int setRevisionPeriod;

    private int gsrMessageCount;

    private ScheduledFuture gsrTask;

    private Map<Integer, List<ClientEntry>> gossipSets;

    private Map<Integer, Integer> setSizes;

    @PostConstruct
    public void postConstruct(){
        enabled = false;
        version = 1l;
        minSetSize = 10;
        gossipSets = new HashMap<>();
        setSizes = new HashMap<>();
        agentState.registerObserver(this);
    }

    @PreDestroy
    public void preDestroy() {
        agentState.removeObserver(this);
    }

    private void removeStaleEntries() {
        System.out.println("before rse");
        System.out.println(gossipSets.toString());
        System.out.println(setSizes.toString());

        long currentTime = Instant.now().toEpochMilli() % 1000000;
        for(Integer setId : this.gossipSets.keySet()) {
            this.gossipSets.get(setId).removeIf(entry -> currentTime - entry.getTimestamp() >= setRevisionPeriod);
            this.setSizes.put(setId, this.gossipSets.get(setId).size());
        }

        System.out.println("after rse");
        System.out.println(gossipSets.toString());
        System.out.println(setSizes.toString());

    }

    private synchronized void balanceGossipSets() {

        removeStaleEntries();

        int totalEntries = 0;
        for(Integer setId:this.gossipSets.keySet()) {
            totalEntries += this.setSizes.get(setId);
        }

        System.out.println("Total entries = " + totalEntries);

        if(totalEntries == 0) return;

        int requiredSetCount = totalEntries % minSetSize == 0 ? totalEntries / minSetSize : (totalEntries / minSetSize) + 1;

        List<ClientEntry> entriesToReallocate = new ArrayList<>();

        if(this.gossipSets.keySet().size() > requiredSetCount) {
            int unnecesarySetCount = this.gossipSets.keySet().size() - requiredSetCount;
            while(unnecesarySetCount > 0) {
                Integer setId = Collections.min(setSizes.entrySet(), Map.Entry.comparingByValue()).getKey();
                entriesToReallocate.addAll(this.gossipSets.get(setId));
                this.gossipSets.remove(setId);
                this.setSizes.remove(setId);
                unnecesarySetCount--;
            }
        } else if(this.gossipSets.keySet().size() < requiredSetCount) {
            int setsNeeded = requiredSetCount - this.gossipSets.keySet().size();
            while(setsNeeded > 0) {
                int newId = (int)(Math.random() * 1000);
                if(this.gossipSets.keySet().contains(newId)) continue;
                this.gossipSets.put(newId, new ArrayList<>());
                this.setSizes.put(newId, 0);
                setsNeeded--;
            }
        }

        for(Integer setId:this.gossipSets.keySet()) {
            List<ClientEntry> gossipSet = this.gossipSets.get(setId);
            if(gossipSet.size() > minSetSize) {
                entriesToReallocate.addAll(gossipSet.subList(minSetSize, gossipSet.size()));
                gossipSet.subList(minSetSize, gossipSet.size()).clear();
                this.setSizes.put(setId, gossipSet.size());
            }
        }

        for(ClientEntry entry:entriesToReallocate) {
            Integer setId = Collections.min(setSizes.entrySet(), Map.Entry.comparingByValue()).getKey();
            this.gossipSets.get(setId).add(entry);
            this.setSizes.put(setId, this.gossipSets.get(setId).size());
        }

    }

    private String getGedcbServerState() {
        String state = "Version=" + version + "{";
        for(Integer setId:gossipSets.keySet()) {
            state += "[ ";
            for(ClientEntry entry:gossipSets.get(setId)) {
                state += entry.getClientId() + " ";
            }
            state += "]";
        }
        state += "}";
        return state;
    }

    private void reviseGossipSet() {
        try {

            balanceGossipSets();
            version++;

            logger.log("GSR", getGedcbServerState());

            Set<Integer> setIds = this.gossipSets.keySet();
            for(Integer setId:setIds) {
                List<ClientEntry> entries = this.gossipSets.get(setId);
                List<String> gossipSet = entries.stream().map(entry -> entry.getClientId()).collect(Collectors.toList());
                SetRevisionMessage setRevisionMessage = SetRevisionMessage.builder().version(version).clientIds(gossipSet).build();

                List<String> clientsThatReceiveGsrMessage = new ArrayList<>();
                while(clientsThatReceiveGsrMessage.size() < gsrMessageCount && clientsThatReceiveGsrMessage.size() < gossipSet.size()) {
                    int randomIndex = (int)(Math.random() * 1000) % gossipSet.size();
                    String clientId = gossipSet.get(randomIndex);
                    if(!clientsThatReceiveGsrMessage.contains(clientId)) {
                        proxy.sendGsrMessage(clientId, setRevisionMessage);
                        clientsThatReceiveGsrMessage.add(clientId);
                    }
                }

            }


        } catch (Exception e) {
            e.printStackTrace();
            logger.logErrorEvent("GSR Failed! - " + e.getMessage());
        }
    }

    public synchronized void registerInteraction(String clientId) {

        if(!enabled) return;

        ClientEntry clientEntry = null;
        for(Integer setId : gossipSets.keySet()) {

            clientEntry = gossipSets.get(setId)
                    .stream()
                    .filter(entry -> entry.getClientId().equals(clientId))
                    .findFirst()
                    .orElse(null);

            if(clientEntry != null) break;

        }

        if(clientEntry != null) {
            clientEntry.setTimestamp(Instant.now().toEpochMilli() % 1000000);
        } else {
            Integer setId = Collections.min(setSizes.entrySet(), Map.Entry.comparingByValue()).getKey();
            gossipSets
                    .get(setId)
                    .add(ClientEntry
                            .builder()
                            .clientId(clientId)
                            .timestamp(Instant.now().toEpochMilli() % 1000000)
                            .build()
                    );
            setSizes.put(setId, gossipSets.get(setId).size());
        }

        System.out.println(setSizes.toString());
        System.out.println(gossipSets.toString());

    }

    private void initialize(Map<String, Integer> parameters) {
        System.out.println("Parameters");
        System.out.println(parameters);
        this.gsrMessageCount = parameters.get("gsrMessageCount");
        this.version = 1l;
        this.minSetSize = parameters.get("minSetSize");
        this.setRevisionPeriod = parameters.get("setRevisionPeriod");

        int randomId = (int)(Math.random() * 1000);
        this.gossipSets.clear();
        this.setSizes.clear();
        this.gossipSets.put(randomId, new ArrayList<>());
        this.setSizes.put(randomId, 0);

        this.enabled = true;
        if(gsrTask != null && !gsrTask.isDone()) {
            gsrTask.cancel(true);
        }

        System.out.println(gossipSets.toString());
        System.out.println(setSizes.toString());

        gsrTask = scheduledExecutorService.scheduleWithFixedDelay(() -> this.reviseGossipSet(), 0, this.setRevisionPeriod, TimeUnit.MILLISECONDS);
    }

    private void reset() {
        this.enabled = false;
        if(gsrTask != null && !gsrTask.isDone()) {
            gsrTask.cancel(true);
        }
        int randomId = (int)(Math.random() * 1000);
        this.gossipSets.clear();
        this.setSizes.clear();
        this.gossipSets.put(randomId, new ArrayList<>());
        this.setSizes.put(randomId, 0);
        this.version++;
    }

    @Override
    public synchronized void update() {
        if(!enabled && (agentState.getCircuitBreakerType().equals("GEDCB") && agentState.isServerAvailable())) {
            System.out.println("Enable GEDCB Register");
            initialize(agentState.getCircuitBreakerParameters());
        } else if(enabled && !(agentState.getCircuitBreakerType().equals("GEDCB") && agentState.isServerAvailable())) {
            reset();
        }
    }
}

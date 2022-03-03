package com.protocb.serveragent.logger;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Bucket;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;
import com.protocb.serveragent.config.EnvironmentVariables;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;

import static com.protocb.serveragent.config.AgentConstants.*;

@Component
public class Logger {

    @Autowired
    private EnvironmentVariables environmentVariables;

    private String experimentSession;

    private List<String> eventsToLog;

    private File sessionLog;

    private Writer bufferedWriter;

    private Bucket bucket;

    @PostConstruct
    public void postConstruct(){
        try {

            FileInputStream serviceAccount = new FileInputStream(environmentVariables.getServiceAccountFilePath());

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setStorageBucket(environmentVariables.getStorageBucket())
                    .build();

            FirebaseApp firebaseApp = FirebaseApp.initializeApp(options);

            bucket = StorageClient.getInstance(firebaseApp).bucket();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void createExperimentSessionLog() {
        try {
            sessionLog = new File(environmentVariables.getLogFilePath());
            sessionLog.createNewFile();
            bufferedWriter = new BufferedWriter(new FileWriter(sessionLog));
            bufferedWriter.append("EventId" + "," + "Timestamp" + "," + "Message" + "," + "Thread" + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shipExperimentSessionLog() {
        try {
            bufferedWriter.flush();
            String agentIp = environmentVariables.getAgentIp();
            bucket.create(experimentSession + "/" + "server-" + agentIp + ".csv", Files.readAllBytes(Paths.get(sessionLog.getAbsolutePath())),"csv");
            System.out.println("Logs shipped for " + experimentSession);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void log(String eventId, String message) {
        try {
            if(eventsToLog.contains(eventId)) {
                bufferedWriter.append(eventId + "," + Instant.now().toEpochMilli() + "," + message + "," + Thread.currentThread().getName() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logSchedulingEvent(String message) {
        log(SCHEDULING_EVENT_ID, message);
    }

    public void logErrorEvent(String message) {
        log(ERROR_EVENT_ID, message);
    }

    public void setExperimentSession(String experimentSession) {
        this.experimentSession = experimentSession;
        createExperimentSessionLog();
    }

    public void setEventsToLog(List<String> eventsToLog) {
        this.eventsToLog = eventsToLog;
    }
}

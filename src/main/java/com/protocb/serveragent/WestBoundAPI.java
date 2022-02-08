package com.protocb.serveragent;

import com.protocb.serveragent.dto.ServerRequestBody;
import com.protocb.serveragent.logger.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

import static com.protocb.serveragent.config.EnvironmentVariables.WESTBOUND_ENDPOINT;

@RestController
@RequestMapping(WESTBOUND_ENDPOINT)
public class WestBoundAPI {

    @Autowired
    private Logger logger;

    @PostMapping("/")
    public ResponseEntity handleClientAgentRequest(@RequestBody ServerRequestBody serverRequestBody) {
        try {

            long currentTime = Instant.now().toEpochMilli() % 1000000;
            long timeElapsedSinceRequestOriginated = serverRequestBody.getTimestamp() - currentTime;

            long minExpectedDelay = serverRequestBody.getMinLatency() - (2 * timeElapsedSinceRequestOriginated);

            if(minExpectedDelay > 0) {
                Thread.sleep(minExpectedDelay);
            }

            return ResponseEntity.ok().body(null);

        } catch (Exception e) {
            e.printStackTrace();
            logger.logErrorEvent("Error while handling client request");
            return ResponseEntity.internalServerError().body(null);
        }
    }

}
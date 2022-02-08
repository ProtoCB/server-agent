package com.protocb.serveragent.circuitbreaker.gedcb;

import com.protocb.serveragent.AgentState;
import com.protocb.serveragent.circuitbreaker.gedcb.dto.GossipSetState;
import com.protocb.serveragent.circuitbreaker.gedcb.dto.SetRevisionMessage;
import com.protocb.serveragent.logger.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

import static com.protocb.serveragent.config.EnvironmentVariables.GEDCB_ENDPOINT;

@RestController
@RequestMapping(GEDCB_ENDPOINT)
public class GossipReceiver {

    @Autowired
    private AgentState agentState;

    @Autowired
    private GEDCBClientRegister gedcbClientRegister;

    @Autowired
    private Logger logger;

    @PostMapping("/gossip")
    public ResponseEntity receiveGossipMessage(@RequestBody GossipSetState gossipSetState) {
        try {

            if(!agentState.isAlive() || !agentState.getCircuitBreakerType().equals("GEDCB")) {
                throw new Exception("Cannot accept gossip messages");
            }

            GossipSetState response = gedcbClientRegister.consumeIncomingInformation(gossipSetState);
            return ResponseEntity.ok().body(response);

        } catch(Exception e) {
            System.out.println(e.getMessage());
            logger.logErrorEvent("Recevied gossip message when not accepting");
            GossipSetState response = GossipSetState.builder()
                    .opinion(new HashMap<>())
                    .age(new HashMap<>())
                    .version(-1l)
                    .build();
            return ResponseEntity.ok().body(response);
        }
    }

    @PostMapping("/gsr")
    public ResponseEntity receiveGSRMessage(@RequestBody SetRevisionMessage setRevisionMessage) {
        try {

            if(!agentState.isAlive() || !agentState.getCircuitBreakerType().equals("GEDCB")) {
                throw new Exception("Cannot accept GSR messages");
            }

            gedcbClientRegister.processSetRevisionMessage(setRevisionMessage);

            return ResponseEntity.ok().body(null);

        } catch(Exception e) {
            System.out.println(e.getMessage());
            logger.logErrorEvent("Recevied GSR message when not accepting");
            return ResponseEntity.ok().body(null);
        }
    }

}



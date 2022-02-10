package com.protocb.serveragent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import static com.protocb.serveragent.config.EnvironmentVariables.*;

@SpringBootApplication
public class ServerAgentApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServerAgentApplication.class, args);
	}

    @EventListener(ApplicationReadyEvent.class)
    public void doSomethingAfterStartup() {
        System.out.println("hello world, I have just started up");
        System.out.println(AGENT_HOST);
        System.out.println(AGENT_PORT);
        System.out.println(STORAGE_BUCKET);
        System.out.println(CONTROLLER_URL);
        System.out.println(AGENT_SECRET);
    }

}

package com.protocb.serveragent;

import com.protocb.serveragent.config.EnvironmentVariables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;


@SpringBootApplication
public class ServerAgentApplication {

    @Autowired
    private EnvironmentVariables environmentVariables;

	public static void main(String[] args) {
		SpringApplication.run(ServerAgentApplication.class, args);
	}

    @EventListener(ApplicationReadyEvent.class)
    public void doSomethingAfterStartup() {
        System.out.println("Agent Host IP: " + environmentVariables.getAgentHost());
        System.out.println("Agent Port: " + environmentVariables.getAgentPort());
        System.out.println("Storage Bucket: " + environmentVariables.getStorageBucket());
        System.out.println("Controller URL: " + environmentVariables.getControllerUrl());
    }

}

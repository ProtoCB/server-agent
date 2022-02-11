package com.protocb.serveragent;

import com.protocb.serveragent.config.EnvironmentVariables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;


@SpringBootApplication
public class ServerAgentApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServerAgentApplication.class, args);
	}

}

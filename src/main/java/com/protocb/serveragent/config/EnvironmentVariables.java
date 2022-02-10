package com.protocb.serveragent.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class EnvironmentVariables {

    @Value("${agent.host}")
    private String agentHost;

    @Value("${server.port}")
    private int agentPort;

    @Value("${controller.url}")
    private String controllerUrl;

    @Value("${storage.bucket}")
    private String storageBucket;

    @Value("${agent.secret}")
    private String agentSecret;

}

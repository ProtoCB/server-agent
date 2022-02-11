package com.protocb.serveragent.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
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

    @Value("${protocb.home}")
    private String protocbDirectory;

    public String getControllerUrl() {
        return controllerUrl;
    }

    public String getStorageBucket() {
        return storageBucket;
    }

    public String getAgentSecret() {
        return agentSecret;
    }

    public String getAgentIp() {
        return this.agentHost + ":" + this.agentPort;
    }

    public String getLogFilePath() {
        return this.protocbDirectory + "/logs/server-" + this.getAgentIp() + ".csv";
    }

    public String getLogDirectory() {
        return this.protocbDirectory + "/logs";
    }

    public String getServiceAccountFilePath() {
        return this.protocbDirectory + "/serviceAccount.json";
    }

}

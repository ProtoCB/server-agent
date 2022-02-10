package com.protocb.serveragent.config;

import org.springframework.beans.factory.annotation.Value;

public class EnvironmentVariables {

    @Value("${agent.host}")
    public static String AGENT_HOST;

    @Value("${server.port}")
    public static int AGENT_PORT;

    @Value("${controller.url}")
    public static String CONTROLLER_URL;

    @Value("${storage.bucket}")
    public static String STORAGE_BUCKET;

    @Value("${agent.secret}")
    public static String AGENT_SECRET;

    public static final int HEARTBEAT_DELAY = 1500;
    public static final int HEARTBEAT_TIMEOUT = 750;
    public static final String NORTHBOUND_ENDPOINT = "/api/v1/northbound";
    public static final String WESTBOUND_ENDPOINT = "/api/v1/westbound";
    public static final String SCHEDULING_EVENT_ID = "SCH";
    public static final String ERROR_EVENT_ID = "ERR";
    public static final int SCHEDULER_POOL_SIZE = 6;
    public static final String SERVICE_ACCOUNT_FILE_PATH = "/home/aashay/protocb/serviceAccount.json";
    public static final String LOG_DIRECTORY = "/home/aashay/protocb/logs";
}

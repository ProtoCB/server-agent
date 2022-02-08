package com.protocb.serveragent.config;

public class EnvironmentVariables {
    public static final int BUFFER_SIZE = 20;
    public static final String AGENT_URL = "192.168.7.5";
    public static final String CONTROLLER_URL = "localhost:8000";
    public static final int SCHEDULER_POOL_SIZE = 6;
    public static final String LOG_DIRECTORY = "/home/aashay/Downloads/logs";
    public static final String SCHEDULING_EVENT_ID = "SCH";
    public static final String ERROR_EVENT_ID = "ERR";
    public static final String SERVICE_ACCOUNT_FILE_PATH = "/home/aashay/projects/currentProjects/gedcb/protoCB/firebase-storage/serviceAccount.json";
    public static final String STORAGE_BUCKET = "storage-test-87931.appspot.com";
    public static final String AGENT_SECRET = "agent-secret";
    public static final int HEARTBEAT_DELAY = 3000;
    public static final int HEARTBEAT_TIMEOUT = 1500;
    public static final String NORTHBOUND_ENDPOINT = "/api/v1/northbound";
    public static final String WESTBOUND_ENDPOINT = "/api/v1/westbound";
    public static final String GEDCB_ENDPOINT = "/api/v1/gedcb";
}

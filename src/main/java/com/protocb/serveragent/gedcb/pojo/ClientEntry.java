package com.protocb.serveragent.gedcb.pojo;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ClientEntry {

    private String clientId;

    private long timestamp;

}

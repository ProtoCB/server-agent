package com.protocb.serveragent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServerResponseBody {

    @JsonProperty
    private Boolean serverAvailable;

}

package com.protocb.serveragent.gedcb.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SetRevisionMessage {

    @JsonProperty
    private Long version;

    @JsonProperty
    private List<String> clientIds;

}

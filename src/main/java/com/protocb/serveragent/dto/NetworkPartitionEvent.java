package com.protocb.serveragent.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
@Builder
public class NetworkPartitionEvent {

    @NonNull
    private boolean networkPartitioned;

    @NonNull
    private List<String> partition;

    @NonNull
    private Long time;

}

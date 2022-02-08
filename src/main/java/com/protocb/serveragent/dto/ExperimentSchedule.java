package com.protocb.serveragent.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class ExperimentSchedule {

    @NonNull
    private Long start;

    @NonNull
    private Long end;

}

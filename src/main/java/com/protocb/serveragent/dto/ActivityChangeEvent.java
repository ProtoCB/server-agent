package com.protocb.serveragent.dto;

import com.protocb.serveragent.scheduler.ActivityState;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class ActivityChangeEvent {

    @NonNull
    private Long time;

    @NonNull
    private ActivityState state;

}

package com.journeyplus.iam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "Request body for setting approval delegation")
public class DelegateRequest {

    @Schema(description = "ID of the user to delegate approvals to (or null to clear)", example = "3")
    private Long delegateApproverId;

    @Schema(description = "Start time of delegation (mandatory if delegateApproverId is provided)", example = "2026-07-01T08:00:00")
    private LocalDateTime delegationStart;

    @Schema(description = "End time of delegation (mandatory if delegateApproverId is provided)", example = "2026-07-15T18:00:00")
    private LocalDateTime delegationEnd;
}

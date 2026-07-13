package com.journeyplus.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@lombok.Getter
@lombok.Setter
@Schema(description = "Response DTO for top traveller spends")
public class TopTravellerResponse {
    @Schema(description = "ID of the traveller", example = "10")
    private Long employeeId;

    @Schema(description = "Name of the traveller", example = "John Doe")
    private String name;

    @Schema(description = "Email of the traveller", example = "john@example.com")
    private String email;

    @Schema(description = "Department ID of the traveller", example = "DEPT-SALES")
    private String departmentId;

    @Schema(description = "Total travel spend in USD", example = "5430.50")
    private BigDecimal totalSpend;

    public TopTravellerResponse() {}

    public TopTravellerResponse(Long employeeId, String name, String email, String departmentId, BigDecimal totalSpend) {
        this.employeeId = employeeId;
        this.name = name;
        this.email = email;
        this.departmentId = departmentId;
        this.totalSpend = totalSpend;
    }
}

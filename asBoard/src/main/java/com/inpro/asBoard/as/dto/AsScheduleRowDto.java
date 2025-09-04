package com.inpro.asBoard.as.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.inpro.asBoard.as.common.CustomBooleanDeserializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
public class AsScheduleRowDto {
    private Long scheduleId;          // 일정 PK
    private Long asId;                // AS ID (FK)

    private String farmCode;
    private String farmName;
    private String regionName;
    private String projectName;
    private String asType;

    private LocalDateTime reqDate;    // 접수일 (헤더)
    private LocalDateTime planDate;   // 예정일 (스케줄)
    private LocalDateTime completeDate; // 완료일 (스케줄)

    @JsonDeserialize(using = CustomBooleanDeserializer.class)
    private Boolean useFlag;

    // 출력용
    public String getCompletionLabel() {
        if (completeDate == null) return "미완료";
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        return !completeDate.toLocalDate().isAfter(today) ? "완료" : "미완료";
    }
}

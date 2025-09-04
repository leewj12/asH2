package com.inpro.asBoard.as.dto;

import com.inpro.asBoard.as.dto.AsScheduleDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DashUpcomingDto {
    private Long asId;
    private String farmCode;
    private String farmName;
    private String regionName;
    private String asType;
    private String projectName;
    private LocalDateTime reqDate;
    private LocalDateTime planDate;

    // 상태 계산용
    private Boolean statusFlag; // from AS_HEADER
    private List<AsScheduleDto> scheduleList; // 상태 계산용
    private String statusLabel; // 보류 / 완료 / 진행 / 경고 / 대기
}
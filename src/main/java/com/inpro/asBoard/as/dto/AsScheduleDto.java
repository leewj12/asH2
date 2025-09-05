package com.inpro.asBoard.as.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.inpro.asBoard.as.common.CustomBooleanDeserializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AsScheduleDto {

    private Long scheduleId;            // 일정 고유 ID
    private Long asId;                  // 연결된 AS_ID

    private LocalDateTime planDate;     // 예정일
    private LocalDateTime completeDate; // 완료일 (체크 시 자동 저장)

    private String asContent;           // 점검 내용

    @JsonDeserialize(using = CustomBooleanDeserializer.class)
    private Boolean useFlag;            // 논리 삭제 여부

    private LocalDateTime regDate;      // 등록일
    private LocalDateTime modDate;      // 수정일

    // 📌 프론트 출력용 상태명 (대기 / 진행중 / 완료 / 보류 / 경고 등)
    private String statusLabel;

    // 📌 선택 체크박스 UI에 활용 가능
    private Boolean isCompleted;        // 완료 여부 (derived from completeDate != null)

    private String farmCode;
    private String farmName;
    private String regionName;
}
package com.inpro.asBoard.as.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.inpro.asBoard.as.common.CustomBooleanDeserializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AsDto {
    private Long asId;
    private String farmCode;
    private String farmName;
    private String regionName;;
    private String farmerName;
    private String phoneNumb;
    private String farmAddr1;
    private String farmAddr2;
    private String projectName;

    private String reqUser;
    private String asType;
    private String reqContent;
    private String asManager;

    private LocalDateTime regDate;
    private LocalDateTime updDate;
    private LocalDateTime reqDate;
    private LocalDateTime planDate;

    private String asResult;
    private String etc;

    @JsonDeserialize(using = CustomBooleanDeserializer.class)
    private Boolean useFlag;       // 논리삭제 여부

    @JsonDeserialize(using = CustomBooleanDeserializer.class)
    private Boolean statusFlag;    // ✅ 상태 플래그 (true: 정상, false: 보류)

    private List<AsEquipDto> equipList;

    private List<AsScheduleDto> scheduleList;

    // 프론트 출력용 상태명 (진행중/완료/보류 등)
    private String statusLabel;

    private List<AsFileDto> fileList;

    private List<Long> deletedFileIds;

}

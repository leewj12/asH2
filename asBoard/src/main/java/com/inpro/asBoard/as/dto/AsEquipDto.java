package com.inpro.asBoard.as.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.inpro.asBoard.as.common.CustomBooleanDeserializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AsEquipDto {
    private Long asEquipId;
    private Long asId;
    private String tableName;
    private String eqmntSeq;
    private String subSeq;
    private String eqmntKind;
    private String eqmntName;
    private LocalDateTime useDate; // 설치일

    @JsonDeserialize(using = CustomBooleanDeserializer.class)
    private Boolean useFlag; // 논리삭제 여부
}

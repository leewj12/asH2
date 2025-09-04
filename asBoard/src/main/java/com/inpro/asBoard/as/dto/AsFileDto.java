package com.inpro.asBoard.as.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.inpro.asBoard.as.common.CustomBooleanDeserializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AsFileDto {
    private Long fileId;          // 파일 PK
    private Long asId;            // AS ID (FK)

    private String originalName;  // 사용자가 올린 원본 파일명
    private String uuidName;      // 서버에 저장된 UUID 파일명
    private String filePath;      // 저장된 경로 (ex: /upload/as/2025/08/04)
    private Integer fileSize;     // 파일 크기 (byte)
    private String fileType;      // MIME 타입
    private LocalDateTime regDate; // 업로드 시간

    @JsonDeserialize(using = CustomBooleanDeserializer.class)
    private Boolean useFlag;      // 논리 삭제 여부
}
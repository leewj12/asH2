package com.inpro.asBoard.storage;

import lombok.Builder;
import lombok.Getter;

import java.nio.file.Path;

@Getter
@Builder
public class SavedFile {
    private final String originalName;
    private final String uuidName;
    private final String publicDir;   // 예: /upload/as/2025-08-18
    private final long size;          // ⚠ 도메인 DTO가 int면 캐스팅 주의
    private final String contentType;
    private final Path absolutePath;  // 실제 파일 위치
}
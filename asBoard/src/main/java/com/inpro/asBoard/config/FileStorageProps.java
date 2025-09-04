package com.inpro.asBoard.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "asboard.file")
public class FileStorageProps {

    //========================================================================
    // 1. 파일 저장 설정 프로퍼티
    // 2. URL : (ConfigProps) prefix=asboard.file
    // 3. Param : uploadRoot (required, String), publicBase (optional, String, default="/upload")
    // 4. 설명 : 업로드 물리 저장 루트와 공개 URL prefix 설정
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================

    /** 실제 저장 루트(절대 경로) 예) D:/as_uploads, /data/as_uploads */
    private String uploadRoot;

    /** 공개 URL prefix 예) /upload */
    private String publicBase = "/upload";
}
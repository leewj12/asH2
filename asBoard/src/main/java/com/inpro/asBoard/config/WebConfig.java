package com.inpro.asBoard.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
@EnableConfigurationProperties(FileStorageProps.class)
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final FileStorageProps props;

    //========================================================================
    // 1. 업로드 공개경로 매핑 설정
    // 2. URL : (Config) 정적 리소스 핸들러
    // 3. Param :
    // 4. 설명 : 외부 업로드 디렉토리(uploadRoot)를 /{publicBase}/** URL로 서빙하도록 매핑
    //          예) uploadRoot=D:/as_uploads → file:/D:/as_uploads/ 를
    //              /upload/** 같은 퍼블릭 경로로 노출
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 예) uploadRoot = D:/as_uploads  →  file:/D:/as_uploads/
        String fileLocation = Paths.get(props.getUploadRoot()).toUri().toString();
        String publicBase = props.getPublicBase();
        if (!publicBase.startsWith("/")) publicBase = "/" + publicBase;
        if (publicBase.endsWith("/")) publicBase = publicBase.substring(0, publicBase.length()-1);

        registry.addResourceHandler(publicBase + "/**")
                .addResourceLocations(fileLocation)   // 외부 디렉토리 노출
                .setCachePeriod(3600);
    }
}

package com.inpro.asBoard.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class JacksonConfig {

    //========================================================================
    // 1. JSON String trim→null 역직렬화 커스터마이저
    // 2. URL : (Config) Jackson ObjectMapper
    // 3. Param :
    // 4. 설명 : 입력 JSON 문자열을 trim 후 빈 문자열("")은 null로 변환
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonStringTrimToNull() {
        return builder -> builder.deserializerByType(String.class, new JsonDeserializer<String>() {
            @Override
            public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String s = p.getValueAsString(); // null, "  ", "text"
                if (s == null) return null;
                s = s.trim();
                return s.isEmpty() ? null : s;
            }
        });
    }
}
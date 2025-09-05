package com.inpro.asBoard.as.common;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

//========================================================================
// 1. 커스텀 Boolean 역직렬화기
// 2. URL : (Jackson) JsonDeserializer<Boolean>
// 3. Param : JSON 문자열 값("0" | "1" | "true" | "false" | 공백/null)
// 4. 설명 : "1"/"true" → true, "0"/"false" → false, 공백/누락 → null, 그 외는 예외
// 5. 작성 : wjlee(25.09.01)
// 6. 수정 :
//========================================================================
public class CustomBooleanDeserializer extends JsonDeserializer<Boolean> {

    //========================================================================
    // 1. 역직렬화 로직
    // 2. URL : (Jackson) JsonParser → Boolean
    // 3. Param : p(JsonParser), ctxt(DeserializationContext)
    // 4. 설명 : 입력 문자열 트리밍 후 매핑(true/false/null), 허용 외 값은 IllegalArgumentException
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    @Override
    public Boolean deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        value = value.trim();

        if ("1".equals(value) || "true".equalsIgnoreCase(value)) return true;
        if ("0".equals(value) || "false".equalsIgnoreCase(value)) return false;

        throw new IllegalArgumentException("Invalid boolean value: " + value);
    }
}
package com.inpro.asBoard.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class RequestPathAdvice {

    //========================================================================
    // 1. 현재 경로 모델 주입
    // 2. URL : (Advice) 모든 요청
    // 3. Param : request (HttpServletRequest)
    // 4. 설명 : 템플릿에서 사이드바 활성화 등에 쓰도록 'currentPath'에 요청 URI 주입
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI();   //   /as6/list  같은 값
    }
}
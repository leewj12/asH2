package com.inpro.asBoard;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootRedirectController {

    //========================================================================
    // 1. 루트 리다이렉트
    // 2. URL : [GET]{...}/
    // 3. Param :
    // 4. 설명 : 기본 페이지를 /as/dashboard 로 리다이렉트
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    @GetMapping("/")
    public String root() { return "redirect:/as/dashboard"; }
}


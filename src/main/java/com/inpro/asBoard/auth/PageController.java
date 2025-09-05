package com.inpro.asBoard.auth;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageController {

    //========================================================================
    // 1. 로그인 페이지
    // 2. URL : [GET]{...}/login
    // 3. Param : redirect (optional, String)
    // 4. 설명 : 로그인 페이지(성공 시 이동할 redirect 전달)
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    @GetMapping("/login")
    public String login(@RequestParam(required=false) String redirect, Model model) {
        model.addAttribute("redirect", redirect == null ? "/" : redirect);
        return "auth/login";   // templates/auth/login.html
    }

    //========================================================================
    // 1. 회원가입 페이지
    // 2. URL : [GET]{...}/signup
    // 3. Param :
    // 4. 설명 : 회원가입 페이지
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    @GetMapping("/signup")
    public String signup() {
        return "auth/signup";  // templates/auth/signup.html
    }

    //========================================================================
    // 1. 접근 거부 페이지
    // 2. URL : [GET]{...}/403
    // 3. Param :
    // 4. 설명 : 권한 없음(403) 에러 페이지
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    @GetMapping("/403")
    public String forbidden() { return "error/403"; } // 선택
}

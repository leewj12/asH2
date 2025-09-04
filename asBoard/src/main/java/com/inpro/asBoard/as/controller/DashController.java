package com.inpro.asBoard.as.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashController {

    //========================================================================
    // 1. 대시보드 페이지
    // 2. URL : [GET]{...}/as/dashboard
    // 3. Param :
    // 4. 설명 : 대시보드 페이지
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    @GetMapping("/as/dashboard")
    public String showDashboardPage(Model model) {
        return "as/dashboard";
    }
}
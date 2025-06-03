package com.csj.framework.ui.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SimplePageController {

    @GetMapping("/ui-test")
    public String testPage(Model model) {
        model.addAttribute("message", "Main UI is working!");
        return "test-page"; // Refers to src/main/resources/templates/test-page.html
    }
}

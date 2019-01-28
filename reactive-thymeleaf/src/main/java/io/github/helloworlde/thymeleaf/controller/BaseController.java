package io.github.helloworlde.thymeleaf.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author HelloWood
 * @date 2019-01-15 20:32
 */
@Controller
@RequestMapping("/")
public class BaseController {

    @GetMapping("")
    public String root() {
        return "redirect:/posts";
    }
}
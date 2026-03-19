package energy.eddie.exampleapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @SuppressWarnings("java:S6856")
    @GetMapping("/{path:[^.]*}")
    public String vue() {
        return "forward:/index.html";
    }
}

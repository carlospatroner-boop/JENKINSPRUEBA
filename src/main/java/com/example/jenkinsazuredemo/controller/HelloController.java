package com.example.jenkinsazuredemo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HelloController {

    @GetMapping("/")
    public Map<String, String> hello() {
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Hola desde Azure!");
        response.put("plataforma", "Spring Boot");
        response.put("deploy", "Jenkins CI/CD");
        response.put("estado", "Exitoso");
        return response;
    }
}
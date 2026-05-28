package com.nekosaur.ee;

import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

@Controller
public class ExpectedEconomicsController {
    private final ExpectedEconomicsService service;
    public ExpectedEconomicsController(ExpectedEconomicsService service) { this.service = service; }

    @GetMapping("/")
    public String index() { return "index"; }

    @PostMapping("/api/eev/calculate")
    @ResponseBody
    public Object calculate(@RequestBody ExpectedEconomicsDTO dto) {
        return service.calculate(dto);
    }
}
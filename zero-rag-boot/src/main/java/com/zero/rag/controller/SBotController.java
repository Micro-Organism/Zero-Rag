package com.zero.rag.controller;

import com.zero.rag.service.SBotService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequiredArgsConstructor
public class SBotController {

    SBotService sBotService;

    @Autowired
    public SBotController(SBotService sBotService) {
        this.sBotService = sBotService;
    }

    @GetMapping
    public String home() {
        return "index";
    }

    @PostMapping("/ask")
    public ResponseEntity<String> ask(@RequestBody String question) {
        try {
            return ResponseEntity.ok(sBotService.askQuestion(question));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Sorry, I can't process your question right now.");
        }
    }
}

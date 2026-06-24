package com.nexushr.Ai;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.ai.chat.client.ChatClient; // Requires Spring AI dependency

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
public class AiAssistantController {

    private final ChatClient chatClient;

    // Inject Spring AI's ChatClient (Auto-configured via your application.yml)
    public AiAssistantController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @PostMapping("/query")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Map<String, String> queryAssistant(@RequestBody Map<String, String> request) {
        try {
            String userPrompt = request.get("prompt");
            String systemRules = "You are NexusHR AI...";

            String aiResponse = chatClient.prompt()
                    .system(systemRules)
                    .user(userPrompt)
                    .call()
                    .content();

            return Map.of("answer", aiResponse);
        } catch (Exception e) {
            // This will print the ACTUAL error to your browser console
            e.printStackTrace();
            return Map.of("answer", "ERROR: " + e.getMessage());
        }
    }
}
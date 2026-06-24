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
        String userPrompt = request.get("prompt");

        // System prompt to give the AI context about its identity and job
        String systemRules = "You are NexusHR AI, an enterprise workforce assistant. " +
                "Provide concise, professional answers regarding HR, payroll, and performance metrics.";

        // Call the OpenAI model via Spring AI
        String aiResponse = chatClient.prompt()
                .system(systemRules)
                .user(userPrompt)
                .call()
                .content();

        return Map.of("answer", aiResponse);
    }
}
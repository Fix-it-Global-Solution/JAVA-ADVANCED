package com.fixit.api.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleGeminiAiConfig {

    @Bean
    public ChatClient chatClient(GoogleGeminiChatModel googleGeminiChatModel) {
        return ChatClient.builder(googleGeminiChatModel).build();
    }

}


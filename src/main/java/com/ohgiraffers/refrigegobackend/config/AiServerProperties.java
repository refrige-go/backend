package com.ohgiraffers.refrigegobackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai-server")
public class AiServerProperties {
    
    private String baseUrl = "http://localhost:8000";
    private int timeout = 30000;
    private Endpoints endpoints = new Endpoints();
    
    @Data
    public static class Endpoints {
        private String semanticSearch = "/api/search/semantic";
        private String vectorSearch = "/api/search/vector";
        private String recipeSearch = "/api/search/recipes";
        private String ingredientSearch = "/api/search/ingredients";
        private String spellCheck = "/api/spell/spell-check";
    }
}

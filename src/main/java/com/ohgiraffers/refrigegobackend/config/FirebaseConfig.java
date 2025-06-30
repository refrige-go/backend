package com.ohgiraffers.refrigegobackend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.service-account.path:firebase-service-account.json}")
    private String serviceAccountPath;

    @Value("${firebase.project-id}")
    private String projectId;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            try {
                InputStream serviceAccount = new ClassPathResource(serviceAccountPath).getInputStream();
                
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setProjectId(projectId)
                        .build();

                FirebaseApp app = FirebaseApp.initializeApp(options);
                log.info("Firebase Admin SDK 초기화 완료 - Project ID: {}", projectId);
                return app;
            } catch (IOException e) {
                log.error("Firebase 서비스 계정 파일을 읽을 수 없습니다: {}", e.getMessage(), e);
                throw e;
            }
        } else {
            log.info("Firebase Admin SDK가 이미 초기화되어 있습니다.");
            return FirebaseApp.getInstance();
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
} 
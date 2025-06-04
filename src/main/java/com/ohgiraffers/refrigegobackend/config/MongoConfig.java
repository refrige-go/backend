package com.ohgiraffers.refrigegobackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing
@Profile("!test")  // test 환경에서는 제외
public class MongoConfig {
}


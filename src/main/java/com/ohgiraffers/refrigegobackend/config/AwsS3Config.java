package com.ohgiraffers.refrigegobackend.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AwsS3Config {

    @Value("${aws.region}")
    private String region;

    @Value("${aws.access-key:}") // 로컬용 키 (없으면 빈 값)
    private String accessKey;

    @Value("${aws.secret-key:}")
    private String secretKey;

    @Bean
    public AmazonS3 amazonS3() {
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.fromName(region));

        // 로컬에서 키 값이 있으면 수동 인증, 아니면 EC2 IAM 사용
        if (!accessKey.isBlank() && !secretKey.isBlank()) {
            BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
            builder = builder.withCredentials(new AWSStaticCredentialsProvider(credentials));
        }

        return builder.build();
    }
}

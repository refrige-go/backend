package com.ohgiraffers.refrigegobackend.config;


import com.ohgiraffers.refrigegobackend.common.UserRole;
import com.ohgiraffers.refrigegobackend.config.handler.AuthFailHandler;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    private AuthFailHandler authFailHandler;

    /*
     * 비밀번호를 인코딩 하기 위한 bean
     * Bcrypt는 비밀번호 해싱에 가장 많이 사용되는 알고리즘 중 하나이다.
     * */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain confiig(HttpSecurity http) throws Exception {

        // 세션기반 로그인과 다르게 밑에 세가지 disable 
        http.csrf((auth) -> auth.disable());
        http.formLogin((auth) -> auth.disable());
        http.httpBasic((auth) -> auth.disable());
        
        
        //접근할 수 있는 경로 설정
        http.authorizeHttpRequests((auth) -> auth
                .requestMatchers("/login", "/", "/user/signup", "/api/recipes/saveAll").permitAll()
                .anyRequest().authenticated());

        //세션은 STATELESS 로
        http.sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

}

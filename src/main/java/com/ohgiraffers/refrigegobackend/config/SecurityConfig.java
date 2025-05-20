package com.ohgiraffers.refrigegobackend.config;


import com.ohgiraffers.refrigegobackend.auth.jwt.LoginFilter;
import com.ohgiraffers.refrigegobackend.config.handler.AuthFailHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    //AuthenticationManager 인자로 받을 AuthenticationConfiguration 객체 생성자 주입
    private final AuthenticationConfiguration authenticationConfiguration;

    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration) {

        this.authenticationConfiguration = authenticationConfiguration;
    }


    private AuthFailHandler authFailHandler;

    //AuthenticationManager Bean 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    /*
     * 비밀번호를 인코딩 하기 위한 bean
     * Bcrypt는 비밀번호 해싱에 가장 많이 사용되는 알고리즘 중 하나이다.
     * */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // 세션기반 로그인과 다르게 밑에 세가지 disable 
        http.csrf((auth) -> auth.disable());
        http.formLogin((auth) -> auth.disable());
        http.httpBasic((auth) -> auth.disable());
        
        
        //접근할 수 있는 경로 설정
        http.authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/login", "/", "/user/signup", "/user-ingredients/**","/ingredients/**" ).permitAll()
                        .anyRequest().authenticated());

        //필터 추가 LoginFilter()는 인자를 받음 (AuthenticationManager() 메소드에 authenticationConfiguration 객체를 넣어야 함) 따라서 등록 필요
        http.addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration)), UsernamePasswordAuthenticationFilter.class);
        
        //세션은 STATELESS 로
        http.sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

}

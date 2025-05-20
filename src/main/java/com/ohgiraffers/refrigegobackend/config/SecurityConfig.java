package com.ohgiraffers.refrigegobackend.config;


import com.ohgiraffers.refrigegobackend.common.UserRole;
import com.ohgiraffers.refrigegobackend.config.handler.AuthFailHandler;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
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
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Bean
    public SecurityFilterChain confiig(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(auth -> {
            //어떤 페이지에 어떤 아이디가 접근할 수 있는지 설정
            auth.requestMatchers("/auth/login", "/user/signup", "/auth/fail","/").permitAll();
            auth.requestMatchers("/auth/*").hasAnyAuthority(UserRole.ADMIN.getRole());
            auth.requestMatchers("/user/*").hasAnyAuthority(UserRole.USER.getRole());
            auth.anyRequest().authenticated();
        }).formLogin( login ->{
            //어떤요청을 보냈을때 로그인을 한걸로 설정할건지
            login.loginPage("/auth/login");
            login.usernameParameter("user");
            login.passwordParameter("pass");
            login.defaultSuccessUrl("/", true);
            login.failureUrl("/");
            //로그인 실패했을 때 써주는 핸들러
            login.failureHandler(authFailHandler);

        }).logout(logout ->{

            logout.logoutRequestMatcher(new AntPathRequestMatcher("/auth/logout"));
            // 어떤요청이 들어왔을 때 로그아웃 할건지
            logout.deleteCookies("JSESSIONID");
            //로그아웃 했을 때 사용자의 브라우저에서 해당 세션을 삭제함
            logout.invalidateHttpSession(true);
            logout.logoutSuccessUrl("/");
            //로그아웃에 성공하고 어떤페이지로 갈건지
        }).sessionManagement(session ->{
            //사용자가 세션을 할때 몇개의 세션을 열어줄건지
            session.maximumSessions(1);
            //동일한 아이디로 2명이 로그인 할 수 없음, 한명만 허용
            session.invalidSessionUrl("/");
            //이미로그인 한게 있으면 로그인성공했을때 기본경로로감
        }).csrf(csrf -> csrf.disable());

        return http.build();
    }

}

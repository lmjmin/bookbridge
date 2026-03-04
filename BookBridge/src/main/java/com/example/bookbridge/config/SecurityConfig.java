package com.example.bookbridge.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            HandlerMappingIntrospector introspector) throws Exception {

        // MVC 기반 매처 (DispatcherServlet 기준)
        MvcRequestMatcher.Builder mvc = new MvcRequestMatcher.Builder(introspector).servletPath("/");

        http
            .csrf(csrf -> csrf.disable())
            .headers(h -> h.frameOptions(f -> f.sameOrigin()))   // H2 console 등
            .cors(c -> {})                                       // WebConfig의 CORS 적용

            .authorizeHttpRequests(auth -> auth
                // 스프링이 관리하는 정적 리소스 공통 위치
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()

                // H2 콘솔
                .requestMatchers(mvc.pattern("/h2-console/**")).permitAll()

                // 정적/헬스체크/루트
                .requestMatchers(
                    mvc.pattern("/"),
                    mvc.pattern("/index"),
                    mvc.pattern("/index.html"),
                    mvc.pattern("/first.html"),
                    mvc.pattern("/s/**"),              // 정적 파일 프록시 경로
                    mvc.pattern("/css/**"),
                    mvc.pattern("/js/**"),
                    mvc.pattern("/img/**"),
                    mvc.pattern("/images/**"),
                    mvc.pattern("/media/**"),
                    mvc.pattern("/uploads/**"),
                    mvc.pattern("/favicon.ico"),
                    mvc.pattern("/manifest.json"),
                    mvc.pattern("/actuator/health"),
                    mvc.pattern("/actuator/info"),
                    mvc.pattern("/error")              // Whitelabel 접근 허용
                ).permitAll()

                // API (배포 안정화를 위해 우선 전부 허용 — 추후 축소)
                .requestMatchers(mvc.pattern("/api/**")).permitAll()
                .requestMatchers(mvc.pattern("/kakao/**")).permitAll()
                .requestMatchers(mvc.pattern("/books/**")).permitAll()
                .requestMatchers(mvc.pattern("/book/**")).permitAll()
                .requestMatchers(mvc.pattern("/list/**")).permitAll()
                .requestMatchers(mvc.pattern("/listings/**")).permitAll()
                .requestMatchers(mvc.pattern("/search/**")).permitAll()

                // CORS preflight
                .requestMatchers(mvc.pattern(HttpMethod.OPTIONS, "/**")).permitAll()

                // 나머지
                .anyRequest().permitAll()
            )
            .httpBasic(b -> b.disable())
            .formLogin(f -> f.disable())
            .logout(l -> l.disable());

        return http.build();
    }
}

package com.noscendo.authorize.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.method.AuthorizationAdvisorProxyFactory;
import org.springframework.security.authorization.method.AuthorizationAdvisorProxyFactory.TargetVisitor;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll()
                );
        return http.build();
    }

    @Bean
    Customizer<AuthorizationAdvisorProxyFactory> addVisitors() {
        return (factory) -> factory.setTargetVisitor(TargetVisitor.of(responseEntity, TargetVisitor.defaults()));
    }

    private final TargetVisitor responseEntity = (proxyFactory, target) -> {
        if (target instanceof ResponseEntity<?> entity) {
            Object body = entity.getBody();
            HttpHeaders header = entity.getHeaders();
            HttpStatusCode code = entity.getStatusCode();
            return new ResponseEntity<>(proxyFactory.proxy(body), header, code);
        }
        return null;
    };
}

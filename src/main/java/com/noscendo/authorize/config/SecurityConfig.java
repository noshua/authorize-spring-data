package com.noscendo.authorize.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.authorization.method.AuthorizationAdvisorProxyFactory;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final AuthorizationAdvisorProxyFactory.TargetVisitor page = (proxyFactory, target) -> {
        if (target instanceof PageImpl<?> page) {
            List<Object> content = (List<Object>) proxyFactory.proxy(page.getContent());
            return new PageImpl<>(content, page.getPageable(), page.getTotalElements());
        }
        return null;
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll()
                )
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    Customizer<AuthorizationAdvisorProxyFactory> addVisitors() {
        return (factory) -> factory.setTargetVisitor(AuthorizationAdvisorProxyFactory.TargetVisitor.of(page, AuthorizationAdvisorProxyFactory.TargetVisitor.defaults()));
    }
}

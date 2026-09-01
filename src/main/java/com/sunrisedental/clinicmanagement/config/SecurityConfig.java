package com.sunrisedental.clinicmanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .authorizeHttpRequests(authorize -> authorize

                .requestMatchers(
                        "/login",
                        "/error",
                        "/access-denied",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/favicon.ico")
                .permitAll()

                .requestMatchers(
                        "/admin/**",
                        "/staff/**",
                        "/audit/**")
                .hasRole("ADMIN")

                .requestMatchers("/api/**")
                .authenticated()

                .anyRequest()
                .authenticated()
            )

            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error")
                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )

            .exceptionHandling(exceptions -> exceptions
                .accessDeniedPage("/access-denied")
            )

            .sessionManagement(session -> session
                .sessionFixation(fixation ->
                        fixation.migrateSession())
                .maximumSessions(1)
                .expiredUrl("/login?expired")
            );

        return http.build();
    }
}
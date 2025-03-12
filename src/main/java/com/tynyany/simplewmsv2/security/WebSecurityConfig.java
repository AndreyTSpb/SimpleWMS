package com.tynyany.simplewmsv2.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public UserDetailsService userDetailsService() throws Exception {
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(User
                .withUsername("user@mail.ru")
                .password(encoder().encode("password"))
                .roles("USER")
                .build());

        manager.createUser(User
                .withUsername("admin@mail.ru")
                .password(encoder().encode("password"))
                .roles("ADMIN")
                .build());

        return manager;
    }

    @Bean
    public static PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.sessionManagement(httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.ALWAYS));

        return http.authorizeHttpRequests((auth) -> auth.anyRequest().permitAll()).build();

        //http.authorizeRequests((requests) -> requests.anyRequest().authenticated());
//        http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
//        http.formLogin(
//                form -> form
//                        .loginPage("/login")
//                        .permitAll()
//        );
//        http.httpBasic(Customizer.withDefaults());
//        return http.build();
    }

    @Bean
    public WebSecurityCustomizer configureWebSecurity() {
        return (web) -> web.ignoring().requestMatchers("/img/**", "/js/**", "/css/**", "/vendor/**");
    }
}

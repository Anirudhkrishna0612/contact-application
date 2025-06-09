package com.project.contact;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class MyConfig {

    @Bean
    public UserDetailsService getUserDetailService() {
        return new UserDetailsServiceImpl(); // Corrected class name
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(this.getUserDetailService());
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // CSRF disabled for simpler development, re-enable for production!
            .authorizeHttpRequests(authorize -> authorize
                // Publicly accessible paths
                .requestMatchers(
                    "/",
                    "/about",
                    "/logout",      // Spring Security's actual logout endpoint
                    "/sure/logout", // Our new logout confirmation page
                    "/signup",
                    "/do_register",
                    "/login",
                    "/img/**",
                    "/css/**",
                    "/js/**"
                ).permitAll()
                // Role-based access
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/user/**").hasRole("USER")
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/do_login")
                .defaultSuccessUrl("/user/index", true) // Redirect to user dashboard after successful login
                .failureUrl("/login?error")
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout")) // URL that triggers Spring Security logout
                .logoutSuccessUrl("/login?logout") // Redirect to login page after successful logout
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            );

        http.authenticationProvider(authenticationProvider());

        return http.build();
    }
}
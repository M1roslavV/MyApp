package com.mypropertyapp.exception_config;

import com.mypropertyapp.user.UserDetailsServiceCustom;
import com.mypropertyapp.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class AppConfig {

    private final UserRepository userRepository;
    private static final String[] WHITE_LIST_URL = {"/","/images/**",
            "/script/**", "/style/**", "/sign_up", "/sign_up/process", "/login", "/login/verify", "/aboutUs", "/files/**"};

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceCustom(userRepository);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/dashboard").hasAnyAuthority("OWNER","ADMIN","EMPLOYEE")
                        .requestMatchers("/dashboard/add_property").hasAnyAuthority("OWNER","ADMIN")
                        .requestMatchers(WHITE_LIST_URL).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/sign_in")
                        .defaultSuccessUrl("/dashboard", false)
                        .permitAll()
                )
                .logout((logout) -> logout
                        .logoutUrl("/logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessUrl("/login?logout")
                )
                .exceptionHandling((exceptions) -> exceptions
                        .accessDeniedPage("/403")
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}

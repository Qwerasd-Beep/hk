package com.security.config;

import com.security.fliter.JwtAuthFilter;
import com.security.handler.AccDeniedHandler;
import com.security.handler.LoginFailHandler;
import com.security.handler.LoginOkHandler;
import com.security.handler.LogoutOkHandler;
import jakarta.annotation.Resource;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Resource
    private AccDeniedHandler accDeniedHandler;


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Resource
    private LoginOkHandler loginOkHandler;
    @Resource
    private LoginFailHandler loginFailHandler;
    @Resource
    private JwtAuthFilter jwtAuthFilter;
    @Resource
    private LogoutOkHandler logoutOkHandler;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity sec) throws Exception{
        return sec.formLogin((f)->{
            f.loginProcessingUrl("/login")
                    .usernameParameter("name")
                    .passwordParameter("password")
                    .successHandler(loginOkHandler)
                    .failureHandler(loginFailHandler);
        })
                .authorizeHttpRequests(a->{
                    String[] allowPath={
                            "/favicon.ico" ,
                            "/login",
                            "/swagger-ui.html",
                            "/swagger-ui/**",
                            "/v3/api-docs/**",
                            "/doc.html",
                            "/webjars/**",
                            "/ai/**",
                            "/tool/**",
                            "/ai-image/**",
                            // MCP Server 的 SSE 端点（演示环境放行，生产应加鉴权）
                            "/sse",
                            "/sse/message"

                    };
                    a.dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.ERROR).permitAll()
                            .requestMatchers(allowPath).permitAll()
                            .anyRequest().authenticated();
                })
                .csrf(t->{
                    t.disable();
                })
                .addFilterBefore(jwtAuthFilter, LogoutFilter.class)
                .cors(c->{
                    c.configurationSource(myCors());
                })
                .logout(o->{
                    o.logoutUrl("/logout")
                            .logoutSuccessHandler(logoutOkHandler)
                    ;
                })
                .exceptionHandling(h->{
                    h.accessDeniedHandler(accDeniedHandler);  //访问权限不路处理器
                })
                .build();
    }

    private CorsConfigurationSource myCors() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


}

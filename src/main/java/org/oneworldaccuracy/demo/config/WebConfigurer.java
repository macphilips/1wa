package org.oneworldaccuracy.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * Configuration of web application with Servlet 3.0 APIs.
 */
@Configuration
@Slf4j
public class WebConfigurer implements ServletContextInitializer {

    public WebConfigurer() {
    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        log.info("Web application fully configured");
    }

    /**
     * Creates CORS Filter bean.
     */
    @Bean
    public CorsFilter corsFilter() {
        log.debug("Registering CORS filter");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setMaxAge(1800L);
        source.registerCorsConfiguration("/api/**", config);
        return new CorsFilter(source);
    }

    // @Bean
    // public PasswordEncoder passwordEncoder() {
    //     return new BCryptPasswordEncoder();
    // }
}

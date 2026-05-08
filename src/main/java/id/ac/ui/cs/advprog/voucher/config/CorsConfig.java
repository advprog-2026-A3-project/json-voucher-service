package id.ac.ui.cs.advprog.voucher.config;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    private final String allowedOrigins;

    public CorsConfig(@Value("${APP_CORS_ALLOWED_ORIGINS:http://localhost:3000}") String allowedOrigins){
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry){
        registry.addMapping("/api/**")
            .allowedOrigins(parseAllowedOrigins())
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*");
    }

    private String[] parseAllowedOrigins(){
        return Arrays.stream(allowedOrigins.split(","))
            .map(String::trim)
            .filter(origin -> !origin.isEmpty())
            .toArray(String[]::new);
    }
}
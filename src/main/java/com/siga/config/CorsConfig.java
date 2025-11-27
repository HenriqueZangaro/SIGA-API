package com.siga.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração de CORS para permitir requisições do app web e celular.
 * 
 * Esta configuração permite:
 * - Requisições de localhost (desenvolvimento web)
 * - Requisições do IP local (celular na mesma rede)
 * - Todos os métodos HTTP (GET, POST, PUT, DELETE, etc.)
 * - Todos os headers (incluindo X-User-UID)
 */
@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    // Origens permitidas (web e celular)
                    .allowedOriginPatterns(
                        "http://localhost:*",
                        "http://127.0.0.1:*",
                        "http://192.168.*.*:*",
                        "http://10.*.*.*:*",
                        "http://172.16.*.*:*",
                        "http://172.17.*.*:*",
                        "http://172.18.*.*:*",
                        "http://172.19.*.*:*",
                        "http://172.20.*.*:*",
                        "http://172.21.*.*:*",
                        "http://172.22.*.*:*",
                        "http://172.23.*.*:*",
                        "http://172.24.*.*:*",
                        "http://172.25.*.*:*",
                        "http://172.26.*.*:*",
                        "http://172.27.*.*:*",
                        "http://172.28.*.*:*",
                        "http://172.29.*.*:*",
                        "http://172.30.*.*:*",
                        "http://172.31.*.*:*",
                        "exp://*",
                        "*"
                    )
                    // Métodos HTTP permitidos
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD")
                    // Headers permitidos (incluindo o X-User-UID usado para autenticação)
                    .allowedHeaders("*")
                    // Headers expostos na resposta
                    .exposedHeaders("*")
                    // Permite envio de cookies/credenciais
                    .allowCredentials(false)
                    // Cache de preflight (1 hora)
                    .maxAge(3600);
                
                System.out.println("✅ CORS configurado para permitir requisições de qualquer origem local");
            }
        };
    }
}


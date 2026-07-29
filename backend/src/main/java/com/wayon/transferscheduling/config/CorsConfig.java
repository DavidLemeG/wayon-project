package com.wayon.transferscheduling.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * O front-end roda em outra origem (dev server do Vite), entao o navegador
 * exige CORS para chamar a API na 8080. Os padroes de origem permitidos ficam
 * em propriedade (app.cors.allowed-origin-patterns) para nao precisar
 * recompilar ao publicar o front em outro dominio.
 *
 * <p>Usa allowedOriginPatterns (padrao, ex.: "http://localhost:*"), nao
 * allowedOrigins (lista exata). O Vite sobe na 5173 por padrao, mas incrementa
 * a porta (5174, 5175...) se algo ja estiver ouvindo nela — o que acontece na
 * pratica sempre que sobra um processo `node` de uma sessao anterior. Uma
 * lista fixa de portas quebra a cada vez que isso acontece; o padrao continua
 * restrito a localhost (nao abre para a internet), so nao trava numa porta
 * especifica.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String[] allowedOriginPatterns;

    public CorsConfig(@Value("${app.cors.allowed-origin-patterns}") String[] allowedOriginPatterns) {
        this.allowedOriginPatterns = allowedOriginPatterns;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(allowedOriginPatterns)
                .allowedMethods("GET", "POST")
                .allowedHeaders("Content-Type")
                .maxAge(3600);
    }

}

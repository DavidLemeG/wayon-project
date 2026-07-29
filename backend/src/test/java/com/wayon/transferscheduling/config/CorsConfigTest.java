package com.wayon.transferscheduling.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * O front-end (Vite) chama a API de outra origem, entao o navegador dispara
 * um preflight OPTIONS antes do POST. Sem CORS liberado, a chamada e
 * bloqueada no navegador mesmo com a API respondendo normalmente.
 *
 * <p>Usa MockMvc, e nao TestRestTemplate, de proposito: o TestRestTemplate usa
 * HttpURLConnection por baixo, que descarta silenciosamente headers restritos
 * (Origin, Access-Control-Request-*) — o preflight chegaria ao servidor sem os
 * headers que o tornam um preflight, e o teste passaria/falharia por engano.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CorsConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void preflightDaPortaPadraoDoViteEAutorizado() throws Exception {
        assertPreflightAutorizado("http://localhost:5173");
    }

    /**
     * O Vite incrementa a porta (5174, 5175...) quando 5173 ja esta em uso —
     * acontece na pratica sempre que sobra um processo `node` de uma sessao
     * anterior. allowedOriginPatterns("http://localhost:*") cobre isso sem
     * precisar listar cada porta.
     */
    @Test
    void preflightDeOutraPortaEmLocalhostTambemEAutorizado() throws Exception {
        assertPreflightAutorizado("http://localhost:5174");
    }

    private void assertPreflightAutorizado(String origin) throws Exception {
        mockMvc.perform(options("/api/transfers")
                        .header("Origin", origin)
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", origin))
                .andExpect(header().string("Access-Control-Allow-Methods", "GET,POST"));
    }

    @Test
    void preflightDeOrigemForaDeLocalhostERecusado() throws Exception {
        mockMvc.perform(options("/api/transfers")
                        .header("Origin", "http://site-nao-autorizado.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }

    @Test
    void requisicaoSimplesDaOrigemAutorizadaRecebeHeaderDeLiberacao() throws Exception {
        mockMvc.perform(get("/api/transfers")
                        .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

}

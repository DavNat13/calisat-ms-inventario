package com.califorge.msinventario.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de la Fase 8 (Endurecer seguridad) ejecutando la SecurityFilterChain real.
 * Carga solo la config de seguridad (sin JPA/BD). Las propiedades JWT son ficticias;
 * el JwtDecoder real hace discovery OIDC (HTTP) al construirse, asi que se sustituye
 * por un mock: aqui solo se ejercita la autorizacion de la filter chain, no la
 * validacion de tokens.
 *
 * Se usa un controller de prueba que responde 200 en cualquier ruta, para
 * distinguir "rechazado por falta de autenticacion" (401) de "404".
 */
@SpringJUnitConfig
@WebAppConfiguration
@Import({SecurityConfig.class, SecurityConfigTest.TestControllerConfig.class})
class SecurityConfigTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean(name = "jwtDecoder")
    private JwtDecoder jwtDecoder;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void health_esAccesibleSinAutenticacion() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void actuatorInfo_requiereAutenticacion() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void endpointDeNegocio_requiereAutenticacion() throws Exception {
        mockMvc.perform(get("/api/v1/stock"))
                .andExpect(status().isUnauthorized());
    }

    @Configuration
    @RestController
    static class TestControllerConfig {
        @GetMapping("/**")
        public ResponseEntity<String> any() {
            return ResponseEntity.ok("ok");
        }
    }
}

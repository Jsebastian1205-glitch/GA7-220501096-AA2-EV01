package com.buildzone.api.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.buildzone.api.Fixtures;
import com.buildzone.api.model.Usuario;
import com.buildzone.api.model.enums.Rol;

/** Pruebas unitarias de {@link JwtService}: emision y validacion de tokens. */
class JwtServiceTest {

    private static final String SECRETO = "V+ClppfJJpplwJCoBw1+E+TWBZw4ONFPwklx1J/omza5hFoT6/D9yO/EiKdHLRvV";
    private static final String OTRO_SECRETO = "iuiBiY3JEX+r74Fv2LF7dGEsCUx6/nsfpc/6ySTZ5vQQSTio0JyavpVkGEiHkIzY";

    private final Usuario ana = Fixtures.usuario(1L, "ana.gomez", Rol.USUARIO);

    @Test
    @DisplayName("Un token recien emitido es valido y contiene el username")
    void tokenValidoDevuelveUsername() {
        JwtService jwt = new JwtService(SECRETO, 60_000);

        String token = jwt.generarToken(ana);

        assertThat(jwt.extraerUsername(token)).contains("ana.gomez");
    }

    @Test
    @DisplayName("Un token vencido se rechaza")
    void tokenVencidoSeRechaza() {
        JwtService jwtVencido = new JwtService(SECRETO, -1_000);

        String token = jwtVencido.generarToken(ana);

        assertThat(jwtVencido.extraerUsername(token)).isEmpty();
    }

    @Test
    @DisplayName("Un token firmado con otra clave se rechaza")
    void tokenConOtraFirmaSeRechaza() {
        String tokenAjeno = new JwtService(OTRO_SECRETO, 60_000).generarToken(ana);

        assertThat(new JwtService(SECRETO, 60_000).extraerUsername(tokenAjeno)).isEmpty();
    }

    @Test
    @DisplayName("Un texto que no es JWT se rechaza sin lanzar excepcion")
    void textoBasuraSeRechaza() {
        JwtService jwt = new JwtService(SECRETO, 60_000);

        assertThat(jwt.extraerUsername("esto-no-es-un-token")).isEmpty();
        assertThat(jwt.extraerUsername("")).isEmpty();
    }

    @Test
    @DisplayName("Un secreto que no es Base64 (p. ej. generado por la nube) tambien sirve")
    void secretoNoBase64SeDeriva() {
        JwtService jwt = new JwtService("clave-generada-por-la-plataforma_sin-formato*", 60_000);

        assertThat(jwt.extraerUsername(jwt.generarToken(ana))).contains("ana.gomez");
    }
}

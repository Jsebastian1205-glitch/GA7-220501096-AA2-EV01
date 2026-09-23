package com.buildzone.api.security;

import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.buildzone.api.model.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Emite y valida los tokens JWT (firmados con HMAC-SHA256).
 * <p>
 * El token solo lleva el nombre de usuario (subject) y el rol como
 * informacion; en cada peticion el filtro vuelve a consultar el usuario
 * en la base de datos, de modo que un cambio de rol o una desactivacion
 * hecha por el administrador tiene efecto inmediato.
 */
@Service
public class JwtService {

    private final SecretKey clave;
    private final long expiracionMs;

    public JwtService(@Value("${buildzone.jwt.secret}") String secretoBase64,
                      @Value("${buildzone.jwt.expiracion-ms}") long expiracionMs) {
        this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretoBase64));
        this.expiracionMs = expiracionMs;
    }

    /**
     * @param usuario usuario autenticado
     * @return token JWT firmado con vigencia {@code expiracionMs}
     */
    public String generarToken(Usuario usuario) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(usuario.getUsername())
                .claim("rol", usuario.getRol().name())
                .claim("uid", usuario.getId())
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expiracionMs))
                .signWith(clave)
                .compact();
    }

    /**
     * Valida firma y vigencia del token.
     *
     * @param token JWT recibido
     * @return el nombre de usuario si el token es valido; vacio si esta
     *         vencido, alterado o mal formado
     */
    public Optional<String> extraerUsername(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(clave)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.ofNullable(claims.getSubject());
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}

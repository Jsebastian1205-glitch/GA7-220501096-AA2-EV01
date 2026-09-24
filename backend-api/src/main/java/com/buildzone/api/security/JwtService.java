package com.buildzone.api.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
        this.clave = construirClave(secretoBase64);
        this.expiracionMs = expiracionMs;
    }

    /**
     * Convierte el secreto configurado en una clave HMAC de al menos 256 bits.
     * Si el valor es Base64 valido y suficientemente largo se usa tal cual;
     * si no (por ejemplo un texto generado por la plataforma de despliegue),
     * se deriva una clave de 256 bits con SHA-256 para no impedir el arranque.
     */
    static SecretKey construirClave(String secreto) {
        try {
            byte[] bytes = Decoders.BASE64.decode(secreto);
            if (bytes.length >= 32) {
                return Keys.hmacShaKeyFor(bytes);
            }
        } catch (RuntimeException ex) {
            // No es Base64 valido: se deriva la clave mas abajo.
        }
        try {
            byte[] derivada = MessageDigest.getInstance("SHA-256").digest(secreto.getBytes(StandardCharsets.UTF_8));
            return Keys.hmacShaKeyFor(derivada);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no disponible en la JVM", ex);
        }
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

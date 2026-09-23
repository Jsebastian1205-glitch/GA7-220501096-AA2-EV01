package com.buildzone.api.config;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.buildzone.api.repository.UsuarioRepository;
import com.buildzone.api.security.JwtAuthenticationFilter;
import com.buildzone.api.security.JwtService;
import com.buildzone.api.security.RespuestaErrorSeguridad;

/**
 * Configuracion de seguridad de la API.
 * <ul>
 *   <li>Sin sesion de servidor (STATELESS): cada peticion se autentica con JWT.</li>
 *   <li>CSRF deshabilitado: no se usan cookies de sesion, el token viaja en un encabezado.</li>
 *   <li>CORS habilitado para el front-end estatico.</li>
 *   <li>Autorizacion por ruta y verbo HTTP segun el rol (tabla en README.md).</li>
 * </ul>
 */
@Configuration
public class SecurityConfig {

    private static final String ADMIN = "ADMIN";

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final RespuestaErrorSeguridad respuestaErrorSeguridad;
    private final List<String> origenesPermitidos;

    public SecurityConfig(JwtService jwtService,
                          UsuarioRepository usuarioRepository,
                          RespuestaErrorSeguridad respuestaErrorSeguridad,
                          @Value("${buildzone.cors.origenes-permitidos:*}") String origenes) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.respuestaErrorSeguridad = respuestaErrorSeguridad;
        this.origenesPermitidos = Arrays.stream(origenes.split(",")).map(String::trim).toList();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Permite que la consola H2 (solo perfil dev) se muestre en un iframe del mismo origen.
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(respuestaErrorSeguridad)
                    .accessDeniedHandler(respuestaErrorSeguridad))
            .authorizeHttpRequests(auth -> auth
                    // Preflight CORS
                    .requestMatchers(antMatcher(HttpMethod.OPTIONS, "/**")).permitAll()
                    // Publico
                    .requestMatchers(antMatcher("/api/auth/**")).permitAll()
                    .requestMatchers(antMatcher(HttpMethod.GET, "/api/planes")).permitAll()
                    .requestMatchers(antMatcher(HttpMethod.GET, "/api/productos/**"),
                            antMatcher(HttpMethod.GET, "/api/marcas/**"),
                            antMatcher(HttpMethod.GET, "/api/categorias/**")).permitAll()
                    .requestMatchers(antMatcher("/h2-console/**"), antMatcher("/error")).permitAll()
                    // Solo ADMIN
                    .requestMatchers(antMatcher(HttpMethod.GET, "/api/planes/todos")).hasRole(ADMIN)
                    .requestMatchers(antMatcher(HttpMethod.POST, "/api/planes")).hasRole(ADMIN)
                    .requestMatchers(antMatcher(HttpMethod.PUT, "/api/planes/**")).hasRole(ADMIN)
                    .requestMatchers(antMatcher(HttpMethod.DELETE, "/api/planes/**")).hasRole(ADMIN)
                    .requestMatchers(antMatcher(HttpMethod.GET, "/api/usuarios")).hasRole(ADMIN)
                    .requestMatchers(antMatcher(HttpMethod.PATCH, "/api/usuarios/**")).hasRole(ADMIN)
                    .requestMatchers(antMatcher(HttpMethod.GET, "/api/suscripciones")).hasRole(ADMIN)
                    .requestMatchers(antMatcher("/api/productos/**"), antMatcher("/api/marcas/**"),
                            antMatcher("/api/categorias/**")).hasRole(ADMIN)
                    // Todo lo demas requiere sesion (perfil propio, suscripciones propias)
                    .anyRequest().authenticated())
            .addFilterBefore(new JwtAuthenticationFilter(jwtService, usuarioRepository),
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** BCrypt con factor de costo por defecto (10). */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(origenesPermitidos);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

package com.buildzone.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.buildzone.api.dto.CambioEstadoRequest;
import com.buildzone.api.dto.CambioPasswordRequest;
import com.buildzone.api.dto.CambioRolRequest;
import com.buildzone.api.dto.PerfilRequest;
import com.buildzone.api.dto.UsuarioResponse;
import com.buildzone.api.security.UsuarioAutenticado;
import com.buildzone.api.service.UsuarioService;

import jakarta.validation.Valid;

/**
 * Endpoints de usuarios. Las rutas {@code /me} operan sobre el usuario
 * autenticado (tomado del token, nunca de un id enviado por el cliente);
 * el resto son de administracion y exigen rol ADMIN (ver SecurityConfig).
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /** GET /api/usuarios/me - perfil del usuario autenticado. */
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> miPerfil(@AuthenticationPrincipal UsuarioAutenticado actual) {
        return ResponseEntity.ok(usuarioService.obtenerPerfil(actual.id()));
    }

    /** PUT /api/usuarios/me - actualiza nombre, apellido y correo propios. */
    @PutMapping("/me")
    public ResponseEntity<UsuarioResponse> actualizarMiPerfil(@AuthenticationPrincipal UsuarioAutenticado actual,
                                                              @Valid @RequestBody PerfilRequest request) {
        return ResponseEntity.ok(usuarioService.actualizarPerfil(actual.id(), request));
    }

    /** PUT /api/usuarios/me/password - cambia la contrasena propia. */
    @PutMapping("/me/password")
    public ResponseEntity<Void> cambiarMiPassword(@AuthenticationPrincipal UsuarioAutenticado actual,
                                                  @Valid @RequestBody CambioPasswordRequest request) {
        usuarioService.cambiarPassword(actual.id(), request);
        return ResponseEntity.noContent().build();
    }

    /** GET /api/usuarios - lista todos los usuarios (ADMIN). */
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    /** PATCH /api/usuarios/{id}/rol - cambia el rol de un usuario (ADMIN). */
    @PatchMapping("/{id}/rol")
    public ResponseEntity<UsuarioResponse> cambiarRol(@AuthenticationPrincipal UsuarioAutenticado actual,
                                                      @PathVariable Long id,
                                                      @Valid @RequestBody CambioRolRequest request) {
        return ResponseEntity.ok(usuarioService.cambiarRol(actual.id(), id, request.rol()));
    }

    /** PATCH /api/usuarios/{id}/estado - activa o desactiva una cuenta (ADMIN). */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<UsuarioResponse> cambiarEstado(@AuthenticationPrincipal UsuarioAutenticado actual,
                                                         @PathVariable Long id,
                                                         @Valid @RequestBody CambioEstadoRequest request) {
        return ResponseEntity.ok(usuarioService.cambiarEstado(actual.id(), id, request.estado()));
    }
}

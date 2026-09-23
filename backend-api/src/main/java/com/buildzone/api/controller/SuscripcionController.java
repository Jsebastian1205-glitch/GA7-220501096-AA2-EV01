package com.buildzone.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.buildzone.api.dto.SuscripcionRequest;
import com.buildzone.api.dto.SuscripcionResponse;
import com.buildzone.api.security.UsuarioAutenticado;
import com.buildzone.api.service.SuscripcionService;

import jakarta.validation.Valid;

/** Endpoints de suscripciones del usuario autenticado y consulta global (ADMIN). */
@RestController
@RequestMapping("/api/suscripciones")
public class SuscripcionController {

    private final SuscripcionService suscripcionService;

    public SuscripcionController(SuscripcionService suscripcionService) {
        this.suscripcionService = suscripcionService;
    }

    /** POST /api/suscripciones - suscribe al usuario autenticado a un plan. */
    @PostMapping
    public ResponseEntity<SuscripcionResponse> suscribir(@AuthenticationPrincipal UsuarioAutenticado actual,
                                                         @Valid @RequestBody SuscripcionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(suscripcionService.suscribir(actual.id(), request.planId()));
    }

    /** GET /api/suscripciones/me/activa - suscripcion vigente, o 204 si no tiene. */
    @GetMapping("/me/activa")
    public ResponseEntity<SuscripcionResponse> miSuscripcionActiva(
            @AuthenticationPrincipal UsuarioAutenticado actual) {
        return suscripcionService.obtenerActiva(actual.id())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    /** GET /api/suscripciones/me - historial de suscripciones propias. */
    @GetMapping("/me")
    public ResponseEntity<List<SuscripcionResponse>> miHistorial(@AuthenticationPrincipal UsuarioAutenticado actual) {
        return ResponseEntity.ok(suscripcionService.historial(actual.id()));
    }

    /** DELETE /api/suscripciones/{id} - cancela una suscripcion propia (o cualquiera si es ADMIN). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@AuthenticationPrincipal UsuarioAutenticado actual, @PathVariable Long id) {
        suscripcionService.cancelar(actual, id);
        return ResponseEntity.noContent().build();
    }

    /** GET /api/suscripciones - todas las suscripciones (ADMIN). */
    @GetMapping
    public ResponseEntity<List<SuscripcionResponse>> listarTodas() {
        return ResponseEntity.ok(suscripcionService.listarTodas());
    }
}

package com.buildzone.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.buildzone.api.dto.PlanRequest;
import com.buildzone.api.dto.PlanResponse;
import com.buildzone.api.service.PlanService;

import jakarta.validation.Valid;

/** Endpoints de planes: consulta publica y administracion (ADMIN). */
@RestController
@RequestMapping("/api/planes")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    /** GET /api/planes - planes activos (publico, sin sesion). */
    @GetMapping
    public ResponseEntity<List<PlanResponse>> listarActivos() {
        return ResponseEntity.ok(planService.listarActivos());
    }

    /** GET /api/planes/todos - todos los planes, incluidos los inactivos (ADMIN). */
    @GetMapping("/todos")
    public ResponseEntity<List<PlanResponse>> listarTodos() {
        return ResponseEntity.ok(planService.listarTodos());
    }

    /** POST /api/planes - crea un plan (ADMIN). */
    @PostMapping
    public ResponseEntity<PlanResponse> crear(@Valid @RequestBody PlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(planService.crear(request));
    }

    /** PUT /api/planes/{id} - edita un plan (ADMIN). */
    @PutMapping("/{id}")
    public ResponseEntity<PlanResponse> actualizar(@PathVariable Long id, @Valid @RequestBody PlanRequest request) {
        return ResponseEntity.ok(planService.actualizar(id, request));
    }

    /** DELETE /api/planes/{id} - desactiva un plan (borrado logico, ADMIN). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        planService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}

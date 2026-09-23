package com.buildzone.api.controller;

import java.util.List;

import jakarta.validation.Valid;

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

import com.buildzone.api.dto.MarcaRequest;
import com.buildzone.api.dto.MarcaResponse;
import com.buildzone.api.service.MarcaService;

/**
 * Endpoints REST para administrar Marca: listar, consultar, crear,
 * actualizar y eliminar (CRUD completo).
 */
@RestController
@RequestMapping("/api/marcas")
public class MarcaController {

    private final MarcaService marcaService;

    public MarcaController(MarcaService marcaService) {
        this.marcaService = marcaService;
    }

    /** GET /api/marcas - lista todas las marcas. */
    @GetMapping
    public ResponseEntity<List<MarcaResponse>> listar() {
        return ResponseEntity.ok(marcaService.listar());
    }

    /** GET /api/marcas/{id} - consulta una marca por id. */
    @GetMapping("/{id}")
    public ResponseEntity<MarcaResponse> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(marcaService.obtenerPorId(id));
    }

    /** POST /api/marcas - crea una marca nueva. */
    @PostMapping
    public ResponseEntity<MarcaResponse> crear(@Valid @RequestBody MarcaRequest request) {
        MarcaResponse creada = marcaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    /** PUT /api/marcas/{id} - actualiza una marca existente. */
    @PutMapping("/{id}")
    public ResponseEntity<MarcaResponse> actualizar(@PathVariable Integer id,
                                                      @Valid @RequestBody MarcaRequest request) {
        return ResponseEntity.ok(marcaService.actualizar(id, request));
    }

    /** DELETE /api/marcas/{id} - elimina una marca. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        marcaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

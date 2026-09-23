package com.buildzone.api;

import java.math.BigDecimal;

import com.buildzone.api.model.PlanSuscripcion;
import com.buildzone.api.model.Usuario;
import com.buildzone.api.model.enums.Rol;

/** Objetos de prueba reutilizables por las pruebas unitarias. */
public final class Fixtures {

    private Fixtures() {
    }

    public static Usuario usuario(Long id, String username, Rol rol) {
        Usuario u = new Usuario("Nombre" + id, "Apellido" + id, username, username + "@buildzone.com", "HASH-" + id);
        u.setId(id);
        u.setRol(rol);
        return u;
    }

    public static PlanSuscripcion plan(Long id, String nombre, int duracionDias, boolean activo) {
        PlanSuscripcion p = new PlanSuscripcion(nombre, "Plan " + nombre, new BigDecimal("29900"), duracionDias);
        p.setId(id);
        p.setActivo(activo);
        return p;
    }
}

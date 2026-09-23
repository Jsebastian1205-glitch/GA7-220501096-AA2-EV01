package com.buildzone.api.model.enums;

/**
 * Roles de la aplicacion. Spring Security los usa como autoridades
 * con el prefijo {@code ROLE_} (por ejemplo {@code ROLE_ADMIN}).
 */
public enum Rol {
    /** Usuario registrado: consulta el catalogo y gestiona su suscripcion. */
    USUARIO,
    /** Administrador: gestiona usuarios, planes, suscripciones y catalogo. */
    ADMIN
}

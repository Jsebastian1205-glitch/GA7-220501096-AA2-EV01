package com.buildzone.api.model.enums;

/** Ciclo de vida de una suscripcion. */
public enum EstadoSuscripcion {
    /** Vigente: la fecha de fin aun no ha pasado y no fue cancelada. */
    ACTIVA,
    /** El usuario (o un administrador) la cancelo antes de su fin. */
    CANCELADA,
    /** Llego a su fecha de fin sin ser cancelada. */
    VENCIDA
}

package com.buildzone.api.exception;

/**
 * Usuario/correo o contrasena incorrectos al iniciar sesion. Se traduce
 * a HTTP 401. El mensaje no revela cual de los dos datos fallo, para no
 * facilitar la enumeracion de cuentas.
 */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Credenciales invalidas. Verifica tu correo/usuario y contrasena.");
    }
}

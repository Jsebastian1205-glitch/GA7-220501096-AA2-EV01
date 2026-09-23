package com.buildzone.api.model;

import java.time.LocalDateTime;

import com.buildzone.api.model.enums.EstadoUsuario;
import com.buildzone.api.model.enums.Rol;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Cuenta de usuario de la aplicacion web.
 * <p>
 * Se mapea a la tabla {@code usuario_cuenta} (y no a la tabla
 * {@code usuario} que ya existe en la base {@code buildzone}) para no
 * alterar la estructura previa. La contrasena se guarda SIEMPRE como
 * hash BCrypt, nunca en texto plano.
 */
@Entity
@Table(name = "usuario_cuenta")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 60)
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 60)
    private String apellido;

    @Column(name = "username", nullable = false, unique = true, length = 40)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 120)
    private String email;

    /** Hash BCrypt de la contrasena. */
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 20)
    private Rol rol = Rol.USUARIO;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoUsuario estado = EstadoUsuario.ACTIVO;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    /** Constructor vacio requerido por JPA. */
    public Usuario() {
    }

    public Usuario(String nombre, String apellido, String username, String email, String passwordHash) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    /** Asigna la fecha de registro justo antes de insertar el registro. */
    @PrePersist
    void antesDeInsertar() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
    }

    /** @return true si la cuenta puede iniciar sesion y usar la API. */
    public boolean estaActivo() {
        return estado == EstadoUsuario.ACTIVO;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public EstadoUsuario getEstado() {
        return estado;
    }

    public void setEstado(EstadoUsuario estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}

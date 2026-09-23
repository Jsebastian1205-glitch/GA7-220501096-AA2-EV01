package com.buildzone.api.integracion;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

/**
 * Pruebas de integracion de la API completa (controlador + seguridad +
 * servicio + JPA) sobre H2 en memoria. Verifican el contrato HTTP que
 * consume el front-end y, sobre todo, las reglas de seguridad por rol.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegracionTest {

    private static final AtomicInteger SECUENCIA = new AtomicInteger();

    @Autowired
    private MockMvc mvc;

    // ── Utilidades ──

    /** Registra un usuario nuevo con datos unicos y devuelve la respuesta JSON. */
    private String registrarUsuario() throws Exception {
        String username = "usuario" + SECUENCIA.incrementAndGet();
        String cuerpo = """
                {"nombre":"Prueba","apellido":"Integracion","username":"%s",
                 "email":"%s@test.com","password":"Secreta123"}
                """.formatted(username, username);
        return mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }

    private String tokenDeUsuarioNuevo() throws Exception {
        return JsonPath.read(registrarUsuario(), "$.token");
    }

    private String tokenAdmin() throws Exception {
        String respuesta = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identificador\":\"admin@buildzone.com\",\"password\":\"Admin1234\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(respuesta, "$.token");
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    // ── Autenticacion ──

    @Test
    @DisplayName("Registro valido devuelve 201, token Bearer y rol USUARIO")
    void registroValido() throws Exception {
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content("""
                        {"nombre":"Laura","apellido":"Rios","username":"laura.rios",
                         "email":"laura@test.com","password":"Secreta123"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.usuario.rol").value("USUARIO"))
                .andExpect(jsonPath("$.usuario.password").doesNotExist())
                .andExpect(jsonPath("$.usuario.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("Registro con datos invalidos devuelve 400 con la lista de detalles")
    void registroInvalido() throws Exception {
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content("""
                        {"nombre":"","apellido":"Rios","username":"x","email":"no-es-correo","password":"123"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detalles.length()", greaterThanOrEqualTo(4)));
    }

    @Test
    @DisplayName("Registro con correo repetido devuelve 409")
    void registroDuplicado() throws Exception {
        String cuerpo = """
                {"nombre":"Admin","apellido":"Copia","username":"otroadmin",
                 "email":"admin@buildzone.com","password":"Secreta123"}
                """;
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").isNotEmpty());
    }

    @Test
    @DisplayName("Login con contrasena incorrecta devuelve 401")
    void loginIncorrecto() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identificador\":\"admin\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").isNotEmpty());
    }

    // ── Seguridad por ruta ──

    @Test
    @DisplayName("Ruta protegida sin token devuelve 401 en formato JSON")
    void perfilSinToken() throws Exception {
        mvc.perform(get("/api/usuarios/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.ruta").value("/api/usuarios/me"));
    }

    @Test
    @DisplayName("Token alterado devuelve 401")
    void tokenAlterado() throws Exception {
        mvc.perform(get("/api/usuarios/me").header(HttpHeaders.AUTHORIZATION, "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Con token valido se consulta y actualiza el propio perfil")
    void perfilPropio() throws Exception {
        String token = tokenDeUsuarioNuevo();

        mvc.perform(get("/api/usuarios/me").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("USUARIO"));

        mvc.perform(put("/api/usuarios/me").header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Nuevo\",\"apellido\":\"Nombre\",\"email\":\"nuevo.correo@test.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nuevo"))
                .andExpect(jsonPath("$.email").value("nuevo.correo@test.com"));
    }

    @Test
    @DisplayName("Un USUARIO recibe 403 en rutas de administrador")
    void usuarioSinPermisosDeAdmin() throws Exception {
        String token = tokenDeUsuarioNuevo();

        mvc.perform(get("/api/usuarios").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        mvc.perform(post("/api/planes").header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Pirata\",\"precio\":0,\"duracionDias\":30}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/marcas").header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"MarcaPirata\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("El ADMIN lista usuarios, crea planes y marcas")
    void adminGestiona() throws Exception {
        String token = tokenAdmin();

        mvc.perform(get("/api/usuarios").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)));

        mvc.perform(post("/api/planes").header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Estudiante\",\"descripcion\":\"Tarifa SENA\",\"precio\":9900,\"duracionDias\":30}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.activo").value(true));

        mvc.perform(post("/api/marcas").header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"MarcaDePrueba\",\"descripcion\":\"Integracion\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Catalogo y planes activos son publicos (GET sin token)")
    void consultasPublicas() throws Exception {
        mvc.perform(get("/api/productos")).andExpect(status().isOk());
        mvc.perform(get("/api/marcas")).andExpect(status().isOk());
        mvc.perform(get("/api/categorias")).andExpect(status().isOk());
        mvc.perform(get("/api/planes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Gratuito"));
    }

    @Test
    @DisplayName("Escritura en el catalogo sin token devuelve 401")
    void catalogoSinToken() throws Exception {
        mvc.perform(post("/api/marcas").contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"X\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Una cuenta desactivada por el ADMIN no puede entrar y su token deja de servir")
    void cuentaDesactivada() throws Exception {
        String registro = registrarUsuario();
        String tokenUsuario = JsonPath.read(registro, "$.token");
        String username = JsonPath.read(registro, "$.usuario.username");
        long id = ((Number) JsonPath.read(registro, "$.usuario.id")).longValue();

        mvc.perform(patch("/api/usuarios/" + id + "/estado").header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin()))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"INACTIVO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("INACTIVO"));

        mvc.perform(get("/api/usuarios/me").header(HttpHeaders.AUTHORIZATION, bearer(tokenUsuario)))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identificador\":\"" + username + "\",\"password\":\"Secreta123\"}"))
                .andExpect(status().isForbidden());
    }

    // ── Suscripciones ──

    @Test
    @DisplayName("Flujo completo: suscribirse, consultar activa, cancelar e historial")
    void flujoSuscripcion() throws Exception {
        String token = tokenDeUsuarioNuevo();
        String planes = mvc.perform(get("/api/planes")).andReturn().getResponse().getContentAsString();
        long planId = ((Number) JsonPath.read(planes, "$[1].id")).longValue();

        mvc.perform(get("/api/suscripciones/me/activa").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNoContent());

        String creada = mvc.perform(post("/api/suscripciones").header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"planId\":" + planId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("ACTIVA"))
                .andExpect(jsonPath("$.plan.id").value(planId))
                .andReturn().getResponse().getContentAsString();
        long suscripcionId = ((Number) JsonPath.read(creada, "$.id")).longValue();

        mvc.perform(get("/api/suscripciones/me/activa").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(suscripcionId));

        mvc.perform(delete("/api/suscripciones/" + suscripcionId).header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/suscripciones/me/activa").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/suscripciones/me").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("CANCELADA"));
    }

    @Test
    @DisplayName("Un USUARIO no puede cancelar la suscripcion de otro (403)")
    void cancelarSuscripcionAjena() throws Exception {
        String tokenDuenio = tokenDeUsuarioNuevo();
        String tokenIntruso = tokenDeUsuarioNuevo();
        String planes = mvc.perform(get("/api/planes")).andReturn().getResponse().getContentAsString();
        long planId = ((Number) JsonPath.read(planes, "$[0].id")).longValue();

        String creada = mvc.perform(post("/api/suscripciones").header(HttpHeaders.AUTHORIZATION, bearer(tokenDuenio))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"planId\":" + planId + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long suscripcionId = ((Number) JsonPath.read(creada, "$.id")).longValue();

        mvc.perform(delete("/api/suscripciones/" + suscripcionId).header(HttpHeaders.AUTHORIZATION, bearer(tokenIntruso)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Ruta inexistente devuelve 404 (no 500)")
    void rutaInexistente() throws Exception {
        mvc.perform(get("/api/no-existe").header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin())))
                .andExpect(status().isNotFound());
    }
}

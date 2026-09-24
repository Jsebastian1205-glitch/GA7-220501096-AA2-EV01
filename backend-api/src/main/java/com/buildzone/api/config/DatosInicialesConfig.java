package com.buildzone.api.config;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.buildzone.api.model.PlanSuscripcion;
import com.buildzone.api.model.Usuario;
import com.buildzone.api.model.enums.Rol;
import com.buildzone.api.repository.PlanSuscripcionRepository;
import com.buildzone.api.repository.UsuarioRepository;

/**
 * Siembra los datos minimos para que la aplicacion sea usable desde el
 * primer arranque, en cualquier ambiente:
 * <ul>
 *   <li>Un usuario ADMIN (credenciales en application.properties /
 *       variables de entorno), solo si ese correo aun no existe.</li>
 *   <li>Los planes Gratuito y Premium, solo si no hay ningun plan.</li>
 * </ul>
 * Es idempotente: si los datos ya existen no hace nada.
 */
@Component
@Order(1)
public class DatosInicialesConfig implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DatosInicialesConfig.class);

    private final UsuarioRepository usuarioRepository;
    private final PlanSuscripcionRepository planRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminUsername;
    private final String adminPassword;

    public DatosInicialesConfig(UsuarioRepository usuarioRepository,
                                PlanSuscripcionRepository planRepository,
                                PasswordEncoder passwordEncoder,
                                @Value("${buildzone.admin.email}") String adminEmail,
                                @Value("${buildzone.admin.username}") String adminUsername,
                                @Value("${buildzone.admin.password}") String adminPassword) {
        this.usuarioRepository = usuarioRepository;
        this.planRepository = planRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminUsername = adminUsername;
        // Si la variable de entorno llega vacia se usa la contrasena por defecto
        // (y se advierte en el log) en lugar de crear un admin sin contrasena.
        if (adminPassword == null || adminPassword.isBlank()) {
            LOG.warn("buildzone.admin.password esta vacio: se usa la contrasena por defecto. Cambiela.");
            this.adminPassword = "Admin1234";
        } else {
            this.adminPassword = adminPassword;
        }
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        crearAdministradorSiNoExiste();
        crearPlanesSiNoHay();
    }

    private void crearAdministradorSiNoExiste() {
        if (usuarioRepository.existsByEmailIgnoreCase(adminEmail)
                || usuarioRepository.existsByUsernameIgnoreCase(adminUsername)) {
            return;
        }
        Usuario admin = new Usuario("Administrador", "BuildZone", adminUsername, adminEmail,
                passwordEncoder.encode(adminPassword));
        admin.setRol(Rol.ADMIN);
        usuarioRepository.save(admin);
        LOG.info("Usuario administrador inicial creado: {} (cambie la contrasena tras el primer ingreso)",
                adminEmail);
    }

    private void crearPlanesSiNoHay() {
        if (planRepository.count() > 0) {
            return;
        }
        planRepository.save(new PlanSuscripcion("Gratuito",
                "Acceso basico al catalogo y al comparador de componentes.", BigDecimal.ZERO, 3650));
        planRepository.save(new PlanSuscripcion("Premium",
                "Comparaciones ilimitadas, alertas de precio y soporte prioritario.", new BigDecimal("29900"), 30));
        planRepository.save(new PlanSuscripcion("Premium Anual",
                "Todo lo de Premium con dos meses de descuento.", new BigDecimal("299000"), 365));
        LOG.info("Planes de suscripcion iniciales creados.");
    }
}

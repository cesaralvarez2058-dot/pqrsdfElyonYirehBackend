package com.elyonyireh.pqrsdfelyonyirehbackend.config;

import com.elyonyireh.pqrsdfelyonyirehbackend.models.Sede;
import com.elyonyireh.pqrsdfelyonyirehbackend.models.Usuario;
import com.elyonyireh.pqrsdfelyonyirehbackend.repositories.SedeRepository;
import com.elyonyireh.pqrsdfelyonyirehbackend.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SedeRepository sedeRepository;

    @Override
    public void run(String... args) throws Exception {
        // ─── 1. Super Admin ───────────────────────────────────────────────
        String superAdminEmail = "superadmin@elyonyireh.com";

        if (!usuarioRepository.existsByEmail(superAdminEmail)) {
            Usuario superAdmin = new Usuario();
            superAdmin.setNombre("Super");
            superAdmin.setApellidos("Administrador");
            superAdmin.setEmail(superAdminEmail);
            superAdmin.setPassword(passwordEncoder.encode("Admin#2026"));
            superAdmin.setRol(Usuario.Rol.SUPER_ADMIN);
            superAdmin.setActivo(true);

            usuarioRepository.save(superAdmin);
            System.out.println("==================================================");
            System.out.println(" Se ha creado el usuario SUPER_ADMIN por defecto: ");
            System.out.println(" Email: " + superAdminEmail);
            System.out.println(" Password: Admin#2026 ");
            System.out.println("==================================================");
        }

        // ─── 2. Sedes ─────────────────────────────────────────────────────
        inicializarSede(
            "Fundación Elyon Yireh - Cartagena",
            "Cartagena",
            "Sede principal ubicada en la ciudad de Cartagena de Indias, Bolívar. " +
            "Ofrece programas académicos y servicios comunitarios en la región Caribe.",
            "Cartagena de Indias, Bolívar",
            "+57 (605) 123-4567",
            "cartagena@elyonyireh.edu.co"
        );

        inicializarSede(
            "Fundación Elyon Yireh - Medellín",
            "Medellín",
            "Sede ubicada en la ciudad de Medellín, Antioquia. " +
            "Atiende a la comunidad de la región Andina con programas especializados.",
            "Medellín, Antioquia",
            "+57 (604) 765-4321",
            "medellin@elyonyireh.edu.co"
        );
    }

    private void inicializarSede(String nombre, String ciudad, String descripcion,
                                  String direccion, String telefono, String email) {
        if (!sedeRepository.existsByNombre(nombre)) {
            Sede sede = new Sede();
            sede.setNombre(nombre);
            sede.setCiudad(ciudad);
            sede.setDescripcion(descripcion);
            sede.setDireccion(direccion);
            sede.setTelefono(telefono);
            sede.setEmailSede(email);
            sede.setActiva(true);
            sedeRepository.save(sede);
            System.out.println("✔ Sede creada: " + nombre);
        }
    }
}

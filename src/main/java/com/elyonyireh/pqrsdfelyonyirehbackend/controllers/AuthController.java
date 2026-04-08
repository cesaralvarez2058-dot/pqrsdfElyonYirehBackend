package com.elyonyireh.pqrsdfelyonyirehbackend.controllers;

import com.elyonyireh.pqrsdfelyonyirehbackend.dtos.LoginRequestDTO;
import com.elyonyireh.pqrsdfelyonyirehbackend.dtos.LoginResponseDTO;
import com.elyonyireh.pqrsdfelyonyirehbackend.dtos.RegistrarAdminRequestDTO;
import com.elyonyireh.pqrsdfelyonyirehbackend.models.Usuario;
import com.elyonyireh.pqrsdfelyonyirehbackend.repositories.UsuarioRepository;
import com.elyonyireh.pqrsdfelyonyirehbackend.services.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            if (authentication.isAuthenticated()) {
                Usuario usuario = usuarioRepository.findByEmail(request.getEmail()).orElseThrow();
                String token = jwtService.generateToken(usuario.getEmail(), usuario.getRol().name());
                
                return ResponseEntity.ok(new LoginResponseDTO(
                        token,
                        usuario.getNombre(),
                        usuario.getApellidos(),
                        usuario.getEmail(),
                        usuario.getRol().name()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Credenciales inválidas"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Credenciales inválidas"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = auth.getName();
        Optional<Usuario> userOpt = usuarioRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            Usuario usuario = userOpt.get();
            return ResponseEntity.ok(Map.<String, Object>of(
                    "nombre", usuario.getNombre(),
                    "apellidos", usuario.getApellidos(),
                    "email", usuario.getEmail(),
                    "rol", usuario.getRol().name()
            ));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // ==== RUTAS SOLO PARA SUPER ADMIN ====

    @PostMapping("/admin")
    public ResponseEntity<?> registrarAdmin(@RequestBody RegistrarAdminRequestDTO dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "El email ya está en uso."));
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(dto.getNombre());
        nuevoUsuario.setApellidos(dto.getApellidos());
        nuevoUsuario.setEmail(dto.getEmail());
        nuevoUsuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        
        try {
            nuevoUsuario.setRol(Usuario.Rol.valueOf(dto.getRol()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Rol inválido."));
        }

        usuarioRepository.save(nuevoUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Administrador registrado exitosamente."));
    }

    @GetMapping("/admin")
    public ResponseEntity<List<Map<String, Object>>> listarAdmins() {
        List<Map<String, Object>> usuarios = usuarioRepository.findAll().stream()
                .map(u -> Map.<String, Object>of(
                        "id", u.getId(),
                        "nombre", u.getNombre(),
                        "apellidos", u.getApellidos(),
                        "email", u.getEmail(),
                        "rol", u.getRol().name()
                )).collect(Collectors.toList());
        return ResponseEntity.ok(usuarios);
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> eliminarAdmin(@PathVariable Long id) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Usuario no encontrado."));
        }
        
        Usuario usuario = usuarioOpt.get();
        if (usuario.getRol() == Usuario.Rol.SUPER_ADMIN && usuarioRepository.findAll().stream().filter(u -> u.getRol() == Usuario.Rol.SUPER_ADMIN).count() <= 1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "No se puede eliminar al último Super Administrador."));
        }

        usuarioRepository.delete(usuario);
        return ResponseEntity.ok(Map.of("message", "Usuario eliminado."));
    }
}

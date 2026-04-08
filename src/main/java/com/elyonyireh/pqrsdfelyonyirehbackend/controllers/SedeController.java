package com.elyonyireh.pqrsdfelyonyirehbackend.controllers;

import com.elyonyireh.pqrsdfelyonyirehbackend.models.Sede;
import com.elyonyireh.pqrsdfelyonyirehbackend.services.SedeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sedes")
public class SedeController {

    private final SedeService sedeService;

    @Autowired
    public SedeController(SedeService sedeService) {
        this.sedeService = sedeService;
    }

    /**
     * Endpoint PÚBLICO — el frontend lo consume sin autenticación
     * para mostrar el selector de sedes.
     */
    @GetMapping
    public ResponseEntity<List<Sede>> listarSedesActivas() {
        return ResponseEntity.ok(sedeService.listarSedesActivas());
    }

    @GetMapping("/todas")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<List<Sede>> listarTodasLasSedes() {
        return ResponseEntity.ok(sedeService.listarTodasLasSedes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sede> obtenerSede(@PathVariable Long id) {
        return sedeService.obtenerSede(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<Sede> crearSede(@RequestBody Sede sede) {
        Sede nuevaSede = sedeService.guardarSede(sede);
        return new ResponseEntity<>(nuevaSede, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<Sede> actualizarSede(@PathVariable Long id, @RequestBody Sede sedeDetails) {
        try {
            return ResponseEntity.ok(sedeService.actualizarSede(id, sedeDetails));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<Void> desactivarSede(@PathVariable Long id) {
        if (sedeService.desactivarSede(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

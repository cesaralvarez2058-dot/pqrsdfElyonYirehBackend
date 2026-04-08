package com.elyonyireh.pqrsdfelyonyirehbackend.controllers;

import com.elyonyireh.pqrsdfelyonyirehbackend.models.Area;
import com.elyonyireh.pqrsdfelyonyirehbackend.services.AreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/areas")
public class AreaController {

    private final AreaService areaService;

    @Autowired
    public AreaController(AreaService areaService) {
        this.areaService = areaService;
    }

    @GetMapping
    public ResponseEntity<List<Area>> listar() {
        return ResponseEntity.ok(areaService.listarAreas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Area> obtener(@PathVariable Long id) {
        return areaService.obtenerArea(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Area> crear(@RequestBody Area area) {
        Area nuevaArea = areaService.guardarArea(area);
        return new ResponseEntity<>(nuevaArea, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Area> actualizar(@PathVariable Long id, @RequestBody Area areaDetails) {
        try {
            return ResponseEntity.ok(areaService.actualizarArea(id, areaDetails));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (areaService.eliminarArea(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/page")
    public ResponseEntity<Page<Area>> listarPaginadas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(areaService.listarAreasPaginadas(pageable));
    }
}

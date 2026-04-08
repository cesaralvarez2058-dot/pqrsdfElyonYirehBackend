package com.elyonyireh.pqrsdfelyonyirehbackend.controllers;

import com.elyonyireh.pqrsdfelyonyirehbackend.dtos.DashboardEstadisticasDTO;
import com.elyonyireh.pqrsdfelyonyirehbackend.models.Area;
import com.elyonyireh.pqrsdfelyonyirehbackend.models.Pqrsdf;
import com.elyonyireh.pqrsdfelyonyirehbackend.repositories.AreaRepository;
import com.elyonyireh.pqrsdfelyonyirehbackend.repositories.PqrsdfRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final PqrsdfRepository pqrsdfRepository;
    private final AreaRepository areaRepository;

    @Autowired
    public DashboardController(PqrsdfRepository pqrsdfRepository, AreaRepository areaRepository) {
        this.pqrsdfRepository = pqrsdfRepository;
        this.areaRepository = areaRepository;
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<DashboardEstadisticasDTO> obtenerEstadisticas() {
        // 1. Total
        long total = pqrsdfRepository.count();

        // 2. Por estado
        List<String> estados = Arrays.asList(
            "SIN ASIGNAR", "EN REPARTO", "EN DEFINICION",
            "RESPONDIDO", "CERRADO", "RADICADO"
        );
        Map<String, Long> porEstado = new LinkedHashMap<>();
        for (String estado : estados) {
            porEstado.put(estado, pqrsdfRepository.countByEstado(estado));
        }

        // 3. Por área
        List<Area> areas = areaRepository.findAll();
        List<DashboardEstadisticasDTO.AreaConteoDTO> porArea = areas.stream()
            .map(area -> {
                long count = pqrsdfRepository.findAll().stream()
                    .filter(p -> p.getArea() != null && p.getArea().getId().equals(area.getId()))
                    .count();
                return new DashboardEstadisticasDTO.AreaConteoDTO(area.getNombre(), count);
            })
            .collect(Collectors.toList());

        // 4. Recientes (últimas 5)
        List<Pqrsdf> recientes = pqrsdfRepository.findTop5ByOrderByFechaRadicacionDesc();

        DashboardEstadisticasDTO dto = new DashboardEstadisticasDTO(total, porEstado, porArea, recientes);
        return ResponseEntity.ok(dto);
    }
}

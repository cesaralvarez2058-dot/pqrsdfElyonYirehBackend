package com.elyonyireh.pqrsdfelyonyirehbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.elyonyireh.pqrsdfelyonyirehbackend.models.Pqrsdf;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardEstadisticasDTO {
    private long totalPqrsdf;
    private Map<String, Long> porEstado;
    private List<AreaConteoDTO> porArea;
    private List<Pqrsdf> recientes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AreaConteoDTO {
        private String nombreArea;
        private long cantidad;
    }
}

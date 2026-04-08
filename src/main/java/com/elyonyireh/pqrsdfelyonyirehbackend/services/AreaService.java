package com.elyonyireh.pqrsdfelyonyirehbackend.services;

import com.elyonyireh.pqrsdfelyonyirehbackend.models.Area;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AreaService {
    List<Area> listarAreas();
    Optional<Area> obtenerArea(Long id);
    Area guardarArea(Area area);
    Area actualizarArea(Long id, Area areaDetails);
    boolean eliminarArea(Long id);
    Page<Area> listarAreasPaginadas(Pageable pageable);
}

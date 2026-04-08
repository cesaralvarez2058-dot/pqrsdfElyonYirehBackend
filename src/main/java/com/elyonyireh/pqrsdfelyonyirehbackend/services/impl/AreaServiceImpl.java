package com.elyonyireh.pqrsdfelyonyirehbackend.services.impl;

import com.elyonyireh.pqrsdfelyonyirehbackend.models.Area;
import com.elyonyireh.pqrsdfelyonyirehbackend.repositories.AreaRepository;
import com.elyonyireh.pqrsdfelyonyirehbackend.services.AreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class AreaServiceImpl implements AreaService {

    private final AreaRepository areaRepository;

    @Autowired
    public AreaServiceImpl(AreaRepository areaRepository) {
        this.areaRepository = areaRepository;
    }

    @Override
    public List<Area> listarAreas() {
        return areaRepository.findAll();
    }

    @Override
    public Optional<Area> obtenerArea(Long id) {
        return areaRepository.findById(id);
    }

    @Override
    public Area guardarArea(Area area) {
        return areaRepository.save(area);
    }

    @Override
    public Area actualizarArea(Long id, Area areaDetails) {
        return areaRepository.findById(id).map(existingArea -> {
            existingArea.setNombre(areaDetails.getNombre());
            existingArea.setResponsableNombre(areaDetails.getResponsableNombre());
            existingArea.setResponsableEmail(areaDetails.getResponsableEmail());
            existingArea.setResponsableCelular(areaDetails.getResponsableCelular());
            return areaRepository.save(existingArea);
        }).orElseThrow(() -> new RuntimeException("Area no encontrada con id " + id));
    }

    @Override
    public boolean eliminarArea(Long id) {
        return areaRepository.findById(id).map(area -> {
            areaRepository.delete(area);
            return true;
        }).orElse(false);
    }

    @Override
    public Page<Area> listarAreasPaginadas(Pageable pageable) {
        return areaRepository.findAll(pageable);
    }
}

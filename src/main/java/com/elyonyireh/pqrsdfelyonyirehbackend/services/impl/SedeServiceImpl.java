package com.elyonyireh.pqrsdfelyonyirehbackend.services.impl;

import com.elyonyireh.pqrsdfelyonyirehbackend.models.Sede;
import com.elyonyireh.pqrsdfelyonyirehbackend.repositories.SedeRepository;
import com.elyonyireh.pqrsdfelyonyirehbackend.services.SedeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SedeServiceImpl implements SedeService {

    private final SedeRepository sedeRepository;

    @Autowired
    public SedeServiceImpl(SedeRepository sedeRepository) {
        this.sedeRepository = sedeRepository;
    }

    @Override
    public List<Sede> listarSedesActivas() {
        return sedeRepository.findByActivaTrue();
    }

    @Override
    public List<Sede> listarTodasLasSedes() {
        return sedeRepository.findAll();
    }

    @Override
    public Optional<Sede> obtenerSede(Long id) {
        return sedeRepository.findById(id);
    }

    @Override
    public Sede guardarSede(Sede sede) {
        return sedeRepository.save(sede);
    }

    @Override
    public Sede actualizarSede(Long id, Sede sedeDetails) {
        return sedeRepository.findById(id).map(existingSede -> {
            existingSede.setNombre(sedeDetails.getNombre());
            existingSede.setCiudad(sedeDetails.getCiudad());
            existingSede.setDescripcion(sedeDetails.getDescripcion());
            existingSede.setDireccion(sedeDetails.getDireccion());
            existingSede.setTelefono(sedeDetails.getTelefono());
            existingSede.setEmailSede(sedeDetails.getEmailSede());
            existingSede.setActiva(sedeDetails.isActiva());
            return sedeRepository.save(existingSede);
        }).orElseThrow(() -> new RuntimeException("Sede no encontrada con id " + id));
    }

    @Override
    public boolean desactivarSede(Long id) {
        return sedeRepository.findById(id).map(sede -> {
            sede.setActiva(false);
            sedeRepository.save(sede);
            return true;
        }).orElse(false);
    }
}

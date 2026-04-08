package com.elyonyireh.pqrsdfelyonyirehbackend.services;

import com.elyonyireh.pqrsdfelyonyirehbackend.models.Sede;

import java.util.List;
import java.util.Optional;

public interface SedeService {
    List<Sede> listarSedesActivas();
    List<Sede> listarTodasLasSedes();
    Optional<Sede> obtenerSede(Long id);
    Sede guardarSede(Sede sede);
    Sede actualizarSede(Long id, Sede sedeDetails);
    boolean desactivarSede(Long id);
}

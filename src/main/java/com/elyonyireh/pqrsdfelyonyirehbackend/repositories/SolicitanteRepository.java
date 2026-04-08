package com.elyonyireh.pqrsdfelyonyirehbackend.repositories;

import com.elyonyireh.pqrsdfelyonyirehbackend.models.Solicitante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SolicitanteRepository extends JpaRepository<Solicitante, Long> {
    Optional<Solicitante> findByNumeroIdentificacion(String numeroIdentificacion);
}

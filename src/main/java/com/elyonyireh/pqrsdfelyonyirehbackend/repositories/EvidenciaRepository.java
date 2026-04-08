package com.elyonyireh.pqrsdfelyonyirehbackend.repositories;

import com.elyonyireh.pqrsdfelyonyirehbackend.models.Evidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvidenciaRepository extends JpaRepository<Evidencia, Long> {
    List<Evidencia> findByPqrsdfId(Long pqrsdfId);
}

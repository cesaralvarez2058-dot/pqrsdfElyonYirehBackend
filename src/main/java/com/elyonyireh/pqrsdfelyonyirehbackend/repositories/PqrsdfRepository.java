package com.elyonyireh.pqrsdfelyonyirehbackend.repositories;

import com.elyonyireh.pqrsdfelyonyirehbackend.models.Pqrsdf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PqrsdfRepository extends JpaRepository<Pqrsdf, Long> {
    Optional<Pqrsdf> findByNumeroRadicado(String numeroRadicado);
    long countByEstado(String estado);
    List<Pqrsdf> findTop5ByOrderByFechaRadicacionDesc();
    
    // Buscar PQRSD asignadas (en un estado dado, ej. 'EN REPARTO') cuya fecha de asignación es anterior a un límite y no se ha enviado recordatorio
    List<Pqrsdf> findByEstadoAndFechaAsignacionBeforeAndRecordatorioEnviadoFalse(String estado, java.time.LocalDateTime limitDate);
}

package com.elyonyireh.pqrsdfelyonyirehbackend.repositories;

import com.elyonyireh.pqrsdfelyonyirehbackend.models.Sede;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SedeRepository extends JpaRepository<Sede, Long> {
    List<Sede> findByActivaTrue();
    boolean existsByNombre(String nombre);
}

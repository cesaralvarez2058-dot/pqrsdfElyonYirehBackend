package com.elyonyireh.pqrsdfelyonyirehbackend.repositories;

import com.elyonyireh.pqrsdfelyonyirehbackend.models.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AreaRepository extends JpaRepository<Area, Long> {
}

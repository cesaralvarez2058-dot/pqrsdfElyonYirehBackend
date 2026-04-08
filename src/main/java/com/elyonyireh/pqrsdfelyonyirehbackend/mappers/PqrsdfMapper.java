package com.elyonyireh.pqrsdfelyonyirehbackend.mappers;

import com.elyonyireh.pqrsdfelyonyirehbackend.dtos.RadicarPqrsdfRequestDTO;
import com.elyonyireh.pqrsdfelyonyirehbackend.models.Pqrsdf;
import com.elyonyireh.pqrsdfelyonyirehbackend.models.Solicitante;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PqrsdfMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "radicados", ignore = true)
    Solicitante toSolicitante(RadicarPqrsdfRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "numeroRadicado", ignore = true)
    @Mapping(target = "fechaRadicacion", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "solicitante", ignore = true)
    @Mapping(target = "evidencias", ignore = true)
    @Mapping(target = "respuestaAdmin", ignore = true)
    @Mapping(target = "fechaRespuesta", ignore = true)
    @Mapping(target = "calificacionUsuario", ignore = true)
    @Mapping(target = "observacionUsuario", ignore = true)
    @Mapping(target = "area", ignore = true)
    Pqrsdf toPqrsdf(RadicarPqrsdfRequestDTO dto);
}

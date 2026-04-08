package com.elyonyireh.pqrsdfelyonyirehbackend.services;

import com.elyonyireh.pqrsdfelyonyirehbackend.dtos.RadicarPqrsdfRequestDTO;
import com.elyonyireh.pqrsdfelyonyirehbackend.dtos.RadicarPqrsdfResponseDTO;
import com.elyonyireh.pqrsdfelyonyirehbackend.dtos.ConsultaPublicaRequestDTO;
import com.elyonyireh.pqrsdfelyonyirehbackend.dtos.ConsultaPublicaResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.elyonyireh.pqrsdfelyonyirehbackend.dtos.CalificarPqrsdfRequestDTO;
import com.elyonyireh.pqrsdfelyonyirehbackend.models.Pqrsdf;

public interface PqrsdfService {
    RadicarPqrsdfResponseDTO radicarPqrsdf(RadicarPqrsdfRequestDTO requestDTO, List<MultipartFile> archivos);
    
    ConsultaPublicaResponseDTO consultarPublico(ConsultaPublicaRequestDTO requestDTO);
    
    RadicarPqrsdfResponseDTO calificarPqrsdfPublico(CalificarPqrsdfRequestDTO requestDTO);
    
    Page<Pqrsdf> listarPqrsdfs(Pageable pageable);
    
    Optional<Pqrsdf> obtenerPqrsdfPorId(Long id);
    
    java.util.Optional<com.elyonyireh.pqrsdfelyonyirehbackend.models.Evidencia> obtenerEvidenciaPorId(Long id);
    
    byte[] generarDocumentoWord(Long id);
    
    RadicarPqrsdfResponseDTO actualizarEstadoPqrsdf(Long id, String nuevoEstado);
    
    RadicarPqrsdfResponseDTO asignarArea(Long pqrsdfId, Long areaId);
    
    boolean eliminarPqrsdf(Long id);
}

package com.indra.asistencia.service;

import com.indra.asistencia.dto.*;
import com.indra.asistencia.models.Justificacion;

import java.time.LocalDate;
import java.util.List;

public interface IAsistenciaService {
    String registrarAsistencia(String username, String accion);
    List<AsistenciaResponseDto> getHistorial(String username);
    JustificacionResponseDto solicitarJustificacion(String username, JustificacionRequestDto dto);
    List<Justificacion> getJustificacionesPendientes();
    String aprobarJustificacion(Long id);
    Object reportePorRangoFechas(LocalDate desde, LocalDate hasta);
}
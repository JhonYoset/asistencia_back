package com.indra.asistencia.service;

import com.indra.asistencia.dto.JustificacionRequestDto;
import com.indra.asistencia.dto.JustificacionResponseDto;
import com.indra.asistencia.models.Justificacion;

import java.util.List;

public interface IJustificacionService {
    JustificacionResponseDto solicitarJustificacion(String username, JustificacionRequestDto dto);
    List<Justificacion> getJustificacionesPendientes();
    String aprobarJustificacion(Long id);
    List<Justificacion> getMisJustificaciones(String username);
}
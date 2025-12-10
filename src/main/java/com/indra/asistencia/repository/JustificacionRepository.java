package com.indra.asistencia.repository;

import com.indra.asistencia.models.Justificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JustificacionRepository extends JpaRepository<Justificacion, Long> {
    List<Justificacion> findByEstado(String estado);
}
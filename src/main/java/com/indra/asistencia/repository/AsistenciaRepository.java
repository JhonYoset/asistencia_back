package com.indra.asistencia.repository;

import com.indra.asistencia.models.Asistencia;
import com.indra.asistencia.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {
    Optional<Asistencia> findFirstByUsuarioAndFechaRegistroOrderByEntradaDesc(User usuario, LocalDate fecha);
}
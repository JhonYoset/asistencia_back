package com.indra.asistencia.repository;

import com.indra.asistencia.models.Asistencia;
import com.indra.asistencia.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    // ✅ JPQL QUERY (intenta primero esta)
    @Query("SELECT a FROM Asistencia a WHERE a.usuario = :usuario AND a.fechaRegistro = :fecha ORDER BY a.entrada DESC")
    List<Asistencia> findByUsuarioAndFechaRegistro(@Param("usuario") User usuario, @Param("fecha") LocalDate fecha);

    // ✅ NATIVE SQL QUERY (usar si la anterior falla)
    @Query(value = "SELECT * FROM ASISTENCIA WHERE user_id = :usuarioId AND TRUNC(fecha_registro) = :fecha ORDER BY entrada DESC", nativeQuery = true)
    List<Asistencia> findByUsuarioIdAndFechaNative(@Param("usuarioId") Long usuarioId, @Param("fecha") LocalDate fecha);

    // ✅ QUERY MÁS SIMPLE (sin ORDER BY)
    @Query("SELECT a FROM Asistencia a WHERE a.usuario.id = :usuarioId AND a.fechaRegistro = :fecha")
    List<Asistencia> findAllByUsuarioIdAndFecha(@Param("usuarioId") Long usuarioId, @Param("fecha") LocalDate fecha);

    // Query original (mantener por compatibilidad)
    Optional<Asistencia> findFirstByUsuarioAndFechaRegistroOrderByEntradaDesc(User usuario, LocalDate fecha);

    @Procedure(name = "sp_reporte_puntualidad_empleado")
    List<Object[]> reportePuntualidadEmpleado(
            @Param("p_username") String username,
            @Param("p_desde") LocalDate desde,
            @Param("p_hasta") LocalDate hasta
    );
    
    @Procedure(name = "sp_reporte_asistencia_rango")
    List<Object[]> reporteGeneralAsistencia(
            @Param("p_desde") LocalDate desde,
            @Param("p_hasta") LocalDate hasta
    );
}
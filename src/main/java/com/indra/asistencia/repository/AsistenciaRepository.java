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

    @Query("SELECT a FROM Asistencia a WHERE a.usuario.id = :usuarioId AND a.fechaRegistro = :fecha ORDER BY a.entrada DESC")
    List<Asistencia> findByUsuarioIdAndFecha(@Param("usuarioId") Long usuarioId, @Param("fecha") LocalDate fecha);

    @Query("SELECT a FROM Asistencia a WHERE a.usuario.id = :usuarioId AND a.fechaRegistro = :fecha ORDER BY a.id DESC")
    List<Asistencia> findByUsuarioIdAndFechaOrderByIdDesc(@Param("usuarioId") Long usuarioId, @Param("fecha") LocalDate fecha);

    Optional<Asistencia> findFirstByUsuarioAndFechaRegistroOrderByEntradaDesc(User usuario, LocalDate fecha);

    @Query(value = "SELECT * FROM ASISTENCIA WHERE user_id = :usuarioId AND fecha_registro = :fecha ORDER BY entrada DESC", nativeQuery = true)
    List<Asistencia> findByUsuarioIdAndFechaNative(@Param("usuarioId") Long usuarioId, @Param("fecha") LocalDate fecha);

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
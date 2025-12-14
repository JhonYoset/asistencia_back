package com.indra.asistencia.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ASISTENCIA")
@Data 
@Builder 
@AllArgsConstructor 
@NoArgsConstructor
public class Asistencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User usuario;

    private LocalDateTime entrada;
    private LocalDateTime salida;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDate fechaRegistro;
    
    private String estado;
    
    @PrePersist
    protected void onCreate() {
        // ✅ ASEGURAR QUE SIEMPRE TENGA FECHA
        if (fechaRegistro == null) {
            fechaRegistro = LocalDate.now();
        }
        // ✅ ASEGURAR QUE SIEMPRE TENGA ESTADO
        if (estado == null) {
            estado = salida == null ? "EN_OFICINA" : "COMPLETADO";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        // ✅ ASEGURAR QUE TENGA FECHA AL ACTUALIZAR
        if (fechaRegistro == null) {
            fechaRegistro = LocalDate.now();
        }
        // ✅ ACTUALIZAR ESTADO SEGÚN SALIDA
        if (salida != null && "EN_OFICINA".equals(estado)) {
            estado = "COMPLETADO";
        }
    }
}
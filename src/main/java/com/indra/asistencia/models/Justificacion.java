package com.indra.asistencia.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "JUSTIFICACION")
@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class Justificacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User usuario;

    private LocalDate fecha;
    private String tipo; // "TARDANZA" o "AUSENCIA"
    private String motivo;
    private String estado = "PENDIENTE"; // PENDIENTE, APROBADO, RECHAZADO
    private LocalDateTime fechaSolicitud = LocalDateTime.now();
}

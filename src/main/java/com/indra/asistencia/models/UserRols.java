package com.indra.asistencia.models;




import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_roles")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRols {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)    
    private Long id;

 
    @ManyToOne 
    @JoinColumn(name = "role_id")
    Role role;

    @ManyToOne 
    @JoinColumn(name = "user_id")
    User user;
}

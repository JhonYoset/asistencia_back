package com.indra.asistencia.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.indra.asistencia.models.Role;

public interface IRoleRepository extends JpaRepository<Role, Long> {


}

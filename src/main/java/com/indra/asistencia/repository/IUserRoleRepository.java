package com.indra.asistencia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.ResponseBody;

import com.indra.asistencia.models.UserRols;

@Repository
public interface IUserRoleRepository extends JpaRepository<UserRols, Long> {

    @Query("SELECT ur FROM UserRols ur WHERE ur.user.id = :userId")
    List<UserRols> getRolesByUser(@Param("userId") Long userId);

}

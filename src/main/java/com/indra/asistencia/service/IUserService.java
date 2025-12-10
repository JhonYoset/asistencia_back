package com.indra.asistencia.service;

import com.indra.asistencia.models.User;

public interface IUserService {

    User getByUserName(String username);

}

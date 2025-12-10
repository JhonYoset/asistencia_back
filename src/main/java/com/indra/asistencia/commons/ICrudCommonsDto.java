package com.indra.asistencia.commons;

public interface ICrudCommonsDto<DTOReq, DTORes, ID> {

    public DTORes save(DTOReq entity);
    public DTORes update(ID id, DTOReq entity);
    public DTORes findById(ID id);
    public DTORes delete(ID id);    

}

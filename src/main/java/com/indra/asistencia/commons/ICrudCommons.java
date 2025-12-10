package com.indra.asistencia.commons;

public interface ICrudCommons<T, ID> {

    public T save(T entity);
    public T update(ID id, T entity);
    public T findById(ID id);
    public T delete(ID id);    

}

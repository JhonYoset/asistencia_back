package com.indra.asistencia.commons;

import org.springframework.data.domain.PageImpl;

public interface IPaginationCommons<T> {

    public PageImpl<T> getPagination(PaginationModel paginationModel);

}

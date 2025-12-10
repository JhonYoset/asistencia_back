package com.indra.asistencia.commons;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaginationModel {

    private Integer pageNumber;
    private Integer rowsPerPage;

    private List<FilterModel> filters;
    private List<SortModel> sorts;    

}

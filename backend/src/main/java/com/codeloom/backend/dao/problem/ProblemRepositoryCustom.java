package com.codeloom.backend.dao.problem;

import com.codeloom.backend.dto.ProblemFilters;
import com.codeloom.backend.dto.ProblemListDto;
import java.util.List;

public interface ProblemRepositoryCustom {
    List<ProblemListDto> findProblemListDtos(ProblemFilters filters);
}

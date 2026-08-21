package com.codeloom.backend.dao.problem;

import com.codeloom.backend.dto.*;
import java.util.List;

public interface ProblemRepositoryCustom {
    List<ProblemListDto> findProblemListDtos(ProblemFilters filters);
}

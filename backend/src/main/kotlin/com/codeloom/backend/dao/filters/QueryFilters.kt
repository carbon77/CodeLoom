package com.codeloom.backend.dao.filters

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource

data class QueryFilters(
    val clauses: List<String>,
    val params: MapSqlParameterSource,
)


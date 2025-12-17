package com.codeloom.backend.dao.filters

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource

class FilterBuilder {
    private val filters = mutableListOf<QueryFilter>()

    fun addFilter(filter: QueryFilter): FilterBuilder {
        if (filter.isValid()) {
            filters.add(filter)
        }
        return this
    }

    fun build(): QueryFilters {
        val clauses = mutableListOf<String>()
        val params = MapSqlParameterSource()

        for (filter in filters) {
            clauses.add(filter.getClause())
            params.addValues(filter.getParams())
        }

        return QueryFilters(clauses, params)
    }
}

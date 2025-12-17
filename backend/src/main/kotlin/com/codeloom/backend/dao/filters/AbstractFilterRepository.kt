package com.codeloom.backend.dao.filters

import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

abstract class AbstractFilterRepository(
    protected val jdbcTemplate: NamedParameterJdbcTemplate,
) {
    protected inline fun <reified T> queryForList(
        tableName: String,
        filters: QueryFilters,
        columns: List<String>? = null,
        rowMapper: RowMapper<T>? = null,
    ): List<T> {
        val whereClause =
            if (filters.clauses.isEmpty()) ""
            else filters.clauses.joinToString(" AND ") { "($it)" }
        val columnList = columns?.joinToString(", ") ?: "*"

        val sql = """
            SELECT $columnList
            FROM $tableName p
            $whereClause
        """.trimIndent()

        return jdbcTemplate.query(sql, filters.params, rowMapper ?: BeanPropertyRowMapper<T>(T::class.java))
    }
}
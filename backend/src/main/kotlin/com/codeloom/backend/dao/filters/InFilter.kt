package com.codeloom.backend.dao.filters

class InFilter<T>(
    private val column: String,
    private val items: List<T>?,
) : QueryFilter {
    override fun isValid() = items != null
    override fun getClause() = "$column IN (:${column}_items)"
    override fun getParams() = mapOf(
        "${column}_items" to items!!.map { it.toString() },
    )
}


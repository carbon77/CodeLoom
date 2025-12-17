package com.codeloom.backend.dao.filters

interface QueryFilter {
    fun isValid(): Boolean
    fun getClause(): String
    fun getParams(): Map<String, Any>
}


package com.kangaroo.statements

import com.kangaroo.DatabaseManager

/**
 * Query abstract class.
 */
abstract class Query
{
    abstract var sqlQuery : String
    abstract var databaseManager : DatabaseManager

    val tableName by lazy {
        databaseManager.reflectClass.tableName
    }

    val properties by lazy {
        databaseManager.reflectClass.properties
    }

    abstract fun setDatabaseManager(databaseManager: DatabaseManager) : Query

    abstract fun execute()

    fun formatQuery(query : String, length : Int = 2) : String {
        // removes the last comma
        return query.take(query.length - length)
    }
}
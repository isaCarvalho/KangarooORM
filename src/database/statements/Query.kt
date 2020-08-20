package database.statements

import database.DatabaseManager

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

    val formatQuery = { query : String ->
        // removes the last comma
        query.take(query.length - 2)
    }
}
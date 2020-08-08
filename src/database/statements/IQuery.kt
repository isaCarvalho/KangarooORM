package database.statements

import database.DatabaseManager

/**
 * Query Interface.
 */
interface IQuery
{
    var sqlQuery : String
    var databaseManager : DatabaseManager

    fun setDatabaseManager(databaseManager: DatabaseManager) : IQuery

    fun execute()
}
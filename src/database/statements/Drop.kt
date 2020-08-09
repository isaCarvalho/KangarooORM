package database.statements

import database.DatabaseExecutor
import database.DatabaseManager

class Drop : IQuery {

    override var sqlQuery: String = ""
    override lateinit var databaseManager: DatabaseManager

    override fun setDatabaseManager(databaseManager: DatabaseManager): Drop {
        this.databaseManager = databaseManager
        return this
    }

    /**
     * Method that drops a table and a sequence
     */
    fun dropTableAndSequence() : Drop {
        val sequenceName = databaseManager.tableName + "_seq"

        sqlQuery = "DROP TABLE IF EXISTS ${databaseManager.tableName};\n" +
                "DROP SEQUENCE IF EXISTS $sequenceName;"

        return this
    }

    fun dropTable(tableName : String) : Drop {
        sqlQuery += "DROP TABLE IF EXISTS $tableName;"

        return this
    }

    fun dropSequence(sequenceName : String) : Drop {
        sqlQuery += "DROP SEQUENCE IF EXISTS $sequenceName;"

        return this
    }

    override fun execute() {
        DatabaseExecutor.executeOperation(sqlQuery)
    }
}
package database.statements

import database.DatabaseExecutor
import database.DatabaseManager

class Drop : IQuery {

    override var sqlQuery: String = ""

    /**
     * Method that drops a table and a sequence
     * @param databaseManager
     */
    fun dropTableAndSequence(databaseManager: DatabaseManager) : Drop {
        val sequenceName = databaseManager.tableName + "_seq"

        sqlQuery = "DROP TABLE IF EXISTS ${databaseManager.tableName};\n" +
                "DROP SEQUENCE IF EXISTS $sequenceName;"

        return this
    }

    override fun execute() {
        DatabaseExecutor.executeOperation(sqlQuery)
    }
}
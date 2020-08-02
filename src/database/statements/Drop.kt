package database.statements

import database.DatabaseExecutor
import database.DatabaseManager

class Drop(private val databaseManager: DatabaseManager) {

    /**
     * Method that drops a table and a sequence
     */
    fun dropTableAndSequence() {
        val sequenceName = databaseManager.tableName + "_seq"

        val sqlQuery = "DROP TABLE IF EXISTS ${databaseManager.tableName};\n" +
                "DROP SEQUENCE IF EXISTS $sequenceName;"

        DatabaseExecutor.executeOperation(sqlQuery)
    }
}
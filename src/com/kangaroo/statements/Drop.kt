package com.kangaroo.statements

import com.kangaroo.annotations.ManyToMany
import com.kangaroo.database.DatabaseExecutor
import com.kangaroo.database.DatabaseManager

class Drop : Query() {

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
        val sequenceName = tableName + "_seq"

        properties.forEach {
            val relation = it.relation
            if (relation is ManyToMany) {
                relation as ManyToMany
                dropTable(relation.foreignKey.referencedTable)
                dropSequence("${relation.foreignKey.referencedTable}_seq")
            }
        }

        sqlQuery += "\nDROP TABLE IF EXISTS $tableName;\n" +
                "DROP SEQUENCE IF EXISTS $sequenceName;"

        return this
    }

    fun dropTable(tableName : String) : Drop {
        sqlQuery += "\nDROP TABLE IF EXISTS $tableName;"

        return this
    }

    fun dropSequence(sequenceName : String) : Drop {
        sqlQuery += "\nDROP SEQUENCE IF EXISTS $sequenceName;"

        return this
    }

    override fun execute() {
        DatabaseExecutor.executeOperation(sqlQuery)
    }
}
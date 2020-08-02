package database.statements

import database.DatabaseExecutor
import database.DatabaseManager

class Select
{
    /**
     * Method that selects the values from the database.
     * @param where
     * @return unit
     */
    fun select(databaseManager: DatabaseManager, where : String? = null) : MutableMap<Int, MutableMap<String, String>> {
        // initiates the query with the select statement
        var sqlQuery = "SELECT "

        // for each property, puts its name in the select's fields.
        databaseManager.propertiesList.forEach {
            sqlQuery += it.name
            sqlQuery += if (databaseManager.propertiesList.indexOf(it) == databaseManager.propertiesList.size - 1)
                " "
            else
                ", "
        }

        // appends the from statement
        sqlQuery += "FROM ${databaseManager.tableName}"

        // appends the where statement
        if (where != null) {
            sqlQuery += " $where"
        }

        // executes the query and puts the result inside of a mutable map
        val result = DatabaseExecutor.execute(sqlQuery)
        val resultMap = mutableMapOf<Int, MutableMap<String, String>>()

        var index = 0
        while (result!!.next()) {
            val map = mutableMapOf<String, String>()
            databaseManager.propertiesList.forEach {
                val value = result.getString(it.name)
                map[it.name] = value
            }

            resultMap[index] = map
            index++
        }

        // returns the result converted to a mutable map
        return resultMap
    }
}
package com.kangaroo.statements

import com.kangaroo.DatabaseHelper.checkTypes
import com.kangaroo.DatabaseHelper.getMappedPropertyOrNull
import com.kangaroo.DatabaseHelper.getPrimaryKeyOrNull
import com.kangaroo.DatabaseExecutor
import com.kangaroo.DatabaseManager
import kotlin.reflect.KMutableProperty1

class Update : Query() {

    override var sqlQuery: String = ""
    override lateinit var databaseManager: DatabaseManager

    override fun setDatabaseManager(databaseManager: DatabaseManager): Update {
        this.databaseManager = databaseManager
        return this
    }

    /**
     * Method that update a entity in the database
     * @param entity
     */
    fun update(entity : Any) : Update {
        // initiates the query with the update statement
        sqlQuery = "UPDATE $tableName SET "

        // for each member, sets the new value
        databaseManager.reflectClass.members.forEach {
            val property = getMappedPropertyOrNull(it.name, properties)

            if (property != null) {
                val prop = it as KMutableProperty1<Any, *>
                val value = prop.get(entity)

                sqlQuery += "${it.name} = ${checkTypes(property.type, value.toString())}, "
            }
        }

        sqlQuery = formatQuery(sqlQuery)

        // searches for the primary key for the where statement
        databaseManager.reflectClass.members.forEach {
            val prop = it as KMutableProperty1<Any, *>
            val value = prop.get(entity)

            val property = getMappedPropertyOrNull(it.name, properties)
            if (getPrimaryKeyOrNull(properties) != null && property != null && property.primaryKey)
                sqlQuery += " WHERE ${it.name} = $value"
        }
        sqlQuery += ";"

        return this
    }

    fun update(tableName : String, values : MutableMap<String, String>, condition: String?) : Update {
        val keys = values.keys
        sqlQuery += "UPDATE $tableName SET "

        values.forEach { (t, u) ->
            sqlQuery += "$t = $u"
            sqlQuery += if (keys.indexOf(t) == keys.size -1)
                " "
            else
                ", "
        }

        if (condition != null)
            sqlQuery += "WHERE $condition"

        sqlQuery += ";"

        return this;
    }

    override fun execute() {
        DatabaseExecutor.executeOperation(sqlQuery)
    }
}
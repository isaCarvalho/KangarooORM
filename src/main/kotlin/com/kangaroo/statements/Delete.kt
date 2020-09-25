package com.kangaroo.statements

import com.kangaroo.database.DatabaseExecutor
import com.kangaroo.database.DatabaseHelper.getPrimaryKeyOrNull
import com.kangaroo.database.DatabaseManager
import com.kangaroo.reflections.ReflectClass
import kotlin.reflect.KProperty1

class Delete : Query() {

    override var sqlQuery: String = ""
    override lateinit var databaseManager: DatabaseManager

    override fun setDatabaseManager(databaseManager: DatabaseManager): Delete {
        this.databaseManager = databaseManager
        return this
    }

    /**
     * Method that deletes an entity from the database
     * @param entity
     */
    fun delete(entity : Any) : Delete {
        val clazz = ReflectClass(entity::class)

        // initiates the query with the delete statements
        var sqlQuery = "DELETE FROM ${clazz.tableName} WHERE "

        // deletes the entity
        val primaryKey = getPrimaryKeyOrNull(clazz.properties)
        if (primaryKey != null) {
            sqlQuery += "${primaryKey.name} = "

            clazz.members.forEach {
                if (it.name == primaryKey.name)
                {
                    it as KProperty1<Any, *>
                    val primaryKeyValue = it.get(entity)

                    sqlQuery += primaryKeyValue.toString()
                }
            }
        }

        DatabaseExecutor.executeOperation(sqlQuery)

        return this
    }

    fun delete(tableName : String, condition: String? = null) : Delete {
        sqlQuery += "DELETE FROM $tableName"

        if (condition != null)
            sqlQuery += " WHERE $condition"
        sqlQuery += ";"

        return this
    }

    override fun execute() {
        DatabaseExecutor.executeOperation(sqlQuery)
    }
}
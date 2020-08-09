package database.statements

import database.DatabaseHelper.checkTypes
import database.DatabaseHelper.getMappedPropertyOrNull
import database.DatabaseExecutor
import database.DatabaseManager
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.declaredMemberProperties

class Delete : IQuery {

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
    fun <T : Any> delete(entity : T) : Delete {
        // initiates the query with the delete statements
        sqlQuery = "DELETE FROM ${databaseManager.tableName} WHERE "

        // gets the entity's declaredMember
        val members = entity::class.declaredMemberProperties

        members.forEach {
            val property = getMappedPropertyOrNull(it.name, databaseManager.propertiesList)

            // checks if the member is a mapped property
            if (property != null) {
                val prop = it as KMutableProperty1<T, *>
                val value = prop.get(entity)

                sqlQuery += "${it.name} = "
                sqlQuery += checkTypes(property.type, value.toString())

                if (members.indexOf(it) != members.size -1) {
                    sqlQuery+= " AND "
                }
            }
        }
        sqlQuery += ";"

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
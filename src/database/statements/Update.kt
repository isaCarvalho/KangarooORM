package database.statements

import database.DatabaseHelper.checkTypes
import database.DatabaseHelper.getMappedPropertyOrNull
import database.DatabaseHelper.getPrimaryKeyOrNull
import database.DatabaseExecutor
import database.DatabaseManager
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.declaredMemberProperties

class Update : IQuery {

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
    fun <T : Any> update(entity : T) : Update {
        // initiates the query with the update statement
        sqlQuery = "UPDATE ${databaseManager.tableName} SET "

        // gets the entity's declaredMembers
        val members = entity::class.declaredMemberProperties

        // for each member, sets the new value
        members.forEach {
            val property = getMappedPropertyOrNull(it.name, databaseManager.propertiesList)

            if (property != null) {
                val prop = it as KMutableProperty1<T, *>
                val value = prop.get(entity)

                sqlQuery += "${it.name} = "
                sqlQuery += checkTypes(property.type, value.toString())

                if (members.indexOf(it) != members.size -1) {
                    sqlQuery+= ", "
                }
            }
        }

        // searches for the primary key for the where statement
        members.forEach {
            val prop = it as KMutableProperty1<T, *>
            val value = prop.get(entity)

            val property = getMappedPropertyOrNull(it.name, databaseManager.propertiesList)
            if (getPrimaryKeyOrNull(databaseManager.propertiesList) != null && property != null && property.primaryKey)
                sqlQuery += " WHERE ${it.name} = $value"
        }
        sqlQuery += ";"

        return this
    }

    override fun execute() {
        DatabaseExecutor.executeOperation(sqlQuery)
    }
}
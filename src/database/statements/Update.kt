package database.statements

import database.DatabaseHelper.checkTypes
import database.DatabaseHelper.getMappedPropertyOrNull
import database.DatabaseHelper.getPrimaryKeyOrNull
import database.DatabaseExecutor
import database.DatabaseManager
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.declaredMemberProperties

class Update(private val databaseManager: DatabaseManager) {

    /**
     * Method that update a entity in the database
     * @param entity
     * @return unit
     */
    fun <T : Any> updateEntity(entity : T) {
        // initiates the query with the update statement
        var sqlQuery = "UPDATE ${databaseManager.tableName} SET "

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

        DatabaseExecutor.executeOperation(sqlQuery)
    }
}
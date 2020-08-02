package database.statements

import database.DatabaseHelper.checkTypes
import database.DatabaseHelper.getMappedPropertyOrNull
import database.DatabaseExecutor
import database.DatabaseManager
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.declaredMemberProperties

class Delete(private val databaseManager: DatabaseManager) {

    /**
     * Method that deletes an entity from the database
     * @param entity
     */
    fun <T : Any> deleteEntity(entity : T) {
        // initiates the query with the delete statements
        var sqlQuery = "DELETE FROM ${databaseManager.tableName} WHERE "

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

        DatabaseExecutor.executeOperation(sqlQuery)
    }
}
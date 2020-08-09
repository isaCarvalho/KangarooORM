package database.statements

import Book
import database.DatabaseHelper.checkTypes
import database.DatabaseHelper.getMappedPropertyOrNull
import database.DatabaseExecutor
import database.DatabaseHelper.getMappedOneToOneOrNull
import database.DatabaseManager
import java.lang.reflect.Type
import kotlin.reflect.*
import kotlin.reflect.full.cast
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.reflect

class Insert : IQuery
{
    override var sqlQuery: String = ""
    override lateinit var databaseManager: DatabaseManager

    override fun setDatabaseManager(databaseManager: DatabaseManager): Insert {
        this.databaseManager = databaseManager
        return this
    }

    /**
     * Method that inserts an entity in the database
     * @param entity
     */
    fun <T : Any> insert(entity : T) : Insert {
        // gets the entity's declared properties
        val members = entity::class.declaredMemberProperties

        // initiates the query with the insert statement
        sqlQuery = "INSERT INTO ${databaseManager.tableName} ("

        // puts the properties' names in the insert's fields
        members.forEach {
            // checks if the member is a mapped property
            val property = getMappedPropertyOrNull(it.name, databaseManager.propertiesList)

            if (property != null) {
                sqlQuery += "${it.name}, "
            }

            val propertyRelation = getMappedOneToOneOrNull(it.name, databaseManager.oneToOneList)
            if (propertyRelation != null)
                sqlQuery += "id_${it.name}, "
        }

        // removes the last comma
        sqlQuery = sqlQuery.take(sqlQuery.length - 2)
        sqlQuery += ") VALUES \n("

        // one to one Relations
        members.forEach {
            // is the entity
            val oneToOne = getMappedOneToOneOrNull(it.name, databaseManager.oneToOneList)

            // if the entity is a relation
            if (oneToOne != null)
            {
                // gets the object
                val prop = it as KMutableProperty1<T, *>
                val newEntity = prop.get(entity)

                // if the relation entity is not a null object
                if (newEntity != null)
                {
                    // we search for its id to do the join
                    newEntity::class.declaredMemberProperties.forEach {newEntityProp ->
                        if (newEntityProp.name == "id")
                        {
                            val prop1 = newEntityProp as KMutableProperty1<Any, *>
                            sqlQuery += "${prop1.get(newEntity)}, "
                        }
                    }
                }
            }
        }

        // puts the entity's declaredMember values in the insert's values
        members.forEach {
            val property = getMappedPropertyOrNull(it.name, databaseManager.propertiesList)

            // checks if the member is a mapped property
            if (property != null) {
                // converts the entity's declaredMember to a mutable property, so we can retrieve the values.
                val prop = it as KMutableProperty1<T, *>
                // gets the value
                val value = prop.get(entity)

                // checks if we have to wrap the value in ''
                sqlQuery += "${checkTypes(property.type, value.toString())}, "
            }
        }

        // removes the last comma
        sqlQuery = sqlQuery.take(sqlQuery.length - 2)
        sqlQuery += ");\n"

        return this
    }

    /**
     * Method that inserts a line in the database
     * @param tableName
     * @param fields
     * @param values
     */
    fun insert(tableName : String, fields : Array<String>, values : Array<String>) : Insert {

        // initiates the query with the insert statement
        sqlQuery = "INSERT INTO $tableName ("

        // puts the properties' names in the insert's fields
        fields.forEach {
            sqlQuery += "$it, "
        }

        // removes the last comma
        sqlQuery = sqlQuery.take(sqlQuery.length - 2)
        sqlQuery += ") VALUES \n("

        values.forEach {
            sqlQuery += "$it, "
        }

        // removes the last comma
        sqlQuery = sqlQuery.take(sqlQuery.length - 2)
        sqlQuery += ");\n"

        return this
    }

    override fun execute() {
        DatabaseExecutor.executeOperation(sqlQuery, true)
    }
}
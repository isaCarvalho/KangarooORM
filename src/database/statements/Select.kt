package database.statements

import database.DatabaseExecutor
import database.DatabaseHelper.checkTypes
import database.DatabaseHelper.getMappedPropertyOrNull
import database.DatabaseManager
import java.util.*
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.declaredMemberProperties

class Select
{
    /**
     * Method that selects the values from the database.
     * @param where
     * @return unit
     */
    fun selectAll(databaseManager: DatabaseManager, where : String? = null) : MutableMap<Int, MutableMap<String, String>> {
        // initiates the query with the select statement
        var sqlQuery = "SELECT * FROM ${databaseManager.tableName}"

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

    /**
     * Method that selects a entity
     * @param entity
     * @param databaseManager
     * @return Entity?
     */
    fun <T : Any> select(entity: T, databaseManager: DatabaseManager) : T? {
        // gets the entity's declared properties
        val members = entity::class.declaredMemberProperties

        // initiates the query with the insert statement
        var sqlQuery = "SELECT * FROM ${databaseManager.tableName} WHERE "

        // puts the properties' names in the insert's fields
        members.forEach {
            // checks if the member is a mapped property
            val property = getMappedPropertyOrNull(it.name, databaseManager.propertiesList)

            if (property != null) {
                val prop = it as KMutableProperty1<T, *>
                // gets the value
                val value = prop.get(entity)

                sqlQuery += "${property.name} = ${checkTypes(property.type, value.toString())}"

                sqlQuery += if (members.indexOf(it) == members.size - 1)
                    ";"
                else
                    " AND "
            }
        }

        var hasValue = false
        val result = DatabaseExecutor.execute(sqlQuery)

        while (result!!.next()) {
            hasValue = true

            entity::class.declaredMemberProperties.forEach {
                val property = getMappedPropertyOrNull(it.name, databaseManager.propertiesList)

                if (property != null)
                {
                    when (property.type)
                    {
                        "varchar" -> {
                            val value = result.getString(property.name)
                            val prop = it as KMutableProperty1<T, String>

                            prop.set(entity, value)
                        }

                        "int" -> {
                            val value = result.getInt(property.name)
                            val prop = it as KMutableProperty1<T, Int>

                            prop.set(entity, value)
                        }

                        "float" -> {
                            val value = result.getFloat(property.name)
                            val prop = it as KMutableProperty1<T, Float>

                            prop.set(entity, value)
                        }

                        "long" -> {
                            val value = result.getLong(property.name)
                            val prop = it as KMutableProperty1<T, Long>

                            prop.set(entity, value)
                        }

                        "double" -> {
                            val value = result.getDouble(property.name)
                            val prop = it as KMutableProperty1<T, Double>

                            prop.set(entity, value)
                        }

                        "short" -> {
                            val value = result.getShort(property.name)
                            val prop = it as KMutableProperty1<T, Short>

                            prop.set(entity, value)
                        }

                        "boolean" -> {
                            val value = result.getBoolean(property.name)
                            val prop = it as KMutableProperty1<T, Boolean>

                            prop.set(entity, value)
                        }

                        "date" -> {
                            val value = result.getString(property.name)
                            val prop = it as KMutableProperty1<T, Date>

                            prop.set(entity, Date(value))
                        }
                    }
                }
            }
        }

        return if (hasValue)
            entity
        else
            null
    }

    /**
     * SQL Count
     * @param databaseManager
     */
    fun count(databaseManager: DatabaseManager) : Int = selectAll(databaseManager).size
}
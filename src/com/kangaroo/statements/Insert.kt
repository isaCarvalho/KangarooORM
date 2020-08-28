package com.kangaroo.statements

import com.kangaroo.DatabaseHelper.checkTypes
import com.kangaroo.DatabaseExecutor
import com.kangaroo.DatabaseHelper.getMappedOneToManyOrNull
import com.kangaroo.DatabaseHelper.getMappedOneToOneOrNull
import com.kangaroo.DatabaseHelper.getPrimaryKeyOrNull
import com.kangaroo.DatabaseManager
import com.kangaroo.annotations.Property
import com.kangaroo.reflections.ReflectClass
import kotlin.reflect.*
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.starProjectedType

class Insert : Query()
{
    override var sqlQuery: String = ""
    override lateinit var databaseManager: DatabaseManager

    override fun setDatabaseManager(databaseManager: DatabaseManager): Insert {
        this.databaseManager = databaseManager
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

    private fun insertRecursive(entity : Any) : Any? {

        val selectObject = Select()
        selectObject.databaseManager = DatabaseManager().setEntity(entity::class)

        val clazz = ReflectClass(entity::class)

        // begins the insert statement
        var query = "INSERT INTO ${clazz.tableName} ("

        // for each entity property that is not a list
        clazz.members.forEach {
            if (!it.returnType.toString().contains("List"))
                query += "${it.name}, "
        }
        query = "${formatQuery(query)}) VALUES \n("

        // searches for one to one relations
        clazz.members.forEach {

            // gets the property type, like if it is an int or a object
            var propType = it.returnType.toString()

            // if the property is not a list, gets its value
            if (!propType.contains("List")) {

                it as KProperty1<Any, *>
                var value = it.get(entity)

                // if the value is an entity. ex: user's book
                if (getMappedOneToOneOrNull(it.name, clazz.properties) != null && value != null) {

                    // inserts the entity and returns its primary key value
                    value = insertRecursive(value)

                    if (value != null) {
                        propType = value::class.simpleName!!
                        query = query.replace(it.name, "id_${it.name}")
                    }
                }

                query += "${checkTypes(propType.toLowerCase().replace("kotlin.", ""), value.toString())}, "
            }
        }

        // executes the insert
        query = "${formatQuery(query)});"
        DatabaseExecutor.executeOperation(query, true)

        // gets the entity's primary value to insert the one to many relations
        val newPrimaryValue = selectObject.getPrimaryKeyValue(entity)

        if (newPrimaryValue != null) {
            // for each one to many relations
            clazz.members.forEach {
                if (getMappedOneToManyOrNull(it.name, clazz.properties) != null) {
                    // gets the list
                    it as KProperty1<Any, *>
                    val propList = it.get(entity)

                    if (propList != null) {
                        propList as List<*>

                        // for each property of the item
                        propList.forEach { item ->
                            insertRecursive(item!!)
                        }
                    }
                }
            }
        }

        return newPrimaryValue
    }

    override fun execute() {
        DatabaseExecutor.executeOperation(sqlQuery, true)
    }

    fun insert(entity : Any) : Insert {
        insertRecursive(entity)

        return this
    }
}
package com.kangaroo.statements

import com.kangaroo.database.DatabaseHelper.checkTypes
import com.kangaroo.database.DatabaseExecutor
import com.kangaroo.database.DatabaseHelper.getMappedOneToManyOrNull
import com.kangaroo.database.DatabaseHelper.getMappedOneToOneOrNull
import com.kangaroo.database.DatabaseManager
import com.kangaroo.annotations.Property
import com.kangaroo.database.DatabaseHelper.getMappedManyToManyOrNull
import com.kangaroo.reflections.ReflectClass
import javax.swing.text.html.parser.Entity
import kotlin.reflect.*
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation

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
            val annotation = it.findAnnotation<Property>()

            if (!it.returnType.toString().contains("List")) {
                // if the property is auto incremented, does not inserts
                if (annotation == null || !annotation.autoIncrement)
                    query += "${it.name}, "
            }
        }
        query = "${formatQuery(query)}) VALUES \n("

        // searches for one to one relations
        clazz.members.forEach {

            // gets the property type, like if it is an int or a object
            var propType = it.returnType.toString()

            val annotation = it.findAnnotation<Property>()

            // if the property is not a list, gets its value
            if (!propType.contains("List")) {
                if (annotation == null || !annotation.autoIncrement) {
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
        }

        // executes the insert
        query = "${formatQuery(query)});"
        DatabaseExecutor.executeOperation(query, true)

        // gets the entity's primary value to insert the one to many relations
        val newPrimaryValue = selectObject.getPrimaryKeyValue(entity)

        if (newPrimaryValue != null) {
            // for each one to many relations
            clazz.members.forEach {
                // gets the list
                it as KMutableProperty1<Any, Any>

                // if the relation is a one to many, inserts each item
                if (getMappedOneToManyOrNull(it.name, clazz.properties) != null) {
                    val propList = it.get(entity)
                    propList as List<*>

                    // for each property of the item
                    propList.forEach { item ->
                        item!!::class.declaredMemberProperties.forEach {prop ->
                            prop as KMutableProperty1<Any, Any>
                            if (prop.name == "id_${entity::class.simpleName!!.toLowerCase()}")
                                prop.set(item, newPrimaryValue)
                        }
                        insertRecursive(item)
                    }
                }

                // if the relation is a many to many
                val manyToMany = getMappedManyToManyOrNull(it.name, clazz.properties)
                if (manyToMany != null) {
                    val propList = it.get(entity)
                    propList as List<*>

                    propList.forEach { item ->
                        // verifies if the item already exists, and if it doesn't, inserts
                        val primaryKey = selectObject.getPrimaryKeyValue(item!!) ?: insertRecursive(item)

                        // if the insert was possible
                        if (primaryKey != null) {
                            // gets the table for insert and the primary key of the entity
                            val table = manyToMany.foreignKey.referencedTable
                            val entityPrimaryKeyField = "id_${entity::class.simpleName!!.toLowerCase()}"

                            // creates the query
                            val insertQuery = "INSERT INTO $table " +
                                    "($entityPrimaryKeyField, ${manyToMany.foreignKey.referencedProperty})" +
                                    " VALUES ($newPrimaryValue, $primaryKey);"

                            // inserts the value
                            DatabaseExecutor.executeOperation(insertQuery, true)

                            // updates the list of the entity in the other side of the relation
                            item::class.declaredMemberProperties.forEach {propItem ->
                                if (propItem.name == tableName) {
                                    propItem as KMutableProperty1<Any, Any>
                                    val entityList = propItem.get(item) as List<*>

                                    val newList = entityList.toMutableList()
                                    newList.add(entity)

                                    propItem.set(item, newList.toList())
                                }
                            }
                        }
                    }
                }

                val annotation = it.findAnnotation<Property>()
                if (annotation != null && annotation.primaryKey) {
                    it.set(entity, newPrimaryValue)
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
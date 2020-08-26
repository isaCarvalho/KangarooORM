package database.statements

import database.DatabaseHelper.checkTypes
import database.DatabaseExecutor
import database.DatabaseHelper.getMappedOneToOneOrNull
import database.DatabaseHelper.getPrimaryKeyOrNull
import database.DatabaseManager
import database.annotations.Property
import database.reflections.ReflectClass
import kotlin.reflect.*
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.declaredMembers
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

        val clazz = ReflectClass(entity::class)

        // gets the primary key and primary value
        var primaryValue : Any? = null
        val primaryKey = getPrimaryKeyOrNull(clazz.properties)

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

                if (it.name == primaryKey!!.name)
                    primaryValue = value

                // if the value is an entity. ex: user's book
                if (getMappedOneToOneOrNull(it.name, clazz.properties) != null && value != null) {

                    var existinValue : Any? = null

                    // checks if the entity already exists
                    val valuesPrimaryKey = getPrimaryKeyOrNull(ReflectClass(value::class).properties)

                    if (valuesPrimaryKey != null) {

                        var valuesPrimaryKeyValue : Any? = null
                        value::class.declaredMemberProperties.forEach {prop ->
                            if (prop.name == valuesPrimaryKey.name) {
                                prop as KProperty1<Any, *>
                                valuesPrimaryKeyValue = prop.get(value!!)
                            }
                        }

                        if (valuesPrimaryKeyValue != null) {
                            existinValue = getPrimaryKeysValue(valuesPrimaryKey, valuesPrimaryKeyValue!!, value)
                        }
                    }

                    // inserts the entity and returns its primary key value
                    value = existinValue ?: insertRecursive(value)

                    if (value != null) {
                        propType = value::class.simpleName!!
                        query = query.replace(it.name, "id_${it.name}")
                    }
                }

                query += "${checkTypes(propType.toLowerCase().replace("kotlin.", ""), value.toString())}, "
            }
        }

        query = "${formatQuery(query)});"
        DatabaseExecutor.executeOperation(query, true)

        return if (primaryKey != null && primaryValue != null)
            getPrimaryKeysValue(primaryKey, primaryValue!!, entity)
        else
            null
    }

    private fun getPrimaryKeysValue(primaryKey : Property, primaryValue : Any, entity : Any) : Any? {
        val selectObject = Select()
        selectObject.databaseManager = DatabaseManager().setEntity(entity::class)

        val newValue = selectObject.select("${primaryKey.name} = $primaryValue", entity::class.starProjectedType)
        if (newValue != null) {
            newValue::class.declaredMemberProperties.forEach {
                if (it.name == primaryKey.name) {
                    it as KProperty1<Any, *>
                    return it.get(newValue)
                }
            }
        }
        return null
    }

    override fun execute() {
        DatabaseExecutor.executeOperation(sqlQuery, true)
    }

    fun insert(entity : Any) : Insert {
        insertRecursive(entity)

        return this
    }
}
package database.statements

import database.DatabaseHelper.checkTypes
import database.DatabaseHelper.getMappedPropertyOrNull
import database.DatabaseExecutor
import database.DatabaseHelper.getMappedOneToOneOrNull
import database.DatabaseManager
import database.annotations.Table
import database.reflections.ReflectClass
import javafx.scene.control.Tab
import kotlin.reflect.*
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.jvmErasure

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

    override fun execute() {
        DatabaseExecutor.executeOperation(sqlQuery, true)
    }

    fun insert(entity : Any, type : KType) : Insert {

        // gets the table's name
        val table = ReflectClass(entity::class).tableName

        sqlQuery = "INSERT INTO $tableName ("

        // gets the declared members of the entity
        val members = entity::class.declaredMemberProperties

        // for each entity property
        members.forEach {
            sqlQuery += "${it.name}, "
        }
        sqlQuery = "${formatInsert(sqlQuery)}) VALUES \n("

        // sets the values
        members.forEach {
            var propType = it.returnType

            // gets the members values: ex: user's id
            it as KProperty1<Any, *>
            var value = it.get(entity)

            // if the value is an entity. ex: user's book
            if (getMappedOneToOneOrNull(it.name, properties) != null && value != null)
            {
                val valuesReflectProperties = ReflectClass(value::class).properties
                // gets the value of its primary key to insert
                value::class.declaredMemberProperties.forEach { prop ->
                    val property = getMappedPropertyOrNull(prop.name, valuesReflectProperties)
                    if (property != null && property.primaryKey)
                    {
                        prop as KProperty1<Any, *>
                        value = prop.get(value!!)

                        propType = prop.returnType
                        sqlQuery = sqlQuery.replace(it.name, "id_${it.name}")
                    }
                }
            }

            sqlQuery += "${checkTypes(propType.toString().replace("kotlin.", ""), value.toString())}, "
        }

        sqlQuery = "${formatInsert(sqlQuery)});"
        execute()

        return this
    }

    private val formatInsert = { query : String ->
        // removes the last comma
        query.take(query.length - 2)
    }
}
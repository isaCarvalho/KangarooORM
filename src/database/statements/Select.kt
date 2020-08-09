package database.statements

import database.DatabaseExecutor
import database.DatabaseHelper.checkTypes
import database.DatabaseHelper.getMappedPropertyOrNull
import database.DatabaseHelper.getMappedParameter
import database.DatabaseManager
import java.sql.ResultSet
import kotlin.collections.ArrayList
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KParameter
import kotlin.reflect.full.declaredMemberProperties

class Select : IQuery {

    var resultSet : ResultSet? = null

    override var sqlQuery: String = ""
    override lateinit var databaseManager: DatabaseManager

    override fun execute() {
        resultSet = DatabaseExecutor.execute(sqlQuery)
    }

    override fun setDatabaseManager(databaseManager: DatabaseManager) : Select
    {
        this.databaseManager = databaseManager
        return this
    }

    /**
     * Method that selects the values from the database.
     * @return ArrayList<T>
     */
    inline fun <reified T : Any> selectAll(where : String? = null): ArrayList<T> {
        // initiates the query with the select statement
        sqlQuery = "SELECT * FROM ${databaseManager.tableName}"

        if (where != null)
            sqlQuery += " $where;"

        // executes the query and cleans the sql query
        execute()
        cleanSqlQuery()

        val constructor = T::class.constructors.first()
        val results = ArrayList<T>()

        while (resultSet != null && resultSet!!.next()) {

            val entity = setParameterValue(constructor, resultSet!!)
            results.add(entity)
        }

        // returns the result converted to a mutable map
        return results
    }

    /**
     * Method that selects a entity filtering by one field
     * @param field
     * @param operator
     * @param value
     * @return Entity?
     */
    inline fun <reified T : Any> select(field : String, operator : String, value : String) : T? {
        return select<T>("WHERE $field $operator $value")
    }

    /**
     * Method that selects a entity filtering by fields
     * @param where
     * @return Entity?
     */
    inline fun <reified T : Any> select(where : String) : T? {

        // initiates the query with the insert statement
        sqlQuery = "SELECT * FROM ${databaseManager.tableName} $where;"

        // executes the query and cleans the sql query
        execute()
        cleanSqlQuery()

        var entity : T? = null
        val constructor = T::class.constructors.first()

        if (resultSet != null && resultSet!!.next()) {
            entity = setParameterValue(constructor, resultSet!!)
        }

        return entity
    }

    /**
     * Method that checks if a entity exists
     * @param entity
     * @return Boolean
     */
    fun <T : Any> exists(entity: T): Boolean {
        // gets the entity's declared properties
        val members = entity::class.declaredMemberProperties

        // initiates the query with the insert statement
        sqlQuery = "SELECT * FROM ${databaseManager.tableName} WHERE "

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

        // executes the query and cleans the sql query
        execute()
        cleanSqlQuery()

        while (resultSet != null && resultSet!!.next()) {
            hasValue = true
        }

        return hasValue
    }

    /**
     * SQL Count
     */
    fun count(tableName : String? = null): Int {
        sqlQuery = "SELECT count(*) FROM "

        sqlQuery += tableName ?: databaseManager.tableName

        // executes and cleans the query
        execute()
        cleanSqlQuery()

        return if (resultSet!!.next())
            resultSet!!.getInt("count")
        else
            -1
    }

    /**
     * SQL Max int
     */
    fun maxInt(field : String, tableName: String? = null): Int {
        sqlQuery = "SELECT max($field) FROM "
        sqlQuery += tableName ?: databaseManager.tableName

        // executes and cleans the query
        execute()
        cleanSqlQuery()

        return if (resultSet!!.next())
            resultSet!!.getInt("max")
        else
            -1
    }

    /**
     * SQL Min int
     */
    fun minInt(field : String, tableName: String? = null): Int {
        sqlQuery = "SELECT min($field) FROM "
        sqlQuery += tableName ?: databaseManager.tableName

        // executes and cleans the query
        execute()
        cleanSqlQuery()

        return if (resultSet!!.next())
            resultSet!!.getInt("min")
        else
            -1
    }

    /**
     * SQL Max Float
     */
    fun maxFloat(field : String, tableName: String? = null): Float {
        sqlQuery = "SELECT max($field) FROM "
        sqlQuery += tableName ?: databaseManager.tableName

        // executes and cleans the query
        execute()
        cleanSqlQuery()

        return if (resultSet!!.next())
            resultSet!!.getFloat("max")
        else
            -1F
    }

    /**
     * SQL Min Float
     */
    fun minFloat(field : String, tableName: String? = null): Float {
        sqlQuery = "SELECT min($field) FROM "
        sqlQuery += tableName ?: databaseManager.tableName

        // executes and cleans the query
        execute()
        cleanSqlQuery()

        return if (resultSet!!.next())
            resultSet!!.getFloat("min")
        else
            -1F
    }

    /**
     * SQL Sum Int
     */
    fun sumInt(field : String, tableName: String? = null): Int {
        sqlQuery = "SELECT sum($field) FROM "
        sqlQuery += tableName ?: databaseManager.tableName

        execute()
        cleanSqlQuery()

        return if (resultSet!!.next())
            resultSet!!.getInt("sum")
        else
            -1
    }

    /**
     * SQL Sum Float
     */
    fun sumFloat(field : String, tableName: String? = null): Float {
        sqlQuery = "SELECT sum($field) FROM "
        sqlQuery += tableName ?: databaseManager.tableName

        execute()
        cleanSqlQuery()

        return if (resultSet!!.next())
            resultSet!!.getFloat("sum")
        else
            -1F
    }

    /**
     * SQL AVG
     */
    fun avg(field : String, tableName: String? = null) : Float {
        sqlQuery = "SELECT avg($field) FROM "
        sqlQuery += tableName ?: databaseManager.tableName

        execute()
        cleanSqlQuery()

        return if (resultSet!!.next())
            resultSet!!.getFloat("avg")
        else
            -1F
    }

    /**
     * method that sets the parameters values
     */
    fun <T : Any> setParameterValue(constructor : KFunction<T>, result : ResultSet) : T
    {
        // parameters of constructor
        val constructorParameterValues = mutableMapOf<KParameter, Any>()

        databaseManager.propertiesList.forEach {
            val parameter = getMappedParameter(constructor, it.name)

            when (it.type) {
                "varchar" -> {
                    val value = result.getString(it.name)
                    constructorParameterValues[parameter] = value
                }

                "int" -> {
                    val value = result.getInt(it.name)
                    constructorParameterValues[parameter] = value
                }

                "float" -> {
                    val value = result.getFloat(it.name)
                    constructorParameterValues[parameter] = value
                }

                "long" -> {
                    val value = result.getLong(it.name)
                    constructorParameterValues[parameter] = value
                }

                "double" -> {
                    val value = result.getDouble(it.name)
                    constructorParameterValues[parameter] = value
                }

                "short" -> {
                    val value = result.getShort(it.name)
                    constructorParameterValues[parameter] = value
                }

                "boolean" -> {
                    val value = result.getBoolean(it.name)
                    constructorParameterValues[parameter] = value
                }

                "date" -> {
                    val value = result.getString(it.name)
                    constructorParameterValues[parameter] = value
                }
            }
        }

        return constructor.callBy(constructorParameterValues)
    }

    fun select(tableName : String, fields : Array<String>, condition: String?) : ArrayList<MutableMap<String, String>> {
        sqlQuery += "SELECT "

        fields.forEach {
            sqlQuery += it

            sqlQuery += if (fields.indexOf(it) == fields.size -1)
                " "
            else
                ", "
        }

        sqlQuery += "FROM $tableName"
        if (condition != null)
            sqlQuery += " WHERE $condition"

        sqlQuery += ";"

        val list = ArrayList<MutableMap<String, String>>()

        // executes and cleans the query
        execute()
        cleanSqlQuery()

        while (resultSet!!.next()) {
            val map = mutableMapOf<String, String>()
            fields.forEach {
                val value = resultSet!!.getString(it)
                map[it] = value
            }

            list.add(map)
        }

        return list
    }

    fun cleanSqlQuery() {
        sqlQuery = ""
    }
}
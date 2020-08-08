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

class Select {

    /**
     * Method that selects the values from the database.
     * @param databaseManager
     * @return ArrayList<T>
     */
    inline fun <reified T : Any> selectAll(databaseManager: DatabaseManager, where : String? = null): ArrayList<T> {
        // initiates the query with the select statement
        var sqlQuery = "SELECT * FROM ${databaseManager.tableName}"

        if (where != null)
            sqlQuery += " $where"

        // executes the query and puts the result inside of a mutable map
        val result = DatabaseExecutor.execute(sqlQuery)
        val constructor = T::class.constructors.first()

        val results = ArrayList<T>()
        while (result!!.next()) {

            val entity = setParameterValue(databaseManager, constructor, result)
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
     * @param databaseManager
     * @return Entity?
     */
    inline fun <reified T : Any> select(field : String, operator : String, value : String, databaseManager: DatabaseManager) : T? {
        return select<T>("WHERE $field $operator $value", databaseManager)
    }

    /**
     * Method that selects a entity filtering by fields
     * @param where
     * @return Entity?
     */
    inline fun <reified T : Any> select(where : String, databaseManager: DatabaseManager) : T? {

        // initiates the query with the insert statement
        val sqlQuery = "SELECT * FROM ${databaseManager.tableName} $where;"

        var entity : T? = null
        val result = DatabaseExecutor.execute(sqlQuery)
        val constructor = T::class.constructors.first()

        if (result!!.next()) {
            entity = setParameterValue(databaseManager, constructor, result)
        }

        return entity
    }

    /**
     * Method that checks if a entity exists
     * @param entity
     * @param databaseManager
     * @return Boolean
     */
    fun <T : Any> exists(entity: T, databaseManager: DatabaseManager): Boolean {
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
        }

        return hasValue
    }

    /**
     * SQL Count
     * @param databaseManager
     */
    fun count(databaseManager: DatabaseManager): Int {
        val result = DatabaseExecutor.execute("SELECT count(*) FROM ${databaseManager.tableName}")

        return if (result!!.next())
            result.getInt("count")
        else
            -1
    }

    /**
     * SQL Max int
     * @param databaseManager
     */
    fun maxInt(field : String, databaseManager: DatabaseManager): Int {
        val result = DatabaseExecutor.execute("SELECT max($field) FROM ${databaseManager.tableName}")

        return if (result!!.next())
            result.getInt("max")
        else
            -1
    }

    /**
     * SQL Min int
     * @param databaseManager
     */
    fun minInt(field : String, databaseManager: DatabaseManager): Int {
        val result = DatabaseExecutor.execute("SELECT min($field) FROM ${databaseManager.tableName}")

        return if (result!!.next())
            result.getInt("min")
        else
            -1
    }

    /**
     * SQL Max Float
     * @param databaseManager
     */
    fun maxFloat(field : String, databaseManager: DatabaseManager): Float {
        val result = DatabaseExecutor.execute("SELECT max($field) FROM ${databaseManager.tableName}")

        return if (result!!.next())
            result.getFloat("max")
        else
            -1F
    }

    /**
     * SQL Min Float
     * @param databaseManager
     */
    fun minFloat(field : String, databaseManager: DatabaseManager): Float {
        val result = DatabaseExecutor.execute("SELECT min($field) FROM ${databaseManager.tableName}")

        return if (result!!.next())
            result.getFloat("min")
        else
            -1F
    }

    /**
     * SQL Sum Int
     * @param databaseManager
     */
    fun sumInt(field : String, databaseManager: DatabaseManager): Int {
        val result = DatabaseExecutor.execute("SELECT sum($field) FROM ${databaseManager.tableName}")

        return if (result!!.next())
            result.getInt("sum")
        else
            -1
    }

    /**
     * SQL Sum Float
     * @param databaseManager
     */
    fun sumFloat(field : String, databaseManager: DatabaseManager): Float {
        val result = DatabaseExecutor.execute("SELECT sum($field) FROM ${databaseManager.tableName}")

        return if (result!!.next())
            result.getFloat("sum")
        else
            -1F
    }

    /**
     * SQL AVG
     */
    fun avg(field : String, databaseManager: DatabaseManager) : Float {
        val result = DatabaseExecutor.execute("SELECT avg($field) FROM ${databaseManager.tableName}")

        return if (result!!.next())
            result.getFloat("avg")
        else
            -1F
    }

    /**
     * method that sets the parameters values
     */
    fun <T : Any> setParameterValue(databaseManager: DatabaseManager, constructor : KFunction<T>, result : ResultSet) : T
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
}
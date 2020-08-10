package database.statements

import database.DatabaseExecutor
import database.DatabaseHelper.checkTypes
import database.DatabaseHelper.getMappedOneToOneOrNull
import database.DatabaseHelper.getMappedPropertyOrNull
import database.DatabaseHelper.getMappedParameter
import database.DatabaseManager
import java.sql.ResultSet
import kotlin.collections.ArrayList
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KParameter
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.jvmErasure

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
        executeAggregationsSQL("SELECT count(*) FROM ", tableName)

        return if (resultSet!!.next())
            resultSet!!.getInt("count")
        else
            -1
    }

    /**
     * SQL Max int
     */
    fun maxInt(field : String, tableName: String? = null): Int {
        executeAggregationsSQL("SELECT max($field) FROM ", tableName)

        return if (resultSet!!.next())
            resultSet!!.getInt("max")
        else
            -1
    }

    /**
     * SQL Min int
     */
    fun minInt(field : String, tableName: String? = null): Int {
        executeAggregationsSQL("SELECT min($field) FROM ", tableName)

        return if (resultSet!!.next())
            resultSet!!.getInt("min")
        else
            -1
    }

    /**
     * SQL Max Float
     */
    fun maxFloat(field : String, tableName: String? = null): Float {
        executeAggregationsSQL("SELECT max($field) FROM ", tableName)

        return if (resultSet!!.next())
            resultSet!!.getFloat("max")
        else
            -1F
    }

    /**
     * SQL Min Float
     */
    fun minFloat(field : String, tableName: String? = null): Float {
        executeAggregationsSQL("SELECT min($field) FROM ", tableName)

        return if (resultSet!!.next())
            resultSet!!.getFloat("min")
        else
            -1F
    }

    /**
     * SQL Sum Int
     */
    fun sumInt(field : String, tableName: String? = null): Int {
        executeAggregationsSQL("SELECT sum($field) FROM ", tableName)

        return if (resultSet!!.next())
            resultSet!!.getInt("sum")
        else
            -1
    }

    /**
     * SQL Sum Float
     */
    fun sumFloat(field : String, tableName: String? = null): Float {
        executeAggregationsSQL("SELECT sum($field) FROM ", tableName)

        return if (resultSet!!.next())
            resultSet!!.getFloat("sum")
        else
            -1F
    }

    /**
     * SQL AVG
     */
    fun avg(field : String, tableName: String? = null) : Float {
        executeAggregationsSQL("SELECT avg($field) FROM ", tableName)

        return if (resultSet!!.next())
            resultSet!!.getFloat("avg")
        else
            -1F
    }

    /**
     * method that sets the parameters values
     */
    inline fun <reified T : Any> setParameterValue(constructor : KFunction<T>, result : ResultSet) : T
    {
        // parameters of constructor
        val constructorParameterValues = mutableMapOf<KParameter, Any>()

        databaseManager.propertiesList.forEach {
            val parameter = getMappedParameter(constructor, it.name)
            constructorParameterValues[parameter] = mapParameterType(it.name, it.type, result)!!
        }

        // foreach one to one property, selects its data from the database
        T::class.declaredMemberProperties.forEach {
            val oneToOne = getMappedOneToOneOrNull(it.name, databaseManager.oneToOneList)
            if (oneToOne != null) {

                val resultSelect = DatabaseExecutor.execute("SELECT * FROM ${oneToOne.foreignKey.referencedTable} " +
                        "WHERE id = ${result.getInt("id_${it.name}")}")

                // if finds the result of the one to one object
                if (resultSelect!!.next()) {
                    // gets the parameter entity. Example: the user's book.
                    val parameter = getMappedParameter(constructor, it.name)

                    // gets the properties of the new entity
                    val newEntityClass = it.returnType.jvmErasure

                    // map of the entity's parameters to create the inside object
                    val mapConstructorEntity = mutableMapOf<KParameter, Any>()
                    // the entity constructor
                    val entityConstructor = newEntityClass.constructors.first()

                    newEntityClass.declaredMemberProperties.forEach {prop ->
                        val entityParameter = getMappedParameter(entityConstructor, prop.name)

                        val type = prop.returnType.toString().replace("kotlin.", "")
                        mapConstructorEntity[entityParameter] = mapParameterType(prop.name, type, resultSelect) as Any
                    }

                    // building the object
                    val entity = entityConstructor.callBy(mapConstructorEntity)

                    // adding to the main object construction
                    constructorParameterValues[parameter] = entity
                }
            }
        }

        return constructor.callBy(constructorParameterValues)
    }

    fun mapParameterType(name : String, type: String, result: ResultSet) : Any? {

        when (type.toLowerCase()) {
            "varchar" -> {
                return result.getString(name)
            }

            "string" -> {
                return result.getString(name)
            }

            "int" -> {
                return result.getInt(name)

            }

            "float" -> {
                return result.getFloat(name)

            }

            "long" -> {
                return result.getLong(name)

            }

            "double" -> {
                return result.getDouble(name)

            }

            "short" -> {
                return result.getShort(name)

            }

            "boolean" -> {
                return result.getBoolean(name)

            }

            "date" -> {
                return result.getString(name)
            }

            else -> {
                return null
            }
        }
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

    private val executeAggregationsSQL = { query : String, tableName : String? ->
        sqlQuery = query + (tableName ?: databaseManager.tableName)

        // executes and cleans the query
        execute()
        cleanSqlQuery()
    }
}
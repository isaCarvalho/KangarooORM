package com.kangaroo.statements

import com.kangaroo.database.DatabaseExecutor
import com.kangaroo.database.DatabaseHelper.checkTypes
import com.kangaroo.database.DatabaseHelper.getMappedParameterOrNull
import com.kangaroo.database.DatabaseHelper.getPrimaryKeyOrNull
import com.kangaroo.database.DatabaseManager
import com.kangaroo.annotations.OneToMany
import com.kangaroo.annotations.OneToOne
import com.kangaroo.annotations.Property
import com.kangaroo.reflections.ReflectClass
import java.sql.ResultSet
import kotlin.collections.ArrayList
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.jvmErasure

class Select : Query() {

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

    fun selectAll(tableName : String, fields : Array<String>, condition: String?) : ArrayList<MutableMap<String, String>> {
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

    fun find(id : Int, type: KType) : Any? {
        return select("id = $id", type)
    }

    fun exists(entity : Any, type: KType) : Boolean {
        var where = "false"

        val clazz = ReflectClass(entity::class)

        val primaryKey = getPrimaryKeyOrNull(clazz.properties)
        clazz.members.forEach {
            if (primaryKey != null && it.name == primaryKey.name) {
                it as KProperty1<Any, *>
                where = "${it.name} = ${it.get(entity)}"
            }
        }

        val result = select(where, type)
        return result != null
    }

    fun getPrimaryKeyValue(entity : Any) : Any? {
        var where = ""

        val clazz = ReflectClass(entity::class)
        val primaryKey = getPrimaryKeyOrNull(clazz.properties)

        clazz.members.forEach {
            if (primaryKey == null || primaryKey.name != it.name) {
                it as KProperty1<Any, *>

                val value = it.get(entity)
                val property = it.findAnnotation<Property>()

                if (property != null)
                    where += "${it.name} = ${checkTypes(property.type, value.toString())} AND "
            }
        }

        where = formatQuery(where, 5)

        val result = select(where, entity::class.starProjectedType)
        if (result != null && primaryKey != null) {
            result::class.declaredMemberProperties.forEach {
                it as KProperty1<Any, *>

                if (it.name == primaryKey.name)
                    return it.get(result)
            }
        }

        return null
    }

    fun select(where: String, type: KType) : Any? {
        val result = selectAll(where, type)

        return if (result.isEmpty())
            null
        else
            result.first()
    }

    fun selectAll(where: String, type : KType) : List<Any>
    {
        val clazz = ReflectClass(type.jvmErasure)
        val tableName = clazz.tableName

        val query = "SELECT * FROM $tableName WHERE $where;"

        val result = DatabaseExecutor.execute(query)

        val list = ArrayList<Any>()

        while (result!!.next()) {
            val mapParameters = mutableMapOf<KParameter, Any>()

            clazz.members.forEach {
                var annotations = it.annotations.find { annotation -> annotation is OneToOne }

                val parameter = getMappedParameterOrNull(clazz.cls.constructors.first(), it.name)

                if (annotations != null) {
                    annotations as OneToOne

                    val whereRecursive = "id = ${result.getInt("id_${it.name}")}"
                    mapParameters[parameter!!] = selectAll(whereRecursive, it.returnType).first()
                }

                annotations = it.annotations.find { annotation -> annotation is OneToMany }
                if (annotations != null) {
                    annotations as OneToMany

                    val whereRecursive = "id_${clazz.cls.simpleName?.toLowerCase()} = " +
                            "${result.getInt(getPrimaryKeyOrNull(properties)!!.name)}"

                    mapParameters[parameter!!] = selectAll(whereRecursive, it.returnType.arguments.first().type!!)

                }

                annotations = it.annotations.find { annotation -> annotation is Property }
                if (annotations != null) {
                    annotations as Property

                    mapParameters[parameter!!] = mapParameterType(it.name, annotations.type, result)!!
                }
            }

            list.add(clazz.cls.constructors.first().callBy(mapParameters))
        }

        return list
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

    private fun mapParameterType(name : String, type: String, result: ResultSet) : Any? {

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

    private fun cleanSqlQuery() {
        sqlQuery = ""
    }

    private val executeAggregationsSQL = { query : String, tableName : String? ->
        sqlQuery = query + (tableName ?: tableName)

        // executes and cleans the query
        execute()
        cleanSqlQuery()
    }
}
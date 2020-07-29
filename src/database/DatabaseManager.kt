package database

import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

/**
 * This class is the one how is going to create and manipulate the database
 */
class DatabaseManager {

    private val propertiesList = ArrayList<Property>()
    private lateinit var tableName : String
    private lateinit var cls : KClass<*>

    private val numericTypes = arrayListOf("int", "float", "double", "long", "short")

    fun <T : Any> setEntity(c : KClass<T>) {
        this.cls = c::class

        val tableName = c.annotations.find { it is Table } as Table
        this.tableName = tableName.tableName

        c.memberProperties.forEach {

            val property = it.annotations.find { annotation -> annotation is Property }
            if (property != null)
                propertiesList.add(property as Property)
        }

        createTable()
    }

    fun select () {
        var sqlQuery = "SELECT "

        propertiesList.forEach {
            sqlQuery += it.name

            sqlQuery += if (propertiesList.indexOf(it) == propertiesList.size - 1)
                " "
            else
                ", "
        }

        sqlQuery += "FROM $tableName"

        println(sqlQuery)
    }

    fun <T: Any> insert(entity : T) {
        println(entity.javaClass.declaredFields.distinct())
    }

    private fun createTable() {
        var sqlQuery = "DROP TABLE IF EXISTS $tableName;\n"
        var sequenceQuery = ""

        sqlQuery += "CREATE TABLE IF NOT EXISTS $tableName (\n"
        propertiesList.forEach {
            sqlQuery += "${it.name} ${it.type}"

            if (it.type !in numericTypes) {
                sqlQuery += "(${it.size})"
            }

            if (it.primaryKey) {
                sqlQuery += " primary key"
            }

            if (!it.nullable) {
                sqlQuery += " not null"
            }

            if (it.unique) {
                sqlQuery += " unique"
            }

            sqlQuery += if (propertiesList.indexOf(it) == propertiesList.size - 1)
                "\n)"
            else
                ",\n"

            if (it.autoIncrement) {
                val sequenceName = tableName + "_seq"

                sequenceQuery += "CREATE SEQUENCE $sequenceName INCREMENT 1 MINVALUE 1 START 1;\n"
                sequenceQuery += "ALTER TABLE $tableName ALTER COLUMN ${it.name} SET DEFAULT nextval('$sequenceName');\n"
            }
        }

        println(sqlQuery)
        println(sequenceQuery)
    }

    fun delete() : String = " DELETE FROM $tableName "

    fun where(field : String, operator : String, value : String) : String =
        " WHERE $field $operator $value "

    fun and(field : String, operator : String, value : String) : String =
        " AND $field $operator $value "

    fun or(field : String, operator : String, value : String) : String =
        " OR $field $operator $value "

    fun orderBy(field : String, asc : Boolean) : String {
        var orderByClause = " ORDER BY $field"

        orderByClause += if (asc)
            " ASC "
        else
            " DESC "

        return orderByClause
    }
}
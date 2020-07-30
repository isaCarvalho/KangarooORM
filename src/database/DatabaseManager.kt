package database

import kotlin.reflect.*
import kotlin.reflect.full.*

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

    fun <T : Any> insert(entity : T) {
        val members = entity::class.declaredMemberProperties

        var sqlQuery = "INSERT INTO $tableName ("
        var index = 0

        /** properties names **/
        members.forEach {
            if (it.name == propertiesList[index].name) {
                sqlQuery += it.name

                sqlQuery += if (members.indexOf(it) == members.size - 1)
                    ") VALUES \n("
                else
                    ", "

            }
            index++
        }

        /** properties values **/
        index = 0
        members.forEach {
            if (it.name == propertiesList[index].name) {
                val prop = it as KMutableProperty1<T, *>
                val value = prop.get(entity)

                sqlQuery += checkNumericTypes(propertiesList[index].type, value.toString())

                sqlQuery += if (members.indexOf(it) == members.size - 1)
                    ");\n"
                else
                    ", "
            }
            index++
        }

        println(sqlQuery)
    }

    fun <T : Any> delete(entity : T) {
        var sqlQuery = "DELETE FROM $tableName WHERE "

        val members = entity::class.declaredMemberProperties
        var index = 0
        members.forEach {
            val property = propertiesList[index]

            if (it.name == property.name) {
                val prop = it as KMutableProperty1<T, *>
                val value = prop.get(entity)

                sqlQuery += "${it.name} = "
                sqlQuery += checkNumericTypes(property.type, value.toString())

                if (members.indexOf(it) != members.size -1) {
                    sqlQuery+= " AND "
                }
            }
            index++
        }
        sqlQuery += ";"

        println(sqlQuery)
    }

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

    private fun checkNumericTypes(type : String, value : String) : String {
        return if (type in numericTypes)
            value
        else
            "'$value'"
    }

    private fun containsPrimaryKey() : Property? {
        propertiesList.forEach {
            if (it.primaryKey)
                return it
        }
        return null
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
                "\n);"
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
}
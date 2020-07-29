import java.lang.Exception
import java.lang.NullPointerException
import java.sql.ResultSet
import java.sql.SQLException
import kotlin.properties.Delegates

open class Entity
{
    private val numericTypes = arrayListOf("int", "float", "double", "long", "short")
    private lateinit var fields : Array<out String>

    var properties : Pair<String, Map<String, Pair<String, Int>>> by Delegates.observable(Pair("", mapOf())) {
        _, _, _ ->
        val tableName = properties.first
        val attributes = properties.second

        val dropTable = "DROP TABLE IF EXISTS $tableName";

        val keys = attributes.keys
        var sqlQuery = "CREATE TABLE IF NOT EXISTS $tableName (\n"

        attributes.forEach {

            sqlQuery += if (it.value.first in numericTypes)
                "${it.key} ${it.value.first} "
            else
                "${it.key} ${it.value.first}(${it.value.second}) "

            sqlQuery += if (keys.indexOf(it.key) == attributes.size - 1)
                "\n)"
            else
                ",\n"
        }

        try {
            executeOperation(dropTable)
            executeOperation(sqlQuery)
        } catch (ex : Exception) {
            ex.printStackTrace()
        }
    }

    fun select(vararg fields : String) : String {
        this.fields = fields

        var sqlQuery = "SELECT "
        fields.forEach {
            sqlQuery += if (fields.indexOf(it) != (fields.size -1))
                "$it, "
            else
                it
        }
        sqlQuery += " FROM ${properties.first} "

        return sqlQuery
    }

    fun where(field : String, operator : String, value : String) : String {
        return " WHERE $field $operator $value "
    }

    fun and(field : String, operator : String, value : String) : String {
        return " AND $field $operator $value "
    }

    fun or(field : String, operator : String, value : String) : String {
        return " OR $field $operator $value "
    }

    fun orderBy(field : String, asc : Boolean) : String {
        var orderByClause = " ORDER BY $field"

        orderByClause += if (asc)
            " ASC "
        else
            " DESC "

        return orderByClause
    }

    fun insert(fieldsValues : Map<String, String>) : Boolean {
        val keys = fieldsValues.keys
        val values = fieldsValues.values

        var sqlQuery = "INSERT INTO ${properties.first} ( "
        keys.forEach{
            sqlQuery += if (keys.indexOf(it) != keys.size - 1)
                "$it, "
            else
                "$it) VALUES ("
        }

        values.forEach {
            sqlQuery += if (values.indexOf(it) != values.size -1)
                "$it, "
            else
                "$it)"
        }

        return try {
            executeOperation(sqlQuery)
            true
        } catch (ex : Exception) {
            ex.printStackTrace()
            false
        }
    }

    fun update(fieldsValues : Map<String, String>) : String {
        var sqlQuery = "UPDATE ${properties.first} SET "
        val keys = fieldsValues.keys

        fieldsValues.forEach {
            sqlQuery += "${it.key} = ${it.value}"

            sqlQuery += if (keys.indexOf(it.key) == fieldsValues.size - 1)
                ", "
            else
                " "
        }

        return sqlQuery
    }

    fun delete() : String {
        return "DELETE FROM ${properties.first} "
    }

    fun execute(query : String) : ArrayList<String> {
        val sqlQuery = query.trim() + ";"
        var resultSet : ResultSet? = null

        println(sqlQuery)

        try {
            val conn = DatabaseConfig.connect()

            val stmt = conn!!.createStatement()
            resultSet = stmt.executeQuery(sqlQuery)

            DatabaseConfig.close()
        } catch (ex : SQLException) {
            ex.printStackTrace()
        } catch (ex : NullPointerException) {
            ex.printStackTrace()
        } finally {
            val list = ArrayList<String>()

            if (this.fields.isNotEmpty() && resultSet!!.next())  {
                fields.forEach {
                    list.add(resultSet.getString(it))
                }
            }

            return list
        }
    }

    private fun executeOperation(query : String) {
        val conn = DatabaseConfig.connect()
        val sqlQuery = query.trim() + ";"

        println(sqlQuery)

        val stmt = conn!!.createStatement()
        stmt.executeUpdate(sqlQuery)

        DatabaseConfig.close()
    }
}
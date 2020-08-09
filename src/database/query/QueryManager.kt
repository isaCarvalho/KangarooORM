package database.query

import database.statements.*

class QueryManager(private val tableName: String)
{
    fun createTable(columns : Array<String>) : QueryManager {
        Create().createTable(tableName, columns)
                .execute()

        return this
    }

    fun createSequence(propertyName : String) : QueryManager {
        Create().createSequence(tableName, propertyName)
                .execute()

        return this
    }

    fun select(fields : Array<String>, condition : String? = null) : ArrayList<MutableMap<String, String>> {
        return Select().select(tableName, fields, condition)
    }

    fun delete(condition: String? = null) : QueryManager {
        Delete().delete(tableName, condition)
                .execute()

        return this
    }

    fun update(values : MutableMap<String, String>, condition: String? = null) : QueryManager {
        Update().update(tableName, values, condition)
                .execute()

        return this
    }

    fun insert(fields: Array<String>, values: Array<String>) : QueryManager {
        Insert().insert(tableName, fields, values)
                .execute()

        return this
    }

    fun dropTable() : QueryManager {
        Drop().dropTable(tableName)
                .execute()

        return this
    }

    fun dropSequence() : QueryManager {
        val sequenceName = "${tableName}_seq"
        Drop().dropSequence(sequenceName)
                .execute()

        return this
    }
}
package database.query

import database.statements.*

class QueryManager(private val tableName: String)
{
    private val selectObject = Select()

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
        return selectObject.select(tableName, fields, condition)
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

    /**
     * Method that returns how many data the table contains.
     * @return Int
     */
    fun count() : Int = selectObject.count(tableName)

    /**
     * Method that returns the max int data the table contains.
     * @return Int
     */
    fun maxInt(field: String) : Int = selectObject.maxInt(field, tableName)

    /**
     * Method that returns the min int data the table contains.
     * @return Int
     */
    fun minInt(field: String) : Int = selectObject.minInt(field, tableName)

    /**
     * Method that returns the max float data the table contains.
     * @return Float
     */
    fun maxFloat(field: String) : Float = selectObject.maxFloat(field, tableName)

    /**
     * Method that returns the min float data the table contains.
     * @return Float
     */
    fun minFloat(field: String) : Float = selectObject.minFloat(field, tableName)

    /**
     * Method that returns the sum int data the table contains.
     * @return Int
     */
    fun sumInt(field: String) : Int = selectObject.sumInt(field, tableName)

    /**
     * Method that returns the average
     */
    fun avg(field: String) : Float = selectObject.avg(field, tableName)
}
package com.kangaroo.facades

import com.kangaroo.statements.*

class QueryFacade(private val tableName: String)
{
    private val selectObject = Select()

    /**
     * Method that creates a table with an array of columns
     * @param columns Array<String>
     * @return QueryManager
     */
    fun createTable(columns : Array<String>) : QueryFacade {
        Create().createTable(tableName, columns)
                .execute()

        return this
    }

    /**
     * Method that creates a sequence. Receives the property that is going to be the property with
     * the sequence value.
     * @param propertyName String
     * @return QueryManager
     */
    fun createSequence(propertyName : String) : QueryFacade {
        Create().createSequence(propertyName, tableName)
                .execute()

        return this
    }

    /**
     * Method that selects data from the database. Receives an array of fields and a where condition.
     * @param fields Array<String>
     * @param condition String?
     * @return ArrayList<MutableMap<String, String>>
     */
    fun select(fields : Array<String>, condition : String? = null) : ArrayList<MutableMap<String, String>> {
        return selectObject.selectAll(tableName, fields, condition)
    }

    /**
     * Method that deletes a row of the database.
     * @param condition String?
     * @return QueryManager
     */
    fun delete(condition: String? = null) : QueryFacade {
        Delete().delete(tableName, condition)
                .execute()

        return this
    }

    /**
     * Method that updates a row in the database. Receives a where condition.
     * @param values MutableMap<String, String>
     * @param condition String?
     * @return QueryManager
     */
    fun update(values : MutableMap<String, String>, condition: String? = null) : QueryFacade {
        Update().update(tableName, values, condition)
                .execute()

        return this
    }

    /**
     * Method that inserts a row in the database. Receives an array of fields and values.
     * @param fields Array<String>
     * @param values Array<String>
     * @return QueryManager
     */
    fun insert(fields: Array<String>, values: Array<String>) : QueryFacade {
        Insert().insert(tableName, fields, values)
                .execute()

        return this
    }

    /**
     * Method that drops a table.
     * @return QueryManager
     */
    fun dropTable() : QueryFacade {
        Drop().dropTable(tableName)
                .execute()

        return this
    }

    /**
     * Method that drops a sequence.
     * @return QueryManager
     */
    fun dropSequence() : QueryFacade {
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
     * @param field String
     * @return Int
     */
    fun maxInt(field: String) : Int = selectObject.maxInt(field, tableName)

    /**
     * Method that returns the min int data the table contains.
     * @param field String
     * @return Int
     */
    fun minInt(field: String) : Int = selectObject.minInt(field, tableName)

    /**
     * Method that returns the max float data the table contains.
     * @param field String
     * @return Float
     */
    fun maxFloat(field: String) : Float = selectObject.maxFloat(field, tableName)

    /**
     * Method that returns the min float data the table contains.
     * @param field String
     * @return Float
     */
    fun minFloat(field: String) : Float = selectObject.minFloat(field, tableName)

    /**
     * Method that returns the sum int data the table contains.
     * @param field String
     * @return Int
     */
    fun sumInt(field: String) : Int = selectObject.sumInt(field, tableName)

    /**
     * Method that returns the average
     * @param field String
     * @return Float
     */
    fun avg(field: String) : Float = selectObject.avg(field, tableName)
}
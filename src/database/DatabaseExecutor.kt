package database

import database.logger.Logger
import java.lang.NullPointerException
import java.sql.ResultSet
import java.sql.SQLException

object DatabaseExecutor
{
    var showQuery = false

    /**
     * Method that executes a query and returns an instance of
     * ResultSet or null, if there's no register returned by the
     * query
     * @param query
     */
    fun execute(query : String) : ResultSet? {
        // removes extra spaces in the query
        val sqlQuery = query.trim()

        printQuery(sqlQuery)

        // instantiates the resultSet with null value
        var resultSet : ResultSet? = null

        try {
            // connects the database
            val conn = DatabaseConfig.connect()

            // executes the query
            val stmt = conn!!.createStatement()
            resultSet = stmt.executeQuery(sqlQuery)

            // closes the connection
            DatabaseConfig.close()
        } catch (ex : SQLException) {
            Logger.write("SQL query throws an exception:", ex)
        } catch (ex : NullPointerException) {
            Logger.write("Null pointer exception:", ex)
        } finally {
            return resultSet
        }
    }

    /**
     * Method that executes an operation that does not returns anything,
     * like insert, update and delete.
     * @param query
     */
    fun executeOperation(query : String, insert : Boolean = false) {
        val conn = DatabaseConfig.connect()
        val sqlQuery = query.trim()

        printQuery(sqlQuery)

        try {
            val stmt = conn!!.createStatement()

            if (insert)
                stmt.execute(query)
            else
                stmt.executeUpdate(sqlQuery)

            DatabaseConfig.close()
        } catch (ex : SQLException) {
            Logger.write("SQL query throws an exception:", ex)
        } catch (ex : NullPointerException) {
            Logger.write("Null pointer exception:", ex)
        }
    }

    private fun printQuery(sqlQuery : String) {
        if (showQuery)
            println("\n\n$sqlQuery\n\n")
    }
}
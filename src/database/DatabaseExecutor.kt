package database

import java.lang.NullPointerException
import java.sql.ResultSet
import java.sql.SQLException

object DatabaseExecutor
{
    /**
     * Method that executes a query and returns an instance of
     * ResultSet or null, if there's no register returned by the
     * query
     * @param query
     */
    fun execute(query : String) : ResultSet? {
        // removes extra spaces in the query
        val sqlQuery = query.trim()
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
            ex.printStackTrace()
        } catch (ex : NullPointerException) {
            ex.printStackTrace()
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

        try {
            val stmt = conn!!.createStatement()

            if (insert)
                stmt.execute(query)
            else
                stmt.executeUpdate(sqlQuery)

            DatabaseConfig.close()
        } catch (ex : SQLException) {
            ex.printStackTrace()
        } catch (ex : NullPointerException) {
            ex.printStackTrace()
        }
    }
}
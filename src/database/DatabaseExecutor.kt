package database

import java.lang.NullPointerException
import java.sql.ResultSet
import java.sql.SQLException

object DatabaseExecutor
{
    fun execute(query : String) : ResultSet? {
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
            return resultSet
        }
    }

    fun executeOperation(query : String) {
        val conn = DatabaseConfig.connect()
        val sqlQuery = query.trim()

        println(sqlQuery)

        val stmt = conn!!.createStatement()
        stmt.executeUpdate(sqlQuery)

        DatabaseConfig.close()
    }
}
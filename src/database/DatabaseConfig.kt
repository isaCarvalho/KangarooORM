package database

import kotlin.properties.Delegates
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*

object DatabaseConfig {

    /**
     * Connection string that the database will use.
     * It's initiates in the first mention in the code, after the other values are setted.
     */
    private val connectionString : String by lazy {
        "jdbc:postgresql://$host:$port/$databaseName"
    }

    /**
     * The fields bellow are initiate by Delegates.vetoable, with a validation process.
     */

    // Database Name
    private var databaseName : String by Delegates.vetoable("") { _, _, newValue ->
        newValue.isNotEmpty()
    }

    // Host name
    private var host : String by Delegates.vetoable("") { _, _, newValue ->
        newValue.isNotEmpty()
    }

    // Port
    private var port : Int by Delegates.vetoable(0) { _, _, newValue ->
        newValue != 0 && (newValue.toString().length >= 4)
    }

    // Database user
    private var user : String by Delegates.vetoable("") { _, _, newValue ->
        newValue.isNotEmpty()
    }

    // Database password
    private var password : String by Delegates.vetoable("") { _, _, newValue ->
        newValue.isNotEmpty()
    }

    // Flag that determines whether it's going to be used SSL or not
    private var useSSL : Boolean = false

    // Properties of the connection
    private val connectionProps = Properties()
    // Instance of the connection. It will be null if the connection is not established.
    private var conn : Connection? = null

    /**
     * Method that establishes the connection with the database.
     */
    fun connect() : Connection? {
        connectionProps.apply {
            put("user", user)
            put("password", password)
            put("useSSL", useSSL)
        }

        return try {
            // gets the Postgres' driver class
            Class.forName("org.postgresql.Driver")

            conn = DriverManager.getConnection(connectionString, connectionProps)
            conn
        } catch (ex : SQLException) {
            ex.printStackTrace()
            null
        }
    }

    /**
     * Method that sets the values of the properties above. This method should be called in the very beginning of
     * your application.
     * @param host
     * @param port
     * @param user
     * @param password
     * @param databaseName
     * @param useSSL
     */
    fun setConfiguration(host : String, port : Int, user : String, password : String, databaseName : String, useSSL : Boolean, showQuery : Boolean = false) {
        DatabaseConfig.host = host
        DatabaseConfig.port = port
        DatabaseConfig.user = user
        DatabaseConfig.password = password
        DatabaseConfig.databaseName = databaseName
        DatabaseConfig.useSSL = useSSL
        DatabaseExecutor.showQuery = showQuery
    }

    /**
     * Lambda that closes the connection
     */
    val close = {
        conn!!.close()
    }
}
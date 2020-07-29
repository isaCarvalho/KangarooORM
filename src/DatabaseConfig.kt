import kotlin.properties.Delegates
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*

object DatabaseConfig {

    private val connectionString : String by lazy {
        "jdbc:postgresql://$host:$port/$databaseName"
    }

    private var databaseName : String by Delegates.vetoable("") { _, _, newValue ->
        newValue.isNotEmpty()
    }

    private var host : String by Delegates.vetoable("") { _, _, newValue ->
        newValue.isNotEmpty()
    }

    private var port : Int by Delegates.vetoable(0) { _, _, newValue ->
        newValue != 0 && (newValue.toString().length >= 4)
    }

    private var user : String by Delegates.vetoable("") { _, _, newValue ->
        newValue.isNotEmpty()
    }

    private var password : String by Delegates.vetoable("") { _, _, newValue ->
        newValue.isNotEmpty()
    }

    private var useSSL : Boolean = false

    private val connectionProps = Properties()
    private var conn : Connection? = null

    fun connect() : Connection? {
        connectionProps.apply {
            put("user", user)
            put("password", password)
            put("useSSL", useSSL)
        }

        return try {
            Class.forName("org.postgresql.Driver")

            conn = DriverManager.getConnection(connectionString, connectionProps)
            conn
        } catch (ex : SQLException) {
            ex.printStackTrace()
            null
        }
    }

    fun setConfiguration(host : String, port : Int, user : String, password : String, databaseName : String, useSSL : Boolean) {
        this.host = host
        this.port = port
        this.user = user
        this.password = password
        this.databaseName = databaseName
        this.useSSL = useSSL
    }

    fun close() {
        conn!!.close()
    }
}
import database.DatabaseConfig
import database.DatabaseManager

fun main() {


    val user = User(1, "User 1")

    DatabaseConfig.setConfiguration(
        "127.0.0.1",
        5432,
        "postgres",
        "123456",
        "test",
        false
    )

    val userManager = DatabaseManager()

    userManager.setEntity(User::class)
    userManager.select()
    userManager.insert(user)
}
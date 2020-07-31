import database.DatabaseConfig
import database.DatabaseManager

fun main() {

    val user = User(1, "User 1")
    val user2 = User(2, "User 2")

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
    userManager.insert(user)
    userManager.insert(user2)
    var map = userManager.select("WHERE id = 1")
    println(map)
    map = userManager.select()
    println(map)
    userManager.update(user)
    userManager.delete(user)
    userManager.dropTableAndSequence()
}
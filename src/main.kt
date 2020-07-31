import database.DatabaseConfig
import database.DatabaseManager

fun main() {

    DatabaseConfig.setConfiguration(
        "127.0.0.1",
        5432,
        "postgres",
        "123456",
        "test",
        false
    )

    // Creating the book

    val book = Book(1, "Book 1")
    val bookManager = DatabaseManager()

    bookManager.setEntity(Book::class)
    bookManager.insert(book)

    // Creating the users
    val user = User(1, "User 1", 1)
    val user2 = User(2, "User 2", 1)

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

    // dropping the tables
    userManager.dropTableAndSequence()
    bookManager.dropTableAndSequence()
}
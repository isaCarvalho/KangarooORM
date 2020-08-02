import database.DatabaseConfig
import database.statements.Query

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
    val bookQuery = Query(Book::class)
            .insert(book)

    // Creating the users
    val user = User(1, "User 1", 1)
    val user2 = User(2, "User 2", 1)

    val userQuery = Query(User::class)
            .insert(user)
            .insert(user2)

    var map = userQuery.select("WHERE id = 1")
    println(map)

    map = userQuery.select()
    println(map)

    userQuery.update(user).delete(user)


    // dropping the tables
    userQuery.dropTable()
    bookQuery.dropTable()
}
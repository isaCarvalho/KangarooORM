import database.DatabaseConfig
import database.statements.QueryManager

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
    val bookQuery = QueryManager(Book::class)
            .insert(book)

    // Creating the users
    val user = User(1, "User 1", 1)
    val user2 = User(2, "User 2", 1)

    val userQuery = QueryManager(User::class)
            .insert(user)
            .insert(user2)

    var map = userQuery.selectAll("WHERE id = 1")
    println(map)

    map = userQuery.selectAll()
    println(map)

    userQuery.update(user).delete(user)


    println(userQuery.select(User(3, "user 2", 1)))
    println(userQuery.select(user2))

    // dropping the tables
    userQuery.dropTable()
    bookQuery.dropTable()
}
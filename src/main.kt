import database.DatabaseConfig
import database.query.ModelQueryManager

fun main() {

    DatabaseConfig.setConfiguration(
        "127.0.0.1",
        5432,
        "postgres",
        "123456",
        "test",
        false,
        showQuery = true
    )

    // Creating the book

    val book = Book(1, "Book 1")
    val bookQuery = ModelQueryManager(Book::class)
            .insert(book)

    // Creating the users
    val user = User(1, "User 1", book)
    val user2 = User(2, "User 2", book)

    val userQuery = ModelQueryManager(User::class)
            .insert(user)
            .insert(user2)
//
//    println(userQuery.selectAll<User>("WHERE id = 1"))
//    println(userQuery.selectAll<User>())
//    println(userQuery.select<User>("id", "=", "1"))
//
    userQuery.update(user)
            .delete(user)
//
    println(userQuery.exists(User(3, "user 2", book)))
    println(userQuery.exists(user2))

    println(userQuery.count())
    println(userQuery.maxInt("id"))
    println(userQuery.minInt("id"))
    println(userQuery.sumInt("id"))
    println(userQuery.avg("id"))

    // dropping the tables
    userQuery.dropTable()
    bookQuery.dropTable()
}
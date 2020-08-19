import database.DatabaseConfig
import database.facades.ModelQueryFacade

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

    // Creating the houses
    val house = House(1, "Street 1")
    val house2 = House(2, "Street 2")

    val houseQuery = ModelQueryFacade(House::class)
            .insert(house)
            .insert(house2)

    // Creating the book

    val book = Book(1, "Book 1")

    val bookQuery = ModelQueryFacade(Book::class)
            .insert(book)

    // Creating the users
    val user = User(1, "User 1", book, 1)
    val user2 = User(2, "User 2", book, 2)

    val userQuery = ModelQueryFacade(User::class)
            .insert(user)
            .insert(user2)

    println(userQuery.find(1))
    println(userQuery.select("id = 1"))
    println(userQuery.selectAll("true"))

    userQuery.update(user)
            .delete(user)

    println(userQuery.count())
    println(userQuery.maxInt("id"))
    println(userQuery.minInt("id"))
    println(userQuery.sumInt("id"))
    println(userQuery.avg("id"))

    // dropping the tables
//    userQuery.dropTable()
//    bookQuery.dropTable()
//    houseQuery.dropTable()
}
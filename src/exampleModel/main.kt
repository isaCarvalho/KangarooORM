package exampleModel

import database.DatabaseConfig
import database.facades.ModelQueryFacade
import exampleModel.model.*

fun main() {

    DatabaseConfig.setConfiguration(
        "127.0.0.1",
        5432,
        "postgres",
        "123456",
        "test",
        false,
        showQuery = true,
        showQueryLog = true
    )

    // Creating the tables

    val houseQuery = ModelQueryFacade(House::class)
    val authorQuery = ModelQueryFacade(Author::class)
    val bookQuery = ModelQueryFacade(Book::class)
    val taskQuery = ModelQueryFacade(Task::class)
    val userQuery = ModelQueryFacade(User::class)

    // Creating the objects

    val house = House(1, "Street 1")
    val house2 = House(2, "Street 2")

    val author = Author(1, "Halliday")

    val book = Book(1, "Fundamentos da Fisica 1", author)

    val user = User(1, "User 1", house.id, book, listOf(Task(1, 1), Task(2, 1)))
    val user2 = User(2, "User 2", house2.id, book, listOf())

    // Inserting the objects

    houseQuery.insert(house)
        .insert(house2)

    bookQuery.insert(book) // do not insert the book before

    userQuery.insert(user)
            .insert(user2)

    // Selecting the users

    println(userQuery.find(1))
    println(userQuery.select("id = 1"))
    println(userQuery.selectAll("true"))

    userQuery.delete(user)
        .update(user)


    println(userQuery.count())
    println(userQuery.maxInt("id"))
    println(userQuery.minInt("id"))
    println(userQuery.sumInt("id"))
    println(userQuery.avg("id"))

    println(houseQuery.selectAll("true"))

    // dropping the tables

    taskQuery.dropTable()
    userQuery.dropTable()
    bookQuery.dropTable()
    houseQuery.dropTable()
    authorQuery.dropTable()
}
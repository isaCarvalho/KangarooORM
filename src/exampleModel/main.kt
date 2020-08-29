package exampleModel

import com.kangaroo.database.DatabaseConfig
import com.kangaroo.facades.ModelQueryFacade
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

    val author = Author("Halliday")
    val author2 = Author("Halliday R.")

    val book = Book("Fundamentos da Fisica 1", author)
    val book2 = Book("Fundamentos da Fisica 2", author2)

    val user = User("User 1", house.id, book, listOf(Task(1, 1), Task(2, 1)))
    val user2 = User("User 2", house2.id, book2, listOf())

    // Inserting the objects

    houseQuery.insert(house)
        .insert(house2)

    userQuery.insert(user)
            .insert(user2)

    // Selecting the users

    println(userQuery.find(1))
    println(userQuery.select("id = 1"))
    println(userQuery.selectAll("true"))

    userQuery.delete(user)
        .update(user)


    println("Exists house: ${houseQuery.exists(house)}")
    println("Exists user: ${userQuery.exists(user)}")
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
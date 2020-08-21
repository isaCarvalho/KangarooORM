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

    // Creating the houses
    val house = House(1, "Street 1")
    val house2 = House(2, "Street 2")

    val houseQuery = ModelQueryFacade(House::class)
            .insert(house)
            .insert(house2)

    // Creating author
    val author = Author(1, "Halliday")
    val authorQuery = ModelQueryFacade(Author::class)
            .insert(author)

    // Creating the book
    val book = Book(1, "Fundamentos da Fisica 1", author)
    val bookQuery = ModelQueryFacade(Book::class)
            .insert(book)

    // Creating the tasks
    val taskList = ArrayList<Task>()

    for (i in 1 until 10) {
        taskList.add(Task(i, 1))
    }

    val taskQuery = ModelQueryFacade(Task::class)

    // Creating the users

    val user = User(1, "User 1", 1, book, taskList.toList())
    val user2 = User(2, "User 2", 2, book, listOf())

    val userQuery = ModelQueryFacade(User::class)
            .insert(user)
            .insert(user2)

    // Inserting the tasks

    for (task in taskList) {
        taskQuery.insert(task)
    }

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
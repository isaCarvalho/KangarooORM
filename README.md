# Kangaroo ORM

Kangaroo is a Kotlin-Postgres ORM built for those who search for a reliable and easy way to implement 
data storage with Kotlin and Postgres in your applications.

# Example

This example can be found in the `exampleModel` folder.

* The `Author` class models the author related to the `Book`

```kotlin
import com.kangaroo.annotations.Property
import com.kangaroo.annotations.Table

@Table
class Author(
        @Property("name", "varchar", size = 255) var name : String,
        @Property("id", "int", primaryKey = true, autoIncrement = true) var id : Int = -1
) {
    override fun toString(): String {
        return "id: $id\nname: $name"
    }
}
```

* The `Book` class will be related to the users and has `one to one` relation with the `Author`

```kotlin
import com.kangaroo.annotations.ForeignKey
import com.kangaroo.annotations.OneToOne
import com.kangaroo.annotations.Property
import com.kangaroo.annotations.Table

@Table("books")
class Book (
        @Property("description", "varchar", size = 255)
        var description : String,
        @OneToOne(ForeignKey("fk_book_author", "author", "id"))
        var author: Author? = null,
        @Property("id", "int", true, autoIncrement = true)
        var id : Int = -1
) {
    override fun toString(): String {
        return "id: $id\ndescription: $description\nauthor: $author"
    }
}
```

* The `House` class is a simple class. User will make a reference to this class.

```kotlin
import com.kangaroo.annotations.Property
import com.kangaroo.annotations.Table

@Table("houses")
class House (
        @Property("id", "int", true) var id : Int,
        @Property("address", "varchar", size = 255) var address : String
)
```

* The `Task` class will be related to the `User` class.

```kotlin
import com.kangaroo.annotations.Property
import com.kangaroo.annotations.Table

@Table("tasks")
class Task(
        @Property("id", "int", primaryKey = true) var id : Int,
        @Property("assignment", "varchar", size = 255) var assignment : String,
        @Property("id_user", "int") var id_user : Int = -1
) {
    override fun toString(): String {
        return "id: $id"
    }
}
```

* The `Clothe` class will be related to the `User`.

```kotlin
import com.kangaroo.annotations.ForeignKey
import com.kangaroo.annotations.ManyToMany
import com.kangaroo.annotations.Property
import com.kangaroo.annotations.Table

@Table("clothes")
class Clothe(
        @Property("name", "varchar", size = 255)
        var name : String,
        @ManyToMany(ForeignKey("fk_clothe_user", "users_clothes", "id_user", deleteCascade = true))
        var users : List<User> = listOf(),
        @Property("id", "int", primaryKey = true, autoIncrement = true) var id : Int = -1
)
```

* This is the `User` class. Take a close look on all of its relations.

```kotlin
import com.kangaroo.annotations.*

@Table("users")
class User(
        @Property("name", "varchar", size = 255)
        var name : String,
        @Property("id_house", "int")
        @ForeignKey("fk_user_house", "houses", "id", deleteCascade = true)
        var id_house : Int,
        @OneToOne(ForeignKey("fk_user_book", "books", "id", deleteCascade = true))
        var book : Book? = null,
        @OneToMany(ForeignKey("fk_user_task", "tasks", "id_user", deleteCascade = true))
        var tasks : List<Task> = listOf(),
        @ManyToMany(ForeignKey("fk_user_clothe", "users_clothes", "id_clothe", deleteCascade = true))
        var clothes : List<Clothe> = listOf(),
        @Property("id", "int", true, autoIncrement = true)
        var id : Int = -1
) {
    override fun toString(): String {
        return "Name: $name\n" +
                "id: $id\n" +
                "id_house: $id_house\n" +
                "book: $book\n" +
                "task: $tasks\n" +
                "clothes: $clothes"
    }
}
```

* Now, let's take a look in the main function:

```kotlin
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
        showQuery = false,
        showQueryLog = false
    )

    // Creating the tables

    val houseQuery = ModelQueryFacade(House::class)
    val authorQuery = ModelQueryFacade(Author::class)
    val bookQuery = ModelQueryFacade(Book::class)
    val taskQuery = ModelQueryFacade(Task::class)
    val userQuery = ModelQueryFacade(User::class)
    val clotheQuery = ModelQueryFacade(Clothe::class)

    // Creating the objects

    val house = House(1, "Street 1")
    val house2 = House(2, "Street 2")

    val author = Author("Halliday")
    val author2 = Author("Halliday R.")

    val book = Book("Book 1", author)
    val book2 = Book("Book 2", author2)

    val clothes = listOf(Clothe("short"), Clothe("pants"), Clothe("shirt"))

    val user = User(
            "User 1",
            house.id,
            book,
            listOf(Task(1, "dishes"), Task(2, "clean")),
            clothes
    )
    val user2 = User("User 2", house2.id, book2, listOf(), clothes)

    // Inserting the objects -- DO NOT INSERT THE RELATION OBJECTS

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

    println(clotheQuery.selectAll("true"))

    // dropping the tables

    clotheQuery.dropTable()
    taskQuery.dropTable()
    userQuery.dropTable()
    bookQuery.dropTable()
    houseQuery.dropTable()
    authorQuery.dropTable()
}
```

You can run this code, and test Kangaroo main functions.

# Requirements

* Koltin >= 1.3
* Postgres >= 11

# Author

* Isabela Carvalho
* All contributors

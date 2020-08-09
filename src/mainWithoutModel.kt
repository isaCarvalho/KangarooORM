import database.DatabaseConfig
import database.query.QueryManager

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

    val clothesManager = QueryManager("clothes")
            .createTable(arrayOf(
                    "id int primary key not null",
                    "name varchar(255)"
            ))
            .createSequence("id")
            .insert(arrayOf("id", "name"), arrayOf("1", "'clothe 1'"))
            .insert(arrayOf("id", "name"), arrayOf("2", "'clothe 2'"))
            .insert(arrayOf("id", "name"), arrayOf("3", "'clothe 3'"))
            .update(mutableMapOf(Pair("name", "'Clothe 3'")), "id = 3")
            .delete("id = 2")

    println(clothesManager.select(arrayOf("id", "name")))

    clothesManager.dropTable()
    clothesManager.dropSequence()
}
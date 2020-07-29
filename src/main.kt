fun main() {

    DatabaseConfig.setConfiguration(
        "127.0.0.1",
        5432,
        "postgres",
        "123456",
        "test",
        false
    )

    val user = User(1, "User 1")
    user.insert(mapOf(
        "id" to "1",
        "name" to "'User 1'"
    ))

    val query = user.select("id", "name") +
            user.where("id", "=", "1") +
            user.and("name", "=", "'User 1'") +
            user.or("name", "=", "'User 2'") +
            user.orderBy("name", true)

    println(user.execute(query))
}
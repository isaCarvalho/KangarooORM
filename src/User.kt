import database.annotations.*

@Table("users")
class User(
        @Property("id", "int", true, autoIncrement = true)
        var id : Int,
        @Property("name", "varchar", size = 255)
        var name : String,
        @Property("id_house", "int")
        @ForeignKey("fk_user_house", "houses", "id", deleteCascade = true)
        var id_house : Int,
        @OneToOne(ForeignKey("fk_user_book", "books", "id", deleteCascade = true))
        var book : Book,
        @OneToMany(ForeignKey("fk_user_task", "tasks", "id_user"))
        var tasks : List<Task>
) {
    override fun toString(): String {
        return "Name: $name\n" +
                "id: $id\n" +
                "id_house: $id_house\n" +
                "book: $book\n" +
                "task: $tasks"
    }
}
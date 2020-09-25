package exampleModel.model

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
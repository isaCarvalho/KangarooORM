import database.annotations.ForeignKey
import database.annotations.OneToOne
import database.annotations.Property
import database.annotations.Table

@Table("users")
class User(
        @Property("id", "int", true, autoIncrement = true)
        var id : Int,
        @Property("name", "varchar", size = 255)
        var name : String,
        @Property("id_house", "int")
        @ForeignKey("fk_user_house", "houses", "id")
        var id_house : Int,
        @OneToOne(ForeignKey("fk_user_book", "books", "id"))
        var book : Book
) {
    override fun toString(): String {
        return "Name: $name\n" +
                "id: $id\n" +
                "id_house: $id_house\n" +
                "book: $book"
    }
}
import database.annotations.ForeignKey
import database.annotations.Property
import database.annotations.Table

@Table("users")
class User(
        @Property("id", "int", true, autoIncrement = true) var id : Int,
        @Property("name", "varchar", size = 255) var name : String,
        @Property("id_book", "int") @ForeignKey("fk_user_book", "books", "id") var id_book : Int
)
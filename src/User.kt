import database.ForeignKey
import database.Property
import database.Table

@Table("users")
class User(
        @Property("id", "int", true, autoIncrement = true) var id : Int,
        @Property("name", "varchar", size = 255) var name : String,
        @Property("id_book", "int") @ForeignKey("fk_user_book", "books", "id") var id_book : Int
)
import database.Property
import database.Table

@Table("users")
class User(
        @Property("id", "int", true, autoIncrement = true) var id : Int,
        @Property("name", "varchar", size = 255) var name : String
)
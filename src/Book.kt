import database.Property
import database.Table

@Table("books")
class Book (
        @Property("id", "int", true, autoIncrement = true) var id : Int,
        @Property("description", "varchar", size = 255) var description : String
)
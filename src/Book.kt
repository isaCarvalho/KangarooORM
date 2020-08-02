import database.annotations.Property
import database.annotations.Table

@Table("books")
class Book (
        @Property("id", "int", true, autoIncrement = true) var id : Int,
        @Property("description", "varchar", size = 255) var description : String
)
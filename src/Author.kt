import database.annotations.Property
import database.annotations.Table

@Table
class Author(
        @Property("id", "int", primaryKey = true) var id : Int,
        @Property("name", "varchar", size = 255) var name : String
) {
    override fun toString(): String {
        return "id: $id\nname: $name"
    }
}
import database.annotations.ForeignKey
import database.annotations.OneToOne
import database.annotations.Property
import database.annotations.Table

@Table("books")
class Book (
        @Property("id", "int", true, autoIncrement = true)
        var id : Int,
        @Property("description", "varchar", size = 255)
        var description : String,
        @OneToOne(ForeignKey("fk_book_author","author", "id"))
        var author: Author
) {
    override fun toString(): String {
        return "id: $id\ndescription: $description\nauthor: $author"
    }
}
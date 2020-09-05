package exampleModel.model

import com.kangaroo.annotations.ForeignKey
import com.kangaroo.annotations.OneToOne
import com.kangaroo.annotations.Property
import com.kangaroo.annotations.Table

@Table("books")
class Book (
        @Property("description", "varchar", size = 255)
        var description : String,
        @OneToOne(ForeignKey("fk_book_author", "author", "id"))
        var author: Author? = null,
        @Property("id", "int", true, autoIncrement = true)
        var id : Int = -1
) {
    override fun toString(): String {
        return "id: $id\ndescription: $description\nauthor: $author"
    }
}
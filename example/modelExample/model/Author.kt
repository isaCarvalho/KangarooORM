package exampleModel.model

import com.kangaroo.annotations.Property
import com.kangaroo.annotations.Table

@Table
class Author(
        @Property("name", "varchar", size = 255) var name : String,
        @Property("id", "int", primaryKey = true, autoIncrement = true) var id : Int = -1
) {
    override fun toString(): String {
        return "id: $id\nname: $name"
    }
}
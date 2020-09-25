package exampleModel.model

import com.kangaroo.annotations.Property
import com.kangaroo.annotations.Table

@Table("tasks")
class Task(
        @Property("id", "int", primaryKey = true) var id : Int,
        @Property("assignment", "varchar", size = 255) var assignment : String,
        @Property("id_user", "int") var id_user : Int = -1
) {
    override fun toString(): String {
        return "id: $id"
    }
}
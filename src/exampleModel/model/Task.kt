package exampleModel.model

import com.kangaroo.annotations.Property
import com.kangaroo.annotations.Table

@Table("tasks")
class Task(
        @Property("id", "int", primaryKey = true) var id : Int,
        @Property("id_user", "int") var id_user : Int
) {
    override fun toString(): String {
        return "id: $id"
    }
}
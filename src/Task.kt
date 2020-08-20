import database.annotations.Property
import database.annotations.Table

@Table("tasks")
class Task(
        @Property("id", "int", primaryKey = true, autoIncrement = true) var id : Int,
        @Property("id_user", "int") var id_user : Int
)
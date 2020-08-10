import database.annotations.Property
import database.annotations.Table

@Table("houses")
class House (
        @Property("id", "int", true, autoIncrement = true) var id : Int,
        @Property("address", "varchar", size = 255) var address : String
)
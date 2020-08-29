package exampleModel.model

import com.kangaroo.annotations.Property
import com.kangaroo.annotations.Table

@Table("houses")
class House (
        @Property("id", "int", true) var id : Int,
        @Property("address", "varchar", size = 255) var address : String
)
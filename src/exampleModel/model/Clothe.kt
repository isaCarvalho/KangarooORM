package exampleModel.model

import com.kangaroo.annotations.ForeignKey
import com.kangaroo.annotations.ManyToMany
import com.kangaroo.annotations.Property
import com.kangaroo.annotations.Table

@Table("clothes")
class Clothe(
        @Property("name", "varchar", size = 255)
        var name : String,
        @ManyToMany(ForeignKey("fk_clothe_user", "users_clothes", "id_user", deleteCascade = true))
        var users : List<User> = listOf(),
        @Property("id", "int", primaryKey = true, autoIncrement = true) var id : Int = -1
)
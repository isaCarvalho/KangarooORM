
class User(var id : Int, var name : String) : Entity() {

    init {
        super.properties = Pair("users", mapOf(
                "id" to Pair("int", 11),
                "name" to Pair("varchar", 255)
        ))
    }
}
package database.reflections
import database.annotations.Table
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.starProjectedType

class ReflectClass(val cls : KClass<*>) {

    val members by lazy {
        cls.declaredMemberProperties
    }

    /** List with the properties declared in the entity class */
    val properties = ArrayList<ReflectProperty>()

    /** Table name declared in the entity class */
    var tableName : String

    /** Constructor */
    val primaryConstructor : KFunction<Any> = cls::class.constructors.first()

    val type = cls.starProjectedType

    /**
     * init block
     */
    init {

        val table = cls.annotations.find { it is Table } as Table
        this.tableName = if (table.name == "") cls.simpleName!!.toLowerCase() else table.name

        // setting the properties
        this.members.forEach {
            properties.add(ReflectProperty(it))
        }
    }
}

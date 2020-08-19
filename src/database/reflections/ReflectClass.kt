package database.reflections
import database.annotations.Table
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredMemberProperties

class ReflectClass(val cls : KClass<*>) {

    val members by lazy {
        cls.declaredMemberProperties
    }

    /** List with the properties declared in the entity class */
    val properties = ArrayList<ReflectProperty>()

    /** Table name declared in the entity class */
    var tableName : String

    /** Constructor */
    val primaryConstructor : KFunction<*> = cls::class.constructors.first()

    /**
     * init block
     */
    init {

        val table = cls.annotations.find { it is Table } as Table
        this.tableName = if (table.name == "") cls::simpleName.toString().toLowerCase() else table.name

        // setting the properties
        this.members.forEach {
            properties.add(ReflectProperty(it))
        }
    }
}

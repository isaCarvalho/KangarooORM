package database

/**
 * This annotation receives the table name
 */
@Target(AnnotationTarget.CLASS)
annotation class Table constructor(val tableName : String)

/**
 * This annotation receives the property for the table
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class Property(
        val name : String,
        val type : String,
        val primaryKey : Boolean = false,
        val unique : Boolean = false,
        val nullable : Boolean = false,
        val autoIncrement : Boolean = false,
        val size : Int = -1
)

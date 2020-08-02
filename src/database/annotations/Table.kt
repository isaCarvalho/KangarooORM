package database.annotations

/**
 * This annotation receives the table name
 */
@Target(AnnotationTarget.CLASS)
annotation class Table constructor(val tableName : String)
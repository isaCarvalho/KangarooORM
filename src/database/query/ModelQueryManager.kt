package database.query

import database.DatabaseManager
import database.statements.*
import kotlin.reflect.KClass

class ModelQueryManager(cls : KClass<*>)
{
    /**
     * Database Manager instance. Contains the table and properties' information.
     */
    private val databaseManager = DatabaseManager()
    val selectObject = Select().setDatabaseManager(databaseManager)

    init {
        // sets the entity passed
        databaseManager.setEntity(cls)
        // creates the table
        Create().setDatabaseManager(databaseManager)
                .createTable()
                .createForeignKeyConstraints()
                .createOneToOne()
                .execute()
    }

    /**
     * Method that selects all the data in the table.
     * @param where
     * @return ArrayList<T>
     */

    inline fun <reified T: Any> selectAll(where : String? = null) : ArrayList<T> {
        return selectObject.selectAll(where)
    }

    inline fun <reified T: Any> select(field : String, operator : String, value : String) : T? {
        return selectObject.select<T>(field, operator, value)
    }

    inline fun <reified T: Any> select(where: String) : T? {
        return selectObject.select(where)
    }

    /**
     * Method that returns how many data the table contains.
     * @return Int
     */
    fun count() : Int = selectObject.count()

    /**
     * Method that returns the max int data the table contains.
     * @return Int
     */
    fun maxInt(field: String) : Int = selectObject.maxInt(field)

    /**
     * Method that returns the min int data the table contains.
     * @return Int
     */
    fun minInt(field: String) : Int = selectObject.minInt(field)

    /**
     * Method that returns the max float data the table contains.
     * @return Float
     */
    fun maxFloat(field: String) : Float = selectObject.maxFloat(field)

    /**
     * Method that returns the min float data the table contains.
     * @return Float
     */
    fun minFloat(field: String) : Float = selectObject.minFloat(field)

    /**
     * Method that returns the sum int data the table contains.
     * @return Int
     */
    fun sumInt(field: String) : Int = selectObject.sumInt(field)

    /**
     * Method that returns the average
     */
    fun avg(field: String) : Float = selectObject.avg(field)

    /**
     * Method that selects a entity. If the entity does not exists in the database,
     * it will return null.
     * @param entity
     * @return Entity?
     */
    fun <T : Any> exists(entity: T) : Boolean {
        return selectObject.exists(entity)
    }

    /**
     * Method that inserts a entity
     * @param entity
     */
    fun <T : Any> insert(entity : T) : ModelQueryManager {
        Insert().setDatabaseManager(databaseManager)
                .insert(entity)
                .execute()

        return this
    }

    /**
     * Method that deletes a entity
     * @param entity
     */
    fun <T : Any> delete(entity : T) : ModelQueryManager {
        Delete().setDatabaseManager(databaseManager)
                .delete(entity)
                .execute()

        return this
    }

    /**
     * Method that updates a entity
     * @param entity
     */
    fun <T : Any> update(entity : T) : ModelQueryManager {
        Update().setDatabaseManager(databaseManager)
                .update(entity)
                .execute()

        return this
    }

    /**
     * Method that drops a table.
     */
    fun dropTable() {
        Drop().setDatabaseManager(databaseManager)
                .dropTableAndSequence()
                .execute()
    }
}
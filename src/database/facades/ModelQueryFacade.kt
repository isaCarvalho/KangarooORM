package database.facades

import database.DatabaseManager
import database.statements.*
import kotlin.reflect.KClass

class ModelQueryFacade(cls : KClass<*>)
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
                .createRelations()
                .execute()
    }

    /**
     * Method that selects all the data in the table.
     * @param where
     * @return ArrayList<Any>
     */

    inline fun <reified T : Any> selectAll(where : String? = null) : ArrayList<Any> {
        return selectObject.selectAll<T>(where)
    }

    inline fun <reified T : Any> select(field : String, operator : String, value : String) : Any? {
        return selectObject.select<T>(field, operator, value)
    }

    inline fun <reified T : Any> select(where: String) : Any? {
        return selectObject.select<T>(where)
    }

    /**
     * Method that returns how many data the table contains.
     * @return Int
     */
    fun count() : Int = selectObject.count(databaseManager.reflectClass.tableName)

    /**
     * Method that returns the max int data the table contains.
     * @return Int
     */
    fun maxInt(field: String) : Int = selectObject.maxInt(field, databaseManager.reflectClass.tableName)

    /**
     * Method that returns the min int data the table contains.
     * @return Int
     */
    fun minInt(field: String) : Int = selectObject.minInt(field, databaseManager.reflectClass.tableName)

    /**
     * Method that returns the max float data the table contains.
     * @return Float
     */
    fun maxFloat(field: String) : Float = selectObject.maxFloat(field, databaseManager.reflectClass.tableName)

    /**
     * Method that returns the min float data the table contains.
     * @return Float
     */
    fun minFloat(field: String) : Float = selectObject.minFloat(field, databaseManager.reflectClass.tableName)

    /**
     * Method that returns the sum int data the table contains.
     * @return Int
     */
    fun sumInt(field: String) : Int = selectObject.sumInt(field, databaseManager.reflectClass.tableName)

    /**
     * Method that returns the average
     */
    fun avg(field: String) : Float = selectObject.avg(field, databaseManager.reflectClass.tableName)

    /**
     * Method that selects a entity. If the entity does not exists in the database,
     * it will return null.
     * @param entity
     * @return Entity?
     */
    fun exists(entity: Any) : Boolean {
        return selectObject.exists(entity)
    }

    /**
     * Method that inserts a entity
     * @param entity
     */
    fun insert(entity : Any) : ModelQueryFacade {
        Insert().setDatabaseManager(databaseManager)
                .insert(entity)
                .execute()

        return this
    }

    /**
     * Method that deletes a entity
     * @param entity
     */
    fun delete(entity : Any) : ModelQueryFacade {
        Delete().setDatabaseManager(databaseManager)
                .delete(entity)
                .execute()

        return this
    }

    /**
     * Method that updates a entity
     * @param entity
     */
    fun update(entity : Any) : ModelQueryFacade {
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
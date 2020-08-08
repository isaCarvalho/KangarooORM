package database.statements

import database.DatabaseManager
import kotlin.reflect.KClass

class QueryManager(cls : KClass<*>)
{
    /**
     * Database Manager instance. Contains the table and properties' information.
     */
    val databaseManager = DatabaseManager()

    init {
        // sets the entity passed
        databaseManager.setEntity(cls)
        // creates the table
        Create().createTable(databaseManager)
                .createForeignKeyConstraints()
                .execute()
    }

    /**
     * Method that selects all the data in the table.
     * @param where
     * @return ArrayList<T>
     */

    inline fun <reified T: Any> selectAll(where : String? = null) : ArrayList<T> {
        return Select().selectAll(databaseManager, where)
    }

    inline fun <reified T: Any> select(field : String, operator : String, value : String) : T? {
        return Select().select<T>(field, operator, value, databaseManager)
    }

    inline fun <reified T: Any> select(where: String) : T? {
        return Select().select(where, databaseManager)
    }

    /**
     * Method that returns how many data the table contains.
     * @return Int
     */
    fun count() : Int = Select().count(databaseManager)

    /**
     * Method that returns the max int data the table contains.
     * @return Int
     */
    fun maxInt(field: String) : Int = Select().maxInt(field, databaseManager)

    /**
     * Method that returns the min int data the table contains.
     * @return Int
     */
    fun minInt(field: String) : Int = Select().minInt(field, databaseManager)

    /**
     * Method that returns the max float data the table contains.
     * @return Float
     */
    fun maxFloat(field: String) : Float = Select().maxFloat(field, databaseManager)

    /**
     * Method that returns the min float data the table contains.
     * @return Float
     */
    fun minFloat(field: String) : Float = Select().minFloat(field, databaseManager)

    /**
     * Method that returns the sum int data the table contains.
     * @return Int
     */
    fun sumInt(field: String) : Int = Select().sumInt(field, databaseManager)

    /**
     * Method that returns the average
     */
    fun avg(field: String) : Float = Select().avg(field, databaseManager)

    /**
     * Method that selects a entity. If the entity does not exists in the database,
     * it will return null.
     * @param entity
     * @return Entity?
     */
    fun <T : Any> exists(entity: T) : Boolean {
        return Select().exists(entity, databaseManager)
    }

    /**
     * Method that inserts a entity
     * @param entity
     */
    fun <T : Any> insert(entity : T) : QueryManager {
        Insert().insert(entity, databaseManager)
                .execute()

        return this
    }

    /**
     * Method that deletes a entity
     * @param entity
     */
    fun <T : Any> delete(entity : T) : QueryManager {
        Delete().delete(entity, databaseManager)
                .execute()

        return this
    }

    /**
     * Method that updates a entity
     * @param entity
     */
    fun <T : Any> update(entity : T) : QueryManager {
        Update().update(entity, databaseManager)
                .execute()

        return this
    }

    /**
     * Method that drops a table.
     */
    fun dropTable() {
        Drop().dropTableAndSequence(databaseManager)
                .execute()
    }
}
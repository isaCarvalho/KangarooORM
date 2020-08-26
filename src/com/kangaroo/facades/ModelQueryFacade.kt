package com.kangaroo.facades

import com.kangaroo.DatabaseManager
import com.kangaroo.statements.*
import kotlin.reflect.KClass

class ModelQueryFacade(cls : KClass<*>)
{
    /**
     * Database Manager instance. Contains the table and properties' information.
     */
    private val databaseManager = DatabaseManager()
    private val selectObject = Select().setDatabaseManager(databaseManager)

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
     * Method that inserts a entity
     * @param entity
     */
    fun insert(entity : Any) : ModelQueryFacade {
        Insert().setDatabaseManager(databaseManager)
                .insert(entity)

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

    fun find(id: Int) : Any? {
        return selectObject.find(id, databaseManager.reflectClass.type)
    }

    fun select(where: String) : Any? {
        return selectObject.select(where, databaseManager.reflectClass.type)
    }

    fun selectAll(where: String) : List<Any> {
        return selectObject.selectAll(where, databaseManager.reflectClass.type)
    }
}
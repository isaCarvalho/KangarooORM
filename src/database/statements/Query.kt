package database.statements

import database.DatabaseManager
import kotlin.reflect.KClass

class Query(cls : KClass<*>)
{
    private val databaseManager = DatabaseManager()

    init {
        databaseManager.setEntity(cls)
        Create(databaseManager).createTable()
    }

    fun select(where : String? = null) : MutableMap<Int, MutableMap<String, String>> {
        return Select(databaseManager).selectWhere(where)
    }

    fun <T : Any> insert(entity : T) : Query {
        Insert(databaseManager).insertEntity(entity)

        return this
    }

    fun <T : Any> delete(entity : T) : Query {
        Delete(databaseManager).deleteEntity(entity)

        return this
    }

    fun <T : Any> update(entity : T) : Query {
        Update(databaseManager).updateEntity(entity)

        return this
    }

    fun dropTable() : Query {
        Drop(databaseManager).dropTableAndSequence()

        return this
    }
}
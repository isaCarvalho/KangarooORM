package database.statements

import database.DatabaseManager
import kotlin.reflect.KClass

class QueryManager(cls : KClass<*>)
{
    private val databaseManager = DatabaseManager()

    init {
        databaseManager.setEntity(cls)
        Create().createTable(databaseManager)
                .createForeignKeyConstraints()
                .execute()
    }

    fun select(where : String? = null) : MutableMap<Int, MutableMap<String, String>> {
        return Select().select(databaseManager, where)
    }

    fun <T : Any> insert(entity : T) : QueryManager {
        Insert().insert(entity, databaseManager)
                .execute()

        return this
    }

    fun <T : Any> delete(entity : T) : QueryManager {
        Delete().delete(entity, databaseManager)
                .execute()

        return this
    }

    fun <T : Any> update(entity : T) : QueryManager {
        Update().update(entity, databaseManager)
                .execute()

        return this
    }

    fun dropTable() : QueryManager {
        Drop().dropTableAndSequence(databaseManager)
                .execute()

        return this
    }
}
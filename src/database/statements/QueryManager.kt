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

    fun selectAll(where : String? = null) : MutableMap<Int, MutableMap<String, String>> {
        return Select().selectAll(databaseManager, where)
    }

    fun count() : Int = Select().count(databaseManager)

    fun <T : Any> select(entity: T) : T? {
        return Select().select(entity, databaseManager)
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
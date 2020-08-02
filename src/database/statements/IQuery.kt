package database.statements

/**
 * Query Interface.
 */
interface IQuery
{
    var sqlQuery : String

    fun execute()
}
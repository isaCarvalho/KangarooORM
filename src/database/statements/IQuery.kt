package database.statements

interface IQuery
{
    var sqlQuery : String

    fun execute()
}
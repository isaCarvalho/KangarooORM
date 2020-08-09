package database.statements

import database.DatabaseExecutor
import database.DatabaseHelper
import database.DatabaseManager

class Create : IQuery {

    override lateinit var databaseManager : DatabaseManager
    override var sqlQuery: String = ""

    override fun setDatabaseManager(databaseManager: DatabaseManager): Create {
        this.databaseManager = databaseManager
        return this
    }

    /**
     * Method that creates the table with an entity
     */
    fun createTable() : Create {
        sqlQuery = ""
        var sequenceQuery = ""

        sqlQuery += "CREATE TABLE IF NOT EXISTS ${databaseManager.tableName} (\n"
        databaseManager.propertiesList.forEach {
            sqlQuery += "${it.name} ${it.type}"

            if (it.type !in DatabaseHelper.numericTypes) {
                sqlQuery += "(${it.size})"
            }

            if (it.primaryKey) {
                sqlQuery += " primary key"
            }

            if (!it.nullable) {
                sqlQuery += " not null"
            }

            if (it.unique) {
                sqlQuery += " unique"
            }

            sqlQuery += if (databaseManager.propertiesList.indexOf(it) == databaseManager.propertiesList.size - 1)
                "\n);"
            else
                ",\n"

            // creates a sequence in case of an auto increment attribute
            if (it.autoIncrement) {
                sequenceQuery = createSequence(it.name)
            }
        }

        sqlQuery += "\n" + sequenceQuery + "\n"

        return this
    }

    /**
     * Creates a table without the entity
     */
    fun createTable(tableName : String, columns : Array<String>) : Create {
        sqlQuery += "CREATE TABLE $tableName (\n"

        columns.forEach {
            sqlQuery += it

            sqlQuery += if (columns.indexOf(it) == columns.size -1)
                "\n);"
            else
                ",\n"
        }

        return this
    }

    /**
     * Method that creates all of the table's foreign key constraints
     */
    fun createForeignKeyConstraints() : Create {

        databaseManager.foreignKeyList.forEach {
            val propertyName = it.key
            val foreignKey = it.value

            // verifies if the constraint already exists using pg_catalog
            val constraintQuery = "SELECT con.* FROM pg_catalog.pg_constraint con " +
                    "WHERE conname = '${foreignKey.constraintName}';"

            val result = DatabaseExecutor.execute(constraintQuery)
            // if it does not exists, it will creates the constraint
            if (result != null && result.findColumn("conname") == -1) {

                sqlQuery += "ALTER TABLE ${databaseManager.tableName} ADD CONSTRAINT ${foreignKey.constraintName}\n" +
                        "FOREIGN KEY ($propertyName)\n" +
                        "REFERENCES ${foreignKey.referencedTable}(${foreignKey.referencedProperty})"

                if (foreignKey.deleteCascade) {
                    sqlQuery += "ON DELETE CASCADE\n"
                }

                if (foreignKey.updateCascade) {
                    sqlQuery += "ON UPDATE CASCADE\n"
                }

                sqlQuery += ";"
            }
        }

        return this
    }

    /**
     * Method that creates a sequence with a entity.
     * @param propertyName
     * @return String
     */
    private fun createSequence(propertyName : String) : String {
        val sequenceName = "${databaseManager.tableName}_seq"
        return createSequence(databaseManager.tableName, sequenceName, propertyName)
    }

    /**
     * Method that creates a sequence without any entity.
     * @param tableName
     * @param propertyName
     * @return String
     */
    fun createSequence(tableName: String, propertyName : String) : Create {
        val sequenceName = "${tableName}_seq"
        sqlQuery += createSequence(tableName, sequenceName, propertyName)

        return this
    }

    /**
     * Method that generates the sql to create a sequence.
     * @param sequenceName
     * @param propertyName
     * @param tableName
     * @return String
     */
    private fun createSequence(tableName: String, sequenceName : String, propertyName : String) : String {
        return "CREATE SEQUENCE IF NOT EXISTS $sequenceName INCREMENT 1 MINVALUE 1 START 1;\n" +
                "ALTER TABLE $tableName ALTER COLUMN $propertyName SET DEFAULT nextval('$sequenceName');\n"
    }

    override fun execute() {
        DatabaseExecutor.executeOperation(sqlQuery)
    }
}
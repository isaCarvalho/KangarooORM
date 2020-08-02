package database.statements

import database.DatabaseExecutor
import database.DatabaseHelper
import database.DatabaseManager

class Create(private val databaseManager: DatabaseManager) {

    /**
     * Method that creates the table
     */
    fun createTable() {
        var sqlQuery = ""
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
                sequenceQuery = createSequence(databaseManager.tableName + "_seq", it.name)
            }
        }

        DatabaseExecutor.executeOperation(sqlQuery)
        DatabaseExecutor.executeOperation(sequenceQuery)
        createForeignKeyConstraints()
    }

    /**
     * Method that creates all of the table's foreign key constraints
     */
    private fun createForeignKeyConstraints() {
        var sqlQuery = ""

        databaseManager.foreignKeyList.forEach {
            val propertyName = it.key
            val foreignKey = it.value

            // verifies if the constraint already exists using pg_catalog
            val constraintQuery = "SELECT con.* FROM pg_catalog.pg_constraint con " +
                    "WHERE conname = '${foreignKey.constraintName}';"

            val result = DatabaseExecutor.execute(constraintQuery)
            // if it does not exists, it will creates the constraint
            if (result == null) {

                sqlQuery += "ALTER TABLE ${databaseManager.propertiesList} ADD CONSTRAINT ${foreignKey.constraintName}\n" +
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

        // executes the constraint creation
        if (sqlQuery.isNotEmpty())
            DatabaseExecutor.executeOperation(sqlQuery)
    }

    private fun createSequence(sequenceName : String, propertyName : String) : String {
        return "CREATE SEQUENCE IF NOT EXISTS $sequenceName INCREMENT 1 MINVALUE 1 START 1;\n" +
                "ALTER TABLE ${databaseManager.tableName} ALTER COLUMN $propertyName SET DEFAULT nextval('$sequenceName');\n"
    }
}
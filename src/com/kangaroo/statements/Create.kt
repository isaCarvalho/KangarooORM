package com.kangaroo.statements

import com.kangaroo.database.DatabaseExecutor
import com.kangaroo.database.DatabaseHelper
import com.kangaroo.database.DatabaseHelper.getPrimaryKeyOrNull
import com.kangaroo.database.DatabaseManager
import com.kangaroo.annotations.ForeignKey
import com.kangaroo.annotations.OneToMany
import com.kangaroo.annotations.OneToOne

class Create : Query() {

    override lateinit var databaseManager : DatabaseManager
    override var sqlQuery: String = ""

    override fun setDatabaseManager(databaseManager: DatabaseManager): Create {
        this.databaseManager = databaseManager
        return this
    }

    /**
     * Method that creates the table with an entity's properties
     */
    fun createTable() : Create {
        sqlQuery = ""
        var sequenceQuery = ""

        sqlQuery += "CREATE TABLE IF NOT EXISTS $tableName (\n"
        properties.forEach {
            if (it.propertyAnnotation != null) {
                sqlQuery += "${it.name} ${it.propertyAnnotation!!.type}"

                if (it.propertyAnnotation!!.type !in DatabaseHelper.numericTypes) {
                    sqlQuery += "(${it.propertyAnnotation!!.size})"
                }

                if (it.propertyAnnotation!!.primaryKey) {
                    sqlQuery += " primary key"
                }

                if (!it.propertyAnnotation!!.nullable) {
                    sqlQuery += " not null"
                }

                if (it.propertyAnnotation!!.unique) {
                    sqlQuery += " unique"
                }

                sqlQuery += ",\n"


                // creates a sequence in case of an auto increment attribute
                if (it.propertyAnnotation!!.autoIncrement) {
                    sequenceQuery = createSequence(tableName, "${tableName}_seq", it.name)
                }
            }
        }

        sqlQuery = formatQuery(sqlQuery) + ");\n"
        sqlQuery += sequenceQuery + "\n"

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


    fun createRelations() : Create {
        properties.forEach {
            when(it.relation) {
                is OneToOne -> {
                    val relation = it.relation as OneToOne
                    sqlQuery += createConstraint(
                            tableName,
                            relation.foreignKey.constraintName,
                            relation.foreignKey.referencedProperty,
                            relation.foreignKey.referencedTable,
                            "id_${it.name}",
                            relation.foreignKey.deleteCascade,
                            relation.foreignKey.updateCascade,
                            true
                    )
                }

                is OneToMany -> {
                    val relation = it.relation as OneToMany
                    val referencedProperty = getPrimaryKeyOrNull(properties)

                    sqlQuery += createConstraint(
                            relation.foreignKey.referencedTable,
                            relation.foreignKey.constraintName,
                            referencedProperty!!.name,
                            tableName,
                            relation.foreignKey.referencedProperty,
                            relation.foreignKey.deleteCascade,
                            relation.foreignKey.updateCascade
                    )
                }

                is ForeignKey -> {
                    val relation = it.relation as ForeignKey
                    sqlQuery += createConstraint(
                            tableName,
                            relation.constraintName,
                            relation.referencedProperty,
                            relation.referencedTable,
                            it.name,
                            relation.deleteCascade,
                            relation.updateCascade
                    )
                }
            }
        }

        return this
    }

    private fun createConstraint(
            tableName: String,
            constraintName : String,
            referencedProperty : String,
            referencedTable : String,
            propertyName: String,
            deleteCascade: Boolean,
            updateCascade: Boolean,
            createColumn : Boolean = false
    ) : String {
        var sqlQuery = ""

        // verifies if the constraint already exists using pg_catalog
        val constraintQuery = "SELECT con.* FROM pg_catalog.pg_constraint con " +
                "WHERE conname = '${constraintName}';"

        val result = DatabaseExecutor.execute(constraintQuery)
        // if it does not exists, it will creates the constraint
        if (result != null && !result.next()) {

            if (createColumn)
                sqlQuery += "\nALTER TABLE $tableName ADD COLUMN $propertyName INT;\n"

            sqlQuery += "\nALTER TABLE $tableName ADD CONSTRAINT ${constraintName}\n" +
                    "FOREIGN KEY ($propertyName)\n" +
                    "REFERENCES ${referencedTable}(${referencedProperty})"

            if (deleteCascade) {
                sqlQuery += " ON DELETE CASCADE"
            }

            if (updateCascade) {
                sqlQuery += " ON UPDATE CASCADE"
            }

            sqlQuery += ";"
        }

        return sqlQuery
    }

    /**
     * Method that creates a sequence with a entity.
     * @param propertyName
     * @param tableName String?
     * @return Create
     */
    fun createSequence(propertyName : String, tableName: String? = null) : Create {
        val table = tableName ?: this.tableName
        sqlQuery += createSequence(table, "${table}_seq", propertyName)

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
package database

import kotlin.reflect.*
import kotlin.reflect.full.*

/**
 * This class is the one how is going to create and manipulate the database
 */
class DatabaseManager {

    /** List with the properties annotations declared in the entity class */
    private val propertiesList = ArrayList<Property>()

    /** List with the foreign keys annotations declared in the entity class */
    private val foreignKeyList = mutableMapOf<String, ForeignKey>()

    /** Table name declared in the entity class */
    private lateinit var tableName : String

    /** Entity class */
    private lateinit var cls : KClass<*>

    /** Postgres' numeric types */
    private val numericTypes = arrayListOf("int", "float", "double", "long", "short")

    /** instance of DatabaseExecutor */
    private val databaseExecutor = DatabaseExecutor

    /**
     * Method that sets the entity class and create its table according to the properties
     * @param c
     */
    fun <T : Any> setEntity(c : KClass<T>) {
        // setting the entity class and table name
        this.cls = c::class

        val tableName = c.annotations.find { it is Table } as Table
        this.tableName = tableName.tableName

        // setting the properties
        c.memberProperties.forEach {

            val property = it.annotations.find { annotation -> annotation is Property }
            if (property != null)
                propertiesList.add(property as Property)
        }

        c.memberProperties.forEach {
            val foreignKey = it.annotations.find { annotation -> annotation is ForeignKey }
            if (foreignKey != null)
                foreignKeyList[it.name] = foreignKey as ForeignKey
        }

        // creating the table
        createTable()
    }

    /**
     * Method that selects the values from the database.
     * @param where
     * @return unit
     */
    fun select(where : String? = null) : MutableMap<Int, MutableMap<String, String>> {
        // initiates the query with the select statement
        var sqlQuery = "SELECT "

        // for each property, puts its name in the select's fields.
        propertiesList.forEach {
            sqlQuery += it.name
            sqlQuery += if (propertiesList.indexOf(it) == propertiesList.size - 1)
                " "
            else
                ", "
        }

        // appends the from statement
        sqlQuery += "FROM $tableName"

        // appends the where statement
        if (where != null) {
            sqlQuery += " $where"
        }

        // executes the query and puts the result inside of a mutable map
        val result = DatabaseExecutor.execute(sqlQuery)
        val resultMap = mutableMapOf<Int, MutableMap<String, String>>()

        var index = 0
        while (result!!.next()) {
            val map = mutableMapOf<String, String>()
            propertiesList.forEach {
                val value = result.getString(it.name)
                map[it.name] = value
            }

            resultMap[index] = map
            index++
        }

        // returns the result converted to a mutable map
        return resultMap
    }

    /**
     * Method that inserts an entity in the database
     * @param entity
     */
    fun <T : Any> insert(entity : T) {
        // gets the entity's declared properties
        val members = entity::class.declaredMemberProperties

        // initiates the query with the insert statement
        var sqlQuery = "INSERT INTO $tableName ("

        // puts the properties' names in the insert's fields
        members.forEach {
            // checks if the member is a mapped property
            val property = getMappedPropertyOrNull(it.name)

            if (property != null) {
                sqlQuery += it.name

                sqlQuery += if (members.indexOf(it) == members.size - 1)
                    ") VALUES \n("
                else
                    ", "
            }
        }

        // puts the entity's declaredMember values in the insert's values
        members.forEach {
            val property = getMappedPropertyOrNull(it.name)

            // checks if the member is a mapped property
            if (property != null) {
                // converts the entity's declaredMember to a mutable property, so we can retrieve the values.
                val prop = it as KMutableProperty1<T, *>
                // gets the value
                val value = prop.get(entity)

                // checks if we have to wrap the value in ''
                sqlQuery += checkNumericTypes(property.type, value.toString())

                sqlQuery += if (members.indexOf(it) == members.size - 1)
                    ");\n"
                else
                    ", "
            }
        }

        databaseExecutor.executeOperation(sqlQuery, true)
    }

    /**
     * Method that deletes an entity from the database
     * @param entity
     */
    fun <T : Any> delete(entity : T) {
        // initiates the query with the delete statements
        var sqlQuery = "DELETE FROM $tableName WHERE "

        // gets the entity's declaredMember
        val members = entity::class.declaredMemberProperties

        members.forEach {
            val property = getMappedPropertyOrNull(it.name)

            // checks if the member is a mapped property
            if (property != null) {
                val prop = it as KMutableProperty1<T, *>
                val value = prop.get(entity)

                sqlQuery += "${it.name} = "
                sqlQuery += checkNumericTypes(property.type, value.toString())

                if (members.indexOf(it) != members.size -1) {
                    sqlQuery+= " AND "
                }
            }
        }
        sqlQuery += ";"

        databaseExecutor.executeOperation(sqlQuery)
    }

    /**
     * Method that update a entity in the database
     * @param entity
     * @return unit
     */
    fun <T : Any> update(entity : T) {
        // initiates the query with the update statement
        var sqlQuery = "UPDATE $tableName SET "

        // gets the entity's declaredMembers
        val members = entity::class.declaredMemberProperties

        // for each member, sets the new value
        members.forEach {
            val property = getMappedPropertyOrNull(it.name)

            if (property != null) {
                val prop = it as KMutableProperty1<T, *>
                val value = prop.get(entity)

                sqlQuery += "${it.name} = "
                sqlQuery += checkNumericTypes(property.type, value.toString())

                if (members.indexOf(it) != members.size -1) {
                    sqlQuery+= ", "
                }
            }
        }

        // searches for the primary key for the where statement
        members.forEach {
            val prop = it as KMutableProperty1<T, *>
            val value = prop.get(entity)

            val property = getMappedPropertyOrNull(it.name)
            if (getPrimaryKeyOrNull() != null && property != null && property.primaryKey)
                sqlQuery += " WHERE ${it.name} = $value"
        }
        sqlQuery += ";"

        databaseExecutor.executeOperation(sqlQuery)
    }

    /**
     * Method that drops a table and a sequence
     */
    fun dropTableAndSequence() {
        val sequenceName = tableName + "_seq"

        val sqlQuery = "DROP TABLE IF EXISTS $tableName;\n" +
                "DROP SEQUENCE IF EXISTS $sequenceName;"

        databaseExecutor.executeOperation(sqlQuery)
    }

    /**
     * Method that creates the table
     */
    private fun createTable() {
        var sqlQuery = ""
        var sequenceQuery = ""

        sqlQuery += "CREATE TABLE IF NOT EXISTS $tableName (\n"
        propertiesList.forEach {
            sqlQuery += "${it.name} ${it.type}"

            if (it.type !in numericTypes) {
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

            sqlQuery += if (propertiesList.indexOf(it) == propertiesList.size - 1)
                "\n);"
            else
                ",\n"

            // creates a sequence in case of an auto increment attribute
            if (it.autoIncrement) {
                sequenceQuery = createSequence(tableName + "_seq", it.name)
            }
        }

        databaseExecutor.executeOperation(sqlQuery)
        databaseExecutor.executeOperation(sequenceQuery)
        databaseExecutor.executeOperation(createForeignKeyConstraints())
    }

    /**
     * Method that creates all of the table's foreign key constraints
     */
    private fun createForeignKeyConstraints() : String {
        var sqlQuery = ""

        foreignKeyList.forEach {
            val propertyName = it.key
            val foreignKey = it.value

            sqlQuery += "ALTER TABLE $tableName ADD CONSTRAINT ${foreignKey.constraintName}\n" +
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

        println(sqlQuery)
        return sqlQuery
    }

    private fun createSequence(sequenceName : String, propertyName : String) : String {
        return "CREATE SEQUENCE IF NOT EXISTS $sequenceName INCREMENT 1 MINVALUE 1 START 1;\n" +
            "ALTER TABLE $tableName ALTER COLUMN $propertyName SET DEFAULT nextval('$sequenceName');\n"
    }

    /**
     * Checks if the type of the value is numeric. If it is not,
     * returns the value embraced with ''
     * @param type
     * @param value
     * @return String
     */
    private fun checkNumericTypes(type : String, value : String) : String {
        return if (type in numericTypes)
            value
        else
            "'$value'"
    }

    /**
     * Method that gets a mapped property by its name.
     * Returns null if the property does not exits
     * @param name
     * @return String ?: null
     */
    private fun getMappedPropertyOrNull(name : String) : Property? {
        propertiesList.forEach {
            if (it.name == name)
                return it
        }

        return null
    }

    /**
     * Method that gets a mapped foreign key by its name.
     * Returns null if the foreign key does not exits
     * @param name
     * @return String ?: null
     */
    private fun getMappedForeignKeyOrNull(name : String) : ForeignKey? {
        foreignKeyList.forEach {
            if (it.key == name)
                return it.value
        }

        return null
    }

    /**
     * Method that gets the primary key from the propertiesList.
     * If it does not exists, returns null.
     */
    private fun getPrimaryKeyOrNull() : Property? {
        propertiesList.forEach {
            if (it.primaryKey)
                return it
        }
        return null
    }
}
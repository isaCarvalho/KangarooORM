package database

import database.annotations.ForeignKey
import database.annotations.OneToOne
import database.annotations.Property
import database.annotations.Table
import kotlin.reflect.*
import kotlin.reflect.full.*

/**
 * This class is the one how is going to create and manipulate the database
 */
class DatabaseManager {

    /** List with the properties annotations declared in the entity class */
    val propertiesList = ArrayList<Property>()

    /** List with the properties annotations declared in the entity class */
    val oneToOneList = mutableMapOf<String, OneToOne>()

    /** List with the foreign keys annotations declared in the entity class */
    val foreignKeyList = mutableMapOf<String, ForeignKey>()

    /** Table name declared in the entity class */
    lateinit var tableName : String

    /** Entity class */
    lateinit var cls : KClass<*>

    /**
     * Method that sets the entity class and create its table according to the properties
     * @param c
     */
    fun <T : Any> setEntity(c : KClass<T>) : DatabaseManager {
        // setting the entity class and table name
        this.cls = c::class

        val table = c.annotations.find { it is Table } as Table
        this.tableName = table.name

        // setting the properties
        c.memberProperties.forEach {

            val property = it.annotations.find { annotation -> annotation is Property }
            if (property != null)
                propertiesList.add(property as Property)
        }

        c.memberProperties.forEach {
            val oneToOne = it.annotations.find { annotation -> annotation is OneToOne }
            if (oneToOne != null)
                oneToOneList[it.name] = oneToOne as OneToOne
        }

        c.memberProperties.forEach {
            val foreignKey = it.annotations.find { annotation -> annotation is ForeignKey }
            if (foreignKey != null)
                foreignKeyList[it.name] = foreignKey as ForeignKey
        }

        return this
    }
}
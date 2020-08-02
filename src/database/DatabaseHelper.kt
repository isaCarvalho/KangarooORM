package database

import database.annotations.ForeignKey
import database.annotations.Property

object DatabaseHelper
{
    /** Postgres' numeric types */
    val numericTypes = arrayListOf("int", "float", "double", "long", "short")

    /**
     * Method that gets a mapped foreign key by its name.
     * Returns null if the foreign key does not exits
     * @param name
     * @return String ?: null
     */
    fun getMappedForeignKeyOrNull(name : String, foreignKeyList : MutableMap<String, ForeignKey>) : ForeignKey? {
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
    fun getPrimaryKeyOrNull(propertiesList : ArrayList<Property>) : Property? {
        propertiesList.forEach {
            if (it.primaryKey)
                return it
        }
        return null
    }

    /**
     * Checks if the type of the value is numeric. If it is not,
     * returns the value embraced with ''
     * @param type
     * @param value
     * @return String
     */
    fun checkTypes(type : String, value : String) : String {
        val typeLower = type.toLowerCase()
        val valueLower = value.toLowerCase()

        return if (typeLower in numericTypes)
            value
        else if (typeLower == "boolean")
            if (valueLower == "true")
                "1"
            else
                "0"
        else
            "'$value'"
    }

    /**
     * Method that gets a mapped property by its name.
     * Returns null if the property does not exits
     * @param name
     * @return String ?: null
     */
    fun getMappedPropertyOrNull(name : String, propertiesList: ArrayList<Property>) : Property? {
        propertiesList.forEach {
            if (it.name == name)
                return it
        }

        return null
    }
}
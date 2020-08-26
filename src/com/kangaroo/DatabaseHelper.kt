package com.kangaroo

import com.kangaroo.annotations.OneToMany
import com.kangaroo.annotations.OneToOne
import com.kangaroo.annotations.Property
import com.kangaroo.reflections.ReflectProperty
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

object DatabaseHelper
{
    /** Postgres' numeric types */
    val numericTypes = arrayListOf("int", "float", "double", "long", "short")


    /**
     * Method that gets the primary key from the propertiesList.
     * If it does not exists, returns null.
     */
    fun getPrimaryKeyOrNull(propertiesList : ArrayList<ReflectProperty>) : Property? {
        propertiesList.forEach {
            if (it.propertyAnnotation != null && it.propertyAnnotation!!.primaryKey)
                return it.propertyAnnotation
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
    fun getMappedPropertyOrNull(name : String, properties: ArrayList<ReflectProperty>) : Property? {
        properties.forEach {
            if (it.name == name)
                return it.propertyAnnotation
        }

        return null
    }

    fun getMappedOneToOneOrNull(name: String, properties: ArrayList<ReflectProperty>) : OneToOne? {
        properties.forEach {
            if (it.name == name && it.relation != null && it.relation is OneToOne)
                return it.relation as OneToOne
        }

        return null
    }

    fun getMappedOneToManyOrNull(name: String, properties: ArrayList<ReflectProperty>) : OneToMany? {
        properties.forEach {
            if (it.name == name && it.relation != null && it.relation is OneToMany)
                return it.relation as OneToMany
        }

        return null
    }

    /**
     * Maps the parameter
     */
    fun <T : Any> getMappedParameterOrNull(constructor: KFunction<T>, name: String): KParameter? {

        constructor.parameters.forEach { param ->
            if (param.name == name || "id_${param.name}" == name)
                return param
        }

        return null
    }
}
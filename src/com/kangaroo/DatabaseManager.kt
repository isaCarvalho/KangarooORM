package com.kangaroo

import com.kangaroo.reflections.ReflectClass
import kotlin.reflect.*

/**
 * This class is the one how is going to create and manipulate the database
 */
class DatabaseManager {

    lateinit var reflectClass: ReflectClass

    /**
     * Method that sets the entity class and create its table according to the properties
     * @param c
     */
    fun <T : Any> setEntity(c : KClass<T>) : DatabaseManager {

        this.reflectClass = ReflectClass(c)
        return this
    }
}
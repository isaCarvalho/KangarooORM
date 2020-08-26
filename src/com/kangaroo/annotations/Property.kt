package com.kangaroo.annotations

/**
 * This annotation receives the property for the table
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class Property(
        val name : String,
        val type : String,
        val primaryKey : Boolean = false,
        val unique : Boolean = false,
        val nullable : Boolean = false,
        val autoIncrement : Boolean = false,
        val size : Int = -1
)
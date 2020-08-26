package com.kangaroo.annotations

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
annotation class ForeignKey(
        val constraintName : String,
        val referencedTable : String,
        val referencedProperty : String,
        val updateCascade : Boolean = true,
        val deleteCascade : Boolean = false
)
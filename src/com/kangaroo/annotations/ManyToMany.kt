package com.kangaroo.annotations

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
annotation class ManyToMany(
        val foreignKey : ForeignKey
)
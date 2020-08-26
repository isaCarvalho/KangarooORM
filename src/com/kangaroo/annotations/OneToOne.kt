package com.kangaroo.annotations

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
annotation class OneToOne(
        val foreignKey : ForeignKey
)

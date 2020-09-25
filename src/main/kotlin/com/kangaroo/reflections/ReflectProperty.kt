package com.kangaroo.reflections

import com.kangaroo.annotations.*
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.jvmErasure

class ReflectProperty(property : KProperty1<*, *>) {

    val type = property.returnType.jvmErasure

    val name = property.name

    var propertyAnnotation : Property? = null

    var relation : Annotation? = null

    init {
        property.annotations.forEach {
            if (it is OneToOne || it is ForeignKey || it is OneToMany || it is ManyToMany)
                relation = it

            if (it is Property)
                propertyAnnotation = it
        }
    }

    fun getTypeConstructor() = type::class.constructors.first()
}
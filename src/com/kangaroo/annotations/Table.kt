package com.kangaroo.annotations

/**
 * This annotation receives the table name
 */
@Target(AnnotationTarget.CLASS)
annotation class Table constructor(val name : String = "")
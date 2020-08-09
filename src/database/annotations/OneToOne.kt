package database.annotations

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
annotation class OneToOne(
        val foreignKey : ForeignKey
)

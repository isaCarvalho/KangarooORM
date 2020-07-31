# Kangaroo ORM

Kangaroo is a Koltin-Postgres ORM built for those who search for a reliable and easy way to implement 
data storage with Kotlin and Postgres in your applications.

## Database Configurations

To use this package you'll need to set your database configurations
before using it. If you try to use any functionality before setting the configurations,
Kangaroo will throw SQL Exception. To avoid this, do as follows:

```kotlin
DatabaseConfig.setConfiguration(
        host,
        port,
        user,
        password,
        schema,
        useSSL
)
```

## Model

To define your model class, you should use the annotations as follows:

```kotlin
@Table("modelTable")
class ModelExample(
    @Property("property1", "type") var property1 : T,
    @Property("property2", "type") var property2 : T,
    @Property("property3", "type") var property3 : T
)
```

## Property Values

- name : String

This is the column's name, and it should match the property name declared in the class.

- type : String

This is the type of the new column according to postgres type declaration.

- autoIncrement : Boolean

Sets if the column's value will be auto incremented. It's default value is false. 

- primaryKey : Boolean

Sets if the column's value will be primary key. It's default value is false. 

- nullable : Boolean

Sets if the column's value can be null. It's default value is false. 

- unique : Boolean

Sets if the column's value will be unique. It's default value is false.

- size : Int

Sets the column's size. Numeric types should not have sizes. It's default value is -1.

## Relations

To implement a foreign key constraint do as follows:

```kotlin
@Table("relationTable")
class RelationExample(
    @Property("property1", "type") var property1 : T,
    @Property("property2", "type") @ForeingKey("constraintName", "referencedTable", "referencedProperty") var property2 : T,
)
```
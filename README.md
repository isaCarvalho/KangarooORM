# Kangaroo ORM

Kangaroo is a Kotlin-Postgres ORM built for those who search for a reliable and easy way to implement 
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

## Defining the Model

To define your model class, you should use the annotations as follows:

```kotlin
@Table("modelTable")
class ModelExample(
    @Property("property1", "type") var property1 : T,
    @Property("property2", "type") var property2 : T,
    @Property("property3", "type") var property3 : T
)
```

### Property Values

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

### Relations

To implement a foreign key constraint do as follows:

```kotlin
@Table("relationTable")
class RelationExample(
    @Property("property1", "type") var property1 : T,
    @Property("property2", "type") @ForeingKey("constraintName", "referencedTable", "referencedProperty") var property2 : T,
)
```

## Executing Queries

After you defined your model and the database's configurations, you should
create an instance of the class `QueryManager` passing the model class you want
to map. Do as follows:

```kotlin
fun main() {

    DatabaseConfig.setConfiguration("host", 1234, "user", "password", "example", false)

    val model = ModelExample(exampleProp1, exampleProp2, exampleProp3)
    
    /** Creating table and modifying data */  

    val modelQuery = QueryManager(ModelExample::class) // creates the table
        .insert(model) // returns the queryManager's instance
        .update(model) // returns the queryManager's instance
        .delete(model) // returns the queryManager's instance

    /** Selecting data */    

    // returns an ArrayList of models
    var map = modelQuery.selectAll<Model>()
    map.forEach {
        println(it)
    }
    
    // returns an ArrayList of models with a condition
    map = modelQuery.selectAll<Model>("WHERE prop1 = 1")
    map.forEach {
        println(it)
    }

    val exists = modelQuery.exists(model)
    println(exists) // returns true or false

    val model2 = modelQuery.select<Model>("prop1", "=", "1") // returns null or model
    val model3 = modelQuery.select<Model>("WHERE prop1 = 1") // returns null or model

    // returns an int value with how many model registers there is in the database
    val countModel = modelQuery.count()

    /** Dropping table */  

    modelQuery.dropTable() // returns unit
}
```

## Supported Types

- Varchar, Char
- Short, Int, Long
- Float, Double
- Boolean
- Date
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
        useSSL,
        showQuery, // set to true if you want to show your queries in the console. Default value false.
        showQueryLog // set to tye if you want to show the queries in the log file.
)
```
## Usage With Model

### Defining the Model

To define your model class, you should use the annotations as follows:

```kotlin
@Table("modelTable")
class ModelExample(
    @Property("id", "int", primaryKey = true) var id : Int,
    @Property("property1", "type") var property1 : T,
    @Property("property2", "type") var property2 : T,
    @Property("property3", "type") var property3 : T
)
```

It is vital the model has a primary key named `id` if you want to implement relations. Kangaroo will 
search for this property in the relations.
*Note*: Table name is optional. If you do not set the table name, 
Kang will set the default table name as the class name.

### Property Values

Remember that properties must always be `var` not `val`.

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

As said above, it is vital the table you want to relate with another table has an `id` property. It must be named `id`.
Kangaroo will search for this property when inserts and selects data from the related table. So you'll have to 
implement it. You may implement relations by `@OneToOne`, `@OneToMany`, `@ManyToMany` annotations or just `@ForeignKey`
if you just want to create the constraint but not retrieve the whole object. To implement relations do as follows:

#### Foreign Key Constraint

````kotlin
@Table("relationTable")
class RelationExample(
    @Property("property1", "type") var property1 : T,
    @Property("id_model", "int") @ForeingKey("constraintName", "referencedTable", "referencedProperty") var id_model : Int
)
````

Notice that foreign key is a property. Bellow you'll see that the other relations annotations receives a foreign key.

#### One to One

```kotlin
@Table("relationTable")
class OneToOneExample(
    @Property("property1", "type") var property1 : Any,
    @OneToOne(ForeingKey("constraintName", "referencedTable", "referencedProperty")) var modelExample : ModelExample,
)
```

#### One To Many

```kotlin
@Table
class OneToManyExample(
    @Property("property1", "type") var property1: Any,
    @OneToMany(ForeignKey("constraintName", "referencedTable", "property1")) var examples : List<Example>
)
```
*Note*: In this relation, the referenced property is from the class you just defined and not the relation class like it did before.
Note also that this class does not have a table name, because it is optional.
In this case, the table name will be `oneToManyExample`.

### Example

After you defined your model and the database's configurations, you should
create an instance of the class `ModelQueryFacade` passing the model class you want
to map. Do as follows:

```kotlin
fun main() {

    DatabaseConfig.setConfiguration("host", 1234, "user", "password", "example", false)

    val model = ModelExample(exampleProp1, exampleProp2, exampleProp3)
    
    /** Creating table and modifying data */  

    val modelQuery = ModelQueryFacade(ModelExample::class) // creates the table
        .insert(model) // returns the queryManager's instance
        .update(model) // returns the queryManager's instance
        .delete(model) // returns the queryManager's instance

    /** Selecting data */    

    // returns an ArrayList of models
    var map = modelQuery.selectAll()
    map.forEach {
        println(it)
    }
    
    // returns an ArrayList of models with a condition
    map = modelQuery.selectAll("exampleProp1 = 1")
    map.forEach {
        println(it)
    }

    val exists = modelQuery.exists(model)
    println(exists) // returns true or false

    val model2 = modelQuery.find(1) // returns null or model
    val model3 = modelQuery.select("exampleProp1 = 1") // returns null or model

    /** SQL Aggregation Functions */

    // returns an int value with how many model registers there is in the database
    val countModel = modelQuery.count()
    // returns the maximum value of a model's property
    val max = userQuery.maxInt(exampleProp1)
    // returns the minimum value of a model's property
    val min = userQuery.minInt(exampleProp1)
    // returns the sum of the values of a property
    val sum = userQuery.sumInt(exampleProp1)
    // returns the average of a property
    val avg = userQuery.avg(exampleProp1)

    /** Dropping table */  

    modelQuery.dropTable() // returns unit
}
```

## Usage Without Model

### Example

```kotlin
fun main() {

    DatabaseConfig.setConfiguration("host", 1234, "user", "password", "example", false)

    val examplesManager = QueryFacade("examples")
            .createTable(arrayOf(
                    "id int primary key not null",
                    "name varchar(255)"
            ))
            .createSequence("id")
            .insert(arrayOf("id", "name"), arrayOf("1", "'example 1'"))
            .insert(arrayOf("id", "name"), arrayOf("2", "'example 2'"))
            .insert(arrayOf("id", "name"), arrayOf("3", "'example 3'"))
            .update(mutableMapOf(Pair("name", "'Example 3'")), "id = 3")
            .delete("id = 2")

    println(examplesManager.select(arrayOf("id", "name")))
    println(examplesManager.count())
    println(examplesManager.maxInt("id"))
    println(examplesManager.minInt("id"))
    println(examplesManager.sumInt("id"))
    println(examplesManager.avg("id"))

    examplesManager.dropTable()
    examplesManager.dropSequence()
}
```

## Supported Types

- Varchar, Char
- Short, Int, Long
- Float, Double
- Boolean
- Date

## Logger

Kangaroo has a Logger object that saves the exceptions messages in the directory
`log` in your root folder. The log files are saved by date. As it was said before, you can set in your database configurations if
you want to show the queries in the log file.
*Note*: You should not show the queries in your log life if it is not
essential.
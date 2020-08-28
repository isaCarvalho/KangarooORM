# Kangaroo ORM

## Summary

1. [What is Kangaroo?](#what-is-kangaroo)
2. [Why Kangaroo?](#why-kangaroo)
3. [Database Configurations](#database-configurations)
4. [Usage With Model](#usage-with-model) <br>
4.1 [Defining the Model](#defining-the-model) <br>
4.2 [Property Values](#property-values) <br>
4.3 [Example With Model](#example-with-model) <br>
4.4 [Relations](#relations) <br>
4.4.1 [Foreign Key Constraint](#foreign-key-constraint) <br>
4.4.2 [One To One](#one-to-one) <br>
4.4.3 [One To Many](#one-to-many) <br>
5. [Usage Without Model](#usage-without-model) <br>
5.1 [Example Without Model](#usage-without-model)
6. [Supported Types](#supported-types)
7. [Logger](#logger)
8. [Compatibility](#compatibility)
9. [Author](#author)

## What is Kangaroo?

Kangaroo is a Kotlin-Postgres ORM built for those who search for a reliable and easy way to implement 
data storage with Kotlin and Postgres in your applications.

## Why Kangaroo?

Kotlin is a fabulous language, and a lot of applications must implement data storage, but the database is not the
main topic of these applications. So the programmer now can implement this database with a lot of simplicity. 
Kangaroo is reliable, so once your model's well done, you do not have to worry about the database, and can focus in
the other topics of your applications. But the most important, Kangaroo brings together the better in both object oriented programming
 world and database world: Kotlin and PostgresSQL.

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
@Table("users")
class User(
    @Property("id", "int", primaryKey = true) var id : Int,
    @Property("name", "varchar", size = 255) var name : String,
    @Property("birthday", "varchar", size = 255) var birthday : String
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

### Example With Model

After you defined your model and the database's configurations, you should
create an instance of the class `ModelQueryFacade` passing the model class you want
to map. Do as follows:

```kotlin
fun main() {

    DatabaseConfig.setConfiguration("host", 1234, "user", "password", "exampleModel", false)

    val user = User(1, "User 1", "01-01-2001")
    
    /** Creating table and modifying data */  

    val userQuery = ModelQueryFacade(UserExample::class) // creates the table
        .insert(user) // returns the ModelQueryFacade's instance
        .update(user) // returns the ModelQueryFacade's instance
        .delete(user) // returns the ModelQueryFacade's instance

    /** Selecting data */    

    // returns an ArrayList of users
    var users : ArrayList<User> = userQuery.selectAll()
    users.forEach {
        println(it)
    }
    
    // returns an ArrayList of users with a condition
    users = userQuery.selectAll("birthday = '01-01-2001'")
    users.forEach {
        println(it)
    }

    val exists = userQuery.exists(user) // returns true or false
    val user2 = userQuery.find(1) // returns null or user
    val user3 = userQuery.select("exampleProp1 = 1") // returns null or user

    /** SQL Aggregation Functions */

    // returns an int value with how many user registers there is in the database
    val count = userQuery.count()
    // returns the maximum value of a user's property
    val max = userQuery.maxInt(exampleProp1)
    // returns the minimum value of a user's property
    val min = userQuery.minInt(exampleProp1)
    // returns the sum of the values of a property
    val sum = userQuery.sumInt(exampleProp1)
    // returns the average of a property
    val avg = userQuery.avg(exampleProp1)

    /** Dropping table */  

    userQuery.dropTable() // returns unit
}
```

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

To create an one to one relation, you should put the `@OneToOne` annotation in your objects property as the example. For
this example, we are creating an employee that has a unique code, made of an id and a value, and the code belongs to one
employee alone.

* Implement your entity that is going to be related:

```kotlin
@Table("codes")
class Code(
    @Property("id", "int", primaryKey = true) var id : Int,
    @Property("value", "varchar", size = 11) var value : String
)
```

*Note*: This class has the `id` property as its primary key. It is vital the class has this primary key named `id`.

* Implement the relation class as follows:

```kotlin
@Table("employees")
class Employee(
    @Property("id", "int", primaryKey = true) var id : Int,
    @Property("name", "varchar", size = 255) var name : String,
    @OneToOne(ForeingKey("fk_employee_code", "codes", "id")) var code : Code,
)
```

That is all you'll have to do to implement one to one entity relations. Now, lets take a look in the main function:

```kotlin
fun main() {
    // Configuring the database
    DatabaseConfig.setConfiguration("host", 1234, "user", "password", "exampleModel", false)
    
    // Creating the facades
    val codeQuery = ModelQueryFacade(Code::class)
    val employeeQuery = ModelQueryFacade(Employee::class)
    
    // Creating the objects
    val code = Code(1, "ABCDE")
    val employee = Employee(1, "Employee1", code)

    // Querying everything
        
    employeeQuery.insert(employee)
        .update(employee)

    println(employeeQuery.selectAll())
    
    employeeQuery.delete(employee)
    
    // Dropping the tables
    codeQuery.dropTableAndSequence()
    employeeQuery.dropTableAndSequence()
}
```

#### One To Many

For this example, we are going to use a person that has a lot of clothes, but the clothes belong to one person only. Use
the `@OneToMany` annotation.

* Implementing the Clothe class

```kotlin
@Table("clothes")
class Clothe(
    @Property("id", "int", primaryKey = true) var id: Int,
    @Property("description", "varchar", size = 255) var description : String,
    @Property("id_person", "int") var id_person : Int
)
```

* Implementing the Person class

```kotlin
@Table("persons")
class Person(
    @Property("id", "int", primaryKey = true) var id : Int,
    @Property("name", "varchar", size = 255) var name : String,
    @OneToMany(ForeingKey("fk_person_clothe", "clothes", "id_person")) var clothes : List<Clothe>,
)
```

*Note*: In this relation, the referenced property is from the class you just defined and not the relation class like it did before.
Also, the relation class, in this case, the Person class, contains a `List` typed with the referenced class (Clothe), and 
the referenced class (Clothe) contains a `Property` that is going to be referenced by the other class.

Now, lets take a look in the main function:

```kotlin
fun main() {
    // Configuring the database
    DatabaseConfig.setConfiguration("host", 1234, "user", "password", "exampleModel", false)
    
    // Creating the facades
    val clotheQuery = ModelQueryFacade(Clothe::class)
    val personQuery = ModelQueryFacade(Person::class)
    
    // Creating the objects
    val person = Person(1, "Person1", listOf(Clothe(1, "Short", 1), Clothe(1, "Pants", 1), Clothe(1, "Shirt", 1)))

    // Querying everything
        
    personQuery.insert(person)
        .update(person)

    println(personQuery.selectAll())
    
    personQuery.delete(person)
    
    // Dropping the tables
    clotheQuery.dropTableAndSequence()
    personQuery.dropTableAndSequence()
}
```

## Usage Without Model

### Example Without Model

```kotlin
fun exampleModel.example.main() {

    DatabaseConfig.setConfiguration("host", 1234, "user", "password", "exampleModel", false)

    val examplesManager = QueryFacade("examples")
            .createTable(arrayOf(
                    "id int primary key not null",
                    "name varchar(255)"
            ))
            .createSequence("id")
            .insert(arrayOf("id", "name"), arrayOf("1", "'exampleModel 1'"))
            .insert(arrayOf("id", "name"), arrayOf("2", "'exampleModel 2'"))
            .insert(arrayOf("id", "name"), arrayOf("3", "'exampleModel 3'"))
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

## Compatibility

* Kotlin 1.3 or higher
* Postgres 12 or higher

## Author

* Isabela Carvalho
* All contributors
# Kangaroo ORM

## Summary

1. [What is Kangaroo?](#what-is-kangaroo)
2. [Why Kangaroo?](#why-kangaroo)
3. [Database Configurations](#database-configurations)
4. [Annotations](#annotations)
    1. [@Table](#table)
    2. [@Property](#property)
    3. [@ForeignKey](#foreignkey)
    4. [@OneToOne](#onetoone)
    5. [@OneToMany](#onetomany)
    6. [@ManyToMany](#manytomany)
5. [Usage With Model](#usage-with-model)
    1. [Defining the Model](#defining-the-model)
    2. [Example With Model](#example-with-model)
    3. [Relations](#relations)
        1. [Foreign Key Constraint](#foreign-key-constraint)
        2. [One To One Relation](#one-to-one-relation)
        3. [One To Many Relation](#one-to-many-relation)
        4. [Many To Many Relation](#many-to-many-relation)
6. [Usage Without Model](#usage-without-model)
    1. [Example Without Model](#usage-without-model)
7. [Supported Types](#supported-types)
8. [Logger](#logger)
9. [Compatibility](#compatibility)
10. [Author](#author)

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

Use the method `setConfiguration` from the object `DatabaseConfig`. This method receives:

- host: is the address of where your database's host. It's mandatory.
- port: is your database management system default port. It's mandatory.
- user: is your database username. It's mandatory.
- password: your database password. It's mandatory.
- schema: the schema you want to use. It's mandatory.
- useSSL: set to true if you want to use SSL. It's mandatory.
- showQuery: set to true if you want to see wall of your queries in the console. It's optional, and the default value is
false.
- showQueryLog: set to true if you want to see your queries in the log file. Be careful using this! It's optional, and 
the default value is false.

## Annotations

Kangaroo has annotations for defying your how your class will be mapped to the database. So, if you want to use model,
you must use these annotations in your class. Let's take a look in they:

### @Table

This annotation maps your class to a table. It receives one optional argument: `tableName`. If you do not set your table
name, Kangaroo will map it as the class name.

```kotlin
@Table("users")
class User
```

```kotlin
@Table
class User
```

### @Property

Property annotation maps your **constructor's** fields to the table columns. Remember that properties must always be `var` 
not `val`. 

```kotlin
@Table("users")
class User(
    @Property("id", "int", autoIncrement = true, primaryKey = true, nullable = false, unique = true)
    var id : Int,
    @Property("name", "varchar", autoIncrement = false, primaryKey = false, nullable = true, unique = false, size = 255)
    var name : String
)
```

It receives the values bellow:

- name : String

This is the column's name, and it should match the property name declared in the class.

- type : String

This is the type of the new column according to postgres type declaration.

- autoIncrement : Boolean

Sets if the column's value will be auto incremented. It's default value is false. To use auto increment, set a default
value for the property you are using. Example: if your property is an int, use `-1` as default value.

- primaryKey : Boolean

Sets if the column's value will be primary key. It's default value is false. Remember that if you want to implement
any kind of relation, your model must have a primary key.

- nullable : Boolean

Sets if the column's value can be null. It's default value is false. 

- unique : Boolean

Sets if the column's value will be unique. It's default value is false.

- size : Int

Sets the column's size. Numeric types should not have sizes. It's default value is -1.

### @ForeignKey

It is the first relation annotation. It will be explained with more details in the [Relations](#relations) section.

### @OneToOne

Maps properties in one to one relations. It will be explained with more details in the [Relations](#relations) section.

### @OneToMany

Maps properties in one to many relations. It will be explained with more details in the [Relations](#relations) section.

### @ManyToMany

Maps properties in many to many relations. It will be explained with more details in the [Relations](#relations) section.

## Usage With Model

### Defining the Model

To define your model class, you should use the annotations:

```kotlin
@Table("users")
class User(
    @Property("id", "int", primaryKey = true) var id : Int,
    @Property("name", "varchar", size = 255) var name : String,
    @Property("age", "float") var age : Float,
    @Property("birthday", "varchar", size = 255) var birthday : String
)
```

It is vital the model has a primary key if you want to implement relations. Kangaroo will 
search for this property in the relations.
*Note*: Table name is optional. If you do not set the table name, 
Kang will set the default table name as the class name.

### Example With Model

After you defined your model and the database's configurations, you should
create an instance of the class `ModelQueryFacade` passing the model class you want
to map. This is the class going to do all the mapping, so you must use it with model. 
The `ModelQueryFacade` class receives an instance of KClass, and has the methods:

- `insert` receives an instance of the `KClass` your mapped and returns `this` instance of `ModelQueryFacade`. Inserts the
mapped object to the database's table.

- `update` receives an instance of the `KClass` your mapped and returns `this` instance of `ModelQueryFacade`. Updates the
mapped object in the database.

- `delete` receives an instance of the `KClass` your mapped and returns `this` instance of `ModelQueryFacade`. Deletes the
mapped object.

- `selectAll` receives a where condition of the type `String` and returns an `ArrayList` with all the data filtered by 
the where condition you passed. If you want to query all the data use `selectAll("true")`.

- `select` receives a where condition of the type `String` and returns an instance of the `KClass` you mapped. Returns
`null` if there's no data.

- `exists` receives an instance of the `KClass` your mapped and returns true of false if the object you passed exists.

- `find` receives an `int` which is the numeric primary key of your object, and returns an instance of the `KClass` you
 mapped. Returns `null` if there's no data. 

- `count` receives nothing and returns an `int` with the number of register in your mapped table.

- `maxInt` receives the integer field name you want to query the maximum value and returns it.

- `minInt` receives the integer field name you want to query the minimum value and returns it.

- `sumInt` receives the integer field name you want to query the sum of all the values and returns it.

- `maxFloat` receives the float field name you want to query the maximum value and returns it.

- `minFloat` receives the float field name you want to query the minimum value and returns it.

- `sumFloat` receives the float field name you want to query the sum of all the values and returns it.

- `avg` receives the name of the field and returns its average.

Do as follows:

```kotlin
fun main() {

    DatabaseConfig.setConfiguration("host", 1234, "user", "password", "userExampleSchema", false)

    val user = User(1, "User 1", "01-01-2001")
    
    /** Creating table and modifying data */  

    val userQuery = ModelQueryFacade(UserExample::class) // creates the table
        .insert(user) // returns the ModelQueryFacade's instance
        .update(user) // returns the ModelQueryFacade's instance
        .delete(user) // returns the ModelQueryFacade's instance

    /** Selecting data */    

    // returns an ArrayList of users
    var users : ArrayList<User> = userQuery.selectAll("true")
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
    val user3 = userQuery.select("id = 1") // returns null or user

    /** SQL Aggregation Functions */

    // returns an int value with how many user registers there is in the database
    val count = userQuery.count()
    // returns the maximum value of a user's int property
    val maxInt = userQuery.maxInt("id")
    // returns the minimum value of a user's int property
    val minInt = userQuery.minInt("id")
    // returns the sum of the values of a int property
    val sumInt = userQuery.sumInt("id")
    
    // returns the maximum value of a user's float property
    val maxFloat = userQuery.maxFloat("age")
    // returns the minimum value of a user's float property
    val minFloat = userQuery.minFloat("age")
    // returns the sum of the values of a float property
    val sumFloat = userQuery.sumFloat("age")

    // returns the average of a property both Int and Float
    var avg = userQuery.avg("id")
    avg = userQuery.avg("id")

    /** Dropping table */  

    userQuery.dropTable() // returns unit
}
```

### Relations

As said above, it is vital the table you want to relate with another table has a primary key property. We recommend your
primary key to be named `id`. Kangaroo will search for this property when inserts and selects data from the related table.
So you'll have to implement it. You may implement relations by `@OneToOne`, `@OneToMany`, `@ManyToMany` annotations or 
just `@ForeignKey` if you just want to create the constraint but not retrieve the whole object. Aside of `@ForeignKey`,
that is also a property, you must set default values for your relations. This will be explained with more details further. 
Although we recommend all of your relations to have default values, because it will be updated with the database values.
To implement relations do as follows:

#### Foreign Key Constraint

The foreign key constraint annotation receives the fields bellow:

- constraintName: it is a mandatory field and sets the constraint name.

- referencedTable: it is mandatory, and it is the name your table will relate with.

- referencedProperty: it is mandatory, and it is the name of the property your table will relate or be related with.

- updateCascade: default value is true.

- deleteCascade: default value is false.

````kotlin
@Table
class User(
    @Property("id", "int", primaryKey = true) var property1 : T,
    @Property("id_house", "int") @ForeingKey("fk_user_house", "houses", "id") var id_model : Int = -1
)
````

You may use it combined with a property as the example does or passing it to a relation annotation constructor.

#### One to One Relation

To create `One to One` relation, you should put the `@OneToOne` annotation in your objects property as the example. For
this example, we are creating an employee that has a unique code, made of an id and a value, and the code belongs to one
employee alone.

- Implement your entity that is going to be related:

```kotlin
@Table("codes")
class Code(
    @Property("id", "int", primaryKey = true) var id : Int,
    @Property("value", "varchar", size = 11) var value : String
)
```

*Note*: This class has the `id` property as its primary key. It is vital the class has this primary key named `id`.

- Implement the relation class as follows:

```kotlin
@Table("employees")
class Employee(
    @Property("name", "varchar", size = 255) 
    var name : String,
    @OneToOne(ForeingKey("fk_employee_code", "codes", "id")) 
    var code : Code? = null, // Notice the default null value in the relation
    @Property("id", "int", primaryKey = true, auto_increment = true) 
    var id : Int = -1
)
```

- That is all you'll have to do to implement `One to One` entity relations. Now, lets take a look in the main function:

```kotlin
fun main() {
    // Configuring the database
    DatabaseConfig.setConfiguration("host", 1234, "user", "password", "exampleModel", false)
    
    // Creating the facades
    val codeQuery = ModelQueryFacade(Code::class)
    val employeeQuery = ModelQueryFacade(Employee::class)
    
    // Creating the objects
    val code = Code(1, "ABCDE")
    val employee = Employee("Employee1", code)

    // Querying everything
        
    employeeQuery.insert(employee)
        .update(employee)

    println(employeeQuery.selectAll("true"))
    
    employeeQuery.delete(employee)
    
    // Dropping the tables
    codeQuery.dropTableAndSequence()
    employeeQuery.dropTableAndSequence()
}
```

#### One To Many Relation

For this example, we are going to use a person that has a lot of clothes, but the clothes belong to one person only. Use
the `@OneToMany` annotation.

- Implementing the `Clothe` class

```kotlin
@Table("clothes")
class Clothe(
    @Property("id", "int", primaryKey = true) 
    var id: Int,
    @Property("description", "varchar", size = 255) 
    var description : String,
    @Property("id_person", "int") 
    var id_person : Int = -1 // Notice the default value in the relation property
)
```

- Implementing the `Person` class

```kotlin
@Table("persons")
class Person(
    @Property("name", "varchar", size = 255) 
    var name : String,
    @Property("id", "int", primaryKey = true, autoIncrement = true) 
    var id : Int = -1,
    @OneToMany(ForeingKey("fk_person_clothe", "clothes", "id_person")) 
    var clothes : List<Clothe> = listOf(), // Notice the default value in the relation
)
```

In this relation, the referenced property is from the class you just defined and not the relation 
class like it did before. Also, the relation class, in this case, the Person class, contains a `List` typed 
with the referenced class (`Clothe`), and the referenced class (`Clothe`) contains a `Property` that is going to be 
referenced by the other class. Notice property has default value, because when you're building your object
you do not know yet what is the person id, because we settled the person's id to be auto incremented. 
This will be updated with the database value.
The list of clothes in the `Person` class also has a default value. It is important to do that to prevent `NullPointerExceptions`
in both `OneToMany` and `ManyToMany` relations.

- Now, lets take a look in the main function:

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

    println(personQuery.selectAll("true"))
    
    personQuery.delete(person)
    
    // Dropping the tables
    clotheQuery.dropTableAndSequence()
    personQuery.dropTableAndSequence()
}
```

#### Many To Many Relation

To implement many to many relations, follow the example where one student has a lot of courses and one course has a lot
of students.

- This is the `Student` class. Notice the class's primary key is auto incremented, so we settled a default value. This way,
when you build the object, you do not need to set a value for this property. The value will be updated with the
database value when inserted. Notice also `ManyToMany` and `OneToMany` relations also has default value as empty list.
Its vital you do that when creating the object to prevent `NUllPointerException` later.

```kotlin
class Student(
    @Property("name", "varchar", size = 255) 
    var name : String,
    @Property("age", "int")
    var age : Int,
    @Property("id", "int", primaryKey = true, autoIncrement = true)
    var id : Int = -1,
    @ManyToMany(ForeignKey("fk_user_course", "users_coursers", "id_course")) 
    var courses : List<Course> = listOf()
) {
    fun isMinor() : Boolean {
        return age < 18
    }   
}
```

- This is the `Course` class. Notice both classes have lists. Also, the constraint names is **different** in the two classes.
It has to be different because these constraints relate to different properties. But the referenced table must be equal.
Kangaroo will create the related table. 

```kotlin
class Course(
    @Property("name", "varchar", size = 255) 
    var name : String,
    @Property("hours", "int") 
    var hours : Int,
    @ManyToMany(ForeignKey("fk_course_user", "users_coursers", "id_student")) 
    var students : List<Student> = listOf()
)
```

- Now, lets take a look in the main function. You do not need to insert both ends. The courses will be inserted when the
students are insert and vice-versa.

```kotlin
fun main() {
    DatabaseConfig.setConfiguration("host", 1234, "user", "password", "exampleModel", false)

    // creating the facades

    val courseQuery = ModelQueryFacade(Courses::class)
    val studentQuery = ModelQueryFacade(Student::class)

    // creating the objects

    val courses = listOf(Course("Math", 1), Course("Science", 3))
    
    val student1 = Student("Student1", 22, courses = courses)
    val student2 = Student("Student2", 22, courses = courses)

    // inserting
    
    studentQuery.insert(student1)
        .insert(student2)
        .update(student2) // Remember you can set update cascade
        .delete(student1) // Remember you can set delete cascade
    
    // selecting
    
    val students = studentQuery.selectAll("true")
    println(students)
    
    
}
```

## Usage Without Model

### Example Without Model

Kangaroo supports usage without model classes. So, in this case, you need to use the `QueryFacade` class. While the
`ModelQueryFacade` class receives the class going to map, the `QueryFacade` class receives the table name. This class
has the following methods:

- `insert` receives an `Array` of columns, and an `Array` of values, and inserts in the database. Returns a `QueryFacade`
instance.

- `update` receives a `MutableMap` of a `Pair` of the old and new value and the primary key. Updates a value, and returns
`this` instance of `QueryFacade`.

- `delete` receives the where condition to delete. Returns `this` instance of `QueryFacade`.

- `select` receives an `Array` of fields you want to select, and an optional where condition. Returns an `ArrayList` 
with all the data filtered by the where condition you passed.

- `count` receives nothing and returns an `int` with the number of register in your mapped table.

- `maxInt` receives the integer field name you want to query the maximum value and returns it.

- `minInt` receives the integer field name you want to query the minimum value and returns it.

- `sumInt` receives the integer field name you want to query the sum of all the values and returns it.

- `maxFloat` receives the float field name you want to query the maximum value and returns it.

- `minFloat` receives the float field name you want to query the minimum value and returns it.

- `sumFloat` receives the float field name you want to query the sum of all the values and returns it.

- `avg` receives the name of the field and returns its average.

Do as follows:

```kotlin
fun main() {

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

Kangaroo has a `Logger` object that saves the exceptions messages in the directory
`log` in your root folder. The log files are saved by date. As it was said before, you can set in your database configurations if
you want to show the queries in the log file.
*Note*: You should not show the queries in your log file if it is not
essential.

## Compatibility

* Kotlin 1.3 or higher
* Postgres 12 or higher

## Author

* Isabela Carvalho
* All contributors
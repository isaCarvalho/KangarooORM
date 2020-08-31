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
4.4.4 [Many To Many](#many-to-many) <br>
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
    @Property("age", "float") var age : Float,
    @Property("birthday", "varchar", size = 255) var birthday : String
)
```

It is vital the model has a primary key if you want to implement relations. Kangaroo will 
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

### Example With Model

After you defined your model and the database's configurations, you should
create an instance of the class `ModelQueryFacade` passing the model class you want
to map. Do as follows:

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
    val user3 = userQuery.select("id = 1") // returns null or user

    /** SQL Aggregation Functions */

    // returns an int value with how many user registers there is in the database
    val count = userQuery.count()
    // returns the maximum value of a user's int property
    val maxInt = userQuery.maxInt(id)
    // returns the minimum value of a user's int property
    val minInt = userQuery.minInt(id)
    // returns the sum of the values of a int property
    val sumInt = userQuery.sumInt(id)
    
    // returns the maximum value of a user's float property
    val maxFloat = userQuery.maxFloat(age)
    // returns the minimum value of a user's float property
    val minFloat = userQuery.minFloat(age)
    // returns the sum of the values of a float property
    val sumFloat = userQuery.sumFloat(age)

    // returns the average of a property both Int and Float
    var avg = userQuery.avg(id)
    avg = userQuery.avg(id)

    /** Dropping table */  

    userQuery.dropTable() // returns unit
}
```

### Relations

As said above, it is vital the table you want to relate with another table has a primary key property. We recommend your
primary key to be named `id`. Kangaroo will search for this property when inserts and selects data from the related table.
So you'll have to implement it. You may implement relations by `@OneToOne`, `@OneToMany`, `@ManyToMany` annotations or 
just `@ForeignKey` if you just want to create the constraint but not retrieve the whole object. Aside of `@ForeignKey`,
that is also a property, you should set default values for your relations. This will be explained with more details further.
To implement relations do as follows:

#### Foreign Key Constraint

The foreign key constraint annotation receives three fields: the constraint name, the referenced table and the 
referenced property.

````kotlin
@Table
class User(
    @Property("id", "int", primaryKey = true) var property1 : T,
    @Property("id_house", "int") @ForeingKey("fk_user_house", "houses", "id") var id_model : Int
)
````

You may use it combined with a property as the example does or passing it to a relation annotation constructor.

#### One to One

To create one to one relation, you should put the `@OneToOne` annotation in your objects property as the example. For
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
    @Property("name", "varchar", size = 255) var name : String,
    @OneToOne(ForeingKey("fk_employee_code", "codes", "id")) var code : Code,
    @Property("id", "int", primaryKey = true, auto_increment = true) var id : Int = -1
)
```

- That is all you'll have to do to implement one to one entity relations. Now, lets take a look in the main function:

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

- Implementing the `Clothe` class

```kotlin
@Table("clothes")
class Clothe(
    @Property("id", "int", primaryKey = true) var id: Int,
    @Property("description", "varchar", size = 255) var description : String,
    @Property("id_person", "int") var id_person : Int = -1
)
```

- Implementing the `Person` class

```kotlin
@Table("persons")
class Person(
    @Property("name", "varchar", size = 255) var name : String,
    @Property("id", "int", primaryKey = true, autoIncrement = true) var id : Int = -1,
    @OneToMany(ForeingKey("fk_person_clothe", "clothes", "id_person")) var clothes : List<Clothe> = listOf(),
)
```

In this relation, the referenced property is from the class you just defined and not the relation 
class like it did before. Also, the relation class, in this case, the Person class, contains a `List` typed 
with the referenced class (`Clothe`), and the referenced class (`Clothe`) contains a `Property` that is going to be 
referenced by the other class. Notice property has default value, because when you're building your object
you do not know yet what is the person id, because we settled the person's id to be auto incremented. 
This will be updated with the database value. If you don't set the primary key auto incremented, then you don't need to
set default values in both classes. <br>
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

    println(personQuery.selectAll())
    
    personQuery.delete(person)
    
    // Dropping the tables
    clotheQuery.dropTableAndSequence()
    personQuery.dropTableAndSequence()
}
```

#### Many To Many

To implement many to many relations, follow the example where one student has a lot of courses and one course has a lot
of students.

- This is the `Student` class. Notice the class's primary key is auto incremented, so we settled a default value. This way,
when you build the object, you do not need to set a value for this property. The value will be updated with the
database value when inserted. Notice also `ManyToMany` and `OneToMany` relations also has default value as empty list.
Its vital you do that when creating the object to prevent `NUllPointerException` later.

```kotlin
class Student(
    @Property("name", "varchar", size = 255) var name : String,
    @Property("age", "int") var age : Int,
    @Property("id", "int", primaryKey = true, autoIncrement = true) var id : Int = -1,
    @ManyToMany(ForeignKey("fk_user_course", "users_coursers", "id_course")) var courses : List<Course> = listOf()
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
    @Property("name", "varchar", size = 255) var name : String,
    @Property("hours", "int") var hours : Int,
    @ManyToMany(ForeignKey("fk_course_user", "users_coursers", "id_student")) var students : List<Student> = listOf()
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
    
    val student1 = User("Student1", 22, courses = courses)
    val student2 = User("Student2", 22, courses = courses)

    // inserting
    
    studentQuery.insert(student1)
        .insert(student2)
}
```

## Usage Without Model

### Example Without Model

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

Kangaroo has a Logger object that saves the exceptions messages in the directory
`log` in your root folder. The log files are saved by date. As it was said before, you can set in your database configurations if
you want to show the queries in the log file.
*Note*: You should not show the queries in your log file if it is not
essential.g

## Compatibility

* Kotlin 1.3 or higher
* Postgres 12 or higher

## Author

* Isabela Carvalho
* All contributors
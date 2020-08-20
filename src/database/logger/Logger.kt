package database.logger

import java.io.File
import java.lang.Exception
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object Logger
{
    fun write(message : String, exception: Exception) {
        println("Writing log...")

        val formatter = DateTimeFormatter.ofPattern("dd-MMMM-yyyy")
        val formattedDate = LocalDate.now().format(formatter)

        val fileName = "../../logs/$formattedDate"

        val logFile = File(fileName)
        logFile.printWriter().use {
            it.println("\n$message\n")
            it.println(exception.printStackTrace())
        }

        println("Log written!")
    }
}
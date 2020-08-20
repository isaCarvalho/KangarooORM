package database.logger

import java.io.File
import java.lang.Exception
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Logger
{
    fun write(message : String, exception: Exception) {
        println("Writing log...")

        val logFolder = File("logs")
        if (!logFolder.exists() || !logFolder.isDirectory) {
            logFolder.mkdir()
        }

        val formatter = DateTimeFormatter.ofPattern("dd-MMMM-yyyy")
        val formattedDate = LocalDate.now().format(formatter)

        val fileName = "logs/$formattedDate"

        val logFile = File(fileName)

        if (!logFile.exists()) {
            logFile.createNewFile()
        }

        var string = "\n${LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))} " +
                "- $message - ${exception.message}\n"

        exception.stackTrace.forEach {
            string += "$it\n"
        }

        logFile.appendText(string)

        println("Log written!")
    }
}
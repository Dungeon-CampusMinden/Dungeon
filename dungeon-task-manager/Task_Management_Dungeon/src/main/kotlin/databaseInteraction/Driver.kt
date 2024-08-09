package databaseInteraction

import Task_Management_Dungeon.Database
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

object Driver {
    fun createDriver(): SqlDriver {
        val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:task_manager.db")
        if (!File("task_manager.db").exists()) {
            Database.Schema.create(driver)
        }
        return driver
    }

    fun provideDriver(): SqlDriver{
        return JdbcSqliteDriver(
            "jdbc:sqlite:task_manager.db",
            schema = Database.Schema)
    }


}
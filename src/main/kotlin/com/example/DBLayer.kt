package com.example

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object Cities : Table() {
    val id = integer("id").autoIncrement()
    val city = text("city")
    val country = text("country")
    val population = integer("population").index()
}

fun initDb(jdbcUrl: String, numberOfConnections: Int) {
    Database.connect(hikari(jdbcUrl,numberOfConnections))
    transaction {
        SchemaUtils.create(Cities)
    }

}


private fun hikari(jdbcUrl: String, numberOfConnections: Int): HikariDataSource {
    val config = HikariConfig()
    config.driverClassName = "org.postgresql.Driver"
    config.jdbcUrl = jdbcUrl
    config.username = "game"
    config.password = "game"
    config.maximumPoolSize = numberOfConnections
    config.validate()
    return HikariDataSource(config)
}

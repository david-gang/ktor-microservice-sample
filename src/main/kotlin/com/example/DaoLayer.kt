package com.example

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Random
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.InputStream

const val numberOfConnections = 5
val dispatcher = Dispatchers.IO.limitedParallelism(numberOfConnections)

fun initDao() {
    val jdbcUrl = System.getProperty("jdbcUrl", "jdbc:postgresql://localhost:5432/game")
    initDb(jdbcUrl, numberOfConnections)
}

suspend fun uploadCitiesToDb(inputStream: InputStream) = withContext(dispatcher) {
    csvReader().open(inputStream) {
        val citySeq = readAllWithHeaderAsSequence().map {
            CityInput(
                city = it["city_ascii"]!!,
                country = it["country"]!!,
                population = it["population"]!!.ifBlank { "0" }.toFloat().toInt()
            )
        }.filter { it.population >= 1000_000 }
        transaction {
            Cities.batchInsert(citySeq) { city ->
                this[Cities.city] = city.city
                this[Cities.country] = city.country
                this[Cities.population] = city.population
            }
        }
    }

}

suspend fun fetchRandomRow() = withContext(dispatcher){
    val city = transaction {
        Cities.selectAll().orderBy(Random()).limit(1).single()
    }
    CityInput(city = city[Cities.city], country = city[Cities.country], population = city[Cities.population])
}

data class CityInput(val city: String, val country: String, val population: Int)
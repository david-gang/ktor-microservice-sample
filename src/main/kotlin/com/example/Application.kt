package com.example

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.content.*
//import io.ktor.metrics.micrometer.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.serialization.Serializable
//import io.micrometer.prometheus.PrometheusConfig
//import io.micrometer.prometheus.PrometheusMeterRegistry
import java.util.*

fun main(args: Array<String>) {
    embeddedServer(CIO, port=8081, host = "0.0.0.0") {
        install(ContentNegotiation) {
            json()
        }

        install(Authentication) {
            basic("auth-basic") {
                realm = "Access to the '/' path"
                validate { credentials ->
                    if (credentials.name == "admin" && credentials.password == "admin") {
                        UserIdPrincipal(credentials.name)
                    } else {
                        null
                    }
                }
            }
        }
        initDao()

        //Prometheus
//        val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
//        install(MicrometerMetrics) {
//            registry = appMicrometerRegistry
//        }

        routing {
//            get("/metrics") {
//                call.respond(appMicrometerRegistry.scrape())
//            }
            generateQuestion()
            guess()
            upload()
            get("/") {
                call.application.environment.log.info("Hello from /api/v1!")
                call.respondText("Hello, world 2!")
            }
            authenticate("auth-basic") {
                show()
            }
        }
    }.start(wait = true)
}

fun Route.upload() {
    post("/upload"){
        try{
            val multipartData = call.receiveMultipart()
            multipartData.forEachPart { part ->
                if(part is PartData.FileItem ) {
                    uploadCitiesToDb(part.streamProvider())
                }
                part.dispose()
            }
            call.respondText("File uploaded successfully")
        }
        catch (e: Exception){
            call.application.environment.log.error("Failed upload", e)
            call.respondText("failed", status = HttpStatusCode.InternalServerError)
        }

    }
}

fun Route.generateQuestion() {
    get("/generate") {
        val row = fetchRandomRow()
        val uuid = UUID.randomUUID().toString()
        val case = Case(uuid = uuid, city = row.city, population = row.population)
        val guess = CachedGuess(row.country, 1)
        storeGuess(uuid, guess)
        call.respond(case)
    }
}

fun Route.show() {
    get("/show") {
        val id = call.request.queryParameters["id"]!!
        val guess = fetchGuess(id)
        if(guess == null) {
            call.respond(HttpStatusCode.NotFound, "Nothing found")
        }
        else {
            call.respond(guess.country)
        }
    }
}
fun Route.guess() {
    get("/solve") {
        val params = call.request.queryParameters
        val id = params["id"]!!
        val country = params["country"]!!
        val guess = fetchGuess(id)
        if(guess == null) {
            call.respond(HttpStatusCode.NotFound, "Nothing found")
            return@get
        }
        val result = if(guess.country == country) {
            Result(true, "", false)
        }
        else {
            if(guess.currentGuess<3) {
                storeGuess(id, guess.copy(currentGuess = guess.currentGuess + 1))
            }
            val hint = when(guess.currentGuess) {
                1 -> guess.country.first().toString()
                2 -> "${guess.country.first()}*${guess.country.last()}"
                else -> ""
            }
            Result(false, hint, guess.currentGuess<3)
        }
        call.respond(result)

    }
}

@Serializable
data class Result(val succeeded: Boolean, val hint: String, val retry: Boolean)

@Serializable
data class Case(val uuid: String, val city: String, val population: Int)
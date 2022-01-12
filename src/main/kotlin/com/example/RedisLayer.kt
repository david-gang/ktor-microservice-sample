package com.example

import io.lettuce.core.RedisClient
import io.lettuce.core.api.coroutines
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val redisConnection: RedisCoroutinesCommands<String, String> by lazy {
    initRedis()
}

private fun initRedis(): RedisCoroutinesCommands<String, String> {
    val redisUrl = System.getProperty("redis_url", "redis://password@localhost:6379/0")
    return RedisClient.create(redisUrl).connect().coroutines()
}

suspend fun storeGuess(uuid: String, guess: CachedGuess) {
    val value = Json.encodeToString(guess)
    redisConnection.setex(uuid, 300,  value)
}

suspend fun fetchGuess(uuid: String): CachedGuess? = redisConnection.get(uuid)?.let { Json.decodeFromString(it) }


@Serializable
data class CachedGuess(val country: String, val currentGuess: Int)
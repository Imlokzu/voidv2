package com.maxrave.simpmusic.data.service

import com.maxrave.simpmusic.data.model.imported.SpotifyPlaylistResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json

class ImportService {
    private val client = HttpClient(CIO) {
        followRedirects = true
        expectSuccess = false
    }

    private val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
    }

    suspend fun importSpotifyPlaylist(url: String): Result<SpotifyPlaylistResponse> {
        return try {
            val encodedUrl = java.net.URLEncoder.encode(url, "UTF-8")
            val response = client.get("$BASE_URL/api/spotify-playlist?url=$encodedUrl")
            val body = response.bodyAsText()
            if (response.status.value in 200..299) {
                val parsed = json.decodeFromString(SpotifyPlaylistResponse.serializer(), body)
                Result.success(parsed)
            } else {
                Result.failure(Exception("HTTP ${response.status.value}: $body"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        const val BASE_URL = "https://your-spotify-importer.vercel.app"
    }
}

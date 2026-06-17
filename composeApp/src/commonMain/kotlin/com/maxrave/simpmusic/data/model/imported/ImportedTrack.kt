package com.maxrave.simpmusic.data.model.imported

import kotlinx.serialization.Serializable

@Serializable
data class ImportedTrack(
    val title: String,
    val artist: String,
)

@Serializable
data class SpotifyPlaylistResponse(
    val title: String,
    val thumbnail: String? = null,
    val tracks: List<ImportedTrack>,
    val source: String,
    val error: String? = null,
    val message: String? = null,
)

@Serializable
data class ImportResult(
    val imported: List<MatchedTrack> = emptyList(),
    val unsure: List<MatchedTrack> = emptyList(),
    val notFound: List<ImportedTrack> = emptyList(),
)

@Serializable
data class MatchedTrack(
    val track: ImportedTrack,
    val videoId: String? = null,
    val title: String? = null,
    val artist: String? = null,
    val confidence: Float = 0f,
)
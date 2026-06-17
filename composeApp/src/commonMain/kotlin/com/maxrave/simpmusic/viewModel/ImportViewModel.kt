package com.maxrave.simpmusic.viewModel

import androidx.lifecycle.viewModelScope
import com.maxrave.simpmusic.data.model.imported.ImportedTrack
import com.maxrave.simpmusic.data.model.imported.SpotifyPlaylistResponse
import com.maxrave.simpmusic.data.service.ImportService
import com.maxrave.simpmusic.viewModel.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ImportUIState(
    val url: String = "",
    val isLoading: Boolean = false,
    val playlistTitle: String = "",
    val playlistThumbnail: String? = null,
    val tracks: List<ImportedTrack> = emptyList(),
    val error: String? = null,
    val isUrlValid: Boolean = false,
)

class ImportViewModel(
    private val importService: ImportService,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(ImportUIState())
    val uiState: StateFlow<ImportUIState> = _uiState.asStateFlow()

    fun updateUrl(url: String) {
        _uiState.update {
            it.copy(
                url = url,
                isUrlValid = isValidSpotifyUrl(url),
                error = null,
            )
        }
    }

    fun importPlaylist() {
        val url = _uiState.value.url
        if (!isValidSpotifyUrl(url)) {
            _uiState.update { it.copy(error = "Invalid URL") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = importService.importSpotifyPlaylist(url)

            result.fold(
                onSuccess = { response ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            playlistTitle = response.title,
                            playlistThumbnail = response.thumbnail,
                            tracks = response.tracks,
                            error = null,
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to import playlist",
                        )
                    }
                },
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun reset() {
        _uiState.update {
            ImportUIState()
        }
    }

    private fun isValidSpotifyUrl(url: String): Boolean {
        return url.contains("spotify.com") &&
            (url.contains("/playlist/") || url.contains("/album/"))
    }
}
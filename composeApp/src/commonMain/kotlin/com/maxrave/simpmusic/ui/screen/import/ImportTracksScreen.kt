package com.maxrave.simpmusic.ui.screen.import

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.maxrave.simpmusic.data.model.imported.ImportedTrack
import com.maxrave.simpmusic.ui.theme.md_theme_dark_background
import com.maxrave.simpmusic.ui.theme.md_theme_dark_primary
import com.maxrave.simpmusic.ui.theme.md_theme_dark_surface
import com.maxrave.simpmusic.ui.theme.md_theme_dark_surfaceVariant
import com.maxrave.simpmusic.ui.theme.md_theme_dark_onSurface
import com.maxrave.simpmusic.ui.theme.md_theme_dark_onSurfaceVariant
import com.maxrave.simpmusic.viewModel.ImportViewModel
import org.koin.compose.viewmodel.koinViewModel
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.import_subtitle
import simpmusic.composeapp.generated.resources.import_tracks
import simpmusic.composeapp.generated.resources.import_your_music
import simpmusic.composeapp.generated.resources.paste_playlist_url
import simpmusic.composeapp.generated.resources.retry
import simpmusic.composeapp.generated.resources.start_import

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportTracksScreen(
    navController: NavController,
    viewModel: ImportViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Import Tracks",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = md_theme_dark_surface,
                    ),
            )
        },
        containerColor = md_theme_dark_background,
    ) { paddingValues ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                ImportFeatureCard()
            }

            item {
                OutlinedTextField(
                    value = uiState.url,
                    onValueChange = { viewModel.updateUrl(it) },
                    label = { Text("Paste playlist URL") },
                    placeholder = { Text("https://open.spotify.com/playlist/...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Paste",
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = md_theme_dark_primary,
                            unfocusedBorderColor = md_theme_dark_surfaceVariant,
                            focusedContainerColor = md_theme_dark_surface,
                            unfocusedContainerColor = md_theme_dark_surface,
                        ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                )
            }

            item {
                Button(
                    onClick = { viewModel.importPlaylist() },
                    enabled = uiState.isUrlValid && !uiState.isLoading,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = md_theme_dark_primary,
                            disabledContainerColor = md_theme_dark_surfaceVariant,
                        ),
                    shape = RoundedCornerShape(26.dp),
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            text = "Start Import",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            uiState.error?.let { error ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = Color(0xFF5C1A1A),
                            ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = Color(0xFFFF6B6B),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = error,
                                color = Color(0xFFFFD1D1),
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = "Retry",
                                color = md_theme_dark_primary,
                                modifier =
                                    Modifier
                                        .clickable { viewModel.importPlaylist() }
                                        .padding(8.dp),
                            )
                        }
                    }
                }
            }

            if (uiState.playlistTitle.isNotEmpty()) {
                item {
                    PlaylistHeader(
                        title = uiState.playlistTitle,
                        thumbnail = uiState.playlistThumbnail,
                        trackCount = uiState.tracks.size,
                    )
                }

                items(uiState.tracks) { track ->
                    TrackItem(track = track)
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ImportFeatureCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        colors =
            CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A24),
            ),
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors =
                                    listOf(
                                        md_theme_dark_primary,
                                        md_theme_dark_primary.copy(alpha = 0.6f),
                                    ),
                            ),
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "↓",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Import Your Music",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Spotify, SoundCloud, Telegram & more",
                    style = MaterialTheme.typography.bodyMedium,
                    color = md_theme_dark_onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun PlaylistHeader(
    title: String,
    thumbnail: String?,
    trackCount: Int,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = md_theme_dark_surface,
            ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (thumbnail != null) {
                AsyncImage(
                    model = thumbnail,
                    contentDescription = null,
                    modifier =
                        Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "$trackCount tracks",
                    style = MaterialTheme.typography.bodyMedium,
                    color = md_theme_dark_onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun TrackItem(
    track: ImportedTrack,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = md_theme_dark_surfaceVariant,
            ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = md_theme_dark_onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
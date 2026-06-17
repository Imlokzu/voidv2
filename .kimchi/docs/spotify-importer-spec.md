# Spotify Importer Spec

## Goal
Add a "Import Tracks" screen where users can paste a Spotify playlist URL and import the tracks into Void.

## Architecture
- Screen: `ImportTracksScreen`
- ViewModel: `ImportViewModel`
- Repository/Data: Use existing DI (Koin) + Ktor client
- Navigation: Add `ImportDestination` to nav graph
- Entry point: Add to bottom nav or home screen

## Backend
The server-side importer already exists at `vercel/spotify-importer/`.
Endpoint: `GET /api/spotify-playlist?url=<spotify-url>`

## Data Models

```kotlin
// ImportTrack.kt
data class ImportedTrack(
    val title: String,
    val artist: String,
)

// SpotifyPlaylistResponse.kt
data class SpotifyPlaylistResponse(
    val title: String,
    val thumbnail: String?,
    val tracks: List<ImportedTrack>,
    val source: String,
    val error: String? = null,
    val message: String? = null,
)

// ImportResult.kt
data class ImportResult(
    val imported: List<MatchedTrack>,
    val unsure: List<MatchedTrack>,
    val notFound: List<ImportedTrack>,
)

// MatchedTrack.kt
data class MatchedTrack(
    val track: ImportedTrack,
    val videoId: String?,
    val title: String?,
    val artist: String?,
    val confidence: Float,
)
```

## UI — ImportTracksScreen

```
TopBar: "Import Tracks" + back arrow

Scrollable content:
- Feature card: "Import Your Music" / "Spotify, SoundCloud, Telegram & more"
- TextField: "Paste playlist URL" (paste button)
- Button: "Start Import" (enabled when URL is valid)

Loading state: circular progress

After import:
- Playlist thumbnail + title
- Stats: "Imported X songs — X matched, X unsure, X not found"
- Section: "Matched" — list of tracks (icon + title + artist)
- Section: "Unsure" — list with "Pick better match" option
- Section: "Not Found" — list
- Buttons: "Add matched to library", "Retry not found", "Skip"
```

## UI — ImportResultsScreen (or inline in ImportTracksScreen)
Show matched/unsure/not found categories.

## API Service

Add to existing DI/service layer:
```kotlin
interface ImportService {
    suspend fun importSpotifyPlaylist(url: String): Result<SpotifyPlaylistResponse>
}
```

Use Ktor client with the importer endpoint URL.

## Navigation
- Destination: `ImportDestination`
- Add composable route in `App.kt` or navigation graph files
- Entry: add "Import" icon to bottom nav (next to Library)

## Files to create/modify

### New files:
- `composeApp/.../ui/screen/import/ImportTracksScreen.kt`
- `composeApp/.../ui/screen/import/ImportViewModel.kt`
- `composeApp/.../data/service/ImportService.kt`
- `composeApp/.../ui/navigation/destination/import/ImportDestination.kt`

### Modified files:
- Navigation graph (`App.kt` or graph file) — add import route
- Bottom nav / home — add import entry point
- `strings.xml` — add import-related strings

## Acceptance
- [ ] User can open Import screen from nav
- [ ] User can paste a Spotify playlist URL
- [ ] Import fetches track list from server
- [ ] Results show matched/unsure/not found count
- [ ] Tracks can be added to library (or at least viewed)
- [ ] No compile errors

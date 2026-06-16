# Void Spotify Importer

Browser-backed Vercel function for full Spotify playlist extraction.

## Why this exists

The Android app can scrape the public Spotify page on-device, but large playlists only expose a preview batch there, usually around 30 tracks.

This service uses a headless browser so it can access the same browser-side Spotify runtime path that the desktop web app uses.

## Endpoint

`GET /api/spotify-playlist?url=https://open.spotify.com/playlist/<id>`

Response:

```json
{
  "title": "Playlist title",
  "thumbnail": "https://i.scdn.co/image/...",
  "tracks": [
    { "title": "Track", "artist": "Artist" }
  ],
  "source": "spotify-web-api"
}
```

## Deploy

```bash
cd vercel/spotify-importer
npm install
vercel
```

## Hook into Android

Build the Android app with:

```bash
SPOTIFY_IMPORT_API_URL=https://your-project.vercel.app \
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home \
./gradlew :app:assembleCoreDebug
```

Or add a Gradle property:

```bash
./gradlew -PspotifyImportApiUrl=https://your-project.vercel.app :app:assembleCoreDebug
```

## Notes

- Primary strategy: Spotify browser runtime API
- Fallback strategy: DOM scroll scraping
- This service only returns playlist title, thumbnail, and `title + artist`

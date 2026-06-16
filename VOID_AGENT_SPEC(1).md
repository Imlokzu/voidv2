# Void Music App — Agent Implementation Spec

## Purpose

Build **Void** as a fork of **SimpMusic**, focused on a darker, cleaner, more premium YouTube Music client with:

- Spotify/SoundCloud/Telegram/music-link importing
- “My Version” per-song audio presets
- no-random-track playback controls
- animated artist pages
- redesigned icons and feature cards
- better desktop/tablet layout
- playlist/library tools that do not feel like a cursed RecyclerView graveyard

Void should **not** feel like “SimpMusic with a new icon.” It should feel like its own app.

---

## Current Base

Use **SimpMusic** as the base project.

Reason:

- already has a good-looking UI
- already has many modules/features
- already has glass/liquid-style visuals
- already supports many music-client basics
- easier than fixing OuterTune/InnerTune UI from scratch

Important:

- preserve working playback/search/cache behavior first
- do not break existing SimpMusic features while rebranding
- keep commits small and testable

---

## Product Identity

### Core positioning

> Void = import any song, make your version, and listen without algorithmic trash.

### Main pillars

1. **Import Your Music**
2. **My Version**
3. **No Random Tracks**
4. **Animated Music UI**
5. **Better Library**
6. **Void-native design**

---

## Feature Priority

### v0.1 — Base Fork + Branding

Goal: make the app clearly become Void.

Tasks:

- rename app to `Void`
- change app icon
- change package/application labels where needed
- replace splash screen
- replace default branding text/assets
- apply Void color system
- update About screen
- remove obvious SimpMusic branding
- keep original license notices where required

Suggested Void style:

```text
Background: near black
Accent: purple / violet / cold blue
Surface: glass dark cards
Text: white / soft gray
Effects: subtle blur, grain, glow
Brand mark: white portal / vortex
```

Acceptance:

- app installs as Void
- no obvious visible SimpMusic branding remains in main UI
- playback still works
- search still works
- library still opens
- downloads/cache still work if they worked before

---

## v0.2 — New Icon System

Goal: remove default YouTube Music-style icon feel and make Void visually distinct.

Create custom icons for:

- downloaded track
- cached track
- import
- My Version
- original version
- playlist
- queue
- artist
- album
- explicit track
- synced lyrics
- local file
- failed import
- uncertain match
- official audio
- remix/live/cover warning

### My Version Icon

Use a small portal/vortex symbol.

Example usage:

```text
Track title    🌀
```

Meaning:

- this song has saved Void effects
- tapping can open the My Version settings
- long-press can reset/apply/share

Acceptance:

- icons are visually consistent
- icons work in dark mode
- icons are readable at small sizes
- no random mismatched icon packs

---

## v0.3 — Feature Cards / In-App Infographics

These are internal app cards, not website marketing banners.

Use the uploaded visual direction:

- dark grainy background
- Void portal art
- big bold text
- minimal icon groups
- not too much 3D shadow in actual UI

Recommended wording:

### Import card

Title:

```text
Import Your Music
```

Subtitle:

```text
Spotify, SoundCloud, Telegram & more
```

Screen title inside feature:

```text
Import Tracks
```

Button:

```text
Start Import
```

### Downloads card

Title:

```text
Downloaded Tracks
```

Subtitle:

```text
Listen offline anytime
```

### My Version card

Title:

```text
My Version
```

Subtitle:

```text
Save slowed, bass and reverb presets
```

### No Random card

Title:

```text
No Random Tracks
```

Subtitle:

```text
Only play what you actually saved
```

Acceptance:

- cards look like part of Void brand
- text is short and native-sounding
- cards work on phone/tablet/desktop widths
- no oversized icons that destroy layout

---

# Core Feature: Import Your Music

## Goal

Allow users to import tracks/playlists from external services into Void.

Supported initial sources:

- Spotify
- SoundCloud
- Telegram links/files/text
- plain text / CSV
- YouTube links

Later:

- Apple Music
- TikTok sound links

---

## Important naming

Use these labels:

```text
Feature card: Import Your Music
Screen title: Import Tracks
Button: Start Import
Result screen: Import Results
```

Avoid:

```text
Import your Tracks
```

Reason:

- grammatically usable, but less native
- “your tracks” can sound like the user’s own uploaded songs
- “Import Your Music” is clearer and more app-like

---

## Existing Spotify Importer Context

There is already a server-side Spotify playlist importer.

It exists because local Android scraping from public Spotify pages usually only exposes around 30 tracks.

Architecture:

```text
Android local importer:
- good for single track links
- good for small previews
- may only see ~30 playlist tracks

Server-side importer:
- required for full playlists
- uses browser-backed Spotify web runtime path
- returns title + artist metadata only
```

Existing endpoint concept:

```http
GET /api/spotify-playlist?url=https://open.spotify.com/playlist/<id>
```

Expected response:

```json
{
  "title": "Playlist title",
  "thumbnail": "https://i.scdn.co/image/...",
  "tracks": [
    {
      "title": "Track",
      "artist": "Artist"
    }
  ],
  "source": "spotify-web-api"
}
```

Important:

- do not download Spotify audio
- only extract metadata: title, artist, thumbnail
- Void then matches tracks against YouTube Music

---

## Spotify Importer Improvements

The current importer should be improved before production.

### Fix silent failure

Avoid this pattern:

```ts
const apiTracks = await page.evaluate(...).catch(() => [] as Track[]);
```

Use:

```ts
let apiTracks: Track[] = [];

try {
  apiTracks = await page.evaluate(/* Spotify API extraction */);
} catch (error) {
  console.error("Spotify web API strategy failed:", error);
}
```

Reason:

- silent errors make debugging hell
- Android only sees “30 tracks again” and nobody knows why

### Fix bad threshold

Avoid:

```ts
if (apiTracks.length > 30)
```

Use:

```ts
if (apiTracks.length > 0)
```

Reason:

- real playlists can have 30 or fewer songs
- API result should still be accepted

### Add debug response

Optional dev mode:

```json
{
  "title": "Playlist",
  "thumbnail": "...",
  "tracks": [],
  "source": "spotify-web-api",
  "count": 247,
  "debug": {
    "apiTracks": 247,
    "domTracks": 30,
    "strategy": "spotify-web-api"
  }
}
```

### Add clear error response

```json
{
  "error": "SPOTIFY_IMPORT_FAILED",
  "message": "Could not resolve playlist tracks",
  "title": "Playlist title",
  "thumbnail": "..."
}
```

Acceptance:

- single Spotify playlist imports return full track count when possible
- playlists with <=30 songs still work
- failures include useful error/debug info
- Android can show a proper failed/partial state

---

## Import Results / Repair Screen

After importing, never just silently add everything.

Show:

```text
Imported 184 songs

✅ 160 matched
⚠️ 19 unsure
❌ 5 not found
```

### States

#### Matched

A confident YouTube Music match.

Show:

- Spotify title/artist
- matched YTM title/artist
- duration if available
- source badge

#### Unsure

Possible match, but confidence is low.

Allow user to choose:

- official audio
- lyric video
- music video
- remix
- live version
- cover
- search manually

#### Not found

Allow:

- retry search
- edit search query
- skip
- add as placeholder
- open web search

Acceptance:

- user can review wrong matches
- user can manually fix bad matches
- user can skip missing tracks
- user sees final imported count

---

## Track Matching Rules

When matching imported metadata to YouTube Music, prefer:

1. exact title + artist
2. official audio
3. same/similar duration
4. album version
5. lyric video
6. official music video

Avoid by default:

- slowed versions
- sped up versions
- bass boosted versions
- nightcore edits
- live versions
- covers
- remixes
- reaction videos
- lyric videos with unrelated uploader spam
- “TikTok edit” unless user explicitly wants that

Normalize title before matching:

```text
Remove:
- (official video)
- [official audio]
- lyrics
- lyric video
- slowed
- sped up
- bass boosted
- nightcore
- remix
- live
- visualizer
- tik tok
- 8D
```

Confidence scoring example:

```text
+50 exact title
+30 exact artist
+10 duration within 5 seconds
+10 official channel
-30 live
-30 cover
-20 remix
-20 slowed/sped
-15 very different duration
```

Acceptance:

- imported playlists do not fill with garbage edits
- wrong match rate is visibly reduced
- uncertain results go to repair instead of being blindly added

---

# Core Feature: My Version

## Goal

Allow each song to have saved user audio effects.

This is the main unique Void feature.

A user can make:

```text
After Dark → slowed + reverb
Kerosene → bass boost + 0.85x speed
Phonk track → hard bass + pitch down
```

Void remembers the version for that exact track.

---

## My Version Data Model

For every track:

```json
{
  "trackId": "ytm_or_local_id",
  "enabled": true,
  "speed": 0.85,
  "pitch": -1,
  "bass": 6,
  "reverb": 0.25,
  "vocalBoost": false,
  "spatial": false,
  "name": "Slowed + Bass",
  "autoApply": true,
  "updatedAt": 1710000000
}
```

### Required effect controls

Start simple:

- speed
- pitch
- bass
- reverb

Later:

- treble
- vocal boost
- compressor
- 8D/spatial
- muffled
- equalizer bands

---

## My Version UI

Add to Now Playing:

```text
My Version
Original
Save as My Version
Reset
Apply to Playlist
```

Possible UX:

- tap portal icon = quick toggle
- long press portal icon = open effect sheet
- bottom sheet for sliders
- preview updates live
- save button persists per-track settings

### Quick presets

Inside My Version sheet:

```text
Original
Slowed
Sped Up
Bass Boost
Slowed + Reverb
Night Drive
Phonk Boost
Clean Vocals
```

Important:

This is not a separate “preset store” yet. Keep it as part of My Version.

Acceptance:

- user can edit effects while track plays
- user can save settings per song
- settings auto-apply next time song plays
- user can reset to original
- My Version icon appears beside edited tracks

---

## My Versions Auto Playlist

Automatically create a virtual playlist:

```text
My Versions
```

Contains all tracks with saved effects.

Also useful filters:

```text
Recently edited
Most used preset
Downloaded My Versions
```

Acceptance:

- any track with saved effects appears in My Versions
- removing/resetting effects removes it from the virtual playlist
- My Versions can be downloaded if downloads are supported

---

# Core Feature: No Random Tracks

## Goal

Give users control over what plays next.

Void should avoid annoying autoplay garbage.

---

## Settings

Add a settings section:

```text
Playback Control
```

Options:

```text
Only play my library
Do not autoplay random tracks
Prefer official audio
Avoid live versions
Avoid covers
Avoid remixes
Avoid slowed/sped edits
Avoid repeated songs
Avoid same artist too often
```

### Modes

#### Normal

Existing behavior.

#### Library Only

Only play:

- liked tracks
- playlists
- downloaded tracks
- local files
- imported tracks

#### Strict

Do not add anything outside current queue/playlist.

#### Discovery

Allow recommendations, but filter garbage:

- no live
- no cover
- no remix
- no duplicates
- no recently skipped

Acceptance:

- no-random mode prevents unrelated autoplay
- strict mode never expands queue without user action
- discovery mode still works but filters bad matches

---

# Core Feature: Void Shuffle

## Goal

Make shuffle feel less stupid.

Rules:

- fewer repeats
- avoid same artist too often
- avoid songs skipped recently
- keep same vibe if possible
- allow reshuffle remaining

UI:

```text
Shuffle
Void Shuffle
Reshuffle Remaining
```

Algorithm ideas:

```text
Score each candidate:
- recently played penalty
- same artist penalty
- same album penalty
- skipped penalty
+ liked bonus
+ same playlist vibe bonus
```

Acceptance:

- shuffle does not repeat the same songs constantly
- user can reshuffle remaining queue
- Void Shuffle respects No Random settings

---

# Core Feature: Better Queue

## Goal

Make queue fast and powerful.

Required features:

- drag reorder
- swipe play next
- add to end
- remove selected
- multi-select
- save queue as playlist
- queue history
- undo remove
- reshuffle remaining

UI idea:

```text
Queue
Now Playing
Up Next
History
```

Acceptance:

- queue can be reordered
- multiple tracks can be selected
- queue can be saved as playlist
- user can undo accidental remove

---

# Core Feature: Animated Artist Pages

## Goal

Artist pages should feel premium and alive.

Not just a list of tracks.

---

## Artist Page Layout

Hero section:

- animated banner / GIF / canvas if available
- fallback animated blur from cover art
- artist avatar
- artist name
- follow button
- top track button
- saved count if available

Sections:

```text
Top Tracks
Albums
Singles
Your Saved Songs
Similar Artists
New Releases
```

### Animation Sources

Use in order:

1. existing canvas/video if available
2. artist image/banner if available
3. animated blur from album art
4. Void portal fallback

Fallback animation:

- blurred album art
- slow scale movement
- grain overlay
- subtle particles/glow
- glass overlay for text readability

Acceptance:

- every artist page has a good-looking hero
- pages do not look empty when no animation is available
- animations are not too heavy
- respect reduced motion accessibility settings

---

# Core Feature: Desktop / Tablet Layout

## Goal

Do not stretch mobile UI on wide screens.

Create a proper desktop/tablet layout.

---

## Layout

Wide screen:

```text
Left Sidebar:
- Home
- Search
- Library
- Downloads
- My Versions
- Imports
- Settings

Center:
- current screen content

Right panel optional:
- Queue
- Lyrics
- Artist info

Bottom:
- full player bar
```

Tablet:

```text
Sidebar + content
or
content + queue split
```

Phone:

```text
Bottom navigation
mini player
full-screen now playing
```

Acceptance:

- phone layout remains good
- tablet layout uses extra space
- desktop layout has sidebar and bottom player
- queue/lyrics can be opened as side panels on wide screens

---

# Core Feature: Onboarding

## Goal

Let users set up Void without forcing a 40-screen questionnaire.

---

## First Launch Flow

Screen 1:

```text
Welcome to Void
```

Options:

```text
Import from Spotify
Pick genres
Pick artists
Start empty
```

### Genre setup

Allow multi-select:

```text
Rap
Phonk
Rock
Electronic
Pop
Ukrainian
Sad
Gym
Night Drive
Anime
Metal
Indie
```

### Artist setup

Search/select artists the user likes.

Use this for:

- initial recommendations
- home page sections
- avoiding random garbage
- starting library personalization

Acceptance:

- user can skip onboarding
- user can import first instead of manually picking genres
- selected genres/artists are saved
- home screen uses chosen preferences

---

# Later Feature: Listen Together

## Goal

Allow users to listen in a shared room.

Not v0.1. Do after core playback/import features are stable.

---

## Room Features

```text
Create room
Join room
Host controls playback
Synced queue
Members can suggest songs
Background listening
Floating player
```

### Sync model

Host is source of truth:

```text
trackId
positionMs
isPlaying
queue
lastUpdatedAt
```

Clients correct drift:

```text
if drift > 1500ms:
  seek to host position
```

Acceptance:

- two devices can play same song roughly synced
- queue changes sync
- user can listen in background
- network reconnect does not fully break room

---

# Library+ Features

## Goal

Make library organization better than stock YTM.

Required:

- folders for playlists
- search inside playlists
- sort by title/artist/album/date added
- bulk edit
- pin playlists
- tags
- duplicate finder
- missing import finder

---

## Playlist Folders

Example:

```text
Gym
  - Phonk
  - Hardstyle
  - Rap

Night
  - Slowed
  - Dark Vibe
  - Rain Playlist
```

Acceptance:

- user can create folders
- user can move playlists into folders
- folders appear in Library
- folders work on phone/tablet/desktop

---

## Duplicate Finder

Detect duplicates by:

- same normalized title
- same normalized artist
- similar duration
- same source id if available

Show:

```text
Duplicate Tracks Found
```

Actions:

```text
Keep first
Keep highest quality
Keep official audio
Remove selected
```

Acceptance:

- user can find duplicate tracks in playlist/library
- user can safely remove selected duplicates
- no destructive action without confirmation

---

# Downloads / Cache Manager

## Goal

Make offline storage understandable.

Show:

```text
Downloaded songs: 342
Used space: 3.7 GB
Cache: 900 MB
```

Actions:

- clear cache
- delete playlist downloads
- download only on Wi-Fi
- audio quality
- auto-download liked songs
- remove old unused downloads

Acceptance:

- user can see storage usage
- user can clear cache
- user can remove downloads by playlist
- no accidental deletion without confirmation

---

# Share / Deep Links

## Goal

Create a Void link system.

Supported deep links:

```text
void://track/<id>
void://playlist/<id>
void://preset/<id>
void://artist/<id>
void://import?url=<encoded-url>
```

Web share page later:

```text
Open in Void
Open in YouTube Music
Open in Spotify
Open in Apple Music
```

Most important:

Share My Version:

```text
void://preset/<trackId>/<presetId>
```

Acceptance:

- Android can receive shared links
- Spotify links can open import screen
- track links can open player/search
- My Version links can apply/show effects

---


---

# Existing Void App Folder / Assets

There is already another **Void** app/project folder in the repository/workspace.

Before creating new assets from scratch, the agent should check that existing app folder for reusable Void assets, especially:

- logos
- app icons
- splash images
- PSD/source design files
- feature card images
- import/download/favorite graphics
- color references
- existing branding experiments
- any old Void UI components or styles

Suggested search targets:

```text
app/
apps/
void/
assets/
res/
resources/
drawable/
mipmap/
public/
branding/
icons/
logo/
```

Suggested file types to look for:

```text
.png
.jpg
.webp
.svg
.psd
.ai
figma exports
.xml vector drawables
```

Important:

- do not blindly overwrite existing Void assets
- reuse the best existing logo/icon work when possible
- keep old assets in a backup/archive folder if replacing them
- if multiple logo variants exist, compare them and pick the cleanest one for app icon usage
- use grunge/heavy versions for splash/onboarding, but use clean/vector-like versions for the actual app icon
- if PSD files exist, export preview PNGs before deciding
- prefer consistent Void branding across Android, desktop, feature cards, splash screen, and README

Agent note:

```text
Before generating or redesigning logos/icons, inspect the existing Void app folder and asset directories first.
```


# Technical Guardrails

## Do not break

- playback
- search
- downloads/cache
- existing library
- login/session if present
- desktop/mobile builds

## Code style

- keep changes modular
- avoid huge random refactors
- no hardcoded debug hacks in production UI
- put feature flags around unfinished features
- prefer small PRs/commits

## Suggested modules

```text
modules/importer
modules/matcher
modules/audio-presets
modules/library-tools
modules/void-links
modules/artist-visuals
```

---

# Suggested Data Models

## ImportedTrack

```ts
type ImportedTrack = {
  source: "spotify" | "soundcloud" | "telegram" | "youtube" | "text" | "apple";
  sourceUrl?: string;
  title: string;
  artist: string;
  album?: string;
  durationMs?: number;
  thumbnail?: string;
};
```

## MatchedTrack

```ts
type MatchedTrack = {
  imported: ImportedTrack;
  matchStatus: "matched" | "unsure" | "not_found";
  confidence: number;
  ytmId?: string;
  ytmTitle?: string;
  ytmArtist?: string;
  ytmDurationMs?: number;
  warnings?: string[];
};
```

## MyVersionPreset

```ts
type MyVersionPreset = {
  trackId: string;
  enabled: boolean;
  name?: string;
  speed: number;
  pitch: number;
  bass: number;
  reverb: number;
  autoApply: boolean;
  updatedAt: number;
};
```

## PlaybackControlSettings

```ts
type PlaybackControlSettings = {
  mode: "normal" | "library_only" | "strict" | "discovery";
  preferOfficialAudio: boolean;
  avoidLive: boolean;
  avoidCovers: boolean;
  avoidRemixes: boolean;
  avoidSlowedSped: boolean;
  avoidRepeats: boolean;
  avoidSameArtistTooOften: boolean;
};
```

---

# Screens To Add / Update

## Add

- Import Tracks
- Import Results
- My Version bottom sheet
- My Versions library page
- Playback Control settings
- Playlist folders
- Duplicate finder
- Downloads/cache manager
- Artist page redesign
- Onboarding genre/artist setup

## Update

- Now Playing
- Track row
- Playlist screen
- Queue screen
- Library screen
- Home screen
- Desktop/tablet shell
- Settings screen

---

# Agent Task Order

Recommended implementation order:

```text
1. Rebrand SimpMusic to Void
2. Replace app icon/splash/about branding
3. Add Void theme tokens and basic glass style
4. Add new icon system
5. Add internal feature cards
6. Integrate Spotify importer endpoint config
7. Build Import Tracks screen
8. Build Import Results / Repair screen
9. Build matching confidence model
10. Add My Version data storage
11. Add My Version controls in Now Playing
12. Add My Versions virtual playlist
13. Add No Random Tracks settings
14. Improve queue controls
15. Add Animated Artist page hero
16. Add desktop/tablet layout improvements
17. Add onboarding genre/artist setup
18. Add downloads/cache manager polish
19. Add Void deep links
20. Later: Listen Together beta
```

---

# Acceptance Checklist

## Branding

- [ ] App name is Void
- [ ] App icon changed
- [ ] Splash screen changed
- [ ] About screen changed
- [ ] Main visible branding updated
- [ ] license notices preserved

## Import

- [ ] Spotify playlist URL can import metadata
- [ ] playlists larger than 30 tracks use server-side importer
- [ ] import result screen shows matched/unsure/not found
- [ ] user can repair bad matches
- [ ] user can skip missing tracks
- [ ] no audio is downloaded from Spotify

## My Version

- [ ] user can change speed
- [ ] user can change pitch
- [ ] user can change bass
- [ ] user can change reverb
- [ ] user can save per-track settings
- [ ] saved settings auto-apply
- [ ] portal icon appears beside edited tracks
- [ ] My Versions playlist exists

## Playback Control

- [ ] library-only mode exists
- [ ] strict mode exists
- [ ] avoid live/remix/cover settings exist
- [ ] random autoplay can be disabled
- [ ] shuffle avoids repeats better than default

## UI

- [ ] feature cards added
- [ ] icons are consistent
- [ ] artist pages have animated hero/fallback
- [ ] desktop/tablet layout is not just stretched phone UI
- [ ] UI remains readable in dark mode

## Library

- [ ] playlist folders exist
- [ ] playlist search exists
- [ ] playlist sorting exists
- [ ] duplicate finder exists or is stubbed behind feature flag
- [ ] downloads/cache manager is clear

---

# Notes For Agent

Do not try to build every feature in one giant commit.

Start by making Void stable and branded. Then add importer and My Version. Those two features define the app.

The strongest product loop is:

```text
Import music → match tracks → save library → create My Version → listen without random garbage → share later
```

Avoid overbuilding AI recommendations early. If the core player/importer/presets are bad, AI will just generate premium-grade trash faster.

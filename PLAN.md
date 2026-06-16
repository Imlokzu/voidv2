# Void Music App — Implementation Plan

> Base: SimpMusic fork  
> Repo: https://github.com/Imlokzu/voidv2  
> Owner: Imlokzu  

---

## Phase Overview

| Phase | Goal | Status |
|-------|------|--------|
| P0 | Rebrand SimpMusic → Void | 🔲 |
| P1 | Spotify / SoundCloud Importer | 🔲 |
| P2 | My Version (per-track audio presets) | 🔲 |
| P3 | Playback Control (No Random, Void Shuffle) | 🔲 |
| P4 | Queue + Library upgrades | 🔲 |
| P5 | Animated Artist Pages | 🔲 |
| P6 | Desktop / Tablet layout | 🔲 |
| P7 | Onboarding | 🔲 |
| P8 | Deep links + Share | 🔲 |
| P9 | Listen Together (beta) | 🔲 |

---

## P0 — Rebrand (v0.1)

**Goal:** App installs as Void. No visible SimpMusic branding in main UI.

- [ ] Rename app package labels to `Void`
- [ ] Replace app icon with `assets/void/voidlogofortheapp.png`
- [ ] Replace splash screen
- [ ] Apply Void color system (near-black bg, purple/violet accent, glass dark cards)
- [ ] Update About screen
- [ ] Preserve license notices
- [ ] Verify: playback, search, library, downloads still work

**Key assets:**
- `assets/void/voidlogofortheapp.png` — app icon
- `assets/void/voidlogo.png` — branding logo
- `assets/void/voidicon.zip` — icon set
- `assets/void/void_live.png` / `void_live2.png` — live visuals

---

## P1 — Import Your Music

**Goal:** Import Spotify / SoundCloud / Telegram / YouTube links.

- [ ] Integrate server-side Spotify importer (`vercel/spotify-importer/`)
- [ ] Build `Import Tracks` screen
- [ ] Build `Import Results` / Repair screen (matched / unsure / not found)
- [ ] Track matching confidence model (exact title+artist, duration, official audio)
- [ ] Manual fix for bad matches
- [ ] No audio downloaded from Spotify — metadata only

**Server:**
- `/api/spotify-playlist?url=` — already implemented in `vercel/spotify-importer/`

---

## P2 — My Version

**Goal:** Per-song saved audio effects.

- [ ] Data model: speed, pitch, bass, reverb per trackId
- [ ] UI in Now Playing (bottom sheet with sliders)
- [ ] Quick presets: Original, Slowed, Sped Up, Bass Boost, Slowed+Reverb...
- [ ] Save / Reset per track
- [ ] Portal icon (🌀) beside edited tracks
- [ ] Auto virtual playlist: `My Versions`

---

## P3 — Playback Control

**Goal:** No random garbage autoplay.

- [ ] Settings: Library Only / Strict / Discovery / Normal
- [ ] Avoid live, covers, remixes, slowed/sped
- [ ] Avoid repeats
- [ ] Void Shuffle algorithm (smart scoring)

---

## P4 — Better Queue + Library

- [ ] Drag reorder, swipe play next
- [ ] Multi-select, save queue as playlist
- [ ] Queue history, undo remove
- [ ] Playlist folders
- [ ] Duplicate finder
- [ ] Downloads / cache manager

---

## P5 — Animated Artist Pages

- [ ] Hero: animated banner / GIF / canvas → fallback animated blur from album art
- [ ] Void portal fallback animation
- [ ] Sections: Top Tracks, Albums, Singles, Saved Songs, Similar Artists

---

## P6 — Desktop / Tablet

- [ ] Left sidebar navigation on wide screens
- [ ] Right panel for Queue / Lyrics / Artist info
- [ ] Bottom player bar (persistent)
- [ ] Phone layout unchanged

---

## P7 — Onboarding

- [ ] Screen 1: Welcome + Import from Spotify / Pick genres / Pick artists / Start empty
- [ ] Genre multi-select
- [ ] Artist search/select
- [ ] Skip allowed

---

## P8 — Deep Links

- [ ] `void://track/<id>`
- [ ] `void://playlist/<id>`
- [ ] `void://preset/<trackId>/<presetId>` — share My Version
- [ ] `void://import?url=<encoded-url>` — handle shared Spotify links

---

## P9 — Listen Together (later)

- [ ] Create / join room
- [ ] Host controls playback
- [ ] Synced queue
- [ ] Drift correction (>1500ms seek)

---

## Dev Notes

- Keep commits small and testable.
- Use feature flags for unfinished features.
- Do not break: playback, search, downloads, existing library.
- Prefer small PRs.

---

## Current Assets in Repo

- `VOID_AGENT_SPEC(1).md` — full product spec
- `vercel/spotify-importer/` — serverless Spotify playlist importer
- `assets/void/` — logos, icons, splash images
- `SimpMusic/` — cloned base project

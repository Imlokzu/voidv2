# Track Options Menu Redesign Spec

## Goal
Redesign `NowPlayingBottomSheet` (the 3-dot track menu) to feel premium with:
1. Blurred album art as the menu background
2. Glass-morphic action buttons
3. Smooth staggered entrance animations
4. Motion-blur feel through spring animations

## File
`composeApp/src/commonMain/kotlin/com/maxrave/simpmusic/ui/component/ModalBottomSheet.kt`

## Target Composable
`NowPlayingBottomSheet` (~line 1660 onwards, inside the `ModalBottomSheet` content)

## Current Structure
```
ModalBottomSheet (transparent container)
└── Card (solid #FF242424 bg)
    ├── Drag handle pill
    ├── Row (thumbnail 60dp + title + artist)
    ├── Divider
    ├── ActionButton rows (like, download, add-to-playlist, play-next, queue, artists, album, radio, lyrics, sleep-timer)
    └── Extra spacing
```

## New Design

### Background
- Replace `Card` with a `Box` that fills width
- Inside the Box, add an `AsyncImage` with the song thumbnail
  - `contentScale = ContentScale.Crop`
  - `modifier = Modifier.fillMaxSize()`
  - Apply a strong blur effect: use `Modifier.blur(40.dp)` or the existing haze/liquid glass infra if available
  - Overlay a dark scrim: `Color(0xCC0A0A0F)` (80% opacity dark) on top so text stays readable
- This creates the "blurred cover" background

### Container
- Keep the bottom sheet shape: `RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)`
- Remove the old solid-color `Card`
- Use a `Column` directly with padding
- The drag handle pill should be semi-transparent white: `Color(0x4DFFFFFF)` on a `RoundedCornerShape(50%)`

### Song Info Row
- Keep thumbnail (make it slightly larger: 72dp, `RoundedCornerShape(16.dp)`)
- Add a subtle shadow/elevation to the thumbnail
- Title: `typo().titleSmall`, white
- Artist: `typo().bodyMedium`, `Color(0xB3FFFFFF)`
- Add a crossfade animation when switching songs

### Action Buttons
Each `ActionButton` should:
- Be wrapped in an `AnimatedVisibility` with slide-in from bottom + fade
- Stagger delay: `index * 40ms`
- Use a glass-morphic background:
  ```
  Box(
      modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(Color(0x26FFFFFF)) // 15% white
  )
  ```
- On press: scale down to 0.97 with spring animation
- Icon: 24dp, white
- Text: `typo().labelLarge`, white, weight = Medium
- Height: 56dp minimum
- Add divider between groups (optional) or keep clean spacing of 8dp

### Entrance Animation
When the sheet opens:
1. Background blur fades in (200ms)
2. Drag handle slides down from top (150ms delay)
3. Song info fades + scales from 0.95 → 1.0 (200ms delay)
4. Action buttons stagger in from bottom (300ms base + 40ms * index)
5. Use `spring(dampingRatio = 0.6f, stiffness = 300f)` for bouncy feel

### Scroll
- Keep `Modifier.verticalScroll(rememberScrollState())`
- Add overscroll glow in purple tint (optional, nice-to-have)

### Exit
- When dismissing, reverse the stagger animation quickly (100ms per item)
- The blur backdrop fades out first

## Implementation Notes
- The existing `ActionButton` composable is defined in the same file. You may modify it or create a new `GlassActionButton` wrapper.
- Keep all click handlers and `viewModel.onUIEvent(...)` calls exactly as-is.
- The `AsyncImage` for the blurred background should use `ImageRequest.Builder` with `crossfade(400)`.
- For blur on non-Android platforms, fallback to a dark overlay without blur.
- Do NOT change the `ModalBottomSheet` shell (sheetState, onDismissRequest, scrimColor) — only modify the `content` block.

## Acceptance
- [ ] Background shows blurred album art with dark overlay
- [ ] Action buttons have glass background
- [ ] Buttons stagger-animate in on open
- [ ] Press on button has spring scale feedback
- [ ] All existing actions still work (like, download, add to playlist, etc.)
- [ ] No compile errors

# Frosted Glass + 3-Dot Menu Redesign Spec

## Goal
Redesign the 3-dot menu (NowPlayingBottomSheet) with frosted glass and a quadrant/grid layout instead of stacked lines. Also apply frosted glass to all pop-ups.

## What is Frosted Glass?
Lighter than liquid glass. Just blur + subtle white tint. No lens/refraction effects.

```kotlin
@Composable
fun Modifier.frostedGlass(blurRadius: Dp = 20.dp): Modifier = composed {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        graphicsLayer {
            renderEffect = RenderEffect.createBlurEffect(
                blurRadius.toPx(), blurRadius.toPx(),
                Shader.TileMode.DECAL
            )
        }
    } else {
        background(Color(0x1AFFFFFF))
    }
}
```

But since this is commonMain, we can't use `Build.VERSION`. So instead create the frosted look using simple translucent backgrounds:
- Bottom sheet container: `Color(0xB30E0E14)` with rounded corners 28.dp
- Button backgrounds: `Color(0x26FFFFFF)` (15% white) — same as glass buttons already done
- Dialog backgrounds: `Color(0xB30E0E14)` with rounded corners 24.dp

## 3-Dot Menu Redesign

### Current
Single column of full-width buttons stacked closely.

### New Layout

Use a **2-column grid** for action buttons:
```
┌─────────────┬─────────────┐
│  [💾]       │  [➕]       │
│  Download   │  Add to PL  │
├─────────────┼─────────────┤
│  [▶]        │  [📋]       │
│  Play Next  │  Add Queue  │
├─────────────┼─────────────┤
│  [🎤]       │  [💿]       │
│  Artists    │  Album      │
├─────────────┼─────────────┤
│  [📡]       │  [❤️]       │
│  Radio      │  Like       │
└─────────────┴─────────────┘
```

Implementation: Replace the `actionButtons.forEachIndexed` single-column layout with `FlowRow` or a custom grid using `Row` + `weight()`.

Each button cell:
- `Modifier.weight(1f).padding(6.dp)`
- Container: `Box` with `clip(RoundedCornerShape(20.dp)).background(Color(0x26FFFFFF)).padding(12.dp)`
- Icon on top (28.dp), text below, centered
- Height: ~80.dp minimum
- Spring press effect (already exists, keep it)

### Delete/Lyrics/Sleep Timer
Keep these as full-width single buttons BELOW the grid, since they're conditional.

### Spacing
- Grid gap: 12.dp horizontal, 12.dp vertical
- Padding around grid: 16.dp horizontal, 8.dp vertical
- Delete/Sleep/Lyrics buttons: 16.dp horizontal padding, 8.dp vertical gap

## Other Bottom Sheets
Apply frosted glass container to ALL bottom sheets in `ModalBottomSheet.kt`:
- Card container → `Color(0xB30E0E14)`
- Shape → `RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)`

Sheets to update:
- `SleepTimerBottomSheet`
- `PlaybackSpeedPitchBottomSheet`
- `SortPlaylistBottomSheet`
- `PlaylistBottomSheet`
- `LocalPlaylistBottomSheet`
- `AddToPlaylistModalBottomSheet`
- `ArtistModalBottomSheet`

## AlertDialogs
Change from `Color(0xFF242424)` to `Color(0xB30E0E14)` with `shape = RoundedCornerShape(24.dp)`.

## Files
- `composeApp/src/commonMain/.../ModalBottomSheet.kt` (main file)

## Acceptance
- [ ] 3-dot menu has 2-column grid layout
- [ ] Delete/Lyrics/Sleep timer still appear conditionally below grid
- [ ] All bottom sheets use frosted glass dark container
- [ ] All AlertDialogs use glass container color
- [ ] App compiles successfully

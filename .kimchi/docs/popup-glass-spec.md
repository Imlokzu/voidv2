# Pop-up Liquid Glass Redesign Spec

## Goal
Make ALL pop-ups, bottom sheets, dialogs, and menus in the app feel premium with liquid glass / blurred backgrounds and consistent styling.

## Scope
This is Phase 1 — focus on the core file `ModalBottomSheet.kt` which contains:
- `NowPlayingBottomSheet` (the 3-dot track menu)
- `AddToPlaylistModalBottomSheet`
- `ArtistModalBottomSheet`
- `SleepTimerBottomSheet`
- `PlaybackSpeedPitchBottomSheet`
- `PlaylistBottomSheet`
- `LocalPlaylistBottomSheet`
- `SortPlaylistBottomSheet`

## Design Direction

### 1. NowPlayingBottomSheet (3-dot menu)

**Current:** Already has blurred cover bg + dark overlay + content. But user wants it cleaner.

**New layout:**
```
Box (full sheet container, aspect ~ 16:9 for the top area)
├── AsyncImage (full song cover, ContentScale.Crop, blur(60.dp))
├── Box (dark gradient overlay from bottom — so top of cover shows through)
│   ├── gradient: transparent → #CC0A0A0F
│   └── Box (small centered album art on top of blurred bg)
│       ├── AsyncImage (small cover, 96.dp, RoundedCornerShape(20.dp), shadow)
│       ├── Column (title + artist below)
│       │   ├── Title (typo().titleMedium, white, maxLines=1)
│       │   └── Artist (typo().bodyMedium, #B3FFFFFF)
│   └── Drag handle pill at very top
│
└── Scrollable content area below:
    ├── Glass action buttons (one per row, rounded 16.dp, #26FFFFFF bg)
    ├── Spring press scale (0.97f)
    ├── Staggered entrance animation
```

**Key changes from current:**
- Use a `Box` with aspect ratio for the header area instead of a Row
- Full cover as header bg (blurred)
- Small cover (96.dp, 20.dp corners, shadow) centered on top
- Title + artist centered below small cover
- Remove the horizontal `Row` layout for the header
- Keep glass buttons from previous work
- The content area starts below the header and is scrollable

### 2. All Bottom Sheets (ModalBottomSheet in ModalBottomSheet.kt)

Every `ModalBottomSheet(...)` content block should get a glass treatment.

Create a reusable wrapper composable inside ModalBottomSheet.kt:
```kotlin
@Composable
fun GlassBottomSheetContent(
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val hazeState = rememberHazeState()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
    ) {
        // Glass background
        Box(
            modifier = Modifier
                .matchParentSize()
                .hazeEffect(
                    hazeState,
                    style = HazeMaterials.regular()
                )
                .background(Color(0xB30A0A0F))
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            header()
            content()
        }
    }
}
```

Apply to:
- `SleepTimerBottomSheet`
- `PlaybackSpeedPitchBottomSheet`
- `PlaylistBottomSheet`
- `LocalPlaylistBottomSheet`
- `SortPlaylistBottomSheet`
- `AddToPlaylistModalBottomSheet`
- `ArtistModalBottomSheet`

For sheets that ONLY show a list of items (like playlists, artists), keep them simple with:
- Drag handle
- Title text (if any)
- List items with glass card background (#1AFFFFFF)
- No blurred cover for non-track menus

### 3. All AlertDialogs in ModalBottomSheet.kt

Current: Hardcoded `Color(0xFF242424)` container. 

New: Use liquid glass.

For AlertDialogs, we can't easily apply haze. So instead change to:
```kotlin
AlertDialog(
    containerColor = Color(0xB30E0E14),
    shape = RoundedCornerShape(24.dp),
    ...
)
```

Replace every `Color(0xFF242424)` in AlertDialog declarations with `Color(0xB30E0E14)`.

Also add glass styling to dialog text fields and buttons:
- `OutlinedTextField` in `NamePlaylistDialog`: `containerColor = Color(0x26FFFFFF)`
- Buttons: keep as-is but ensure text is readable on dark glass

## Implementation Order

1. Read the current `ModalBottomSheet.kt` around line 1620 to see the `NowPlayingBottomSheet` ModalBottomSheet block
2. Redesign the header area (cover bg + small cover overlay + centered text)
3. Add `GlassBottomSheetContent` wrapper
4. Apply wrapper to other bottom sheets
5. Update AlertDialog colors
6. Check imports / add missing ones

## Acceptance
- [ ] 3-dot menu has blurred cover header + small centered cover + centered title/artist
- [ ] 3-dot menu buttons are glass-styled with spring press
- [ ] All bottom sheets have consistent glass backdrop
- [ ] All AlertDialogs in the file use dark glass color instead of solid #242424
- [ ] No compile errors

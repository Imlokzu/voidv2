# Motion Blur Spec

## Goal
Add motion blur / directional blur effects during scrolling and screen transitions to make the app feel ultra-smooth and premium.

## Approach
True pixel-motion blur is expensive. We simulate it with:
1. **Scroll velocity blur** — `LazyColumn`/`LazyRow` items get slight directional blur + scale based on scroll speed
2. **Screen transition blur** — Enter/exit transitions include backdrop blur fade
3. **Bottom sheet scrim blur** — Already has dark scrim, add animated blur to it
4. **Player open/close** — Add blur to content behind the player sheet

## Chunk 1: Scroll Velocity Blur Modifier
**Files:**
- New: `composeApp/src/commonMain/kotlin/com/maxrave/simpmusic/ui/component/MotionBlurScroll.kt`

**Implementation:**
Create `Modifier.motionBlurScroll(lazyListState: LazyListState)` that:
- Observes `lazyListState.firstVisibleItemScrollOffset` delta over frames
- Calculates velocity (pixels per frame)
- Applies `Modifier.graphicsLayer { renderEffect = if (velocity > threshold) BlurMaskFilter(...) else null }` on Android
- Falls back to `Modifier.alpha(if (fast) 0.85f else 1f)` + slight vertical squish on non-Android
- Spring back to identity when scroll stops (dampingRatio = 0.7f, stiffness = 200f)

Apply this modifier to:
- `HomeScreen.kt` — LazyColumn items
- `LibraryDynamicPlaylistScreen.kt` — LazyColumn items
- `SearchScreen.kt` — LazyColumn items
- `LocalPlaylistScreen.kt` — LazyColumn items
- `AlbumScreen.kt` — LazyColumn items
- `PlaylistScreen.kt` — LazyColumn items
- `ArtistScreen.kt` — LazyColumn items

## Chunk 2: Navigation Transition Blur
**Files:**
- `composeApp/src/commonMain/kotlin/com/maxrave/simpmusic/App.kt`

**Implementation:**
In the `NavHost` composable, add `enterTransition` / `exitTransition` / `popEnterTransition` / `popExitTransition` that includes:
- `fadeIn(tween(250))` + `scaleIn(initialScale = 0.98f, animationSpec = spring())`
- `fadeOut(tween(200))` + `scaleOut(targetScale = 1.02f, animationSpec = spring())`
- Add a `BlurTransitionEffect` that crossfades a blurred snapshot of the outgoing screen

If NavHost already has custom transitions, update them to include blur.

## Chunk 3: Bottom Sheet + Player Blur Enhancement
**Files:**
- `composeApp/src/commonMain/kotlin/com/maxrave/simpmusic/ui/component/ModalBottomSheet.kt` (already modified)
- `composeApp/src/commonMain/kotlin/com/maxrave/simpmusic/ui/screen/player/NowPlayingScreen.kt`
- `composeApp/src/commonMain/kotlin/com/maxrave/simpmusic/ui/screen/MiniPlayer.kt`

**Implementation:**
- ModalBottomSheet scrim: change from solid `Color.Black.copy(alpha = .5f)` to a crossfade between clear and blurred backdrop. Use `HazeState` from the existing haze library.
- MiniPlayer expand: add a spring-animated scale up (0.98f → 1f) when tapping to expand
- NowPlayingScreen enter: animate the background album art from blur(60.dp) to blur(0.dp) with Crossfade

## Chunk 4: App Launch / Splash Motion
**Files:**
- `composeApp/src/commonMain/kotlin/com/maxrave/simpmusic/App.kt` or splash entry point

**Implementation:**
- On first launch, add a brief (200ms) blur fade-in of the home screen from `Color.Black` + `blur(20.dp)` to identity

## Acceptance
- [ ] Scrolling lists feel smoother with velocity blur
- [ ] Screen transitions have blur fade
- [ ] Bottom sheet scrim uses blur
- [ ] Player open/close has blur animation
- [ ] App launch has smooth fade-in
- [ ] No compile errors

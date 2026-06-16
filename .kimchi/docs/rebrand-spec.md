# Void Rebrand Spec — P0

## Goal
Rebrand SimpMusic → Void with a darker, purple-accented palette and enhanced liquid glass.

## Chunk 1: Color Palette + Theme
**Files:**
- `composeApp/src/commonMain/kotlin/com/maxrave/simpmusic/ui/theme/Color.kt`
- `composeApp/src/commonMain/kotlin/com/maxrave/simpmusic/ui/theme/Theme.kt`

**Changes:**
- Replace all `md_theme_dark_*` colors with Void palette:
  - `primary`: `#7B61FF` (violet)
  - `onPrimary`: `#FFFFFF`
  - `primaryContainer`: `#2A1B5E`
  - `onPrimaryContainer`: `#E8DDFF`
  - `secondary`: `#B4A8D8`
  - `onSecondary`: `#1F1A33`
  - `secondaryContainer`: `#2D2744`
  - `onSecondaryContainer`: `#D5CCEB`
  - `tertiary`: `#8BB8F0`
  - `onTertiary`: `#0A1A2E`
  - `tertiaryContainer`: `#142A44`
  - `onTertiaryContainer`: `#C8DDF8`
  - `background`: `#0A0A0F`
  - `onBackground`: `#E8E6ED`
  - `surface`: `#0E0E14`
  - `onSurface`: `#E8E6ED`
  - `surfaceVariant`: `#1A1A24`
  - `onSurfaceVariant`: `#9E9DB0`
  - `outline`: `#4A4A5A`
  - `inverseOnSurface`: `#0A0A0F`
  - `inverseSurface`: `#E8E6ED`
  - `inversePrimary`: `#9E88FF`
  - `surfaceTint`: `#7B61FF`
  - `outlineVariant`: `#2A2A38`
  - `scrim`: `#000000`
  - `error`: `#FF6B6B`
  - `errorContainer`: `#5C1A1A`
  - `onError`: `#FFFFFF`
  - `onErrorContainer`: `#FFD1D1`
- Remove `colorPrimaryDark`, `back_button_color`, `checkedFilterColor` or update to match
- Update `shimmerBackground` to `#1A1A24`, `shimmerLine` to `#2A2A38`
- Update `overlay` to `#400A0A0F`, `blackMoreOverlay` to `#800A0A0F`
- Update `bottomBarSeedDark` to `#7B61FF`
- Update `customGray` to `#40E8E6ED`, `customDarkGray` to `#401A1A24`
- Update `seed` to `#7B61FF`

## Chunk 2: Branding Strings
**Files:**
- `androidApp/src/main/res/values/app_name.xml`
- `androidApp/src/debug/res/values/app_name.xml`
- `composeApp/src/commonMain/composeResources/values/strings.xml`

**Changes:**
- `app_name`: "SimpMusic" → "Void"
- `app_name` (debug): "SimpMusic Dev" → "Void Dev"
- In `strings.xml`, replace visible "SimpMusic" references with "Void" for user-facing strings:
  - `credit_app` — rewrite to credit Void
  - `crash_log` — "SimpMusic stopped working" → "Void stopped working"
  - `sponsor_block_intro` — "In SimpMusic" → "In Void"
  - `help_build_lyrics_database` — "Help SimpMusic" → "Help Void"
  - `help_build_lyrics_database_description` — same
  - `sync_playlist_warning` — "SimpMusic will create" → "Void will create"
  - `translation_language_message` — "SimpMusic's language" → "Void's language"
  - `log_in_warning` — "SimpMusic now requires" → "Void now requires"
  - `enjoying_simpmusic` → `enjoying_void`
  - `if_you_enjoy_using_simpmusic` → `if_you_enjoy_using_void`
  - `simpmusic_charts` → `void_charts`
  - `simpmusic_lyrics` → `void_lyrics`
  - `lyrics_provider_simpmusic` → `lyrics_provider_void`
  - `auto_backup_description` — "Downloads/SimpMusic" → "Downloads/Void"
  - `open_app` — "Open SimpMusic" → "Open Void"
  - `canvas_info` — "In SimpMusic" → "In Void"
  - `blog_promo_message` — rewrite/remove
- Keep license/credit references to original authors unchanged

## Chunk 3: Android Manifest + Deep Links
**Files:**
- `androidApp/src/main/AndroidManifest.xml`
- `androidApp/src/main/res/xml/shortcuts.xml`

**Changes:**
- `android:label="@string/app_name"` stays (picks up new string)
- Deep link host: `simpmusic.org` → `void.app` (or keep both for migration)
- Deep link scheme: `simpmusic` → `void`
- Update `shortcut` targetPackage references if needed

## Chunk 4: Liquid Glass Enhancement
**Files:**
- `composeApp/src/androidMain/kotlin/com/maxrave/simpmusic/ui/component/LiquidGlassContainer.android.kt`
- `composeApp/src/androidMain/kotlin/com/maxrave/simpmusic/expect/ui/LiquidGlass.android.kt`
- `composeApp/src/commonMain/kotlin/com/maxrave/simpmusic/App.kt` (maybe)

**Changes:**
- In `drawInteractiveGlass`:
  - Increase default blur: `8f.dp.toPx()` → `16f.dp.toPx()`, max `16f.dp.toPx()` → `32f.dp.toPx()`
  - Increase saturation: `1.5f` → `1.8f`
  - Add subtle purple tint to glass surface overlay when background is bright
  - Darken the surface: `0.12f` → `0.18f` min, `0.5f` → `0.6f` max
  - Make liquid glass **default ON** for all surfaces where it's optional
- In `LiquidGlass.android.kt`:
  - Match the blur and color control values
  - Increase lens refraction for stronger glass feel

## Chunk 5: Launcher Icon Replacement
**Files:**
- `androidApp/src/main/res/mipmap-*/ic_launcher*.webp`
- `androidApp/src/main/res/drawable/ic_launcher_background.xml`
- `androidApp/src/main/res/drawable/monochrome.xml`

**Note:** Need to convert `assets/void/voidlogofortheapp.png` to Android adaptive icon format. We will create a simple vector or use the PNG for now.

## Acceptance
- [ ] App name shows "Void" everywhere user-facing
- [ ] Colors are dark with purple/violet accent
- [ ] Liquid glass blur is stronger and more pronounced
- [ ] Deep links use `void://` scheme
- [ ] No visible "SimpMusic" strings in main UI

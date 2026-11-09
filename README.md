# 🍀 Flora Music

<div align="center">

**A modern, feature-packed local music player for Android built with Jetpack Compose & Material 3.**  
Designed for music lovers and power users — featuring word-level synchronized lyrics, physical ID3 tag editing, Spotify-style system media player notification, dynamic palette theming, and a fully modular navigation system.

[![Latest Release](https://img.shields.io/badge/Download-Latest%20APK-brightgreen?style=for-the-badge&logo=android&logoColor=white)](https://github.com/TulipNex/Flora-Music/releases/download/latest-build/Flora-Music-debug.apk)
[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202024.09.00-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Media3 ExoPlayer](https://img.shields.io/badge/AndroidX%20Media3-1.4.1-FF7043?style=for-the-badge)](https://developer.android.com/media/media3)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24%20(Nougat)-blue?style=for-the-badge)](https://developer.android.com)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-36%20(Android%2015)-green?style=for-the-badge)](https://developer.android.com)

[Overview](#overview) • [Key Features](#key-features) • [Architecture](#architecture--tech-stack) • [Project Structure](#project-structure) • [Download & Installation](#download--installation) • [Panduan Bahasa Indonesia](#ringkasan-bahasa-indonesia)

</div>

---

## 🌟 Overview

**Flora Music** is an elegant, offline-first music player engineered with high-performance modern Android components. Unlike traditional players, Flora focuses on deep customizability and lyrics fidelity — offering real-time word-by-word karaoke synchronization, direct physical tag editing via Jaudiotagger, a Spotify-style SystemUI media notification with interactive seekbar, dynamic color adaptation from album artwork, and fully customizable tabs.

---

## ✨ Key Features

### 🎵 1. Advanced Audio & Playback Engine
- **AndroidX Media3 (ExoPlayer 1.4.1)**: Rock-solid local audio decoding with low latency, gapless support, and hardware acceleration.
- **Spotify-Style System Media Player Notification**: Full integration with Android 11, 12, 13, 14, and 15 SystemUI media controls.
  - Interactive *squiggly waveform seekbar* on notification with live duration scrubbing.
  - Dynamic background tinting extracted from album cover palette (`MediaStyle.setMediaSession`).
  - Playback actions: Previous, Play/Pause, Next, Favorite (Like heart button), and Dismiss.
  - Output device switcher (Phone speaker, Bluetooth earphones/TWS).
- **Background Playback & Foreground MediaService**: Uninterrupted playback powered by `MediaLibraryService` with battery optimization safeguards.
- **Audio Intent Integration**: Supports voice queries and standard system search intents (`MEDIA_PLAY_FROM_SEARCH`, `SEARCH`).
- **Sleep Timer**: Customizable sleep timer with gradual audio fade-out before stopping playback.
- **Playback Speed Controller**: Quick presets for variable playback speeds (0.5x – 2.0x).
- **Interactive Queue Manager**: Reorder, remove, or jump to any song in the active playback queue.

### 🎤 2. Word-Level Synchronized Lyrics (BetterLyrics)
- **BetterLyrics API Integration**: Automatic online fetching for time-synchronized lyrics via the BetterLyrics service.
- **TTML XML Parser**: Parses syllable/word-level time tags into precise sub-frame timestamps.
- **Word-Level Karaoke Highlighting**: Smooth real-time color fill animations with grapheme cluster awareness and Right-to-Left (RTL) language handling.
- **Offline Caching**: Downloaded lyrics are persistently cached in a local SQLite database (Room).
- **Smart Fallback**: Seamless fallback to embedded synchronized (`SYLT`/LRC) or unsynced (`USLT`) tags inside the physical audio files.

### 🎨 3. Deep Lyrics & Visual Customization
- **Dynamic Theming**: Extracts dominant colors in real time from album artwork using **AndroidX Palette** to colorize lyric highlights and player backgrounds.
- **Custom Color Pickers**: Interactive HSV/Hue sliders and hex code inputs for both active and inactive lyric lines.
- **Custom Typography**: Pick system fonts (Serif, Sans-Serif, Monospace, Cursive) or import your own **`.ttf` / `.otf`** font files directly via Android's Storage Access Framework (SAF).
- **Layout Fine-Tuning**: Adjust active and non-active font sizes, alignment (Left, Center, Right), anchor position (Top, Center, Bottom), margins, and line height multipliers.
- **Fluid Spring Animations**: Custom bounce-click interactions and smooth swipe gestures.

### 🏷️ 4. Physical ID3 Tag & Metadata Editor
- **Direct File Modification**: Powered by **Jaudiotagger 3.0.1**, edits are committed directly into physical audio files on disk (MP3, FLAC, M4A, OGG, etc.).
- **Editable Metadata Fields**: Title, Artist, Album, Composer, Genre, Writer, Year, Track Number, Disc Number, and Comments.
- **Embed Custom Lyrics**: Write custom synchronized or plain lyrics directly into the file's ID3 tags.
- **Embed Custom Artwork**: Pick an image from your device gallery to replace or inject new album artwork directly into the physical audio file, with automatic cache invalidation.

### 📑 5. Fully Configurable Tab Navigation
- **Flexible Navigation Builder**: Personalize the tab bar layout to suit your habits.
- **Tab Hierarchy Roles**:
  - `MAIN`: Primary navigation tab.
  - `SUB`: Nested sub-tab within a parent module.
  - `HIDDEN`: Hide unused modules from the UI.
  - `HEADER`: Visual section divider.
- **Custom Tabs & Icon Picker**: Create custom tabs and assign customized vector icons.
- **Default Modules**: Beranda (Favorites, History, Top Played), Songs, Albums, Artists, and Collections (Playlists, Genres).

### ⚡ 6. High-Performance Media Scanning & Caching
- **Fast Media Scanner**: Scans device audio with real-time progress reporting.
- **Artwork Pre-Extractor Service**: Background service that pre-extracts and downsamples album covers upon library scan.
- **Multi-Tier LRU Disk Cache**: Custom `ArtworkCacheManager` combined with Coil ensures zero stutter when scrolling large libraries.
- **Folder Blacklist / Exclusion**: Easily exclude ringtones, voice notes, WhatsApp audio, or system sound folders from your library.
- **Custom Artist / Album Covers**: Override artist and album thumbnails independently from the gallery without modifying physical files.
- **View Toggle**: Switch effortlessly between Grid and List views.

---

## 🏗️ Architecture & Tech Stack

Flora follows modern Android Architecture guidelines with **MVVM**, **Repository Pattern**, and uni-directional reactive data flow.

```mermaid
graph TD
    UI[Jetpack Compose UI & Material 3] -->|Observes State| VM[MusicViewModel]
    VM -->|Dispatches Actions| PC[MusicPlayerController]
    VM -->|Requests Data| Repos[MusicRepository / LyricsRepository]
    PC -->|Controls| MS[MusicService - Media3 ExoPlayer]
    MS -->|MediaStyle & MediaSession| Notif[Spotify-Style System Media Notification]
    Repos -->|Reads / Writes| DB[(Room Database & SQLite)]
    Repos -->|Local Scan & Tags| Scanner[MusicScanner & Jaudiotagger]
    Repos -->|Fetches Lyrics| Net[BetterLyrics API - Retrofit/Moshi]
    Scanner -->|Thumbnails| Cache[ArtworkCacheManager & Coil]
```

| Layer | Technologies |
|---|---|
| **User Interface** | Jetpack Compose, Material 3, Compose Navigation, Material Icons Extended |
| **Architecture** | MVVM, Clean Architecture, Kotlin Coroutines, `StateFlow` / `SharedFlow` |
| **Media & Audio** | AndroidX Media3 ExoPlayer (`1.4.1`), `MediaLibraryService`, `MediaSession` |
| **Metadata & Tagging** | Jaudiotagger (`3.0.1`) |
| **Local Persistence** | Room Database (`2.7.0`) with KSP, SharedPreferences |
| **Networking & Parsing**| Retrofit (`2.12.0`), OkHttp (`4.10.0`), Moshi Kotlin (`1.15.2`), TTML XML Parser |
| **Image Loading & Color**| Coil Compose (`2.7.0`), AndroidX Palette (`1.0.0`) |
| **CI / CD** | Automated GitHub Actions workflow building and releasing debug APKs |
| **Target Platforms** | Android 7.0+ (API 24) to Android 15 (API 36) |

---

## 📁 Project Structure

```text
Flora-Music/
├── .github/
│   └── workflows/
│       └── build-apk.yml          # Automated CI/CD workflow to build & release APK
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── data/
│   │   │   │   │   └── repository/        # LyricsRepository & caching logic
│   │   │   │   ├── database/              # Room Database, DAOs, and Entities
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/             # Domain entities (Song, Playlist, LyricsData)
│   │   │   │   │   └── provider/          # Lyrics providers (BetterLyricsProvider)
│   │   │   │   ├── lyrics/                # TTML parser, LRC parser, LyricsEngine
│   │   │   │   ├── network/               # Retrofit API definitions & models
│   │   │   │   ├── player/                # Media3 ExoPlayer controller & MusicService
│   │   │   │   ├── presentation/          # Jetpack Compose UI screens, sliders & ViewModel
│   │   │   │   │   ├── FloraUI.kt         # Main navigation, tabs, and list/grid layouts
│   │   │   │   │   ├── FloraPlayerUI.kt   # Player sheet, queue, tag editor & word-level lyrics
│   │   │   │   │   ├── FloraLyricsSettingsUI.kt  # Lyrics customization screen
│   │   │   │   │   ├── FloraTabSettingsUI.kt     # Tab management screen
│   │   │   │   │   └── MusicViewModel.kt  # Central application state & business logic
│   │   │   │   ├── repository/            # MusicRepository (songs, playlists, scanning)
│   │   │   │   ├── service/               # ArtworkPreExtractorService
│   │   │   │   ├── ui/theme/              # Material 3 colors, typography, theme
│   │   │   │   ├── utils/                 # ArtworkCacheManager, MusicScanner, AudioArtworkFetcher
│   │   │   │   ├── FloraApplication.kt    # Base Application class & Notification Channels
│   │   │   │   └── MainActivity.kt        # Single activity host & media intent handler
│   │   │   ├── res/                       # Drawables, mipmaps, strings, themes
│   │   │   └── AndroidManifest.xml        # Permissions, services, and activity definitions
│   │   └── test/                          # Unit and Robolectric tests
│   ├── build.gradle.kts                   # App module build configuration
│   └── proguard-rules.pro                 # ProGuard / R8 rules
├── assets/                                # Static assets & application fonts
├── BetterLyrics/                          # BetterLyrics standalone module & TTML tests
├── gradle/
│   ├── wrapper/                           # Gradle Wrapper 9.3.1
│   └── libs.versions.toml                 # Version catalog for dependencies and plugins
├── build.gradle.kts                       # Root build configuration
├── gradle.properties                      # JVM arguments & memory tuning
├── settings.gradle.kts                    # Gradle settings & plugin management
├── .env.example                           # Template for secret keys
└── README.md                              # Project documentation
```

---

## 📲 Download & Installation

### Option 1: Direct Download (Ready-to-Install APK)
You do **not** need Android Studio installed on your computer. Download the latest pre-built APK directly to your phone:

👉 **[Download Flora-Music-debug.apk](https://github.com/TulipNex/Flora-Music/releases/download/latest-build/Flora-Music-debug.apk)**

1. Open the link on your Android smartphone.
2. Tap **Install** (allow *"Install unknown apps"* in browser settings if prompted).
3. Open Flora Music, grant storage permissions, and enjoy your offline music library!

---

### Option 2: Build from Source (For Developers)

#### Prerequisites
- **Android Studio**: Android Studio Ladybug (2024.2.1+) or newer.
- **Java Development Kit (JDK)**: JDK 17 or JDK 21.
- **Android SDK**: `minSdkVersion`: 24, `targetSdkVersion`: 36, `compileSdk`: 36.

#### Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/TulipNex/Flora-Music.git
   cd Flora-Music
   ```

2. Copy the environment configuration:
   ```bash
   cp .env.example .env
   ```

3. Build Debug APK:
   ```bash
   ./gradlew assembleDebug
   ```

4. Or run directly on an emulator or connected device via Android Studio (`Shift + F10`).

---

## 🔒 Permissions Used

Flora requests the following permissions to provide full offline player functionality:
- `READ_MEDIA_AUDIO` / `READ_EXTERNAL_STORAGE`: To index and play audio files from local storage.
- `WRITE_EXTERNAL_STORAGE` / `MANAGE_EXTERNAL_STORAGE`: Required by Jaudiotagger to write ID3 metadata changes and embed artwork directly into physical audio files.
- `FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_MEDIA_PLAYBACK`: For uninterrupted background playback.
- `POST_NOTIFICATIONS`: To show the media playback card in the system notification shade.
- `INTERNET` & `ACCESS_NETWORK_STATE`: For fetching synchronized lyrics from the BetterLyrics API.
- `WAKE_LOCK`: To prevent CPU sleep while playing audio.

---

## 🇮🇩 Ringkasan Bahasa Indonesia

**Flora Music** adalah aplikasi pemutar musik lokal Android berbasis **Jetpack Compose** dan **Material 3** dengan segudang fitur modern:

1. **Notifikasi Media Gaya Spotify**:
   - Terintegrasi penuh dengan bilah notifikasi & lockscreen modern Android 11 s.d. Android 15.
   - Dilengkapi *squiggly seekbar* interaktif (bisa geser durasi lagu langsung dari notifikasi).
   - Warna latar belakang notifikasi otomatis berubah dinamis mengikuti warna sampul album (*Dynamic Palette*).
   - Tombol kontrol lengkap: *Previous*, *Play / Pause*, *Next*, *Favorite (Like)*, dan *Stop*.
2. **Word-Level Synchronized Lyrics (Karaoke)**:
   - Menampilkan lirik lagu tersinkronisasi kata-demi-kata secara langsung via integrasi BetterLyrics API, parser TTML, dan fallback lirik ID3 offline.
3. **Editor Tag Metadata Fisik**:
   - Mengubah metadata lagu (Judul, Artis, Album, Genre, Lirik, hingga Album Art) langsung ke dalam file audio fisik di memori perangkat menggunakan pustaka `jaudiotagger`.
4. **Kustomisasi Tampilan Mendalam**:
   - Pemilihan font kustom (bisa memasukkan file `.ttf`/`.otf` sendiri), serta pengaturan ukuran, margin, dan posisi baris aktif.
5. **Sistem Tab Modular**:
   - Tata letak tab aplikasi dapat diatur sesuka hati (pindah posisi, jadikan subtab, buat tab kustom dengan ikon pilihan, atau sembunyikan).
6. **Performa Mulus & Caching Pintar**:
   - Didukung AndroidX Media3 ExoPlayer, pra-ekstraksi album art di latar belakang, serta fitur blacklist folder (menyembunyikan rekaman suara, audio WhatsApp, dsb.).

### 📥 Unduh Aplikasi
Anda dapat mengunduh file APK siap pasang tanpa perlu instal Android Studio di PC melalui link berikut:  
👉 **[Download Flora-Music-debug.apk](https://github.com/TulipNex/Flora-Music/releases/download/latest-build/Flora-Music-debug.apk)**

---

## 📄 License & Credits

- Built with ❤️ using modern Android standards.
- Synchronized lyrics powered by [BetterLyrics](https://lyrics-api.boidu.dev/).
- Audio tagging powered by [Jaudiotagger](https://www.jthink.net/jaudiotagger/).

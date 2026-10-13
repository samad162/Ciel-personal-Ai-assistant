# CIEL — System-Wide AI Companion & Multimodal Intelligence

<p align="center">
  <img src="app/src/main/res/drawable/ciel_avatar.png" width="128" height="128" alt="CIEL AI Companion" style="border-radius: 50%;" />
</p>

<p align="center">
  <strong>A futuristic, voice-first Android AI companion inspired by Siri and Gemini Live — built with anime-inspired warmth, gentle feminine voice identity, cinematic cyberpunk glassmorphism, and deep system-level integration.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android%208.0%2B-blue.svg?style=flat-square&logo=android" alt="Platform" />
  <img src="https://img.shields.io/badge/Language-Kotlin-7F52FF.svg?style=flat-square&logo=kotlin" alt="Language" />
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose%20%2B%20Material%203-4285F4.svg?style=flat-square" alt="UI" />
  <img src="https://img.shields.io/badge/Target%20RAM-4%20GB%20Optimized-success.svg?style=flat-square" alt="Target RAM" />
  <img src="https://img.shields.io/badge/Architecture-MVVM%20%2B%20Clean%20Architecture-teal.svg?style=flat-square" alt="Architecture" />
</p>

---

## 🌟 Overview

**CIEL** is more than a conversational chatbot wrapped in a dark theme. It is a **system-integrated personal intelligence companion** engineered to operate seamlessly across the entire Android operating system. Users can activate CIEL via voice, interact through an ultra-compact floating circle overlay, share their live screen, analyze camera frames, launch installed applications, synthesize artwork, and generate real native documents without interrupting their workflow.

CIEL couples high-performance on-device Android engineering with a distinctive emotional identity: a gentle, mature feminine voice inspired by Kaoruko Waguri (*The Fragments of a Blooming Love*), paired with a cool cybernetic anime aesthetic.

---

## ⚡ Core Features

### 1. 🔘 Non-Intrusive "Little Circle" Floating Assistant Surface
- **Ultra-Compact Footprint:** Replaces screen-covering rectangular windows with an elegant **54dp floating circular orb**. It stays out of your way and never obstructs background apps.
- **100x Starting Animation:** Elastic spring scale-in (`OvershootInterpolator`) with concentric holographic sonic shockwaves blossoming outward on activation.
- **Dynamic Island Mini-Pill:** When speaking or listening, an ultra-slim glass capsule (38dp height) smoothly slides out beside the circle showing live transcripts and spoken responses, automatically collapsing back into the circle after 3 seconds.
- **Edge-Snapping Physics:** Drag the circle anywhere on screen; upon release, it smoothly snaps to the nearest screen edge.
- **Double-Tap / Long-Press:** Double-tap launches the full-screen chat interface; long-press provides quick options.

### 2. 🎙️ Natural Voice Pipeline & Privacy Indicator Fix
- **Low-Power Acoustic VAD:** Replaced aggressive speech recognition restart loops with an energy-efficient raw `AudioRecord` (16 kHz mono PCM) Voice Activity Detector.
- **Eliminated Permanent Green Privacy Indicator:** Microphone resources are strictly scoped:
  $$\text{Standby} \longrightarrow \text{Speech Capture} \longrightarrow \text{AI Processing (Mic Released)} \longrightarrow \text{TTS Response (Mic Released)} \longrightarrow \text{Standby}$$
  The microphone is immediately freed during AI inference and voice playback, turning off Android's green privacy indicator.
- **Gentle Feminine Neural Voice:** Natural pitch, warm cadence, and expressive conversational phrasing powered by `CielVoiceEngine`.

### 3. 🚀 Universal App Launcher & Intent Resolver
- **Natural Phrasing Support:** Understands natural speech commands (*"ciel please open instagram"*, *"can you launch settings"*, *"open youtube"*).
- **Package Alias Mapping:** Automatically resolves common applications (Instagram, WhatsApp, YouTube, Chrome, Maps, Settings, Camera, Spotify, etc.).
- **Truthful Status Reporting:** Launches installed apps directly via launcher intents; if an app is not present, CIEL truthfully informs the user and offers the Play Store without crashing.

### 4. 📄 ChatGPT-Style Native File & Document Generation
- **Native PDF Creation:** Built with `android.graphics.pdf.PdfDocument`, generating formatted reports with cybernetic headers, metadata, and word-wrapped multi-page layouts.
- **Multiple Formats:** Instant generation of **CSV spreadsheets**, **Markdown (`.md`)**, **Plain Text (`.txt`)**, and HTML-formatted DOCX documents.
- **Android `FileProvider` Integration:** Interactive in-chat cards display file name, size, format badge, and direct **Open** / **Share** system intents.

### 5. 🎨 Multimodal Image Generation
- **Text-to-Image Synthesis:** Integrated image generation engine utilizing high-performance Flux models via Pollinations AI.
- **In-Chat Preview Cards:** Generated artwork is saved directly to local storage (`Pictures/CIEL/`) and presented in chat with preview cards, full-screen viewing, and system share sheets.

### 6. 👁️ Live Multimodal Intelligence (Screen & Camera)
- **Live Screen Sharing (Gemini Live-Style):** Uses Android's `MediaProjection` API with explicit user consent. Downscales frames to 720p/540p with throttled sampling to run smoothly on 4 GB RAM devices.
- **Live Camera Vision:** Integrated `Camera2` pipeline for on-demand visual analysis (*"What is this object?"*, *"Explain this diagram"*), with instant hardware teardown when idle.

---

## 📐 Architecture & Technology Stack

| Layer | Technologies & Implementations |
| :--- | :--- |
| **UI Framework** | Jetpack Compose, Material 3, Custom Canvas Glassmorphism, XML Surface Overlays |
| **Architecture** | MVVM, Clean Architecture, Kotlin Coroutines, StateFlow / SharedFlow |
| **Floating Surface** | `WindowManager.addView` with `TYPE_APPLICATION_OVERLAY` & `FLAG_NOT_FOCUSABLE` |
| **Voice & Speech** | Custom `AudioRecord` VAD, Android `SpeechRecognizer`, Google Neural TTS (`en-us-x-sfg-network`) |
| **Vision & Projection** | `MediaProjectionManager`, `VirtualDisplay`, `ImageReader`, `Camera2` API |
| **File Systems** | Android `FileProvider` (`androidx.core.content.FileProvider`), Scoped Storage |
| **Local Database** | Room Database (TypeConverters, Coroutines Flow, Message & Activity DAOs) |
| **Networking** | OkHttp 4, Streaming HTTP SSE, Resilient Fallback Providers |

---

## 📁 Repository Structure

```
ciel_android_project/
├── app/
│   ├── src/main/
│   │   ├── java/ai/ciel/assistant/
│   │   │   ├── MainActivity.kt               # Main entrypoint & Compose host
│   │   │   ├── data/                         # Local Room DB, repositories, remote providers
│   │   │   ├── service/
│   │   │   │   ├── FloatingAssistantService.kt  # Little Circle overlay & animations
│   │   │   │   ├── WakeWordDetectionService.kt # Low-power acoustic VAD engine
│   │   │   │   ├── ScreenCaptureService.kt      # MediaProjection screen vision
│   │   │   │   └── CielQuickSettingsTileService.kt # Android Quick Settings Tile
│   │   │   ├── tools/
│   │   │   │   ├── AssistantToolDispatcher.kt   # Unified intent classification & dispatch
│   │   │   │   ├── DocumentGenerationEngine.kt  # Native PDF, CSV, Markdown generator
│   │   │   │   ├── ImageGenerationEngine.kt     # Image synthesis & Base64 encoder
│   │   │   │   └── CameraVisionManager.kt       # Camera2 on-demand frame capture
│   │   │   ├── ui/
│   │   │   │   ├── dashboard/                # Futuristic companion dashboard
│   │   │   │   ├── chat/                     # Multimodal chat & file attachment cards
│   │   │   │   ├── components/               # Custom CommandBar, AudioWaveform, SlideBar
│   │   │   │   └── theme/                    # Cyberpunk glassmorphism color palette
│   │   │   └── voice/
│   │   │       └── CielVoiceEngine.kt        # Feminine neural voice synthesizer
│   │   └── res/
│   │       ├── layout/
│   │       │   └── layout_floating_ciel_orb.xml # Little Circle floating layout
│   │       ├── xml/
│   │       │   └── ciel_file_paths.xml          # FileProvider storage paths
│   │       └── drawable/                     # Avatars, holographic glow rings, icons
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 🛠️ Getting Started

### Prerequisites
- **Android Studio:** Hedgehog (2023.1.1) or newer
- **JDK:** Java 17 or Java 21
- **Android SDK:** Minimum SDK 26 (Android 8.0), Target SDK 35 (Android 15)

### Build & Run
1. Clone the repository:
   ```bash
   git clone https://github.com/samad162/Ciel-personal-Ai-assistant.git
   cd Ciel-personal-Ai-assistant
   ```
2. Open the project in Android Studio.
3. Sync Gradle and build the project:
   ```bash
   ./gradlew assembleDebug
   ```
4. Install and run on an Android device or emulator:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

### Required Permissions
CIEL requests documented, standard Android permissions only when required:
- `android.permission.RECORD_AUDIO`: Voice command input & acoustic VAD.
- `android.permission.SYSTEM_ALERT_WINDOW`: Displaying the non-intrusive Little Circle floating surface.
- `android.permission.CAMERA`: Live camera vision queries.
- `android.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION`: Live screen sharing with explicit system consent dialog.

---

## 🔒 Security & Privacy Compliance

- **No Always-On Recording:** Microphone capture is paused immediately upon end-of-speech.
- **Scoped Storage & FileProvider:** Generated files are stored within scoped app storage and shared securely via content URIs.
- **Zero Hardcoded Secrets:** Remote provider integrations do not expose private credentials within the client application.
- **Transparent Indicators:** Android's hardware privacy indicators (microphone/camera) are strictly respected.

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

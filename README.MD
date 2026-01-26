# Clean Architecture Template for Compose Multiplatform

A production-ready **Kotlin Multiplatform (KMP)** template application targeting **Android** and **iOS**, built with **Compose Multiplatform** for a 100% shared UI.

This project implements a scalable **Clean Architecture** with **MVI (Model-View-Intent)** pattern, utilizing modern libraries like Koin, Ktor, Room, and Navigation 3.

## 🚀 Features

-   **100% Shared UI**: Write once, run on Android and iOS using Compose Multiplatform.
-   **Clean Architecture**: Separation of concerns into Core, Domain, Data, and UI layers.
-   **MVI Pattern**: Predictable state management using unidirectional data flow.
-   **Type-Safe Navigation**: implementation of `androidx.navigation3` for completely type-safe routing.
-   **Offline-First**: Local database support with Room and DataStore.
-   **Modern Tech Stack**: Uses the latest stable versions of Kotlin, Compose, and Android Jetpack.

## 🛠️ Tech Stack

The project leverages a modern, bleeding-edge ecosystem:

### Core & Architecture
-   **[Kotlin 2.3.0](https://kotlinlang.org/)**: The official language.
-   **[Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)**: Code sharing logic.
-   **[Coroutines](https://github.com/Kotlin/kotlinx.coroutines)**: Asynchronous programming.
-   **[Koin 4.1.1](https://insert-koin.io/)**: Dependency Injection framework (with `koin-compose-viewmodel`).
-   **[Kermit](https://github.com/touchlab/Kermit)**: Multiplatform logging.

### UI & UX
-   **[Compose Multiplatform 1.10.0](https://www.jetbrains.com/lp/compose-multiplatform/)**: Declarative UI framework.
-   **[Material 3](https://m3.material.io/)**: Latest Material Design components.
-   **[Navigation 3](https://developer.android.com/jetpack/compose/navigation)**: Type-safe, multiplatform navigation.
-   **[Coil 3](https://coil-kt.github.io/coil/)**: Image loading engine for CMP (supports Ktor network integration).
-   **[Material Kolor](https://github.com/jordond/MaterialKolor)**: Dynamic Material 3 color palette generation.

### Data & Networking
-   **[Ktor 3.3.3](https://ktor.io/)**: Multiplatform asynchronous HTTP client.
    -   Engines: `OkHttp` (Android), `Darwin` (iOS).
-   **[Room 2.8.4](https://developer.android.com/jetpack/androidx/releases/room)**: Local SQLite database with KSP support.
-   **[DataStore](https://developer.android.com/topic/libraries/architecture/datastore)**: Key-value storage for preferences.
-   **[Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization)**: JSON serialization.
-   **[Kotlinx DateTime](https://github.com/Kotlin/kotlinx-datetime)**: Date and time manipulation.
-   **[FileKit](https://github.com/vinceglb/FileKit)**: Cross-platform file picker and IO.

### Build Tools
-   **Android Gradle Plugin 9.0.0**
-   **Version Catalog**: Dependency management via `libs.versions.toml`.

## 📂 Project Structure

```text
├── androidApp     # Android specific entry point (thin wrapper)
├── iosApp         # iOS specific entry point (Xcode project)
├── sharedUI       # (Presentation) UI features, ViewModels, Navigation, Theme
│   ├── src/commonMain/kotlin/com/example/template/
│   │   ├── navigation/  # Nav3 Routes and Hosts
│   │   ├── theme/       # Design System
│   │   └── ui/screens/  # Feature Screens (MVI)
├── core           # (Domain & Data) Business logic, Services, Repositories
│   ├── src/commonMain/kotlin/com/example/template/core/
│   │   ├── data/        # Repository Implementations, Room, Ktor
│   │   ├── domain/      # UseCases, Repository Interfaces, Models
│   │   └── service/     # storage, logging, etc.
```

## ⚡ Getting Started

### Prerequisites
-   **JDK 17+** (Required for AGP 8+)
-   **Android Studio** (Koala or newer recommended)
-   **Xcode** (For iOS development, macOS only)
-   **KMP Plugin** (Optional, for better IDE support)

### Setup
1.  **Clone** the repository.
    ```bash
    git clone https://github.com/your-username/your-repo.git
    cd your-repo
    ```
2.  **Open** in Android Studio.
3.  **Sync** Gradle project to download dependencies.
4.  **Run** on Android Emulator or iOS Simulator.

### Rename Package (Optional)
To use this as a base for your own app, rename the package `com.example.template`:
1.  Replace `com.example.template` directory structure in `commonMain` source sets.
2.  Update `namespace` in `build.gradle.kts` files.
3.  Search and Replace `com.example.template` globally in the project.

## 🏗️ MVI Architecture

We follow a strict **Unidirectional Data Flow (UDF)**:

1.  **UI State**: Immutable data class in `FeatureUiState.kt`.
2.  **Events**: User actions defined in `FeatureEvent.kt` sealed interface.
3.  **ViewModel**: Handles events in `onEvent()`, updates state via `StateFlow`.
4.  **Screen**: Stateless composable receiving `state` and `onEvent` lambda.

```kotlin
// ViewModel
fun onEvent(event: HomeEvent) {
    when(event) {
        is HomeEvent.Refresh -> refreshData()
        is HomeEvent.OnItemClick -> navigateTo(event.item)
    }
}
```

## 🔧 Build Commands

```bash
# Build Android Debug APK
./gradlew :androidApp:assembleDebug

# Run Unit Tests
./gradlew test

# Lint Check
./gradlew lint
```

## 📜 License

Distributed under the **GNU GPLv3** License. See `LICENSE` for more information.

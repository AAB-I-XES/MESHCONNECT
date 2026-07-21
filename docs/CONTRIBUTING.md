# Contributing to MeshLink 🤝

Thank you for your interest in contributing to **MeshLink**! We welcome bug reports, feature requests, pull requests, and documentation improvements.

---

## 💻 Development Workflow

### 1. Prerequisites
* **Android Studio**: Installed with Android SDK 34/35.
* **JDK**: OpenJDK 17.
* **Git**: Installed locally.

### 2. Fork and Clone
```bash
git clone https://github.com/your-username/MeshLink.git
cd MeshLink
```

### 3. Code Style & Standards
* **Language**: Use Kotlin exclusively for application code and build scripts (`.gradle.kts`).
* **UI**: Write UI using Jetpack Compose and Material Design 3 guidelines.
* **State Management**: Use `ViewModel` and `MutableStateFlow` with `collectAsStateWithLifecycle()`.
* **Resource Strings**: Avoid hardcoding raw string literals in UI composables; place user-facing text in `res/values/strings.xml`.
* **Formatting**: Follow standard Kotlin coding conventions (2-space or 4-space indentation as set in project formatting rules).

---

## 🧪 Building & Verification

Before submitting a Pull Request, verify that your changes compile without errors:

```bash
# Verify compilation via Gradle
./gradlew compileDebugSources

# Run unit tests
./gradlew test
```

---

## 📩 Submitting Pull Requests

1. Create a feature branch: `git checkout -b feature/your-feature-name`.
2. Commit your changes with clear, descriptive commit messages.
3. Push to your fork and submit a Pull Request describing the functional changes and visual enhancements made.

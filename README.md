# MeshLink 🌐🔒

**MeshLink** is an advanced, offline-first decentralized mesh communication platform for Android. Designed for post-infrastructure scenarios, emergency response, remote outdoor exploration, and privacy-focused communication, MeshLink enables peer-to-peer (P2P) messaging, voice calls, encrypted media sharing, and multi-hop store-and-forward relaying without relying on cell towers or internet connectivity.

---

## 🌟 Key Features

* **Decentralized P2P Mesh Communication**: Dynamic multi-hop message relaying over local wireless radios (Wi-Fi Direct, Bluetooth Low Energy / Classic simulation & radio hooks).
* **End-to-End Encryption (E2EE)**: Secure identity generation using asymmetric cryptography, contact verification via QR codes, and payload protection.
* **Store-and-Forward Relaying**: Messages destined for offline nodes are securely buffered and relayed across intermediate peers until reaching their final destination.
* **Push-to-Talk & Real-Time Voice Calls**: Low-latency voice streaming over local mesh networks.
* **Mesh Network Visualizer & Telemetry**: Interactive visual representation of active mesh nodes, dynamic routing tables, signal strength (RSSI), and hop counts.
* **Developer Dashboard**: Live radio diagnostic controls, network simulation parameters, metric monitoring, and packet traffic inspection.
* **Adaptive Material Design 3 UI**: Full edge-to-edge Compose interface with responsive container constraints (`widthIn`) supporting mobile devices, foldables, and tablets.

---

## 🏗️ System Architecture

MeshLink follows **Clean Architecture** principles paired with the **MVVM (Model-View-ViewModel)** UI pattern and unidirectional data flow in Jetpack Compose:

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│     (Jetpack Compose UI, Material 3, Navigation Compose)    │
└──────────────────────────────┬──────────────────────────────┘
                               │ Collects StateFlow / Triggers Events
┌──────────────────────────────▼──────────────────────────────┐
│                      ViewModel Layer                        │
│          (MeshLinkViewModel, State Management)              │
└──────────────────────────────┬──────────────────────────────┘
                               │ Coordinates Domain Logic
┌──────────────────────────────▼──────────────────────────────┐
│                    Core Mesh & Data Layer                   │
│   ┌─────────────────────────────────────────────────────┐   │
│   │   Mesh Engine & Routing Core (Peer Discovery, Hop   │   │
│   │   Calculation, Store-and-Forward Relay)             │   │
│   └──────────────────────────┬──────────────────────────┘   │
│                              │ Persists State               │
│   ┌──────────────────────────▼──────────────────────────┐   │
│   │   Room Database (Messages, Peer Identities, Routes) │   │
│   └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

Detailed technical breakdowns are available in the [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) document.

---

## 📂 Project Structure

```text
MeshLink/
├── app/
│   └── src/main/java/com/example/
│       ├── data/                     # Room Entities, DAOs, App Database
│       ├── mesh/                     # Mesh Network Engine, Node Routing, Signal Simulation
│       ├── ui/
│       │   ├── components/           # Reusable Compose UI elements (GlassCard, Custom Buttons)
│       │   ├── screens/              # Top-level screen Composables
│       │   │   ├── ChatListScreen.kt
│       │   │   ├── ChatDetailScreen.kt
│       │   │   ├── ContactsScreen.kt
│       │   │   ├── MeshVisualizerScreen.kt
│       │   │   ├── VoiceCallScreen.kt
│       │   │   ├── DeveloperDashboardScreen.kt
│       │   │   └── SettingsScreen.kt
│       │   └── theme/                # Material 3 Color Schemes, Typography, Shapes
│       └── MainActivity.kt           # App Entry Point & Navigation Graph
├── docs/                             # Technical Documentation
│   ├── ARCHITECTURE.md               # System Architecture & Routing Protocol Deep Dive
│   └── CONTRIBUTING.md               # Guide for Contributors
├── license/                          # Project Licensing Information
│   └── LICENSE.md                    # MIT License Specification
├── LICENSE                           # Root MIT License Copy
├── metadata.json                     # AI Studio Platform Metadata
└── README.md                         # Main Project README
```

---

## 🚀 Getting Started

### Prerequisites

* **Android Studio**: Jellyfish | Ladybug or newer recommended.
* **JDK**: Version 17 or higher.
* **Android SDK**:
  * Minimum SDK: `24` (Android 7.0)
  * Target / Compile SDK: `34` / `35`

### Building the Project

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-username/MeshLink.git
   cd MeshLink
   ```

2. **Open in Android Studio**:
   Open the root directory in Android Studio and let Gradle sync all dependencies.

3. **Build & Run**:
   * Select a connected Android device or emulator.
   * Run the `app` configuration or execute Gradle via command line:
     ```bash
     ./gradlew assembleDebug
     ```

---

## 📖 How MeshLink Works

1. **Peer Discovery & Handshake**:
   Nodes broadcast localized heartbeat beacons containing their public identity key and signal metrics. Nearby nodes update their peer tables in real time.
2. **Routing Table Calculation**:
   MeshLink computes dynamic shortest paths using distance-vector mesh routing logic. If a target contact is multiple hops away, intermediate nodes automatically route messages through the optimal neighbor node.
3. **Store & Forward Delivery**:
   If a destination node is out of range, messages are marked as `PENDING_RELAY` and stored in the local Room database. Once a routing path opens, the payload is forwarded automatically.
4. **Adaptive UI Keyboard Handling**:
   All chat and entry views utilize Compose window insets (`imePadding()`, `navigationBarsPadding()`) and scroll-to-bottom effects to ensure typing inputs remain visible above the soft keyboard.

---

## 🤝 Contributing

We welcome contributions from the developer community! Please review [`docs/CONTRIBUTING.md`](docs/CONTRIBUTING.md) for details on code style guidelines, pull request submission, and testing procedures.

---

## 📜 License

MeshLink is licensed under the **MIT License**. See [`LICENSE`](LICENSE) or [`license/LICENSE.md`](license/LICENSE.md) for full license details.

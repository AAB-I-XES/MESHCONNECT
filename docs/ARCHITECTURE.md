# MeshLink Architecture & Technical Specifications 📐

This document provides a detailed breakdown of the technical design, data layer, routing protocols, and UI implementation in **MeshLink**.

---

## 1. High-Level Architecture

MeshLink strictly enforces a modern Android architecture based on **Clean Architecture** and **MVVM (Model-View-ViewModel)** with Jetpack Compose.

```
┌──────────────────────────────────────────────────────────────┐
│                   UI Layer (Jetpack Compose)                 │
│  ChatListScreen | ChatDetailScreen | MeshVisualizerScreen... │
└──────────────────────────────┬───────────────────────────────┘
                               │ StateFlow observation
┌──────────────────────────────▼───────────────────────────────┐
│                      MeshLinkViewModel                        │
│   (Maintains UI States, handles user input, dispatches UI)   │
└──────────────────────────────┬───────────────────────────────┘
                               │ Coroutines / Flow
┌──────────────────────────────▼───────────────────────────────┐
│                    Domain & Mesh Layer                       │
│    (MeshNetworkEngine, Peer Discovery, Packet Routing)       │
└──────────────────────────────┬───────────────────────────────┘
                               │ Room Database Access
┌──────────────────────────────▼───────────────────────────────┐
│                     Local Data Layer                         │
│  (AppDatabase: PeerEntity, MessageEntity, RouteEntity)       │
└──────────────────────────────────────────────────────────────┘
```

---

## 2. Local Database Schema (Room)

MeshLink uses Android Room for lightweight, persistent data storage:

### `PeerEntity`
* `peerId: String` (Primary Key) - Unique public key digest / peer identification string.
* `name: String` - User display name.
* `rssi: Int` - Received Signal Strength Indicator (dBm).
* `hopCount: Int` - Direct distance (1 for direct neighbor, 2+ for relayed nodes).
* `isOnline: Boolean` - Presence flag based on active heartbeat beacons.
* `publicKey: String` - Asymmetric public key for encryption verification.

### `MessageEntity`
* `id: Long` (Primary Key, AutoGenerate)
* `senderId: String` - Originator peer ID.
* `receiverId: String` - Intended destination peer ID.
* `content: String` - Text payload or encrypted blob string.
* `timestamp: Long` - Epoch timestamp (ms).
* `isDelivered: Boolean` - Delivery acknowledgement state.
* `isRelayed: Boolean` - Store-and-Forward relay marker.
* `disappearingTimeSec: Int` - Ephemeral timer setting (0 for permanent).

### `RouteEntity`
* `destinationId: String` (Primary Key)
* `nextHopId: String` - Immediate neighbor node through which traffic is forwarded.
* `totalCost: Int` - Link quality metric derived from RSSI and hop count.

---

## 3. Mesh Routing Protocol Specification

MeshLink implements an ad-hoc distance-vector routing protocol optimized for mobile P2P networks:

1. **Heartbeat Broadcasts**:
   Each active node periodically emits a lightweight broadcast frame containing its Node ID and local link table.
2. **Path Discovery & Cost Metrics**:
   Link cost $C$ is computed as:
   $$C = \text{HopCount} \times 10 + \max(0, -70 - \text{RSSI})$$
   Routes with lower cost are prioritized in the `RouteEntity` routing table.
3. **Store-and-Forward Relaying**:
   When a node receives a frame addressed to a non-neighbor peer, it queries its routing table for the optimal `nextHopId`. If no route currently exists, the payload is buffered locally until a route is discovered or expires.

---

## 4. UI Responsiveness & Soft Keyboard Handling

To prevent input fields from being hidden under soft keyboards or cut off on various display sizes:

* **In-App Edge-to-Edge**: `enableEdgeToEdge()` is enabled in `MainActivity.kt`.
* **Zero TopBar / BottomBar Inset Invasiveness**: Top-level `Scaffold` composables specify `contentWindowInsets = WindowInsets(0, 0, 0, 0)`.
* **Dynamic Modifiers**: The inner container applies `.navigationBarsPadding()` and `.imePadding()` so typing text fields float smoothly above the soft keyboard when focused.
* **Large Screen Adaptability**: Wide viewports (tablets/foldables) limit content width using `.widthIn(max = 600.dp)` with top-center alignment to maintain high readability.

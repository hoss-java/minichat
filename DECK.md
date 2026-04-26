---
Title: minichat
Description: plans and project management sheets
Date: 
Robots: noindex,nofollow
Template: index
---

# minichat

## Plan and Time Estimation for P2P Chat System

Time Estimation is based on  **Subscription-Platform** performance, 

here's the key insight:
**Actual pace:** ~1.5 days per card / ~44 work-days for 32 calendar days on a full-stack project with security focus.

**Spend heavily on:** Authentication, testing, security, and refinement (72% of time on auth alone in subscription platform).

**Move fast on:** CRUD features, UI work, and integrations once patterns are established.

---

## Revised Phase-Based Plan with Realistic Timings

| Phase | Backend | Frontend | Duration (work-days) | Calendar Days | Total Status |
|-------|---------|----------|----------------------|----------------|--------------|
| **Phase 1: Auth Foundation** | User model, JWT, login | React setup, auth UI | 8-10 | 10-14 | ⏳ Start here |
| **Phase 2: WebSocket Signaling** | WebSocket server, room mgmt, peer discovery | WebSocket client, room UI | 6-8 | 8-10 | Follow Phase 1 |
| **Phase 3: P2P & Data Channels** | SDP/ICE relaying, STUN/TURN config | WebRTC setup, data channels, localStorage chat | 8-10 | 10-14 | Follow Phase 2 |
| **Phase 4: E2E Encryption** | Key exchange endpoints, key storage | Crypto lib integration, key UI | 10-12 | 12-16 | Follow Phase 3 |
| **Phase 5: Message History & Server Storage** | Message persistence, encrypted storage | Toggle local/server, sync UI | 6-8 | 8-12 | Follow Phase 4 |
| **Phase 6: Testing & Optimization** | Integration tests, edge cases | Unit tests, E2E tests | 8-10 | 10-14 | Follow Phase 5 |
| **Phase 7: Voice/Video** | Signaling optimization | WebRTC audio/video, UI controls | 8-10 | 10-14 | Follow Phase 6 |
| **Phase 8: Client Apps (Electron)** | API versioning, minor adjustments | React + Electron packaging | 6-8 | 8-12 | Follow Phase 7 |
| | | | **~60-86 work-days** | **~86-116 calendar days** | **3-4 months realistic** |

---

## Phase-by-Phase Detailed Breakdown

### Phase 1: Auth Foundation (8-10 work-days | 10-14 calendar days)

**Why longer than you might think:** Your subscription platform spent 9 days on JWT alone. Expect similar here because security is foundational.

#### Backend Tasks
- [ ] **Spring Boot project setup** (Maven, dependencies, structure) — *0.5 days*
  - Spring Web, WebSocket, Security, JPA, MySQL
- [ ] **Database schema** (User, Role, Session entities) — *1 day*
  - Users table with hashed passwords, roles, created_at, last_login
- [ ] **User registration endpoint** (`POST /auth/register`) — *1.5 days*
  - Input validation, password hashing (BCrypt), duplicate user check, return JWT
- [ ] **Login endpoint** (`POST /auth/login`) — *2 days*
  - Email/password validation, token generation, refresh token logic
- [ ] **JWT filter & validation** — *2.5 days*
  - Token validation on all protected endpoints, error handling, token expiration
- [ ] **User profile endpoint** (`GET /auth/me`, `PUT /auth/profile`) — *1 day*
  - Retrieve current user, update profile info
- [ ] **Integration tests** (auth endpoints) — *2 days*
  - Test register, login, JWT validation, refresh tokens

**Backend Subtotal: 10-11 days (expect 10, budget for 12)**

#### Frontend Tasks
- [ ] **React project setup** (Create React App, folder structure, dependencies) — *0.5 days*
  - Install react-router, axios, Context API setup
- [ ] **Auth context/state management** — *1.5 days*
  - Store JWT, user data, login/logout actions
- [ ] **Login page UI** — *1.5 days*
  - Form, validation, error messages, redirect on success
- [ ] **Registration page UI** — *1.5 days*
  - Form, password confirmation, validation, success message
- [ ] **Protected route wrapper** — *1 day*
  - Redirect to login if not authenticated
- [ ] **Basic dashboard/home page** — *0.5 days*
  - Just show "Welcome, {username}" and logout button
- [ ] **HTTP service layer** (API calls) — *1 day*
  - Axios instance with JWT header injection, error handling
- [ ] **Unit tests** (auth service, context) — *2 days*
  - Test login flow, JWT storage, logout

**Frontend Subtotal: 9-10 days (expect 10, budget for 11)**

**Phase 1 Total: 8-10 work-days (~10-12 with buffer) = 10-14 calendar days**

**Deliverable:** User can register, login, see personalized dashboard. JWT works.

---

### Phase 2: WebSocket Signaling (6-8 work-days | 8-10 calendar days)

Now you're on familiar ground (like Phases 2-3 of subscription platform—should be fast).

#### Backend Tasks
- [ ] **Spring WebSocket configuration** — *1 day*
  - WebSocketConfig, message broker setup (SimpleBroker)
- [ ] **User session tracking** (online/offline) — *1.5 days*
  - Track connected users in-memory or Redis, broadcast online status
- [ ] **Room entity & service** — *1 day*
  - Create room, join room, leave room, list rooms
- [ ] **Peer discovery endpoint** — *1 day*
  - `/api/rooms/{roomId}/peers` — return list of online peers
- [ ] **Signaling message handler** — *1.5 days*
  - Relay SDP offers, answers, ICE candidates between peers
  - Message format: `{ type: 'offer|answer|ice', from: userId, to: userId, data: {...} }`
- [ ] **Integration tests** (WebSocket flow) — *1.5 days*
  - Test user connect, room join, message relay

**Backend Subtotal: 6-7 days**

#### Frontend Tasks
- [ ] **WebSocket client setup** — *1 day*
  - SockJS + STOMP library (or raw WebSocket), connection management
- [ ] **Room list page** — *1.5 days*
  - Display available rooms, show online peer count
- [ ] **Create/join room UI** — *1 day*
  - Form to create or join room, room selection
- [ ] **Online users display** — *1 day*
  - Real-time list of peers in current room
- [ ] **Signaling message handler** — *1 day*
  - Listen for SDP/ICE messages, trigger WebRTC connection
- [ ] **Connection state UI** — *0.5 days*
  - Show "connecting", "connected", "error" status
- [ ] **Unit tests** (WebSocket service) — *1.5 days*
  - Mock WebSocket, test message handling

**Frontend Subtotal: 6.5-7 days**

**Phase 2 Total: 6-8 work-days = 8-10 calendar days**

**Deliverable:** Users can create rooms, see online peers, real-time updates work.

---

### Phase 3: P2P & Data Channels (8-10 work-days | 10-14 calendar days)

**Note:** This is the hardest part. WebRTC debugging is tedious (tricky NAT/firewall issues). Budget generously.

#### Backend Tasks
- [ ] **STUN/TURN server configuration** — *1.5 days*
  - Research & setup: Use **Google's public STUN** (free) + configure **Coturn** (self-hosted) or Firebase ICE servers
  - Add to WebSocket signaling response: `{ iceServers: [...] }`
- [ ] **SDP offer/answer relay** — *1 day*
  - Already partially done in Phase 2, refine here
- [ ] **ICE candidate relay** — *1 day*
  - Handle `{ type: 'ice', candidate: {...} }` messages, forward to peer
- [ ] **Connection monitoring** (optional) — *0.5 days*
  - Track P2P connection state, log failures
- [ ] **Integration tests** (P2P signaling) — *1.5 days*
  - Mock WebRTC, test offer/answer/ICE flow

**Backend Subtotal: 5-6 days**

#### Frontend Tasks
- [ ] **WebRTC peer connection setup** — *2 days*
  - Initialize RTCPeerConnection, handle state changes
  - **Consider using `simple-peer` library** (1 day faster than raw WebRTC)
- [ ] **Data channel creation & handling** — *1.5 days*
  - Create data channel, handle `onmessage`, `onopen`, `onerror`
- [ ] **Message sending/receiving** — *1 day*
  - Send messages via data channel, display in chat UI
- [ ] **Chat UI** (message list, input, send button) — *1.5 days*
  - Display messages, timestamp, sender name
- [ ] **localStorage persistence** — *1 day*
  - Save messages to localStorage with key `chat_${peerId}_${roomId}`
  - Load on reconnect
- [ ] **Connection error handling** — *1 day*
  - Display user-friendly errors (NAT issues, connection failed, etc.)
- [ ] **Unit & integration tests** (WebRTC, data channels) — *2 days*
  - Test peer connection flow, message sending, localStorage

**Frontend Subtotal: 9-10 days**

**Phase 3 Total: 8-10 work-days = 10-14 calendar days**

**Deliverable:** Two users can P2P chat, messages stored locally.

**Common Issues to Budget For:**
- STUN/TURN not working behind corporate firewalls (1-2 days debugging)
- Browser WebRTC inconsistencies (add 0.5 days)
- localStorage quota issues (0.5 days)

---

### Phase 4: E2E Encryption (10-12 work-days | 12-16 calendar days)

**Why long:** Crypto is complex. You need careful implementation, thorough testing, and key management edge cases.

#### Backend Tasks
- [ ] **Key entity & schema** — *1 day*
  - Store user public key, key fingerprint, created_at, key_type (auto/manual)
- [ ] **Key generation service** — *1.5 days*
  - Generate Ed25519 keypair using **Bouncy Castle** or **NaCl4j**
  - Return public key only to user
- [ ] **POST /keys/generate endpoint** — *1 day*
  - Create keypair on first login, return public key
- [ ] **GET /keys/{userId} endpoint** — *0.5 days*
  - Public key lookup
- [ ] **Key exchange endpoint** — *1 day*
  - Store peer's public key when user requests it (trust on first use)
- [ ] **Encrypted key storage** — *1.5 days*
  - Encrypt private keys at rest (optional: use HSM or key vault)
- [ ] **Key rotation/revocation** (optional, important) — *1 day*
  - Allow users to rotate keys, invalidate old ones
- [ ] **Integration tests** (key exchange flow) — *2 days*
  - Test keypair generation, storage, retrieval, trust-on-first-use

**Backend Subtotal: 10-11 days**

#### Frontend Tasks
- [ ] **Crypto library integration** — *1 day*
  - Choose **libsodium.js** (recommended) or **TweetNaCl.js**
  - Test bundle size impact
- [ ] **Key generation on first login** — *1.5 days*
  - Generate keypair, prompt user to save or confirm auto-generation
- [ ] **Public key display & fingerprint** — *1 day*
  - Show fingerprint (first 16 chars of public key hash) for manual verification
- [ ] **Fetch peer's public key** — *0.5 days*
  - Call backend to get peer's key before starting chat
- [ ] **Encrypt outgoing messages** — *1.5 days*
  - Before sending via data channel, encrypt with peer's public key
- [ ] **Decrypt incoming messages** — *1.5 days*
  - After receiving, decrypt with your private key
- [ ] **Key import/export UI** — *1 day*
  - Allow users to manually import a key (paste) or export their public key
- [ ] **Trust-on-first-use UI** — *0.5 days*
  - Show warning: "Trusting peer's key for the first time"
- [ ] **Key verification UI** (optional) — *0.5 days*
  - Side-by-side fingerprint comparison for verification
- [ ] **Unit tests** (crypto operations) — *2 days*
  - Test encryption/decryption, key generation, import/export
- [ ] **E2E tests** (encrypted chat flow) — *1 day*
  - Two browser instances, verify end-to-end encryption works

**Frontend Subtotal: 10-11 days**

**Phase 4 Total: 10-12 work-days = 12-16 calendar days**

**Deliverable:** Messages are encrypted E2E, key exchange works, users can see key fingerprints.

**Common Issues:**
- Crypto library bundle size bloat (plan 0.5 days)
- Key format confusion (PEM vs. raw bytes) (0.5 days)
- Decryption failures on old clients (1 day)

---

### Phase 5: Message History & Server Storage (6-8 work-days | 8-12 calendar days)

Back to familiar CRUD-like work—should be quick.

#### Backend Tasks
- [ ] **Message entity & schema** — *0.5 days*
  - `id, roomId, senderId, receiverId, encryptedContent, createdAt, isRead`
- [ ] **Message service & repository** — *1 day*
  - Save message, query by room/user, pagination
- [ ] **POST /messages endpoint** — *1 day*
  - Accept encrypted message, store in DB
- [ ] **GET /messages?roomId={id}&limit=50 endpoint** — *1 day*
  - Return paginated history
- [ ] **Message retention policy** (optional) — *1 day*
  - Auto-delete messages older than X days (configurable)
- [ ] **Read receipt tracking** (optional) — *0.5 days*
  - Mark messages as read
- [ ] **Integration tests** — *1.5 days*

**Backend Subtotal: 7-8 days**

#### Frontend Tasks
- [ ] **Storage toggle UI** — *0.5 days*
  - Checkbox: "Save chat history on server" (default: local only)
- [ ] **Fetch server history on room join** — *1 day*
  - If toggle is on, call GET /messages, merge with local
- [ ] **Display server messages** — *0.5 days*
  - Show different styling or indicator for server vs. local messages
- [ ] **Clear old localStorage** — *0.5 days*
  - When switching to server-only, optionally clear local cache
- [ ] **Sync status indicator** — *0.5 days*
  - Show "synced to server" or "local only" badge on messages
- [ ] **Error handling** (storage failures) — *1 day*
  - If server save fails, fallback to local gracefully
- [ ] **Unit tests** (storage toggle, sync logic) — *1.5 days*

**Frontend Subtotal: 6-7 days**

**Phase 5 Total: 6-8 work-days = 8-12 calendar days**

**Deliverable:** Users can optionally sync chat history to server, toggle works.

---

# Phase 6: Testing & Optimization (8-10 work-days | 10-14 calendar days) - COMPLETE

**Based on your subscription platform:** You spent 6 days on JS unit tests alone. Expect similar rigor here.

#### Backend Tasks
- [ ] **Complete integration test suite** — *3 days*
  - Auth flow, WebSocket signaling, P2P relay, encryption, message storage
  - Aim for 80%+ code coverage
- [ ] **Performance testing** — *1.5 days*
  - Load test WebSocket server (100+ concurrent users)
  - Test message throughput, latency
- [ ] **Security audit** — *1.5 days*
  - Review JWT handling, key storage, SQL injection, CSRF
- [ ] **Error handling edge cases** — *1 day*
  - Network failures, malformed messages, key expiration
- [ ] **Refactoring & cleanup** — *0.5 days*

**Backend Subtotal: 7.5 days**

#### Frontend Tasks
- [ ] **Unit tests** (all services, components) — *3 days*
  - WebSocket service, crypto service, chat component, auth context
  - Message service, storage service, encryption logic
  - Aim for 80%+ code coverage
  - Use Jest + React Testing Library
- [ ] **E2E tests** (Cypress or Playwright) — *2 days*
  - Full user flow: register → login → create room → chat → send message
  - Test both local & server storage modes
  - Test P2P connection establishment
  - Test encryption/decryption end-to-end
- [ ] **Cross-browser testing** — *0.5 days*
  - Test on Chrome, Firefox, Safari (WebRTC behavior differs)
  - Verify encryption works across browsers
- [ ] **Performance profiling** — *0.5 days*
  - Lighthouse audit, bundle size check, memory leaks
  - Test with 100+ messages in chat history
- [ ] **Mobile responsiveness testing** — *0.5 days*
  - Test on mobile viewport, touch interactions
- [ ] **Accessibility testing** — *0.5 days*
  - Screen reader compatibility, keyboard navigation
- [ ] **Error scenario testing** — *1 day*
  - Network disconnection, encryption key missing, storage quota exceeded
  - WebSocket reconnection, data channel failures

**Frontend Subtotal: 8-9 days**

**Phase 6 Total: 8-10 work-days = 10-14 calendar days**

**Deliverable:** 80%+ test coverage, stable on all browsers, documented test cases.

**Common Issues:**
- WebRTC testing complexity (mock with fake RTCPeerConnection) (1 day)
- Flaky async tests in E2E (0.5 days)
- Cross-browser crypto differences (0.5 days)

---

### Phase 7: Voice/Video (8-10 work-days | 10-14 calendar days)

**Note:** Audio/video adds complexity but reuses WebRTC infrastructure from Phase 3. Should be faster than P2P setup.

#### Backend Tasks
- [ ] **Signaling optimization for media** — *1 day*
  - Add media type to SDP offer/answer (`audio: true, video: true`)
  - Handle renegotiation when toggling media on/off
- [ ] **Media state tracking** (optional) — *1 day*
  - Track which peers have audio/video enabled in-room
  - Broadcast media state via WebSocket
- [ ] **TURN server optimization for media** — *1.5 days*
  - Ensure TURN servers have bandwidth for video
  - Test with multiple simultaneous video streams
- [ ] **Monitoring media quality** (optional) — *1 day*
  - Log WebRTC stats (bandwidth, packet loss, latency)
  - Alert on poor connection quality
- [ ] **Integration tests** (media signaling) — *1.5 days*
  - Test offer/answer with audio/video tracks
  - Test renegotiation, track removal

**Backend Subtotal: 6-7 days**

#### Frontend Tasks
- [ ] **WebRTC media stream setup** — *2 days*
  - `navigator.mediaDevices.getUserMedia()` for audio/video
  - Handle permission requests and denials
  - Stop tracks cleanly on disconnect
- [ ] **Audio/video track management** — *1.5 days*
  - Add audio/video tracks to peer connection via `addTrack()`
  - Handle remote track `ontrack` event
  - Display remote video/audio streams
- [ ] **Local video preview** — *1 day*
  - Show user's own camera feed in small preview window
  - Mute/unmute audio control
- [ ] **Remote video display** — *1.5 days*
  - Render remote peer's video stream in main chat area
  - Handle multiple video streams (grid layout for 3+ peers)
- [ ] **Media controls UI** — *1 day*
  - Toggle buttons: Mute audio, Turn off camera
  - Disable/enable button state based on device availability
  - Show indicator when audio/video is off
- [ ] **Error handling** (camera/mic issues) — *1 day*
  - Handle permission denied, device not found, in-use errors
  - Graceful fallback to audio-only if video fails
  - User-friendly error messages
- [ ] **Performance optimization** — *1 day*
  - Bandwidth throttling detection
  - Adaptive bitrate (lower quality if bandwidth is low)
  - CPU optimization for video encoding
- [ ] **Unit & E2E tests** (media flow) — *1.5 days*
  - Mock `getUserMedia()`
  - Test track addition/removal
  - Test video UI rendering

**Frontend Subtotal: 10-11 days**

**Phase 7 Total: 8-10 work-days = 10-14 calendar days**

**Deliverable:** Users can send/receive audio and video in P2P chat, controls work.

**Common Issues:**
- Camera/microphone permission prompts (browser-specific) (0.5 days)
- Video codec compatibility across browsers (1 day)
- Audio echo cancellation setup (0.5 days)
- Bandwidth adaptation complexity (1 day)

---

### Phase 8: Client Apps (Electron/React Native) (6-8 work-days | 8-12 calendar days)

**Note:** Since you're sharing React code, this is mostly packaging + platform-specific adjustments.

#### Backend Tasks
- [ ] **API versioning strategy** — *0.5 days*
  - Plan for backward compatibility as clients evolve
  - Add API version header or path versioning
- [ ] **Client app endpoints** (optional) — *0.5 days*
  - App version check endpoint
  - Client-specific config endpoint (STUN/TURN servers, features)
- [ ] **Minor adjustments** — *0.5 days*
  - CORS configuration for Electron
  - User-Agent detection for app clients
- [ ] **Testing with Electron/React Native clients** — *1 day*
  - Verify auth flow works in Electron
  - Test WebSocket in native environment

**Backend Subtotal: 2.5-3 days**

#### Frontend Tasks

##### Electron (Desktop App)
- [ ] **Electron project setup** — *1 day*
  - `electron-react-boilerplate` or manual setup
  - Main process + renderer process separation
  - Bundler config (Webpack/Vite)
- [ ] **Share React code** — *0.5 days*
  - Extract common components into shared folder
  - Both web and Electron import from `/src/shared`
- [ ] **Platform-specific adjustments** — *1.5 days*
  - File system access (if needed for logs, caches)
  - Native window controls (minimize, maximize, close)
  - App menu (File, Edit, Help)
  - System tray icon (optional)
- [ ] **Auto-update mechanism** — *1 day*
  - Check for new version, download, install, restart
  - Use `electron-updater` library
- [ ] **Packaging & distribution** — *1 day*
  - Create installers (.exe for Windows, .dmg for macOS, .AppImage for Linux)
  - Code signing (optional, for distribution)
- [ ] **Testing (Electron-specific)** — *1 day*
  - Spectron for E2E testing in Electron
  - Test native features, auto-update flow

**Electron Subtotal: 5.5-6 days**

##### React Native (Mobile App) - *Optional, add if needed*
- [ ] **React Native project setup** — *1.5 days*
  - Expo or bare React Native project
  - Native iOS/Android setup
- [ ] **Share React code** — *0.5 days*
  - Extract common logic into shared services
  - Platform-specific UI components (iOS vs Android styling)
- [ ] **Platform-specific adjustments** — *2 days*
  - Push notifications for incoming messages
  - Background task handling (keep WebSocket alive)
  - Camera/microphone permissions (iOS-specific flows)
  - Deep linking (open app from notification)
- [ ] **Navigation adaptation** — *1 day*
  - React Navigation for mobile bottom tabs
  - Mobile-optimized layout for small screens
- [ ] **Testing & packaging** — *1.5 days*
  - Build APK (Android) and IPA (iOS)
  - TestFlight / Google Play Store submission prep

**React Native Subtotal: 6.5-7 days** *(if included)*

**Phase 8 Total:**
- **Desktop only (Electron):** 6-7 work-days = 8-10 calendar days
- **Desktop + Mobile:** 12-14 work-days = 16-20 calendar days

**Deliverable:** Users can download and run Electron desktop app, mobile app available on stores.

---

## COMPLETE PROJECT TIMELINE SUMMARY

| Phase | Work-Days | Calendar Days | Cumulative |
|-------|-----------|---------------|-----------|
| **1: Auth Foundation** | 8-10 | 10-14 | 10-14 |
| **2: WebSocket Signaling** | 6-8 | 8-10 | 18-24 |
| **3: P2P & Data Channels** | 8-10 | 10-14 | 28-38 |
| **4: E2E Encryption** | 10-12 | 12-16 | 40-54 |
| **5: Message History & Server Storage** | 6-8 | 8-12 | 48-66 |
| **6: Testing & Optimization** | 8-10 | 10-14 | 58-80 |
| **7: Voice/Video** | 8-10 | 10-14 | 68-94 |
| **8: Electron Desktop App** | 6-7 | 8-10 | 76-104 |
| **TOTAL (no mobile)** | **60-85** | **86-114** | **3-4 months** |
| **TOTAL (with React Native)** | **72-100** | **102-134** | **4-5 months** |

---

## Resource Allocation Recommendation

### Ideal Team Structure
- **1 Backend Engineer** (Java/Spring Boot) — handles all backend phases
- **1 Frontend Engineer** (React) — handles web + Electron/React Native
- **1 QA Engineer** (Part-time from Phase 3+) — testing & edge cases
- **1 DevOps/Infrastructure** (Part-time) — STUN/TURN setup, deployment, monitoring

### If Solo Developer (You)
**Realistic pace:** 1.5 days per major task (not 0.5-1 day estimates above)
- **Adjusted timeline:** ~100-130 work-days = **5-6 months** (not 3-4)
- **Strategy:** Focus on Phases 1-6 first (core chat), defer Phase 7-8 (media/apps) to v2

---

## Critical Path (Must-Do Before Next Phase)

```
Phase 1 (Auth)
    ↓
Phase 2 (WebSocket)
    ↓
Phase 3 (P2P) ← Don't skip, critical for decentralization
    ↓
Phase 4 (Encryption) ← Security foundation
    ↓
Phase 5 (Server Storage) ← Optional, but recommended for UX
    ↓
Phase 6 (Testing) ← Non-negotiable before production
    ↓
Phase 7 (Media) ← Nice-to-have, can ship without
    ↓
Phase 8 (Apps) ← Final polish, can use web app first
```

---

## Risk Mitigation

| Risk | Mitigation | Buffer |
|------|-----------|--------|
| WebRTC NAT/firewall issues | Setup TURN server early (Phase 3) | +2 days |
| Crypto implementation bugs | Use well-tested libraries (libsodium.js), heavy testing | +3 days |
| Async state management complexity | Use established patterns (Context API or Redux) | +1 day |
| Browser inconsistencies | Test on Chrome/Firefox/Safari from Phase 2 | +1 day |
| WebSocket reliability | Implement reconnection logic with exponential backoff | +1 day |

**Total recommended buffer: +8 days across 8 phases ≈ 1 day per phase**

---
```
## Budget Summary (if outsourcing)

Based on **$80/hr average developer rate:**

| Scenario | Work-Days | Hours | Cost |
|----------|-----------|-------|------|
| Core Chat (Phases 1-5) | 38-48 | 304-384 | **$24.3K - $30.7K** |
| Full Product (Phases 1-7) | 68-94 | 544-752 | **$43.5K - $60.2K** |
| + Mobile App (Phase 8) | 80-104 | 640-832 | **$51.2K - $66.6K** |

**Note:** Prices assume experienced developer; junior devs would be slower but cheaper.
```

```mermaid
gantt
    section %BOARD%
```

# 1 - minichat

## 001-0001
> **Setup repository.** ![status](https://img.shields.io/badge/status-DONE-brightgreen)
> <details >
>     <summary>Details</summary>
> The goal of this card is to setup/initialize minichat repository.
> 
> # DOD (definition of done):
> - Needed beranches are created.
> - Deck and pm are setup.
> - Needed files are created.
> 
> # TODO:
> - [x] 1. Create branches, main and develop
> - [x] 2. Setup deck
> - [x] 3. Create this card!
> - [x] 4. Create needed files such gitignore
> - [x] 5. Add an empty README
> 
> # Reports:
> * Steps to create repo
> > 1.  Create a new repo on Github (the main branch)
> > 2. Colon it
> >>```
> >>git clone git@github.com:hoss-java/<repo>.git
> >>```
> > 3. Add `gitignore`
> > 4. Create a new develop branch
> >>```
> >>git checkout -b develop
> >>```
> > 5. Install `git-deck`
> > 6. Intialize deck
> >>```
> >>git config alias.deck '!bash .git/hooks/git-deck/deck'
> >>
> >>git deck pm init
> >>git deck pm edit
> >>git deck pm initdeck
> >>git deck board mk minichat
> >>```
> > 7. Add the first card, and move it to ONGOING
> >>```
> >>git deck card mk "Setup repo."
> >>git deck card mv 0001 ONGOING
> >>```
> > 8. The first commit
> >>```
> >>git add -A
> >>git commit -m "[B001-C0001] Setup repository."
> >># git commit -m "[B001-C0001] Setup repository." -m "--merge"
> >>#git commit --amend
> >>```
> * Needed files
> >```
> >.commit-check
> >.container-run
> >container-run
> >.gitdefault
> >.gitignore
> >.prompt
> >```
> * Add an empty README.md
> </details>

## 001-0002
> **Configuer github workflows.** ![status](https://img.shields.io/badge/status-DONE-brightgreen)
> <details >
>     <summary>Details</summary>
> The goal of this card is to configure github workflows for this repository.
> 
> # DOD (definition of done):
> 
> # TODO:
> - [] 1.
> 
> # Reports:
> *
> </details>

## 001-0003
> **Create a pre- high-level architecture plan.** ![status](https://img.shields.io/badge/status-DONE-brightgreen)
> <details >
>     <summary>Details</summary>
> 
> # DOD (definition of done):
> 
> # TODO:
> - [x] 1. High-Level Architecture Plan
> - [x] 2. Plan and Time Estimation
> 
> # Reports:
> ## High-Level Architecture Plan
> 
> Building a **signaling server** (Spring Boot) + **chat clients** (React). 
> 
> What to think about:
> 
> ### Backend (Spring Boot)
> - **WebSocket setup** for real-time communication between clients and server
> - **User registration/login** and session management
> - **Room/peer discovery** — how clients find each other
> - **Signaling messages** — exchange SDP offers/answers and ICE candidates (for P2P)
> - **Message routing** — relay signals between peers until they connect directly
> 
> ### Frontend (React)
> - **WebRTC implementation** — establish peer connections after signaling
> - **UI for chat** — message input, display, user list
> - **Connection state management** — track who's online, connection status
> - **Error handling** — fallback if P2P fails
> 
> ### Key Decisions
> - **Pure P2P or hybrid?** Do messages route through server after P2P connects, or only signaling?
> - **Authentication** — JWT tokens, basic auth?
> - **Persistence** — store chat history?
> - **STUN/TURN servers** — needed for NAT traversal (Firebase, Twilio, or self-hosted?)
> 
> ### Tech Stack
> - Spring Boot + WebSocket (SockJS optional)
> - React + WebRTC API
> - Consider libraries: `simple-peer`, `peerjs`, or native WebRTC
> 
> ## High-Level Architecture Plan
> 
> Building a **signaling server** (Spring Boot) + **chat clients** (React). 
> 
> What to think about:
> 
> ### Backend (Spring Boot)
> - **WebSocket setup** for real-time communication between clients and server
> - **User registration/login** and session management
> - **Room/peer discovery** — how clients find each other
> - **Signaling messages** — exchange SDP offers/answers and ICE candidates (for P2P)
> - **Message routing** — relay signals between peers until they connect directly
> 
> ### Frontend (React)
> - **WebRTC implementation** — establish peer connections after signaling
> - **UI for chat** — message input, display, user list
> - **Connection state management** — track who's online, connection status
> - **Error handling** — fallback if P2P fails
> 
> ### Key Decisions
> - **Pure P2P or hybrid?** Do messages route through server after P2P connects, or only signaling?
> - **Authentication** — JWT tokens, basic auth?
> - **Persistence** — store chat history?
> - **STUN/TURN servers** — needed for NAT traversal (Firebase, Twilio, or self-hosted?)
> 
> ### Tech Stack
> - Spring Boot + WebSocket (SockJS optional)
> - React + WebRTC API
> - Consider libraries: `simple-peer`, `peerjs`, or native WebRTC
> 
> ```mermaid
> graph TB
>     subgraph Clients["🖥️ CLIENTS (All Platforms)"]
>         Web["Browser<br/>(React Web)"]
>         Desktop["Desktop<br/>(Electron)"]
>         Mobile["Mobile<br/>(React Native)"]
>     end
> 
>     subgraph Transport["🔐 TRANSPORT LAYER"]
>         HTTPS["HTTPS<br/>+ JWT"]
>         WS["WebSocket<br/>(Signaling)"]
>         P2P["P2P WebRTC<br/>(Data Channels)"]
>     end
> 
>     subgraph Backend["⚙️ SPRING BOOT SERVER"]
>         Auth["🔑 Auth Service<br/>Login/Register<br/>JWT Tokens"]
>         Signal["📡 Signaling Server<br/>Room Management<br/>Peer Discovery<br/>SDP/ICE Relay"]
>         Crypto["🔐 Crypto Service<br/>Key Exchange<br/>Key Storage"]
>         Persist["💾 Message Service<br/>History Storage<br/>Encryption at Rest"]
>     end
> 
>     subgraph Database["🗄️ DATABASE"]
>         Users["Users Table<br/>(email, pwd, keys)"]
>         Messages["Messages Table<br/>(encrypted content)"]
>         Rooms["Rooms Table<br/>(room metadata)"]
>         Keys["Keys Table<br/>(public keys)"]
>     end
> 
>     subgraph External["🌐 EXTERNAL SERVICES"]
>         STUN["STUN Servers<br/>(Google Public)"]
>         TURN["TURN Servers<br/>(Coturn - optional)"]
>     end
> 
>     subgraph ClientStorage["💾 CLIENT STORAGE"]
>         LocalStore["localStorage<br/>(JWT, keys,<br/>messages)"]
>         IndexedDB["IndexedDB<br/>(large message<br/>history)"]
>     end
> 
>     Web --> HTTPS
>     Desktop --> HTTPS
>     Mobile --> HTTPS
>     
>     HTTPS --> Auth
>     HTTPS --> Crypto
>     
>     Web --> WS
>     Desktop --> WS
>     Mobile --> WS
>     
>     WS --> Signal
>     
>     Signal --> STUN
>     Signal --> TURN
>     
>     Web --> P2P
>     Desktop --> P2P
>     Mobile --> P2P
>     
>     P2P --> STUN
>     P2P --> TURN
>     
>     Auth --> Users
>     Signal --> Rooms
>     Signal --> Users
>     Crypto --> Keys
>     Persist --> Messages
>     Persist --> Users
>     
>     Web --> LocalStore
>     Desktop --> LocalStore
>     Mobile --> LocalStore
>     
>     Web --> IndexedDB
>     Desktop --> IndexedDB
>     Mobile --> IndexedDB
> ```
> 
> ```mermaid
> graph TB
>     subgraph Clients["🖥️ CLIENTS (All Platforms)"]
>         Web["Browser<br/>(React Web)"]
>         Desktop["Desktop App<br/>(Electron)"]
>         Mobile["Mobile App<br/>(React Native)"]
>     end
> 
>     subgraph Transport["🔐 TRANSPORT LAYER"]
>         HTTPS["HTTPS<br/>+ JWT Authentication"]
>         WS["WebSocket<br/>(Real-time Signaling)"]
>         P2P["P2P WebRTC<br/>(Direct Data Channels)"]
>     end
> 
>     subgraph Backend["⚙️ SPRING BOOT SERVER"]
>         Auth["🔑 Authentication Service<br/>• Login/Register<br/>• JWT Token Management<br/>• User Sessions"]
>         Signal["📡 Signaling Server<br/>• Room Management<br/>• Peer Discovery<br/>• SDP Offer/Answer Relay<br/>• ICE Candidate Forwarding<br/>• Connection Status Broadcast"]
>         Crypto["🔐 Encryption Service<br/>• Key Generation<br/>• Key Exchange Endpoints<br/>• Public Key Lookup<br/>• Key Storage & Rotation"]
>         Persist["💾 Message Service<br/>• Save Encrypted Messages<br/>• Retrieve Message History<br/>• Message Pagination<br/>• Encryption at Rest"]
>     end
> 
>     subgraph Database["🗄️ DATABASE"]
>         Users["Users Table<br/>id, email, password_hash<br/>public_key, fingerprint"]
>         Messages["Messages Table<br/>id, room_id, sender_id<br/>encrypted_content<br/>created_at, is_read"]
>         Rooms["Rooms Table<br/>id, name, created_by<br/>created_at"]
>         Keys["Keys Table<br/>id, user_id, public_key<br/>key_type, fingerprint"]
>     end
> 
>     subgraph External["🌐 EXTERNAL SERVICES"]
>         STUN["STUN Servers<br/>(Google Public STUN<br/>NAT Traversal)"]
>         TURN["TURN Servers<br/>(Coturn - Relay<br/>if Behind Firewall)"]
>     end
> 
>     subgraph ClientStorage["💾 CLIENT-SIDE STORAGE"]
>         LocalStore["Browser Storage<br/>• JWT Token<br/>• User Keys (Private/Public)<br/>• Chat Messages<br/>• Room Data"]
>         IndexedDB["Large Message Cache<br/>• Message History<br/>• Offline Support"]
>     end
> 
>     Web --> HTTPS
>     Desktop --> HTTPS
>     Mobile --> HTTPS
>     
>     HTTPS --> Auth
>     HTTPS --> Crypto
>     HTTPS --> Persist
>     
>     Web --> WS
>     Desktop --> WS
>     Mobile --> WS
>     
>     WS --> Signal
>     
>     Signal --> STUN
>     Signal --> TURN
>     
>     Web --> P2P
>     Desktop --> P2P
>     Mobile --> P2P
>     
>     P2P --> STUN
>     P2P --> TURN
>     
>     Auth --> Users
>     Auth --> Keys
>     
>     Signal --> Rooms
>     Signal --> Users
>     
>     Crypto --> Keys
>     Crypto --> Users
>     
>     Persist --> Messages
>     Persist --> Users
>     
>     Web --> LocalStore
>     Desktop --> LocalStore
>     Mobile --> LocalStore
>     
>     Web --> IndexedDB
>     Desktop --> IndexedDB
>     Mobile --> IndexedDB
> 
>     style Clients fill:#4a90e2,stroke:#2c5aa0,color:#fff
>     style Transport fill:#9b59b6,stroke:#6c3483,color:#fff
>     style Backend fill:#27ae60,stroke:#1e8449,color:#fff
>     style Database fill:#e74c3c,stroke:#c0392b,color:#fff
>     style External fill:#f39c12,stroke:#d68910,color:#fff
>     style ClientStorage fill:#16a085,stroke:#0e6251,color:#fff
> ```
> 
> ## Revised Phase-Based Plan with Realistic Timings
> 
> | Phase | Backend | Frontend | Duration (work-days) | Calendar Days | Total Status |
> |-------|---------|----------|----------------------|----------------|--------------|
> | **Phase 1: Auth Foundation** | User model, JWT, login | React setup, auth UI | 8-10 | 10-14 | ⏳ Start here |
> | **Phase 2: WebSocket Signaling** | WebSocket server, room mgmt, peer discovery | WebSocket client, room UI | 6-8 | 8-10 | Follow Phase 1 |
> | **Phase 3: P2P & Data Channels** | SDP/ICE relaying, STUN/TURN config | WebRTC setup, data channels, localStorage chat | 8-10 | 10-14 | Follow Phase 2 |
> | **Phase 4: E2E Encryption** | Key exchange endpoints, key storage | Crypto lib integration, key UI | 10-12 | 12-16 | Follow Phase 3 |
> | **Phase 5: Message History & Server Storage** | Message persistence, encrypted storage | Toggle local/server, sync UI | 6-8 | 8-12 | Follow Phase 4 |
> | **Phase 6: Testing & Optimization** | Integration tests, edge cases | Unit tests, E2E tests | 8-10 | 10-14 | Follow Phase 5 |
> | **Phase 7: Voice/Video** | Signaling optimization | WebRTC audio/video, UI controls | 8-10 | 10-14 | Follow Phase 6 |
> | **Phase 8: Client Apps (Electron)** | API versioning, minor adjustments | React + Electron packaging | 6-8 | 8-12 | Follow Phase 7 |
> | | | | **~60-86 work-days** | **~86-116 calendar days** | **3-4 months realistic** |
> </details>

## 001-0004
> **Spring Boot Project Setup** ![status](https://img.shields.io/badge/status-DONE-brightgreen)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to initialize the Spring Boot project with all necessary dependencies and folder structure for the minichat P2P system.
> 
> # DOD (definition of done):
> - Spring Boot project created with Maven
> - All required dependencies added (Spring Web, WebSocket, Security, JPA, MySQL)
> - Project structure organized (controllers, services, models, config folders)
> - Application runs without errors
> - Database connection configured (MySQL)
> 
> # TODO:
> - [x] 1. Create Spring Boot Maven project
> - [x] 2. Add Spring Web, Security, JPA, WebSocket dependencies
> - [x] 3. Configure application.properties for database
> - [x] 4. Create base folder structure
> - [x] 5. Test project startup
> 
> # Reports:
> * To run/test server
> >```
> >mvn spring-boot:run -Dspring-boot.run.arguments="--spring>.profiles.active=dev"
> >
> >mvn test -Dspring.profiles.active=test
> >mvn test -Dspring.profiles.active=test >-Dtest=MiniChatAppTest
> >```
> * File map
> ```
> src/main/java/com/minichat/
> ├── MiniChatApplication.java
> ├── config/
> │   ├── SecurityConfig.java
> │   ├── WebSocketConfig.java
> │   └── JpaConfig.java
> ├── controller/
> │   └── AuthController.java
> ├── service/
> │   ├── AuthService.java
> │   └── JwtService.java
> ├── entity/
> │   ├── User.java
> │   ├── Role.java
> │   ├── RoleType.java
> │   └── Session.java
> ├── repository/
> │   ├── UserRepository.java
> │   └── SessionRepository.java
> ├── dto/
> │   ├── RegisterRequest.java
> │   ├── LoginRequest.java
> │   ├── LoginResponse.java
> │   ├── UserProfileDto.java
> │   └── JwtTokenDto.java
> ├── security/
> │   ├── JwtTokenProvider.java
> │   ├── JwtAuthenticationFilter.java
> │   └── CustomUserDetailsService.java
> ├── exception/
> │   ├── GlobalExceptionHandler.java
> │   ├── UnauthorizedException.java
> │   └── UserAlreadyExistsException.java
> └── util/
>     └── PasswordEncoder.java
> 
> 
> src/main/resources/
> ├── application.properties (already done)
> ├── application-dev.properties
> └── application-test.properties
> 
> src/test/java/com/minichat/
> └── MiniChatApplicationTest.java (placeholder)
> 
> pom.xml (already updated with dependencies)
> 
> ```
> </details>

## 001-0005
> **Database Schema Design** ![status](https://img.shields.io/badge/status-DONE-brightgreen)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to design and create the database schema for user authentication including User, Role, and Session entities.
> 
> # DOD (definition of done):
> - User table created with all required fields (id, email, password, created_at, last_login)
> - Role table created for user roles
> - Session table created for tracking user sessions
> - All tables have proper indexes
> - Schema documented and reviewed
> 
> # TODO:
> - [x] 1. Design User entity (email, password hash, created_at, last_login, is_active)
> - [x] 2. Design Role entity (id, name, description)
> - [x] 3. Design Session entity (id, user_id, token, expires_at)
> - [-] 4. Create migration scripts
> - [x] 5. Set up relationships between tables
> - [x] 6. Create database indexes for performance
> 
> # Reports:
> ## Database Schema Design - Phase 1 Auth Foundation
> 
> ### Completed Tasks
> - ✅ User entity designed with email, username, passwordHash, createdAt, lastLogin, isActive, publicKey, fingerprint
> - ✅ Role entity designed with RoleType enum (USER, ADMIN, MODERATOR) and descriptions
> - ✅ Session entity designed with accessToken, refreshToken, expiresAt, refreshTokenExpiresAt, ipAddress, userAgent
> - ✅ Many-to-many relationship established between User and Role via user_roles junction table
> - ✅ Many-to-one relationship established between Session and User
> - ✅ Database indexes created for performance on: users(username, email, is_active), sessions(user_id, accessToken, refreshToken, expiresAt)
> - ✅ RoleInitializer component auto-seeds default roles on startup
> - ✅ AdminInitializer component creates admin users from configuration
> - ✅ UserInitializer component creates test users in dev profile
> - ✅ AuditingEntityListener configured with @CreatedDate and @LastModifiedDate timestamps
> - ✅ SessionRepository and RoleRepository interfaces created with necessary query methods
> 
> ### Key Design Decisions
> - Username used for login, email reserved for password recovery
> - Both username and email are unique constraints
> - AccessToken and RefreshToken stored separately for token rotation
> - Lazy loading on Session-User relationship for performance
> - Enum-based roles for type safety and consistency
> 
> ### Status
> **COMPLETE** - Ready for Phase 1: User Registration & Login Endpoints
> </details>

## 001-0006
> **User Registration Endpoint** ![status](https://img.shields.io/badge/status-DONE-brightgreen)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to implement the user registration endpoint with input validation, password hashing, and JWT token generation.
> 
> # DOD (definition of done):
> - POST /auth/register endpoint works
> - Input validation implemented (email format, password strength)
> - Password hashing with BCrypt implemented
> - Duplicate user check implemented
> - JWT token returned on successful registration
> - Error responses for invalid input
> 
> # TODO:
> - [x] 1. Create User entity and repository
> - [x] 2. Create registration DTO with validation
> - [x] 3. Implement password hashing with BCrypt
> - [x] 4. Create registration service logic
> - [x] 5. Build registration controller endpoint
> - [x] 6. Add duplicate user validation
> - [x] 7. Generate and return JWT on success
> - [-] 8. Test endpoint with RestTester
> 
> # Reports:
> ## Task Completion Report: User Registration Endpoint
> 
> ### ✅ Completed
> 
> 1. **User Entity & Repository**
>    - User entity with passwordHash, publicKey, fingerprint fields
>    - UserRepository with `existsByEmail()` and `existsByUsername()` methods
> 
> 2. **Registration DTO with Validation**
>    - RegisterRequest DTO with @NotBlank, @Email, @Size annotations
>    - Custom PasswordValidator component with dev/prod mode support
>    - Password strength: min 8 chars, uppercase, lowercase, digit, special char (@$!%*?&)
>    - `isPasswordMatch()` method for password confirmation validation
> 
> 3. **Password Hashing with BCrypt**
>    - BCryptPasswordEncoder(12) configured in SecurityConfig
>    - Password encoded before saving to `passwordHash` field
> 
> 4. **Duplicate User Validation**
>    - `existsByEmail()` check — throws UserAlreadyExistsException
>    - `existsByUsername()` check — throws UserAlreadyExistsException
> 
> 5. **JWT Token Generation**
>    - JwtTokenProvider with `generateAccessToken()` and `generateRefreshToken()`
>    - Tokens returned in LoginResponse on successful registration
>    - Access token: 15 min expiration | Refresh token: 7 days expiration
> 
> 6. **Registration Controller Endpoint**
>    - POST /api/auth/register endpoint
>    - Returns 201 CREATED with LoginResponse (accessToken, refreshToken, email, username)
> 
> 7. **JWT Authentication Filter**
>    - JwtAuthenticationFilter with public endpoints whitelist
>    - Skips JWT validation for: /api/auth/login, /api/auth/register, /api/auth/refresh, /api/auth/forgot-password
>    - Extracts Bearer token from Authorization header
> 
> 8. **Security Configuration**
>    - SecurityFilterChain with CORS enabled
>    - Public endpoints permitted without authentication
>    - Protected endpoints require valid JWT token
>    - Custom authentication entry point (401 response)
> 
> 9. **Testing with Postman**
>    - Valid registration: POST /api/auth/register → 201 + tokens
>    - Duplicate email/username validation tested
>    - Weak password validation tested
>    - Password mismatch validation tested
> 
> ### 📋 Definition of Done Met
> - ✅ POST /auth/register endpoint works
> - ✅ Input validation implemented
> - ✅ Password hashing with BCrypt implemented
> - ✅ Duplicate user check implemented
> - ✅ JWT token returned on successful registration
> - ✅ Error responses for invalid input
> 
> ### 🔧 Tech Stack Used
> - Spring Boot, Spring Security, JWT (JJWT), BCrypt
> - CustomUserDetailsService, JwtTokenProvider
> - CORS configuration for multi-platform support
> </details>

## 001-0007
> **Login Endpoint with JWT** ![status](https://img.shields.io/badge/status-DONE-brightgreen)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to implement the login endpoint with JWT token generation, refresh token logic, and proper error handling.
> 
> # DOD (definition of done):
> - POST /auth/login endpoint works
> - Email and password validation implemented
> - JWT access token generated on successful login
> - Refresh token logic implemented
> - Token expiration configured
> - Error responses for invalid credentials
> - Last login timestamp updated
> 
> # TODO:
> - [x] 1. Create JWT utility class (generate, validate, refresh tokens)
> - [x] 2. Create login DTO with username and password
> - [x] 3. Implement login service with credential validation
> - [x] 4. Build login controller endpoint
> - [x] 5. Add refresh token generation and storage
> - [x] 6. Configure token expiration times
> - [x] 7. Update last_login field in User table
> - [x] 8. Add error handling for wrong credentials
> - [x] 9. Test with multiple login scenarios
> 
> # Reports:
> *
> </details>

## 001-0008
> **JWT Filter and Token Validation** ![status](https://img.shields.io/badge/status-DONE-brightgreen)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to implement JWT filter to validate tokens on all protected endpoints and handle token expiration securely.
> 
> # DOD (definition of done):
> - JWT filter implemented in Spring Security chain
> - Token validation on every protected endpoint
> - Token expiration handled properly
> - Invalid token returns 401 Unauthorized
> - Expired token returns 401 with clear message
> - Token injection into request context
> 
> # TODO:
> - [x] 1. Create JWT filter class extending OncePerRequestFilter
> - [x] 2. Implement token extraction from Authorization header
> - [x] 3. Add token validation logic
> - [x] 4. Check token expiration time
> - [x] 5. Load user from token claims
> - [x] 6. Set authentication in SecurityContext
> - [x] 7. Configure filter in SecurityConfiguration
> - [x] 8. Add error handling for invalid tokens
> - [x] 9. Test with expired and invalid tokens
> - [x] 10. Test with valid tokens
> 
> # Reports:
> # JWT Filter & Validation Task — Completion Report
> 
> ## What Was Done
> 
> **Task:** Implement JWT filter to validate tokens on all protected endpoints and handle token expiration securely.
> 
> **Completion Status:** ✅ **100% Done**
> 
> Most of the JWT filter implementation was **already in place** from previous work. In this iteration, we **updated and enhanced error handling**:
> 
> - [x] JWT filter class extending `OncePerRequestFilter` (pre-existing)
> - Token extraction from Authorization header (pre-existing)
> - Token validation logic (pre-existing)
> - Token expiration time check (pre-existing)
> - Load user from token claims (pre-existing)
> - Set authentication in SecurityContext (pre-existing)
> - Configure filter in SecurityConfiguration (pre-existing)
> - **Add error handling for invalid tokens** ✨ **NEW** — Enhanced to return clear 401 responses with descriptive error messages for expired/invalid tokens
> - Test with expired and invalid tokens
> - Test with valid tokens
> 
> 
> ## Tool Updates: REST API Testing Tool
> 
> We upgraded the testing tool from a basic HTML/JS proxy to a **professional API testing suite** with the following new features:
> 
> ### New Capabilities
> 
> | Feature | Purpose |
> |---------|---------|
> | **Token Management Section** | Save/load JWT tokens from localStorage, toggle visibility, clear tokens — no need to paste token every request |
> | **Endpoint Presets** | Quick buttons for common endpoints (Login, Register, Get Profile, Refresh Token) |
> | **Custom Headers** | Add multiple custom headers dynamically without editing code |
> | **Request/Response Tabs** | Organized view: see request details, response, and request history side-by-side |
> | **Request Info Display** | Shows timestamp, full URL (method + endpoint), and all headers sent |
> | **Response Analysis** | Status badge (color-coded: success/warning/error), response time in milliseconds, response headers |
> | **History Tracking** | Last 10 requests saved; click any to reload and re-test |
> | **Copy Response** | One-click copy response body to clipboard |
> | **Notifications** | Success/error messages for token saves, history clears, etc. |
> 
> 
> ## Quick Guide: How to Use the New REST Tool
> 
> ### Example: Testing JWT Token Expiration & Validation
> 
> #### **Step 1: Set Up Your Token**
> 1. In the **"Authorization Token"** field, paste your JWT token
> 2. Click **💾 (Save)** to store it in localStorage — it'll persist across sessions
> 3. Click **👁️ (Eye)** to toggle visibility if needed
> 
> #### **Step 2: Test Valid Token (Test #10)**
> 1. Click the **"Get Profile"** preset button → auto-fills `GET /users/me`
> 2. Click **"Send Request"**
> 3. **Expected Result:** 
>    - **Response Tab** shows `200` (green badge)
>    - Response body displays your user profile
>    - Response time displays (e.g., `45.32ms`)
> 
> ```
> ✅ Status: 200 (success)
> Response: { "id": 1, "email": "user@example.com", "name": "John" }
> ```
> 
> #### **Step 3: Test Expired Token (Test #9)**
> 1. Go to **History Tab** → click your previous request to reload it
> 2. **Manually edit the token** in the Authorization field:
>    - Remove or truncate the token (e.g., remove last 20 characters)
>    - Or paste an old expired token
> 3. Click **"Send Request"** again
> 4. **Expected Result:**
>    - **Response Tab** shows `401` (red badge)
>    - Error message: `"Invalid token"` or `"Token expired"`
>    - Response time still displays
> 
> ```
> ❌ Status: 401 (error)
> Response: { "error": "Invalid token", "message": "Token has expired or is invalid" }
> ```
> 
> #### **Step 4: Test Invalid Token Format (Test #9 variant)**
> 1. Paste gibberish or malformed text in the token field (e.g., `invalid.token.here`)
> 2. Click **"Send Request"**
> 3. **Expected Result:**
>    - **Response Tab** shows `401` (red badge)
>    - Error message: `"Malformed token"` or `"Invalid token format"`
> 
> ```
> ❌ Status: 401 (error)
> Response: { "error": "Malformed token", "message": "JWT format is invalid" }
> ```
> 
> #### **Step 5: Review Request/Response Details**
> - **Request Tab** → Shows all headers sent (including `Authorization: Bearer <token>`)
> - **Response Tab** → Shows response headers and status
> - **History Tab** → All 4 requests logged with timestamps, clickable to re-run
> 
> 
> ## Key Advantages Over Old Tool
> 
> | Old Tool | New Tool |
> |----------|----------|
> | Manual token paste every request | Save token once, reuse across all requests |
> | Endpoint hardcoded in input | Quick preset buttons for common endpoints |
> | No custom headers support | Add/remove custom headers dynamically |
> | Single response view | Tabbed interface (Request/Response/History) |
> | No request history | Last 10 requests tracked, clickable to reload |
> | No response timing | Response time in milliseconds displayed |
> | No token management | Token save/load/visibility toggle |
> 
> 
> ## Summary
> 
> ✅ **JWT filter** is production-ready with proper error handling for expired/invalid tokens.
> 
> ✅ **REST tool** is now a full-featured API testing suite, eliminating manual token/header management and providing visibility into all request/response details.
> 
> **Tests #9 & #10** can now be executed quickly and repeatedly using the tool's history and preset features.
> </details>

## 001-0009
> **User Profile Endpoints** ![status](https://img.shields.io/badge/status-DONE-brightgreen)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to implement endpoints to retrieve and update user profile information with proper authorization.
> 
> # DOD (definition of done):
> - GET /auth/me endpoint returns current user data
> - PUT /auth/profile endpoint updates user information
> - Only authenticated users can access endpoints
> - User can only update their own profile
> - Updated data persisted to database
> - Proper error handling for unauthorized access
> 
> # TODO:
> - [x] 1. Create user profile DTO
> - [x] 2. Implement GET /auth/me service
> - [x] 3. Build GET /auth/me controller
> - [x] 4. Create update profile DTO with validation
> - [x] 5. Implement PUT /auth/profile service
> - [x] 6. Build PUT /auth/profile controller
> - [x] 7. Add authorization check (user can only update own profile)
> - [ ] 8. Test both endpoints
> 
> # Reports:
> ## **Phase 1 Profile Endpoints Task — Completion Report**
> 
> ### **What Was Already Done**
> - ✅ AuthService had `getCurrentUserProfile()` and `updateUserProfile()` methods
> - ✅ AuthController had `@GetMapping("/me")` and `@PutMapping("/profile")` endpoints
> - ✅ UserProfileDto existed with basic fields
> 
> ### **What We Added/Updated**
> 
> | Item | Action |
> |------|--------|
> | **UserProfileDto** | Added `publicKey` and `isActive` fields; added `@Email` validation |
> | **UpdateProfileRequest** (new DTO) | Created separate DTO for PUT requests with `@Size` validation on username; only includes editable fields (username, email) |
> | **AuthService methods** | Updated both methods to return all UserProfileDto fields (publicKey, isActive); added email duplication check in `updateUserProfile()` |
> | **AuthController endpoints** | Updated PUT endpoint to use `UpdateProfileRequest` instead of UserProfileDto |
> | **Authorization check** | Added logic to prevent email duplication when updating profile |
> 
> ### **Current State (Ready for Testing)**
> - ✅ GET /auth/me — returns authenticated user's full profile
> - ✅ PUT /auth/profile — updates username/email with validation and authorization
> - ✅ Both endpoints require valid JWT token
> - ✅ Email uniqueness enforced
> - ✅ Input validation in place (@NotBlank, @Email, @Size)
> 
> ### **Next: Test both endpoints** (pending)
> 
> 
> **Test Steps Using testuser1:**
> 
> ### **Step 1: Login & Get Token**
> - **Method:** POST
> - **Endpoint:** `/auth/login`
> - **Body:**
> ```json
> {
>   "username": "testuser1",
>   "password": "TestUser123!"
> }
> ```
> - **Expected:** 200 + JWT token in response
> - **Action:** Copy the token, paste in "Authorization Token" field, click **💾 (Save)**
> 
> 
> ### **Step 2: Test GET /auth/me**
> - **Method:** GET
> - **Endpoint:** `/auth/me`
> - **Authorization:** Use saved token
> - **Body:** None
> - **Expected:** 200
> ```json
> {
>   "id": 1,
>   "username": "testuser1",
>   "email": "testuser1@minichat.com",
>   "publicKey": null,
>   "fingerprint": null,
>   "createdAt": "2026-04-26T...",
>   "lastLogin": "2026-04-26T...",
>   "isActive": true
> }
> ```
> 
> 
> ### **Step 3: Test PUT /auth/profile (Update Username)**
> - **Method:** PUT
> - **Endpoint:** `/auth/profile`
> - **Authorization:** Same token
> - **Body:**
> ```json
> {
>   "username": "testuser1_updated",
>   "email": "testuser1@minichat.com"
> }
> ```
> - **Expected:** 200 + updated username in response
> 
> 
> ### **Step 4: Verify Update with GET /auth/me**
> - **Method:** GET
> - **Endpoint:** `/auth/me`
> - **Authorization:** Same token
> - **Body:** None
> - **Expected:** 200 + username shows "testuser1_updated"
> 
> ## The current files map
> 
> ```
> ├── main
> │ ├── java
> │ │ └── com
> │ │     └── minichat
> │ │         ├── config
> │ │         │ ├── AdminInitializer.java
> │ │         │ ├── JpaConfig.java
> │ │         │ ├── RoleInitializer.java
> │ │         │ ├── SecurityConfig.java
> │ │         │ ├── UserInitializer.java
> │ │         │ ├── UserProperties.java
> │ │         │ └── WebSocketConfig.java
> │ │         ├── controller
> │ │         │ └── AuthController.java
> │ │         ├── dto
> │ │         │ ├── JwtTokenDto.java
> │ │         │ ├── LoginRequest.java
> │ │         │ ├── LoginResponse.java
> │ │         │ ├── RegisterRequest.java
> │ │         │ ├── RegisterResponse.java
> │ │         │ ├── UpdateProfileRequest.java
> │ │         │ └── UserProfileDto.java
> │ │         ├── entity
> │ │         │ ├── Role.java
> │ │         │ ├── RoleType.java
> │ │         │ ├── Session.java
> │ │         │ └── User.java
> │ │         ├── exception
> │ │         │ ├── ErrorResponse.java
> │ │         │ ├── GlobalExceptionHandler.java
> │ │         │ ├── InvalidPasswordException.java
> │ │         │ ├── PasswordMismatchException.java
> │ │         │ ├── RoleNotFoundException.java
> │ │         │ ├── UnauthorizedException.java
> │ │         │ └── UserAlreadyExistsException.java
> │ │         ├── MiniChatApp.java
> │ │         ├── repository
> │ │         │ ├── RoleRepository.java
> │ │         │ ├── SessionRepository.java
> │ │         │ └── UserRepository.java
> │ │         ├── scheduler
> │ │         ├── security
> │ │         │ ├── CustomUserDetailsService.java
> │ │         │ ├── JwtAuthenticationFilter.java
> │ │         │ └── JwtTokenProvider.java
> │ │         ├── service
> │ │         │ └── AuthService.java
> │ │         └── util
> │ │             ├── PasswordEncoder.java
> │ │             └── PasswordValidator.java
> │ └── resources
> │     ├── application-dev.properties
> │     ├── application-prod.properties
> │     ├── application.properties
> │     └── application-test.properties
> └── test
>     └── java
>         └── com
>             └── minichat
>                 ├── controller
>                 ├── dto
>                 ├── entity
>                 ├── jwt
>                 ├── MiniChatAppTest.java
>                 ├── scheduler
>                 ├── security
>                 ├── service
>                 └── util
> 
> ```
> </details>

## 001-0010
> **Backend Integration Tests - Auth Endpoints** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to create comprehensive integration tests for all authentication endpoints to ensure security and proper functionality.
> 
> # DOD (definition of done):
> - Integration tests for registration endpoint
> - Integration tests for login endpoint
> - Integration tests for JWT validation
> - Integration tests for refresh token
> - Integration tests for profile endpoints
> - Tests for error cases (invalid input, duplicate users, wrong credentials)
> - All tests passing
> - Code coverage above 80%
> 
> # TODO:
> - [] 1. Setup test database configuration
> - [] 2. Create test fixtures for users
> - [] 3. Test successful registration
> - [] 4. Test duplicate user registration
> - [] 5. Test invalid registration input
> - [] 6. Test successful login
> - [] 7. Test login with wrong credentials
> - [] 8. Test JWT validation
> - [] 9. Test token expiration
> - [] 10. Test refresh token functionality
> - [] 11. Test profile endpoints
> - [] 12. Run code coverage analysis
> 
> # Reports:
> *
> </details>

## 001-0011
> **React Project Setup and Structure** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to initialize React project with proper folder structure, dependencies, and configuration for the P2P chat application.
> 
> # DOD (definition of done):
> - React project created (Create React App)
> - All necessary dependencies installed (react-router, axios, Context API)
> - Folder structure organized (components, services, pages, contexts)
> - Environment configuration setup
> - Project runs without errors
> 
> # TODO:
> - [] 1. Create React app with Create React App
> - [] 2. Install react-router-dom for routing
> - [] 3. Install axios for HTTP requests
> - [] 4. Create folder structure (components, services, pages, contexts, utils)
> - [] 5. Configure environment variables for API base URL
> - [] 6. Test project startup on localhost:3000
> 
> # Reports:
> *
> </details>

## 001-0012
> **Auth Context and State Management** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to create global authentication context for managing user state, JWT tokens, and authentication actions across the application.
> 
> # DOD (definition of done):
> - AuthContext created with Context API
> - User state stored globally
> - JWT token stored in state and localStorage
> - Login action implemented
> - Logout action implemented
> - User data persisted on page refresh
> - Context accessible from all components
> 
> # TODO:
> - [] 1. Create AuthContext.js file
> - [] 2. Define initial auth state (user, token, isAuthenticated)
> - [] 3. Implement login action
> - [] 4. Implement logout action
> - [] 5. Implement token refresh logic
> - [] 6. Add localStorage persistence
> - [] 7. Create AuthProvider component
> - [] 8. Export useAuth custom hook
> - [] 9. Wrap App with AuthProvider
> - [] 10. Test context functionality
> 
> # Reports:
> *
> </details>

## 001-0013
> **Login Page UI Component** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to create a user-friendly login page with form validation, error handling, and redirect on successful login.
> 
> # DOD (definition of done):
> - Login form displays with email and password fields
> - Form validation implemented (empty check, email format)
> - Error messages displayed for invalid credentials
> - Loading state shown during submission
> - Redirect to dashboard on successful login
> - Remember me functionality (optional)
> - Responsive design
> 
> # TODO:
> - [] 1. Create LoginPage component
> - [] 2. Build login form with email and password inputs
> - [] 3. Add form validation (client-side)
> - [] 4. Implement error message display
> - [] 5. Add loading spinner during submission
> - [] 6. Call login API via auth service
> - [] 7. Handle login errors (invalid credentials, network error)
> - [] 8. Redirect to dashboard on success
> - [] 9. Add link to registration page
> - [] 10. Style with basic CSS/Tailwind
> - [] 11. Test form submission
> 
> # Reports:
> *
> </details>

## 001-0014
> **Registration Page UI Component** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to create a registration page with comprehensive form validation, password confirmation, and success messaging.
> 
> # DOD (definition of done):
> - Registration form displays with email, password, and password confirmation fields
> - Password strength indicator implemented
> - Password confirmation validation
> - Email format validation
> - Duplicate user check error displayed
> - Success message shown after registration
> - Redirect to login on success
> - Responsive design
> 
> # TODO:
> - [] 1. Create RegisterPage component
> - [] 2. Build registration form (email, password, confirm password)
> - [] 3. Implement password strength indicator
> - [] 4. Add password confirmation validation
> - [] 5. Implement email format validation
> - [] 6. Add error handling for duplicate emails
> - [] 7. Call register API via auth service
> - [] 8. Show success message
> - [] 9. Redirect to login page
> - [] 10. Add link to login page
> - [] 11. Style with basic CSS/Tailwind
> - [] 12. Test form submission
> 
> # Reports:
> *
> </details>

## 001-0015
> **Protected Route Wrapper** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to create a ProtectedRoute component that checks authentication status and redirects unauthenticated users to the login page.
> 
> # DOD (definition of done):
> - ProtectedRoute component created
> - Authentication check implemented
> - Redirect to login for unauthenticated users
> - Redirect to dashboard for authenticated users trying to access login
> - Loading state shown while checking auth
> - All protected routes use this component
> 
> # TODO:
> - [] 1. Create ProtectedRoute component
> - [] 2. Check authentication status from context
> - [] 3. Show loading spinner while checking
> - [] 4. Render protected component if authenticated
> - [] 5. Redirect to login if not authenticated
> - [] 6. Create route configuration
> - [] 7. Apply ProtectedRoute to dashboard route
> - [] 8. Test with authenticated and unauthenticated users
> 
> # Reports:
> *
> </details>

## 001-0016
> **Basic Dashboard Page** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to create a basic dashboard page that displays a welcome message with user information and logout functionality.
> 
> # DOD (definition of done):
> - Dashboard page renders only for authenticated users
> - Welcome message displays with username
> - User profile information displayed
> - Logout button present and functional
> - Logout clears token and redirects to login
> - Basic layout and styling applied
> - Responsive design
> 
> # TODO:
> - [] 1. Create Dashboard component
> - [] 2. Get user data from auth context
> - [] 3. Display welcome message with username
> - [] 4. Show user profile information
> - [] 5. Add logout button
> - [] 6. Implement logout functionality
> - [] 7. Clear token from localStorage on logout
> - [] 8. Redirect to login after logout
> - [] 9. Style dashboard page
> - [] 10. Test logout functionality
> 
> # Reports:
> *
> </details>

## 001-0017
> **HTTP API Service Layer** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to create an HTTP service layer with axios that handles all API calls, JWT injection, and error handling.
> 
> # DOD (definition of done):
> - Axios instance created with base URL
> - JWT token automatically injected in request headers
> - Error handling implemented for all requests
> - Interceptors for request and response
> - Separate methods for each auth endpoint
> - Error status codes handled properly
> - Service easily testable
> 
> # TODO:
> - [] 1. Create api.js or apiClient.js file
> - [] 2. Configure axios instance with base URL
> - [] 3. Add request interceptor for JWT injection
> - [] 4. Add response interceptor for error handling
> - [] 5. Create auth service methods (login, register, getProfile, updateProfile)
> - [] 6. Handle 401 unauthorized errors
> - [] 7. Handle 400 validation errors
> - [] 8. Handle network errors
> - [] 9. Create global error handler
> - [] 10. Test all API calls
> 
> # Reports:
> *
> </details>

## 001-0018
> **Frontend Unit Tests - Auth Service and Context** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to create comprehensive unit tests for authentication service and auth context to ensure proper state management and API integration.
> 
> # DOD (definition of done):
> - Unit tests for AuthContext created
> - Unit tests for login functionality
> - Unit tests for logout functionality
> - Unit tests for token storage and retrieval
> - Unit tests for API service methods
> - Tests for error cases
> - All tests passing
> - Code coverage above 80%
> 
> # TODO:
> - [] 1. Setup testing library and dependencies (Jest, React Testing Library)
> - [] 2. Create test file for AuthContext
> - [] 3. Test initial auth state
> - [] 4. Test login action
> - [] 5. Test logout action
> - [] 6. Test token persistence in localStorage
> - [] 7. Test token retrieval on page refresh
> - [] 8. Create test file for API service
> - [] 9. Test login API call
> - [] 10. Test register API call
> - [] 11. Test error handling
> - [] 12. Test JWT injection in headers
> - [] 13. Run code coverage analysis
> - [] 14. Fix failing tests
> 
> # Reports:
> *
> </details>

## 001-0019
> **Spring WebSocket configuration** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to set up Spring WebSocket with SimpleBroker for real-time bidirectional communication between clients and server.
> 
> # DOD (definition of done):
> - WebSocketConfig class created and configured
> - SimpleBroker enabled for message routing
> - Server listens on /ws endpoint
> - Client can establish WebSocket connection
> 
> # TODO:
> - [] 1. Create WebSocketConfig class extending WebSocketMessageBrokerConfigurer
> - [] 2. Configure message broker (SimpleBroker)
> - [] 3. Set application destination prefix and endpoint
> - [] 4. Configure STOMP endpoints for client connection
> - [] 5. Test WebSocket connection with browser console
> 
> # Reports:
> *
> </details>

## 001-0020
> **User session tracking (online/offline)** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to track which users are online/offline in real-time and broadcast status changes to all connected clients.
> 
> # DOD (definition of done):
> - In-memory user session store tracks connected users
> - Online/offline events broadcast to room members
> - User list updates in real-time when users join/leave
> - Session data persists only during connection
> 
> # TODO:
> - [] 1. Create UserSession class to store user connection metadata
> - [] 2. Create SessionManager service (in-memory or Redis)
> - [] 3. Implement user connect handler (WebSocket event)
> - [] 4. Implement user disconnect handler (WebSocket event)
> - [] 5. Broadcast online status to room on connect/disconnect
> - [] 6. Handle reconnection scenarios
> 
> # Reports:
> *
> </details>

## 001-0021
> **Room entity and service** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to create Room entity and business logic for creating, joining, leaving, and listing rooms.
> 
> # DOD (definition of done):
> - Room entity with JPA mapping exists
> - RoomService handles CRUD operations
> - Create room endpoint works
> - Join/leave room endpoints work
> - List rooms endpoint returns all available rooms
> 
> # TODO:
> - [] 1. Create Room entity (id, name, createdBy, createdAt)
> - [] 2. Create RoomRepository (JPA interface)
> - [] 3. Create RoomService with business logic
> - [] 4. Implement createRoom(userId, roomName)
> - [] 5. Implement joinRoom(roomId, userId)
> - [] 6. Implement leaveRoom(roomId, userId)
> - [] 7. Implement listRooms() - return all rooms
> - [] 8. Add validation (room name not empty, user exists)
> 
> # Reports:
> *
> </details>

## 001-0022
> **Peer discovery endpoint** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to create an endpoint that returns list of online peers in a room so clients know who to initiate P2P connections with.
> 
> # DOD (definition of done):
> - GET /api/rooms/{roomId}/peers endpoint exists
> - Returns list of online peers with user metadata
> - Response includes userId, username, isOnline
> - Works with session tracking from Phase 2 card 1
> 
> # TODO:
> - [] 1. Create RoomController endpoint GET /api/rooms/{roomId}/peers
> - [] 2. Query SessionManager for online users in room
> - [] 3. Build peer response (userId, username, online status)
> - [] 4. Add authentication check (JWT)
> - [] 5. Handle room not found error
> - [] 6. Test endpoint returns correct peer list
> 
> # Reports:
> *
> </details>

## 001-0023
> **Signaling message handler** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to implement WebSocket message handling for relaying SDP offers/answers and ICE candidates between peers.
> 
> # DOD (definition of done):
> - SDP offer/answer messages relay correctly between peers
> - ICE candidates forward from sender to receiver
> - Message format is consistent: { type, from, to, data }
> - No messages lost during relay
> - Only intended recipient receives message
> 
> # TODO:
> - [] 1. Create SignalingMessage class (type, from, to, data)
> - [] 2. Create @MessageMapping handler for signaling messages
> - [] 3. Implement offer relay logic (send to specific peer)
> - [] 4. Implement answer relay logic
> - [] 5. Implement ICE candidate relay logic
> - [] 6. Add validation (sender authorized, receiver in room)
> - [] 7. Add error handling (peer not found, connection lost)
> - [] 8. Log signaling traffic for debugging
> 
> # Reports:
> *
> </details>

## 001-0024
> **Integration tests - WebSocket flow** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to write integration tests that verify WebSocket connection, room join, peer discovery, and message relay work end-to-end.
> 
> # DOD (definition of done):
> - Test user connects to WebSocket
> - Test user joins room and broadcasts online status
> - Test peer discovery returns correct list
> - Test SDP offer/answer relay between two peers
> - Test ICE candidate forwarding
> - All tests pass
> 
> # TODO:
> - [] 1. Set up WebSocket test client (TestRestTemplate or WebSocketStompClient)
> - [] 2. Write test for WebSocket connection
> - [] 3. Write test for room join and online broadcast
> - [] 4. Write test for peer discovery endpoint
> - [] 5. Write test for offer/answer relay
> - [] 6. Write test for ICE candidate relay
> - [] 7. Write test for peer disconnect and offline broadcast
> - [] 8. Add error case tests (invalid room, unauthorized access)
> 
> # Reports:
> *
> </details>

## 001-0025
> **WebSocket client setup** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to set up WebSocket client library (SockJS + STOMP) and establish persistent connection to signaling server with proper reconnection logic.
> 
> # DOD (definition of done):
> - WebSocket client connects to server on app load
> - Connection persists across page navigation
> - Auto-reconnect on disconnect
> - JWT token passed in WebSocket handshake
> - Connection state tracked in React context
> 
> # TODO:
> - [] 1. Install SockJS and STOMP client libraries
> - [] 2. Create WebSocketService with connect() method
> - [] 3. Implement JWT token passing in handshake
> - [] 4. Implement auto-reconnect with exponential backoff
> - [] 5. Create WebSocket context for React state management
> - [] 6. Add connection status listener
> - [] 7. Handle connection errors gracefully
> - [] 8. Test connection in browser DevTools
> 
> # Reports:
> *
> </details>

## 001-0026
> **Room list page UI** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to build a page that displays all available rooms with online peer count and allows users to select a room to join.
> 
> # DOD (definition of done):
> - Room list page displays all rooms from backend
> - Each room shows name, creator, peer count
> - Users can click room to join
> - Real-time updates when peers join/leave room
> - Empty state shown if no rooms exist
> 
> # TODO:
> - [] 1. Fetch rooms list from GET /api/rooms
> - [] 2. Create RoomList component
> - [] 3. Create RoomCard component (name, creator, peer count)
> - [] 4. Implement onClick handler to join room
> - [] 5. Subscribe to WebSocket for online status updates
> - [] 6. Update peer count in real-time
> - [] 7. Show loading state while fetching
> - [] 8. Show error message if fetch fails
> - [] 9. Add empty state UI
> 
> # Reports:
> *
> </details>

## 001-0027
> **Create/join room UI** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to build forms for creating new rooms and joining existing rooms with proper validation and feedback.
> 
> # DOD (definition of done):
> - Create room form accepts room name
> - Join room form allows entering room ID or selecting from list
> - Form validation prevents empty/invalid input
> - Success feedback after creating/joining
> - Error messages shown for failures
> - User redirected to room chat after success
> 
> # TODO:
> - [] 1. Create CreateRoomForm component
> - [] 2. Add room name input with validation
> - [] 3. Implement create room button (POST /api/rooms)
> - [] 4. Create JoinRoomForm component
> - [] 5. Add room selection/input field
> - [] 6. Implement join room button (POST /api/rooms/{id}/join)
> - [] 7. Add success toast/message
> - [] 8. Add error handling and display
> - [] 9. Clear form after successful submission
> 
> # Reports:
> *
> </details>

## 001-0028
> **Online users display** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to display real-time list of online peers in current room with visual indicators and update when peers connect/disconnect.
> 
> # DOD (definition of done):
> - Online peers list shows in room UI
> - Each peer shows username and online status indicator
> - List updates in real-time when peers join/leave
> - Current user highlighted in list
> - No duplicate entries
> 
> # TODO:
> - [] 1. Create OnlineUsersList component
> - [] 2. Subscribe to WebSocket online status messages
> - [] 3. Store peer list in React state
> - [] 4. Display each peer with username and status dot
> - [] 5. Highlight current user
> - [] 6. Update peer list on join event
> - [] 7. Remove peer on disconnect event
> - [] 8. Handle empty peer list (show "No peers online")
> 
> # Reports:
> *
> </details>

## 001-0029
> **Signaling message handler (frontend)** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to listen for and handle incoming SDP offers/answers and ICE candidates from peers, triggering WebRTC connection initiation.
> 
> # DOD (definition of done):
> - Frontend receives SDP offer from peer
> - Frontend receives SDP answer from peer
> - Frontend receives and processes ICE candidates
> - Received messages trigger WebRTC connection setup
> - Errors logged for debugging
> 
> # TODO:
> - [] 1. Create SignalingHandler service
> - [] 2. Subscribe to WebSocket offer messages
> - [] 3. Store received offers in React state
> - [] 4. Subscribe to WebSocket answer messages
> - [] 5. Subscribe to WebSocket ICE candidate messages
> - [] 6. Implement message validation
> - [] 7. Add error handling (malformed messages, unknown peers)
> - [] 8. Log received signaling messages
> 
> # Reports:
> *
> </details>

## 001-0030
> **Connection state UI** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to display connection state (connecting, connected, disconnected, error) to user with visual indicators and status messages.
> 
> # DOD (definition of done):
> - Connection state displays in UI (connecting, connected, error)
> - Visual indicators (spinner, checkmark, warning icon)
> - Status message explains current state
> - State updates in real-time
> - Error messages are helpful and actionable
> 
> # TODO:
> - [] 1. Create ConnectionStatus component
> - [] 2. Add CSS styles for each state (colors, icons, animations)
> - [] 3. Subscribe to WebSocket connection state
> - [] 4. Show spinner during "connecting"
> - [] 5. Show checkmark for "connected"
> - [] 6. Show error icon and message for "error"
> - [] 7. Display in header or sidebar
> - [] 8. Test all state transitions
> 
> # Reports:
> *
> </details>

## 001-0031
> **Unit tests - WebSocket service** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to write unit tests for WebSocket service, signaling handler, and connection management with mocked WebSocket to verify functionality.
> 
> # DOD (definition of done):
> - WebSocket service connection tested
> - Message subscription tested
> - Message sending tested
> - Reconnection logic tested
> - Error handling tested
> - All tests pass with 80%+ coverage
> 
> # TODO:
> - [] 1. Set up test environment (Jest, testing-library)
> - [] 2. Create mock WebSocket client
> - [] 3. Test connect() establishes connection
> - [] 4. Test subscribe() listens for messages
> - [] 5. Test send() sends messages correctly
> - [] 6. Test reconnect logic with exponential backoff
> - [] 7. Test error handling and disconnect
> - [] 8. Test JWT token passed in handshake
> - [] 9. Write tests for signaling handler
> 
> # Reports:
> *
> </details>

## 001-0032
> **STUN/TURN server configuration** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to configure STUN/TURN servers for NAT traversal and include ICE server list in WebSocket signaling response so clients can establish P2P connections.
> 
> # DOD (definition of done):
> - Google STUN server configured (free, public)
> - Coturn self-hosted TURN server setup (or alternative)
> - ICE server list returned in signaling response
> - Clients receive iceServers in WebSocket message
> - P2P connections work through NAT/firewall
> 
> # TODO:
> - [] 1. Research STUN vs TURN differences
> - [] 2. Add Google STUN server (stun.l.google.com:19302)
> - [] 3. Research and choose TURN solution (Coturn, Firebase, Twilio)
> - [] 4. Install and configure Coturn on server (if self-hosted)
> - [] 5. Create IceServer configuration class
> - [] 6. Add iceServers list to signaling response
> - [] 7. Return iceServers when client joins room
> - [] 8. Test STUN/TURN connectivity
> 
> # Reports:
> *
> </details>

## 001-0033
> **SDP offer/answer relay refinement** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to refine and optimize the SDP offer/answer relay logic from Phase 2, ensuring offers and answers are correctly routed between peers without loss.
> 
> # DOD (definition of done):
> - SDP offer relayed to intended peer only
> - SDP answer relayed back to offer sender
> - No offers/answers lost or duplicated
> - Validation prevents malformed SDP
> - Connection established after answer received
> 
> # TODO:
> - [] 1. Review Phase 2 signaling handler code
> - [] 2. Add SDP validation (check format, required fields)
> - [] 3. Ensure offer routed to single peer (not broadcast)
> - [] 4. Ensure answer routed back to offer sender
> - [] 5. Add logging for offer/answer flow
> - [] 6. Test offer/answer sequence with two clients
> - [] 7. Handle case where peer disconnects mid-handshake
> - [] 8. Add timeout for incomplete handshake
> 
> # Reports:
> *
> </details>

## 001-0034
> **ICE candidate relay** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to implement WebSocket message handling for relaying ICE candidates between peers to establish optimal connection paths through NAT/firewalls.
> 
> # DOD (definition of done):
> - ICE candidates relayed from sender to receiver
> - Candidates arrive in correct order
> - No candidates lost or duplicated
> - Validation prevents malformed candidates
> - Connection uses best available path
> 
> # TODO:
> - [] 1. Create ICE candidate message format validation
> - [] 2. Implement @MessageMapping for ICE candidates
> - [] 3. Route candidate to specific peer (not broadcast)
> - [] 4. Handle late-arriving candidates (after answer sent)
> - [] 5. Add logging for candidate relay
> - [] 6. Implement candidate queuing if peer not ready
> - [] 7. Test candidate relay with STUN/TURN
> - [] 8. Handle candidate gathering complete signal
> 
> # Reports:
> *
> </details>

## 001-0035
> **Connection monitoring (optional)** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to optionally track P2P connection state and log connection failures for debugging and analytics purposes.
> 
> # DOD (definition of done):
> - P2P connection state tracked on backend
> - Connection failures logged with details
> - Analytics data collected (connection attempts, success rate)
> - Logs useful for debugging NAT issues
> 
> # TODO:
> - [] 1. Create connection monitoring service
> - [] 2. Listen for connection state changes via WebSocket
> - [] 3. Log successful P2P connections
> - [] 4. Log connection failures and reasons
> - [] 5. Track connection attempt count
> - [] 6. Calculate success/failure rate per peer
> - [] 7. Store metrics for analytics
> - [] 8. Create dashboard or log viewer (optional)
> 
> # Reports:
> *
> </details>

## 001-0036
> **Integration tests - P2P signaling** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to write integration tests that verify SDP offer/answer and ICE candidate relay works correctly between two simulated WebRTC clients.
> 
> # DOD (definition of done):
> - Test offer generation and relay
> - Test answer generation and relay
> - Test ICE candidate collection and relay
> - Test complete connection handshake
> - All tests pass and are repeatable
> 
> # TODO:
> - [] 1. Set up WebSocket test clients (two instances)
> - [] 2. Write test for peer A sending offer to peer B
> - [] 3. Write test for peer B receiving offer
> - [] 4. Write test for peer B sending answer
> - [] 5. Write test for peer A receiving answer
> - [] 6. Write test for ICE candidate relay both directions
> - [] 7. Write test for complete handshake flow
> - [] 8. Add error case tests (invalid SDP, malformed ICE)
> - [] 9. Test connection timeout scenarios
> 
> # Reports:
> *
> </details>

## 001-0037
> **WebRTC peer connection setup** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to initialize RTCPeerConnection with STUN/TURN servers, handle connection state changes, and manage peer lifecycle for each connected peer.
> 
> # DOD (definition of done):
> - RTCPeerConnection created with ICE servers
> - Connection state changes logged (connecting, connected, failed, disconnected)
> - Peer removed when connection closes
> - Each peer has unique connection instance
> - No memory leaks from unclosed connections
> 
> # TODO:
> - [] 1. Create WebRTCService class
> - [] 2. Implement createPeerConnection(peerId, iceServers)
> - [] 3. Add onconnectionstatechange listener
> - [] 4. Add oniceconnectionstatechange listener
> - [] 5. Log state transitions for debugging
> - [] 6. Implement closePeerConnection(peerId)
> - [] 7. Handle connection failure scenarios
> - [] 8. Store peer connections in Map by peerId
> - [] 9. Clean up resources on peer disconnect
> - [] 10. Consider using simple-peer library to reduce complexity
> 
> # Reports:
> *
> </details>

## 001-0038
> **Data channel creation and handling** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to create data channels for peer-to-peer messaging and implement handlers for data channel events (open, message, error, close).
> 
> # DOD (definition of done):
> - Data channel created by offer initiator
> - Data channel received by answer responder
> - onopen handler called when channel ready
> - onmessage handler receives incoming messages
> - onerror handler logs errors
> - onclose handler cleans up resources
> 
> # TODO:
> - [] 1. Implement createDataChannel(peerId) method
> - [] 2. Create data channel with reliable settings
> - [] 3. Implement ondatachannel listener (for receiver)
> - [] 4. Add onopen event handler
> - [] 5. Add onmessage event handler
> - [] 6. Add onerror event handler
> - [] 7. Add onclose event handler
> - [] 8. Store data channels by peerId
> - [] 9. Test data channel opens successfully
> - [] 10. Handle bufferedAmount to prevent overflow
> 
> # Reports:
> *
> </details>

## 001-0039
> **Message sending and receiving via data channel** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to implement sending and receiving messages through data channels with proper serialization, deserialization, and error handling.
> 
> # DOD (definition of done):
> - Messages sent via data channel to peer
> - Incoming messages parsed and displayed
> - Message format consistent (JSON)
> - Error handling for send failures
> - Unsent messages buffered if channel not ready
> 
> # TODO:
> - [] 1. Create message serialization format (JSON with metadata)
> - [] 2. Implement sendMessage(peerId, messageText) method
> - [] 3. Check data channel readyState before sending
> - [] 4. Handle bufferedAmount to queue messages
> - [] 5. Implement message parsing on receive
> - [] 6. Validate received message format
> - [] 7. Extract sender, receiver, text, timestamp
> - [] 8. Handle send errors gracefully
> - [] 9. Log message delivery for debugging
> 
> # Reports:
> *
> </details>

## 001-0040
> **Chat UI (message list, input, send button)** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to build the chat UI component that displays messages in a list, accepts user input, and sends messages via data channel.
> 
> # DOD (definition of done):
> - Messages display in chronological order
> - Each message shows sender name, text, and timestamp
> - Input field accepts message text
> - Send button sends message to peer
> - UI scrolls to latest message
> - Empty chat shows placeholder
> 
> # TODO:
> - [] 1. Create ChatWindow component
> - [] 2. Create MessageList component (renders message array)
> - [] 3. Create Message component (sender, text, time)
> - [] 4. Create MessageInput component (text input + send button)
> - [] 5. Implement onSend handler (call sendMessage service)
> - [] 6. Add scroll-to-bottom on new message
> - [] 7. Add loading state for unsent messages
> - [] 8. Show error message if send fails
> - [] 9. Clear input after send
> - [] 10. Add emoji/formatting (optional)
> 
> # Reports:
> *
> </details>

## 001-0041
> **localStorage persistence** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to save chat messages to localStorage with peer/room context key and load them on page reload or reconnect.
> 
> # DOD (definition of done):
> - Messages saved to localStorage with key chat_{peerId}_{roomId}
> - Messages loaded on component mount
> - Message history persists across page reloads
> - localStorage quota respected (don't exceed limit)
> - Old messages cleaned up periodically
> 
> # TODO:
> - [] 1. Create LocalStorageService for message persistence
> - [] 2. Implement saveMessage(peerId, roomId, message)
> - [] 3. Serialize message to JSON format
> - [] 4. Implement loadMessages(peerId, roomId, limit)
> - [] 5. Deserialize messages from JSON
> - [] 6. Load messages on ChatWindow mount
> - [] 7. Check localStorage quota before saving
> - [] 8. Implement clearMessages(peerId, roomId)
> - [] 9. Add expiration time for old messages (optional)
> - [] 10. Test persistence across page reloads
> 
> # Reports:
> *
> </details>

## 001-0042
> **Connection error handling** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to handle WebRTC connection failures gracefully and display user-friendly error messages for common issues (NAT problems, firewall, connection timeout).
> 
> # DOD (definition of done):
> - Connection errors caught and logged
> - User-friendly error messages displayed
> - Suggestions provided for resolution
> - Retry option available
> - Fallback to server relay documented
> 
> # TODO:
> - [] 1. Create error message mapper for WebRTC error codes
> - [] 2. Implement error handler in peer connection setup
> - [] 3. Detect NAT/firewall issues from error type
> - [] 4. Display error modal with message and suggestions
> - [] 5. Add "Retry Connection" button
> - [] 6. Log error details for debugging
> - [] 7. Show connection status updates (connecting → failed → retrying)
> - [] 8. Handle timeout scenarios (no answer after X seconds)
> - [] 9. Document fallback to server relay for Phase 5
> - [] 10. Test error scenarios (disconnect WiFi, block STUN)
> 
> # Reports:
> *
> </details>

## 001-0043
> **Unit and integration tests - WebRTC** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to write unit and integration tests for WebRTC peer connection, data channel, and message flow with mocked WebRTC APIs.
> 
> # DOD (definition of done):
> - Peer connection creation tested
> - Data channel creation tested
> - Message sending and receiving tested
> - localStorage persistence tested
> - Connection state changes tested
> - Error scenarios tested
> - Test coverage 75%+
> 
> # TODO:
> - [] 1. Set up WebRTC mock (jest-webrtc or manual mocks)
> - [] 2. Write test for createPeerConnection()
> - [] 3. Write test for data channel creation
> - [] 4. Write test for sendMessage() through data channel
> - [] 5. Write test for receiving message
> - [] 6. Write test for message display in UI
> - [] 7. Write test for localStorage save
> - [] 8. Write test for localStorage load
> - [] 9. Write test for connection state transitions
> - [] 10. Write error handling tests
> - [] 11. Test message buffer overflow handling
> - [] 12. Calculate and verify test coverage
> 
> # Reports:
> *
> </details>

## 001-0044
> **Key entity & schema design** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to create the database schema and JPA entity for storing user cryptographic keys with fingerprint tracking and key type management.
> 
> # DOD (definition of done):
> - Key entity created with all required fields (id, userId, publicKey, keyFingerprint, createdAt, keyType)
> - Database migration/schema applied
> - Key repository interface created
> - Unit tests for Key entity validation pass
> 
> # TODO:
> - [] Design Key entity fields (id, userId, publicKey, keyFingerprint, keyType: ENUM(auto/manual), createdAt, updatedAt)
> - [] Create JPA entity class with proper annotations
> - [] Add database migration script
> - [] Create KeyRepository interface extending JpaRepository
> - [] Add unique constraint on (userId, publicKey) to prevent duplicates
> - [] Add index on userId for fast lookup
> - [] Write unit tests for entity validation
> - [] Document key format expectations (PEM vs raw bytes)
> 
> # Reports:
> *
> </details>

## 001-0045
> **Key generation service & keypair creation** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to implement a secure keypair generation service that creates Ed25519 keypairs and calculates fingerprints for users.
> 
> # DOD (definition of done):
> - KeyGenerationService implemented with generateKeyPair() method
> - Ed25519 keypair generation working (using Bouncy Castle or NaCl4j)
> - Fingerprint calculation algorithm implemented (SHA256 hash of public key)
> - Service returns public key only to caller
> - Private key never exposed in logs or responses
> - Unit tests for key generation and fingerprint calculation pass
> 
> # TODO:
> - [] Add Bouncy Castle or NaCl4j dependency to pom.xml
> - [] Create KeyGenerationService class
> - [] Implement generateKeyPair() method using Ed25519
> - [] Implement calculateFingerprint(publicKey) method (SHA256 hash, first 16 chars)
> - [] Add validation for generated keys
> - [] Ensure private key is NOT serialized or logged
> - [] Write unit tests for key generation
> - [] Write unit tests for fingerprint calculation (deterministic output)
> - [] Add integration test with Key entity persistence
> - [] Document key format (PEM or raw bytes decision)
> 
> # Reports:
> *
> </details>

## 001-0046
> **POST /keys/generate endpoint - create keypair on first login** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to create a REST endpoint that generates and stores a user's keypair on first login, returning only the public key.
> 
> # DOD (definition of done):
> - POST /keys/generate endpoint created and secured with JWT
> - Endpoint checks if user already has a key, prevents duplicate generation
> - Keypair generated via KeyGenerationService
> - Public key returned to client as PEM format
> - Key stored in database with fingerprint
> - Response includes public key and fingerprint
> - Error handling for existing keys
> - Unit & integration tests pass
> 
> # TODO:
> - [] Create KeyController with @PostMapping("/keys/generate")
> - [] Add JWT authentication check via @CurrentUser or SecurityContext
> - [] Query database to check if user already has a key
> - [] If exists: return 409 Conflict with message "Key already exists"
> - [] If not exists: call KeyGenerationService.generateKeyPair()
> - [] Save Key entity to database
> - [] Return response: { publicKey: "...", fingerprint: "...", keyType: "auto", createdAt: "..." }
> - [] Add error handling for service failures
> - [] Write unit test with mocked KeyGenerationService
> - [] Write integration test with real database
> - [] Test duplicate generation prevention
> - [] Test response format and validation
> 
> # Reports:
> *
> </details>

## 001-0047
> **GET /keys/{userId} endpoint - public key lookup** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to create a public endpoint that allows any authenticated user to retrieve another user's public key and fingerprint for encryption purposes.
> 
> # DOD (definition of done):
> - GET /keys/{userId} endpoint created and accessible to authenticated users
> - Returns public key and fingerprint only (no private key)
> - Handles user not found (404)
> - Handles user has no key yet (404)
> - Response format matches POST /keys/generate
> - Unit & integration tests pass
> 
> # TODO:
> - [] Create @GetMapping("/keys/{userId}") in KeyController
> - [] Add JWT authentication check
> - [] Query database for Key by userId
> - [] If not found: return 404 with message "User or key not found"
> - [] Return response: { publicKey: "...", fingerprint: "...", keyType: "auto", createdAt: "..." }
> - [] Add error handling
> - [] Write unit test (mocked repository)
> - [] Write integration test with real database
> - [] Test not found scenario
> - [] Test fingerprint consistency (same key always returns same fingerprint)
> 
> # Reports:
> *
> </details>

## 001-0048
> **Key exchange endpoint - trust on first use (TOFU)** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to implement a key exchange mechanism that allows users to trust a peer's public key on first use with TOFU validation.
> 
> # DOD (definition of done):
> - POST /keys/trust endpoint created
> - Validates peer's public key format before storing
> - Stores trusted peer key with trust timestamp
> - Returns confirmation with fingerprint
> - Prevents duplicate trusts of same peer key
> - Unit & integration tests pass
> 
> # TODO:
> - [] Create @PostMapping("/keys/trust") endpoint in KeyController
> - [] Accept request body: { peerId: "...", publicKey: "..." }
> - [] Validate public key format (PEM validation)
> - [] Query if this peer's key is already trusted by current user
> - [] If already trusted: return 409 Conflict
> - [] Calculate fingerprint of peer's public key
> - [] Store trusted key relationship (create TrustedKey entity if needed)
> - [] Return response: { peerId: "...", fingerprint: "...", trustedAt: "...", status: "trusted" }
> - [] Add proper error handling (invalid key format, peer not found)
> - [] Write unit tests with mocked repository
> - [] Write integration tests
> - [] Test duplicate trust prevention
> - [] Test invalid key format rejection
> 
> # Reports:
> *
> </details>

## 001-0049
> **Encrypted key storage at rest** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to implement encryption at rest for private keys stored in the database, ensuring keys are never stored in plaintext.
> 
> # DOD (definition of done):
> - Private keys encrypted at rest using AES-256-GCM
> - Master encryption key securely managed (config or key vault)
> - Decryption works transparently for authentication flows
> - Encrypted keys cannot be read from raw database dumps
> - Unit & integration tests for encryption/decryption pass
> - Documentation of key management strategy
> 
> # TODO:
> - [] Design encryption strategy (AES-256-GCM recommended)
> - [] Create EncryptionService with encrypt(plaintext, masterKey) and decrypt(ciphertext, masterKey)
> - [] Configure master key storage (environment variable or key vault - decide)
> - [] Add encrypted_private_key and encryption_iv columns to Key entity
> - [] Implement @PrePersist hook in Key entity to encrypt private key before save
> - [] Implement custom deserializer to decrypt private key on load
> - [] Update KeyGenerationService to handle encrypted storage
> - [] Ensure private key is only decrypted when needed (lazy loading)
> - [] Add audit logging for decryption events
> - [] Write unit tests for encryption/decryption
> - [] Write integration test: generate key → store encrypted → retrieve and verify
> - [] Test with database dump to confirm plaintext key is not visible
> - [] Document master key rotation procedure (if applicable)
> 
> # Reports:
> *
> </details>

## 001-0050
> **Key rotation & revocation mechanism** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to implement key rotation and revocation features to allow users to invalidate old keys and generate new ones for enhanced security.
> 
> # DOD (definition of done):
> - POST /keys/rotate endpoint allows users to generate new keypair and deprecate old
> - Old key marked as revoked with timestamp
> - New key becomes active
> - Clients are notified of key rotation via WebSocket
> - Unit & integration tests pass
> 
> # TODO:
> - [] Create KeyStatus enum: ACTIVE, REVOKED, EXPIRED
> - [] Add status and revokedAt fields to Key entity
> - [] Create @PostMapping("/keys/rotate") endpoint
> - [] Validate current user has active key
> - [] Generate new keypair via KeyGenerationService
> - [] Mark old key as REVOKED with timestamp
> - [] Save new key as ACTIVE
> - [] Broadcast WebSocket message to all peers: { type: "key_rotated", userId: "...", newFingerprint: "..." }
> - [] Send notification email to user (optional)
> - [] Return response: { oldFingerprint: "...", newFingerprint: "...", rotatedAt: "..." }
> - [] Update GET /keys/{userId} to return only ACTIVE keys
> - [] Add period check for EXPIRED status (optional)
> - [] Write unit tests for rotation logic
> - [] Write integration tests
> - [] Test that old key is no longer used for new connections
> 
> # Reports:
> *
> </details>

## 001-0051
> **Integration tests - key exchange flow (backend)** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to write comprehensive integration tests covering the complete key exchange flow from generation to trust to usage.
> 
> # DOD (definition of done):
> - Test suite covers all key generation endpoints
> - Test suite covers key exchange (TOFU) flow
> - Test suite covers key rotation
> - Tests use real database and Spring test context
> - All tests pass with 80%+ code coverage on key-related code
> - Test cases documented for future reference
> 
> # TODO:
> - [] Create KeyControllerIntegrationTest class
> - [] Test POST /keys/generate for new user (happy path)
> - [] Test POST /keys/generate for user with existing key (409 conflict)
> - [] Test GET /keys/{userId} for existing key
> - [] Test GET /keys/{userId} for non-existent user (404)
> - [] Test POST /keys/trust happy path (trust peer key)
> - [] Test POST /keys/trust with invalid key format (400 bad request)
> - [] Test POST /keys/trust duplicate trust (409 conflict)
> - [] Test two users exchanging keys and verifying fingerprints match
> - [] Test POST /keys/rotate flow
> - [] Test old key marked as revoked after rotation
> - [] Test new key used for encryption after rotation
> - [] Test E2E: User A generates key → User B fetches A's key → B trusts A's key → fingerprints match
> - [] Add performance test: key generation under load (100+ requests)
> - [] Write test documentation (test scenarios, expected outcomes)
> - [] Verify all tests run in CI/CD pipeline
> 
> # Reports:
> *
> </details>

## 001-0052
> **Crypto library integration & evaluation (Frontend)** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to evaluate and integrate a JavaScript crypto library (libsodium.js or TweetNaCl.js) into the React frontend, with bundle size analysis.
> 
> # DOD (definition of done):
> - Crypto library selected and integrated into React project
> - Library supports Ed25519 keypair generation
> - Library supports message encryption/decryption
> - Bundle size impact analyzed (<100KB for crypto code)
> - Crypto service wrapper created for consistent API
> - Unit tests for crypto operations pass
> - Works across browsers (Chrome, Firefox, Safari)
> 
> # TODO:
> - [] Research & compare: libsodium.js vs TweetNaCl.js vs sodium-plus
> - [] Evaluate bundle size for each option (run webpack-bundle-analyzer)
> - [] Choose winner based on size + feature completeness
> - [] Install chosen library: npm install libsodium.js (or equivalent)
> - [] Create CryptoService class in frontend
> - [] Implement generateKeyPair() method
> - [] Implement encrypt(plaintext, recipientPublicKey) method
> - [] Implement decrypt(ciphertext, recipientPublicKey) method
> - [] Implement getFingerprint(publicKey) method
> - [] Add proper error handling (invalid keys, encryption failures)
> - [] Write unit tests for all crypto operations
> - [] Test encryption/decryption round-trip
> - [] Test fingerprint determinism (same input = same output)
> - [] Cross-browser test (Chrome, Firefox, Safari)
> - [] Document library choice and reasoning
> - [] Record bundle size impact in project README
> 
> # Reports:
> *
> </details>

## 001-0053
> **Key generation on first login (Frontend)** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to prompt users to generate their cryptographic keypair on first login and store the public key on the backend.
> 
> # DOD (definition of done):
> - First-login detection implemented in Auth flow
> - Key generation modal displayed to new users
> - User sees option to auto-generate or manual import
> - Public key sent to backend via POST /keys/generate
> - Private key stored securely in browser
> - User can dismiss/confirm key generation
> - Unit & integration tests pass
> 
> # TODO:
> - [] Detect first login: check if user has public key by calling GET /auth/me and checking keyGenerated flag
> - [] Create KeyGenerationModal component
> - [] Display prompt: "Generate your encryption keypair?" with two buttons: "Auto-Generate" and "Import Existing"
> - [] Implement "Auto-Generate" flow: call CryptoService.generateKeyPair()
> - [] Display generated public key and fingerprint for user review
> - [] Show warning: "Save your private key safely. This is your only copy."
> - [] Add button to copy public key to clipboard
> - [] Call POST /keys/generate with public key
> - [] On success: store private key in localStorage (encrypted with password or device key)
> - [] Mark user as keyGenerated in local state
> - [] Close modal and proceed to dashboard
> - [] Implement "Import Existing" flow: prompt for private key paste, validate, save locally
> - [] Add error handling (import failures, network errors)
> - [] Write unit tests for key generation flow
> - [] Write integration test: login → generate key → verify backend storage
> - [] Test modal dismissal scenarios
> - [] Test localStorage persistence of private key
> 
> # Reports:
> *
> </details>

## 001-0054
> **Public key display & fingerprint UI** ![status](https://img.shields.io/badge/status-NOT--STARTED-lightgrey)
> <details >
>     <summary>Details</summary>
> 
> The goal of this card is to create UI components that display the user's public key and fingerprint for sharing and verification purposes.
> 
> # DOD (definition of done):
> -
> 
> # TODO:
> - [] 1.
> 
> # Reports:
> *
> </details>

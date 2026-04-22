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

### Phase 6: Testing & Optimization (8-10 work-days | 10-14 calendar days)

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
  - Aim for 80%+ code coverage
- [ ] **E2E tests** (Cypress or Playwright) — *2 days*
  - Full user flow: register → login → create room → chat → send message
  - Test both local & server storage modes
- [ ] **Cross-browser testing** — *0.5 days*
  - Test on Chrome, Firefox, Safari (WebRTC behavior differs)
- [ ] **Performance profiling** — *0.5 days*

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
> **Create a pre- high-level architecture plan.** ![status](https://img.shields.io/badge/status-ONGOING-yellow)
> <details open>
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

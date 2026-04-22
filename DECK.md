---
Title: minichat
Description: plans and project management sheets
Date: 
Robots: noindex,nofollow
Template: index
---

# minichat

## Analyzing all parts

|#|Part|Details|Total Duration|Status|
|:-|:-|:-|:-|:-|
|1|-|-|-|-|-|
|:-|:-|:-|::||


## Timeplan

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

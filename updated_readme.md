# 🏆 SportConnect

> **Sport Partner Finder for International Students**  
> A Java console application built for ITC5102 — Humber College

---

## 📋 Table of Contents

- [Overview](#overview)
- [Team](#team)
- [Project Status](#project-status)
- [Features](#features)
- [Architecture](#architecture)
- [Class Structure](#class-structure)
- [Getting Started](#getting-started)
- [How to Use](#how-to-use)
- [Data Persistence](#data-persistence)
- [Email Notifications](#email-notifications)
- [Admin Controls](#admin-controls)
- [Assumptions & Constraints](#assumptions--constraints)

---

## Overview

SportConnect is a **menu-driven Java console application** designed to help international students in Canada find sport partners, form teams, and schedule games.

International students arriving in a new country often face social isolation — especially those who come from environments where sports play a central role in daily life. Existing platforms like Facebook or WhatsApp are too general and not built for sport-based matchmaking. SportConnect fills that gap.

**Key capabilities:**

- Create a profile with sport, skill level, and location
- Search and connect with players who match your sport and skill level
- Send and accept friend requests
- Create and join teams (up to 11 members)
- Schedule and book game sessions
- Chat with accepted friends
- Rate players and record payments after completed sessions
- Async email notifications for every major action
- AES-GCM encrypted file persistence for player accounts

---

## Team

| Member | Role | Phase |
|---|---|---|
| **Sahil Maniya** | Core Player & Admin Features | Phase 1 ·3 |
| **Parth Patel** | Social Features, Email, Encryption | Phase 2 · 3 |
| **Kelvin Idoko** | Team & Booking Features | Phase 2 |
| **Dhruv Patel** | Ratings & Payments | Phase 3 |
| **All Members** | Integration, Testing & Documentation | Phase 4 |

> **Team Name:** Kepsa  
> **Course:** ITC5102 — Java Programming, Humber College

---

## Project Status

| Phase | Focus | Owner | Status | Deadline |
|---|---|---|---|---|
| Phase 1 | Core Player & Admin | Sahil Maniya | ✅ Complete | Apr 13, 2026 |
| Phase 2 | Social & Booking Features | Parth Patel, Kelvin Idoko | ✅ Complete | Apr 16, 2026 |
| Phase 3 | Ratings, Payments & Encryption | Dhruv Patel, Parth Patel, Sahil Maniya | ✅ Complete | Apr 20, 2026 |
| Phase 4 | Testing, Integration & Documentation | All Members | 🔄 Active Sprint | Apr 23, 2026 |

---

## Features

### ✅ Phase 1 — Core Player & Admin *(Sahil Maniya)*

| Feature | Description |
|---|---|
| User Registration & Login | Register with email and password, login with token-based sessions |
| Profile Management | Update sport, skill level, city, bio, age, experience |
| Admin Role (Level 1) | Cancel scheduled sessions, process refunds based on 24h rule |
| Super-Admin Role (Level 2) | Full system permissions, admin management, role-based access |
| Enum Package | `PaymentMethod`, `PaymentStatus`, `RequestStatus`, `SessionStatus`, `SkillLevel` |

### ✅ Phase 2 — Social & Booking Features *(Parth Patel · Kelvin Idoko)*

| Feature | Description |
|---|---|
| Friend Requests | Send, accept, or decline requests — PENDING → ACCEPTED → DECLINED |
| Player Search | Search by sport, skill level, and city |
| Chat | Text messaging between players with ACCEPTED friend status only |
| File Persistence (Chat & Friends) | All chat and friend request data saved to plain-text files and reloaded on startup |
| Email Notifications | Async notifications on friend request sent and accepted *(Parth Patel)* |
| Team Creation & Management | Create teams, join/leave, captain controls, max 11 members, sport validation |
| Game Scheduling | Schedule sessions, create bookings linked to a team |
| Session Cancellation | Admin cancels sessions with 24h refund rule, email sent async |

### ✅ Phase 3 — Ratings, Payments & Encryption *(Dhruv Patel · Parth Patel · Sahil Maniya)*

| Feature | Description |
|---|---|
| Post-Game Ratings | Rate players 1–5 stars with optional comment after a completed session |
| Session Payments | Record payments (CARD / PAYPAL), PAID → REFUNDED lifecycle |
| Transaction Log | Audit trail linking payment to session and player |
| Encrypted File Persistence | `SignupPersistenceService` + `StorageCrypto` — AES-GCM encrypted player accounts saved to `data/player_signups.txt` *(Parth Patel & Sahil Maniya)* |

### 🔄 Phase 4 — Testing, Integration & Documentation *(All Members)*

| Task | Description |
|---|---|
| Integration Testing | End-to-end flow across all phases |
| Final `Main.java` | Consolidated menus, service wiring, `seedData()`, full integration |
| Bug Fixes & Edge Cases | Null checks, invalid inputs, duplicate entries, empty-list handling |
| Code Review & Cleanup | Remove debug output, standardize formatting |
| Javadoc | Inline documentation on all public methods and classes |
| GitHub Final Merge | All feature branches → `develop` → `main` |

---

## Architecture

```
SportConnect/
│
├── src/
│   ├── Main.java                        # Entry point — menus, service wiring, seedData()
│   │
│   ├── models/
│   │   ├── Player.java                  # Core user model
│   │   ├── Admin.java                   # extends Player — adminLevel, role override
│   │   ├── FriendRequest.java           # Request model — PENDING / ACCEPTED / DECLINED
│   │   ├── Chat.java                    # Message model — sender, receiver, timestamp
│   │   ├── Team.java                    # Team model — captain, members, sport
│   │   ├── GameSession.java             # Session model — SCHEDULED / COMPLETED / CANCELLED
│   │   ├── Booking.java                 # Links team to session
│   │   ├── Rating.java                  # Post-game rating — 1–5 stars
│   │   ├── Payment.java                 # Payment — CARD / PAYPAL, PAID / REFUNDED
│   │   └── Transaction.java             # Audit trail for payments
│   │
│   ├── services/
│   │   ├── AuthService.java             # Register / login / logout / token management
│   │   ├── PlayerService.java           # CRUD for players, search by sport/skill/city
│   │   ├── FriendRequestService.java    # Send / accept / decline requests, file I/O
│   │   ├── ChatService.java             # Send messages, chat history, file I/O
│   │   ├── TeamService.java             # Create / join / leave teams
│   │   ├── GameSessionService.java      # Schedule / cancel / complete sessions
│   │   ├── RatingService.java           # Submit and retrieve ratings
│   │   ├── PaymentService.java          # Process and refund payments
│   │   ├── EmailService.java            # Async Gmail SMTP notifications
│   │   ├── SignupPersistenceService.java # Encrypted flat-file player storage
│   │   └── StorageCrypto.java           # AES-GCM encrypt / decrypt utility
│   │
│   └── enums/
│       ├── PaymentMethod.java           # CARD, PAYPAL
│       ├── PaymentStatus.java           # PAID, REFUNDED
│       ├── RequestStatus.java           # PENDING, ACCEPTED, DECLINED
│       ├── SessionStatus.java           # SCHEDULED, COMPLETED, CANCELLED
│       └── SkillLevel.java              # BEGINNER, INTERMEDIATE, ADVANCED
│
├── data/
│   ├── player_signups.txt               # AES-GCM encrypted player accounts (git-ignored)
│   ├── friend_requests.txt              # Plain-text friend request records
│   └── chats.txt                        # Plain-text chat history
│
└── README.md
```

---

## Class Structure

### Models

| Class | Key Attributes | Key Methods |
|---|---|---|
| `Player` | `playerId`, `name`, `email`, `passwordHash`, `sport`, `skill_level`, `city`, `active` | `getPlayerId()`, `getDisplayName()`, `setSport()`, `isActive()` |
| `Admin` *(extends Player)* | `adminLevel`, `fullName`, `lastLoginAt` | `getRole()`, `cancelSession()`, `processRefund()`, `hasPermission()` |
| `FriendRequest` | `requestID`, `fromPlayerID`, `toPlayerID`, `status`, `dateSent` | `accept()`, `decline()`, `getStatus()` |
| `Chat` | `chatId`, `senderId`, `receiverId`, `message`, `timestamp` | `sendMessage()`, `getHistory()` |
| `Team` | `teamID`, `teamName`, `sport`, `captainID`, `memberIDs`, `MAX_MEMBERS=11` | `addMember()`, `removeMember()`, `setCaptain()` |
| `GameSession` | `sessionID`, `teamID`, `sport`, `date`, `time`, `venue`, `status` | `markCompleted()`, `cancel()`, `getStatusDisplay()` |
| `Booking` | `bookingID`, `teamID`, `sessionID`, `bookingDate`, `status` | `cancelBooking()`, `isActive()` |
| `Rating` | `ratingID`, `raterID`, `ratedID`, `sessionID`, `stars`, `comment`, `submitted` | `validate()`, `submit()`, `getStars()` |
| `Payment` | `paymentID`, `playerID`, `sessionID`, `amount`, `method`, `status` | `processPayment()`, `refund()`, `getStatus()` |
| `Transaction` | `transactionID`, `paymentID`, `playerID`, `sessionID`, `timestamp` | `getTransactionID()` |

### Services

| Service | Responsibility | Owner |
|---|---|---|
| `AuthService` | Register/login/logout for players and admins, token management | Sahil Maniya |
| `PlayerService` | Add, update, deactivate players; search by sport, skill, city | Sahil Maniya |
| `SignupPersistenceService` | Serialize, encrypt, and persist player data to file; reload on startup | Parth Patel & Sahil Maniya |
| `StorageCrypto` | AES-GCM encrypt/decrypt with `ENC1:` prefix; silent fail on bad input | Parth Patel & Sahil Maniya |
| `EmailService` | Async Gmail SMTP — all system email notifications | Parth Patel |
| `FriendRequestService` | Send/accept/decline requests; file persistence; email triggers | Parth Patel |
| `ChatService` | Friends-only messaging; file persistence; chat history | Parth Patel |
| `TeamService` | Create/join/leave teams; sport validation; max 11 members | Kelvin Idoko |
| `GameSessionService` | Create/cancel/complete sessions; bookings; 24h refund logic | Kelvin Idoko |
| `RatingService` | Submit post-game ratings; validate score range | Dhruv Patel |
| `PaymentService` | Record payments; process refunds; email confirmation | Dhruv Patel |

---

## Getting Started

### Prerequisites

- Java 17 or higher
- VS Code with Java Extension Pack (or any Java IDE)
- `jakarta.mail` jar (for email — optional)

### Run the Application

```bash
# Clone the repository
git clone https://github.com/your-repo/sportconnect.git
cd sportconnect

# Compile all Java files
javac -cp . src/**/*.java

# Run
java -cp src Main
```

### Email Setup (Optional)

Email notifications are optional. The app runs normally if SMTP credentials are not configured. To enable email, set these environment variables before running:

```bash
export SMTP_USER=your_gmail@gmail.com
export SMTP_PASS=your_app_password
```

> Gmail App Password required — standard Gmail password will not work.  
> If email fails for any reason, the app continues normally. Email runs on a daemon thread.

---

## How to Use

### Standard User Flow

```
1.  Launch the app → Main Menu
2.  Register (account encrypted and saved to file)
3.  Login → Player Portal
4.  Update Profile  →  set sport, skill level, city
5.  Browse Sport Categories
6.  Search Players  →  filter by sport / skill / city
7.  Send Friend Request  →  email notification sent async
8.  Accept Friend Request  →  email notification sent async
9.  Create or Join a Team  →  max 11 members, sport must match
10. Schedule a Game Session  →  linked to your team
11. Chat with Friends  →  ACCEPTED status required
12. Mark Session as COMPLETED
13. Rate Player  →  1–5 stars, optional comment
14. Make Payment  →  CARD or PAYPAL, email confirmation sent async
```

### Admin Flow

```
1.  Login with username: admin / password: admin123
2.  Admin Portal → View Scheduled Sessions
3.  Select a session to cancel
4.  System checks: current time vs. session date/time
5a. 24+ hours before → REFUND processed, email sent, status → REFUNDED
5b. < 24 hours before → No refund, session → CANCELLED
```

### Super-Admin Flow

```
1.  Login with username: superadmin / password: super123
2.  Full system permissions including admin management
3.  hasPermission() enforced on all Level-2 actions
```

---

## Data Persistence

| File | Content | Format |
|---|---|---|
| `data/player_signups.txt` | All registered player accounts | AES-GCM encrypted, one record per line with `ENC1:` prefix |
| `data/friend_requests.txt` | All friend request records | Plain text |
| `data/chats.txt` | All chat message history | Plain text |

**Important:**
- `player_signups.txt` is listed in `.gitignore` — never committed to the repository
- All data files reload automatically on every application startup
- Logout does **not** clear in-memory data — only removes the session token
- All ArrayLists persist for the full duration of one program run

**Encryption details (`StorageCrypto.java`):**
- Algorithm: AES-GCM (authenticated encryption)
- Encrypted lines are prefixed with `ENC1:`
- Decryption failure is silently skipped — legacy plain-text lines are still parsed

---

## Email Notifications

All emails are sent asynchronously on a daemon thread. If email fails, the application continues normally.

| Trigger | Email Sent |
|---|---|
| Player registers | Welcome email |
| Friend request sent | Notification to receiver |
| Friend request accepted | Confirmation to both players |
| Session payment confirmed | Payment receipt |
| Refund issued | Refund confirmation |
| Session cancelled by admin | Cancellation notice to team |

---

## Admin Controls

### Cancellation & Refund Rules

| Condition | Result |
|---|---|
| Cancelled **24 or more hours** before session | ✅ Refund given — Payment status → `REFUNDED`, email sent |
| Cancelled **less than 24 hours** before session | ❌ No refund — Session marked `CANCELLED` only |

### Admin Roles

| Level | Role | Credentials | Permissions |
|---|---|---|---|
| Level 1 | Admin | `admin` / `admin123` | Cancel sessions, process refunds |
| Level 2 | Super-Admin | `superadmin` / `super123` | Full system access, admin management |

---

## Assumptions & Constraints

### Assumptions

- Admin and Super-Admin accounts are predefined and seeded at startup
- Refund is simulated — no real banking integration
- EmailService is optional — configured via environment variables
- All in-memory data (ArrayLists) persists for the full program run
- Logout removes the session token only — does not clear data

### Constraints

- Console-based application — no GUI
- File-based persistence — no relational database
- No networking or real-time features
- Java standard library only — plus `jakarta.mail` jar for email
- In-memory storage uses `ArrayList` and `HashMap`

---

### Golden Rule

> All ArrayLists are declared **once** in `Main.java` as static variables.  
> They are **never cleared on logout** — logout only removes the session token.  
> This ensures chat history, teams, payments, and ratings persist across all logins during one program run.

---

*Team Kepsa — Parth Patel · Sahil Maniya · Dhruv Patel · Kelvin Idoko*  
*SportConnect · ITC5102 · Humber College · April 2026*

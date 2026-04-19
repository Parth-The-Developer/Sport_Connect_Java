# Sport Connect — Java Project

A collaborative Java application built by a team of four, implementing player authentication, social features, team/game session management, and a rating/payment system.

---

## Team & Branch Assignments

| Member | Branch | Responsibility |
|--------|--------|----------------|
| **Sahil Maniya** | `feature/player-auth` | Player & Admin models, PlayerService, AuthService |
| **Parth Patel** | `feature/social` | FriendRequest, Chat, Message models + services |
| **Kelvin** | `feature/game-team` | Team, GameSession, Booking models + services |
| **Dhruv** | `feature/rating-payment/dhruv` | Rating, Payment, RatingService, PaymentService |

---

## Project Structure

```
Sport_Connect_Java/
├── src/
│   ├── enums/
│   │   ├── PaymentMethod.java     (CARD, PAYPAL, APPLE_WALLET, GOOGLE_WALLET)
│   │   ├── PaymentStatus.java     (PENDING, PAID, REFUNDED)
│   │   ├── RequestStatus.java
│   │   ├── SessionStatus.java
│   │   └── SkillLevel.java
│   ├── model/
│   │   ├── Admin.java
│   │   ├── Booking.java
│   │   ├── Chat.java
│   │   ├── FriendRequest.java
│   │   ├── GameSession.java
│   │   ├── Payment.java
│   │   ├── Player.java
│   │   ├── Rating.java
│   │   ├── SportCategory.java
│   │   └── Team.java
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── ChatService.java
│   │   ├── EmailService.java
│   │   ├── FriendRequestService.java
│   │   ├── GameSessionService.java
│   │   ├── PaymentService.java
│   │   ├── PlayerService.java
│   │   ├── RatingService.java
│   │   ├── SignupPersistenceService.java
│   │   ├── StorageCrypto.java
│   │   └── TeamService.java
│   └── Main.java
├── data/
│   ├── chats.txt
│   ├── friend_requests.txt
│   ├── payments.csv
│   ├── player_postal_codes.csv
│   ├── player_signups.txt
│   └── ratings.csv
├── bin/
├── README.md
└── .gitignore
```

---

## How to Compile and Run

**Compile:**
```bash
javac -d bin src/enums/*.java src/model/*.java src/service/*.java src/Main.java
```

**Run:**
```bash
java -cp bin Main
```

---

## Demo Accounts

These accounts are seeded automatically on startup:

| Role        | Username / Email       | Password |
|-------------|------------------------|----------|
| Admin       | admin                  | admin123 |
| Super Admin | superadmin             | super123 |
| Player      | user@mail.com          | 123      |
| Player      | lien@demo.com          | demo123  |
| Player      | brian@demo.com         | demo123  |
| Player      | shahshree@demo.com     | demo123  |
| Player      | hassana@demo.com       | demo123  |
| Player      | pooja@demo.com         | demo123  |
| Player      | riddhi@demo.com        | demo123  |

---

## Features

### Player Menu
| Option | Feature |
|--------|---------|
| 1 | Update Profile (city, skill level) |
| 2 | Search Players (by name, sport, skill) |
| 3 | Manage Friends (send, accept, reject requests) |
| 4 | Open Chat (only with accepted friends) |
| 5 | Manage Team (create, join, leave, delete) |
| 6 | Manage Sessions (schedule, book, complete, cancel) |
| 7 | Rate a Player |
| 8 | Make Payment |
| 9 | Logout |

### Admin Menu
| Option | Feature |
|--------|---------|
| 1 | List all players |
| 2 | Deactivate a player |
| 3 | View persisted signups (AES-GCM decrypted) |

### Super Admin Menu
| Option | Feature |
|--------|---------|
| 1 | List all players |
| 2 | Delete a player |
| 3 | View persisted signups (AES-GCM decrypted) |
| 4 | View payment records (password protected) |

---

## Rating System (Dhruv)

### How it works
- Players can rate each other from **option 7** in the main menu
- Rating is also triggered automatically after a session is **marked completed** (option 6 → 4)
- Stars accept decimal values e.g. `4.5` — only one decimal point is allowed
- A player cannot rate themselves
- If a player rates the same person again, the **old rating is updated** not duplicated
- Average ratings **persist across restarts** — loaded from CSV on startup

### ratings.csv
Located at `data/ratings.csv`:
```
ratedPlayerName, ratedID, stars, comment, ratedBy
Parth, 1, 4.5, "great batting, nice fielding", Dhruv Patel
```

### Files involved
| File | Purpose |
|------|---------|
| `src/model/Rating.java` | Rating data model |
| `src/service/RatingService.java` | Rating logic, CSV save/load |

---

## Payment System (Dhruv)

### How it works
- Accessible from **option 8** in the player main menu
- Player selects payment method, enters amount, then enters postal code
- On first payment, Canadian postal code is saved for the player
- All future payments validate against the stored postal code — wrong code = declined

### Payment Methods
| Method | Flow |
|--------|------|
| Card | Asks card number, CVV, expiry date → validates → processes |
| Apple Wallet | Sends simulated iPhone prompt → waits 3 seconds → success |
| Google Wallet | Sends simulated Android prompt → waits 3 seconds → success |
| PayPal | Asks PayPal email → waits 3 seconds → success |

### Card Validation
| Card Type  | Starts With | Length |
|------------|-------------|--------|
| Visa       | 4           | 16     |
| Mastercard | 5           | 16     |
| Amex       | 34 or 37    | 15     |

- Expiry date must not be in the past (MM/YY format)
- Test card `1234567890123456` always declines
- Any unsupported card type is declined with a clear message

### Canadian Postal Code Validation
- Format: `A1A 1A1` e.g. `L6M 0Z8`
- First payment: postal code is validated and saved to `data/player_postal_codes.csv`
- Future payments: entered postal code must match the stored one

### payments.csv
Located at `data/payments.csv` — accessible only via Super Admin option 4:
```
paymentID, playerID, playerName, sessionID, amount, method, postalCode, status
PAY-1, 9, Dhruv Patel, MANUAL, 40.0, PAYPAL, L6M 0Z8, PAID
```

### Files involved
| File | Purpose |
|------|---------|
| `src/model/Payment.java` | Payment data model |
| `src/service/PaymentService.java` | Payment logic, validation, CSV save/load |
| `src/enums/PaymentMethod.java` | CARD, PAYPAL, APPLE_WALLET, GOOGLE_WALLET |
| `src/enums/PaymentStatus.java` | PENDING, PAID, REFUNDED |

---

## Data Files

| File | Description | Access |
|------|-------------|--------|
| `data/chats.txt` | Chat message history | Players |
| `data/friend_requests.txt` | Friend request records | Players |
| `data/player_signups.txt` | Encrypted signup records | Admin/Super Admin |
| `data/ratings.csv` | Player ratings | Players |
| `data/payments.csv` | Payment records | Super Admin only |
| `data/player_postal_codes.csv` | Stored postal codes per player | System only |

---

## Branch Strategy

```
main          ← submission-ready code only (final PR at end)
  └── develop ← shared integration branch (everyone merges here)
        ├── feature/player-auth
        ├── feature/social
        ├── feature/game-team
        └── feature/rating-payment/dhruv
```

- **`main`** — never pushed to directly; only updated via a final PR from `develop` at the end.
- **`develop`** — the shared meeting point. Pull from here every morning before coding.
- **`feature/…`** — your personal branch. Experiment freely; open a Pull Request when ready to merge.

---

## Getting Started

### Clone the repo

```bash
git clone https://github.com/Parth-The-Developer/Sport_Connect_Java.git
cd Sport_Connect_Java
```

### Create your feature branch

```bash
git checkout develop
git pull origin develop
git checkout -b feature/YOUR-BRANCH-NAME
```

---

## Daily Git Workflow

### Morning — sync before you code

```bash
git pull origin develop
```

### While coding — commit frequently (every 30–60 min)

```bash
git add .
git commit -m "feat: short description of what you built"
git push origin feature/YOUR-BRANCH-NAME
```

### When your feature is ready — open a Pull Request on GitHub

Target: `feature/YOUR-BRANCH-NAME` → `develop`

---

## Commit Message Format

| Prefix | When to use |
|--------|-------------|
| `feat:` | Adding new functionality |
| `fix:` | Fixing a bug |
| `refactor:` | Restructuring code without changing behaviour |
| `test:` | Adding or updating tests |
| `docs:` | Documentation changes only |

**Examples:**
```
feat: add Rating model and RatingService with CSV persistence
feat: add PaymentService with card, PayPal, Apple/Google Wallet
fix: fix Canadian postal code regex validation
refactor: update Rating to accept float stars instead of int
```

---

## Dependency Order

1. **Sahil** — `Player.java`, `Admin.java`, `PlayerService.java`, `AuthService.java`
2. **Parth** and **Kelvin** — work in parallel after Sahil's PR merges
3. **Dhruv** — finish `markSessionCompleted()` after Kelvin's `GameSession` merges
4. **All members** — build and wire `Main.java` together in Week 4

---

## Resolving Merge Conflicts

```
<<<<<<< HEAD  (YOUR version)
public String displayName = "Player";
=======
public String displayName = "User";
>>>>>>> develop  (TEAMMATE's version)
```

1. Open the file, keep the correct version, delete marker lines
2. Stage and commit:
```bash
git add .
git commit -m "fix: resolve conflict in Player.java"
git push origin feature/YOUR-BRANCH
```

---

## Project Timeline

| Week | Focus |
|------|-------|
| Week 1 | Repo setup · Sahil builds Player/Admin/AuthService · everyone clones & creates branches |
| Week 2 | Parth (social) and Kelvin (game/team) work in parallel after Sahil's PR merges |
| Week 3 | Dhruv finishes rating/payment · all PRs reviewed and merged into `develop` |
| Week 4 | All members build `Main.java` together · final PR: `develop` → `main` |

---

## The 5 Golden Rules

| # | Rule |
|---|------|
| ☀️ 1 | **Pull from `develop` every morning** before coding. |
| 🚫 2 | **Never push directly to `develop` or `main`.** Always use a Pull Request. |
| 💾 3 | **Commit every 30–60 minutes** with a clear message. |
| 🎯 4 | **One thing per commit.** Don't mix unrelated changes. |
| 💬 5 | **When stuck on a conflict, talk to your teammate.** |

---

## Quick Reference

```bash
# Morning routine
git pull origin develop

# After coding
git add .
git commit -m "feat: what you built"
git push origin feature/YOUR-BRANCH

# When ready to merge → open a Pull Request on GitHub
```
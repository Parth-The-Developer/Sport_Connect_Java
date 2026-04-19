# Sport Connect — Java Project

A collaborative Java application built by a team of four, implementing player authentication, social features, team/game session management, and a rating/payment system.

---

## Team & Branch Assignments

| Member | Branch | Responsibility |
|--------|--------|----------------|
| **Sahil Maniya** | `feature/player-auth` | Player & Admin models, PlayerService, AuthService |
| **Parth Patel** | `feature/social` | FriendRequest, Chat, Message models + services |
| **Kelvin** | `feature/game-team` | Team, GameSession, Booking models + services |
| **Dhruv** | `feature/rating-payment` | Rating, Payment, Transaction models + services |

---

## Project Structure

```
src/
├── model/
├── service/
├── enums/
└── Main.java
```

---

## Branch Strategy

```
main          ← submission-ready code only (final PR at end)
  └── develop ← shared integration branch (everyone merges here)
        ├── feature/player-auth
        ├── feature/social
        ├── feature/game-team
        └── feature/rating-payment
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

Use a short prefix to keep history readable:

| Prefix | When to use |
|--------|-------------|
| `feat:` | Adding new functionality |
| `fix:` | Fixing a bug |
| `refactor:` | Restructuring code without changing behaviour |
| `test:` | Adding or updating tests |
| `docs:` | Documentation changes only |

**Examples:**
```
feat: add Player login and signup
fix: resolve null pointer in GameSessionService
refactor: extract payment logic into PaymentService
```

---

## Dependency Order

Some code depends on other people's code. Merge to `develop` in this order:

1. **Sahil** — `Player.java`, `Admin.java`, `PlayerService.java`, `AuthService.java`  
   *(everyone else depends on this — merge to `develop` first)*
2. **Parth** and **Kelvin** — work in parallel after Sahil's PR merges
3. **Dhruv** — finish `markSessionCompleted()` after Kelvin's `GameSession` merges
4. **All members** — build and wire `Main.java` together in Week 4

---

## Resolving Merge Conflicts

A conflict happens when two people edited the same line differently.

**What it looks like in your file:**
```
<<<<<<< HEAD  (YOUR version)
public String displayName = "Player";
=======
public String displayName = "User";
>>>>>>> develop  (TEAMMATE's version)
```

**How to fix it:**
1. Open the file. Decide which version to keep. Delete the other version and all the marker lines (`<<<`, `===`, `>>>`).
2. Stage and commit:
   ```bash
   git add .
   git commit -m "fix: resolve conflict in Player.java"
   ```
3. Push:
   ```bash
   git push origin feature/YOUR-BRANCH
   ```

> **Best prevention:** Pull from `develop` every morning. The more you sync, the smaller the differences, the fewer conflicts you'll see.

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
| ☀️ 1 | **Pull from `develop` every morning** before coding. Skipping this causes conflicts. |
| 🚫 2 | **Never push directly to `develop` or `main`.** Always use a Pull Request. |
| 💾 3 | **Commit every 30–60 minutes** with a clear message. Small, frequent saves. |
| 🎯 4 | **One thing per commit.** Don't mix unrelated changes in a single commit. |
| 💬 5 | **When stuck on a conflict, talk to your teammate.** A 2-minute conversation saves hours of bugs. |

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
[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/vaYWUSuE)

# PickupGame

An Android app for finding and organizing local pickup sports games. Post a game you want to play, see games near you on the map, join up, and chat with the other players.

Built with Jetpack Compose, Firebase Auth + Firestore, and Google Maps.

---

## Quick start for graders

The fastest path to a working app on your machine:

1. Clone the repo and open the project in **Android Studio** (Hedgehog or newer).
2. Let Gradle sync. The first sync downloads all dependencies and may take a few minutes.
3. Pick a device:
   - **Physical Android device** with USB debugging enabled, OR
   - **Emulator** running a **Google Play**-enabled system image (see "Emulator setup" below — this matters).
4. Press Run (▶). The app installs and launches.
5. On the login screen, tap **Sign up** at the bottom, create an account with any email + password (≥ 6 chars), and you're in.

Email/password sign-in works out of the box with no extra configuration. **Use this path to grade** unless you specifically want to test Google Sign-In (which requires the extra step in the "Limitations" section below).

---

## Requirements

- Android Studio Hedgehog (2023.1) or newer
- JDK 17 (bundled with recent Android Studio)
- An Android device or emulator running **Android 8.1 (API 27) or higher**
- Internet connection (the app talks to Firebase)

The Firebase project, Maps API key, and Firestore security rules are all provisioned and committed — you don't need to set up your own Firebase project.

---

## How to use the app

### Sign in
- **Email / password** — tap "Sign up" to create an account, or use existing credentials. This is the recommended path for grading.
- **Continue with Google** — uses the modern Android Credential Manager. See "Limitations" below; this requires extra setup to work on a grader's machine.

### Home screen
Shows all open games near you, sorted by distance.
- The header displays your current city (resolved from your location).
- Use the **search bar** to filter by sport or location text.
- Tap any game card to see details.
- Tap the **floating + button** to create a new game.
- Bottom nav switches between Home and Profile.
- The first time the app opens, Android will ask for **location permission** — grant it so distances and city name work. (The app still functions without it, but games won't be sorted by distance.)

### Create a game
Fill in the form:
- **Sport** — Basketball, Soccer, Volleyball, Tennis, Football, Frisbee, or Pickleball.
- **Location** — tap on the map to drop a pin. The location text auto-fills from reverse geocoding; you can edit it.
- **Date / time** — pickers open native Android dialogs.
- **Max players** — minimum 2.
- **Notes** — optional free text.

Tap **Post Game**. You'll bounce back to the home screen and see your new game in the list.

### Game detail
- Shows the full info for a game, who's joined, and a map of the location.
- **Join / Leave** the game (the creator is automatically a player).
- Tap the share icon to share an invite text via any installed app.
- Tap **Chat** to open the per-game chat room (only available to players who've joined).

### Chat
Real-time chat scoped to a single game. Messages stream live via a Firestore snapshot listener.

### Profile
- Edit your display name.
- See games you've created and games you've joined.
- Sign out.

---

## Limitations & how to fix them

### "Continue with Google" is bound to one developer machine

**Symptom:** tapping "Continue with Google" fails with `NoCredentialException`, even though a Google account is signed in on the device.

**Why:** Android's Credential Manager only mints a Google ID token when the **debug keystore signing the APK** has its SHA-1 fingerprint registered in the Firebase project's OAuth client. The committed `app/google-services.json` registers the SHA-1 of *my* debug keystore (`~/.android/debug.keystore` on my development machine). When you build the project on your machine, Android Studio signs the APK with **your** debug keystore, which has a different SHA-1, and Firebase rejects the token request.

This is a fundamental design of the modern Google Sign-In flow on Android — there's no way to make it "just work" across arbitrary machines without sharing the keystore (which is a bad idea).

**To work around it (only if you need to test Google Sign-In):**

1. On your machine, in the project directory:
   ```
   ./gradlew signingReport
   ```
   Find the `Variant: debug` block and copy the `SHA1` value (e.g. `AB:CD:EF:...`).

2. Email me your SHA-1 and I'll add it to the Firebase project, re-download `google-services.json`, and push an update.

**Alternative: just use email/password sign-in.** It exercises the same `AuthViewModel` code path and the same Firestore writes. All app features are reachable from an email/password account.

### Google Sign-In also requires a Google account on the device

The Credential Manager API does **not** open a browser to sign in to Google — it only offers accounts already on the device.

- **Physical device:** if you're already signed in to a Google account, you're set.
- **Emulator:** you must use a **Google Play**-enabled system image AND have added a Google account to it. See "Emulator setup" below.

If no account is present, Google Sign-In throws `NoCredentialException` regardless of SHA-1 setup.

### Map may not load if the API key is locked down

The Maps API key in `app/build.gradle.kts` is registered with my Google Cloud project. If at the time of grading I've restricted the key to specific package + SHA-1 combinations, the map will silently fail to render on your machine. If you see grey tiles where the map should be, that's why — let me know and I can loosen the restriction temporarily.

---

## Emulator setup (if you're not using a physical device)

The emulator must use a **Google Play**-enabled system image (not "Google APIs" or AOSP) for Firebase, Maps, and Google Sign-In to work properly.

1. Android Studio → **Device Manager** → **Create device**.
2. Pick any phone hardware profile (Pixel 6 is fine).
3. On the system image page, pick a recent API level (API 33+ is fine) and **make sure the row's "Services" column shows "Google Play"** — not "Google APIs". Download it if needed.
4. Finish creating the AVD and boot it.
5. Open the emulator's **Settings → Passwords & accounts → Add account → Google** and sign in. (Only required if you intend to test "Continue with Google".)
6. Run the app from Android Studio.

A non-Play emulator image will fail in subtle ways (Firestore connectivity issues, Google Sign-In refusing to work, etc.) — use a Play image to avoid wasted debugging.

---

## Project structure

```
app/src/main/java/edu/nd/pmcburne/hwapp/one/
├── MainActivity.kt              — Compose Navigation host, sets up screen routes
├── model/
│   ├── Game.kt                  — Firestore game document
│   ├── Message.kt               — Chat message
│   └── User.kt                  — User profile
├── ui/
│   ├── components/              — Shared composables (bottom nav, etc.)
│   ├── screens/                 — One file per screen (Login, Home, Create, Detail, Chat, Profile)
│   └── theme/                   — Compose Material 3 theme
├── util/                        — Helpers (geocoding, distance, share, date format)
└── viewmodel/
    ├── AuthViewModel.kt         — Firebase Auth state + email/Google sign-in
    └── LocationViewModel.kt     — Fused location provider, city name, permission state
```

Firestore collections:
- `users/{uid}` — display name, email, photo URL, createdAt
- `games/{gameId}` — sport, location, lat/lng, dateTime, maxPlayers, players (array of uids), creatorId, creatorName, notes
- `games/{gameId}/messages/{msgId}` — chat messages

---

## Tech stack

- **UI:** Jetpack Compose, Material 3, Compose Navigation
- **Auth:** Firebase Authentication (email/password + Google via Credential Manager)
- **Database:** Cloud Firestore (with offline persistence on by default)
- **Maps:** Google Maps Compose
- **Location:** Google Play Services Fused Location Provider, Android Geocoder for reverse geocoding
- **Min SDK:** 27 (Android 8.1) · **Target SDK:** 36

---

## Troubleshooting

| Symptom | Likely cause | Fix |
|---|---|---|
| "Continue with Google" → `NoCredentialException` | No Google account on device, OR your debug SHA-1 isn't registered in Firebase | Add a Google account in Settings, OR send me your debug SHA-1 |
| Sign-up succeeds but no document in Firestore Console | Console UI lag — refresh the page | Cmd+R on the Firestore Data tab |
| Map shows grey tiles | Maps API key is restricted | See "Limitations" above |
| Emulator builds but app crashes on launch with Play Services errors | Emulator using non-Play system image | Recreate AVD with a Google Play image |
| Build fails complaining about `google-services.json` | File missing — git LFS issue, or accidental gitignore | Confirm `app/google-services.json` exists after clone |

If you hit something not listed, please reach out and include the error message — most Firebase / Credential Manager errors have very specific causes once you see the exception class.

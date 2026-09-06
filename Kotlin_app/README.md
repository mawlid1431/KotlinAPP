# TDM Insight — Android App (Setup & Run Guide)

Native Android app for vancomycin AUC₂₄-guided therapeutic drug monitoring.
Kotlin · Jetpack Compose · Material 3 · MVVM · **Supabase** (backend/database) · **Clerk** (authentication).

Academic project — CDE2313, **Albukhary International University (AIU)**.

> This file is the **hands-on guide**: how to set up the backend, the database and Clerk,
> and how to run the app on a real phone.
> For architecture, diagrams and the PK formulas, see the [root README](../README.md).

---

## Table of Contents

1. [What you need before you start](#1-what-you-need-before-you-start)
2. [Get the code](#2-get-the-code)
3. [Backend setup — Supabase (database)](#3-backend-setup--supabase-database)
4. [Auth setup — Clerk](#4-auth-setup--clerk)
5. [Wire the keys into the app (local.properties)](#5-wire-the-keys-into-the-app-localproperties)
6. [Run on your phone from Android Studio](#6-run-on-your-phone-from-android-studio)
7. [Run from the terminal (Gradle + adb)](#7-run-from-the-terminal-gradle--adb)
8. [Install the prebuilt APK (no Android Studio)](#8-install-the-prebuilt-apk-no-android-studio)
9. [First launch — what to expect](#9-first-launch--what-to-expect)
10. [Verify the backend is really connected](#10-verify-the-backend-is-really-connected)
11. [Troubleshooting](#11-troubleshooting)
12. [Useful commands](#12-useful-commands)

---

## 1. What you need before you start

| Requirement | Version / note |
|---|---|
| **Android Studio** | Ladybug (2024.2) or newer — Hedgehog also works |
| **JDK** | **21** — the project compiles with `sourceCompatibility = 21` and `jvmTarget = "21"`. Android Studio ships a bundled JDK 21; use it. |
| **Android SDK** | compileSdk / targetSdk **35** — install "Android 15" in the SDK Manager |
| **Device** | Physical Android phone on **Android 8.0 (API 26)** or newer, or an emulator |
| **USB cable** | A **data** cable — a charge-only cable will not work |
| **Supabase account** | Free tier — <https://supabase.com> |
| **Clerk account** | Free tier — <https://dashboard.clerk.com> |
| **Internet** | Required — auth and history are cloud-backed |

Quick JDK check inside Android Studio:
**File → Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK → 21**

---

## 2. Get the code

```bash
git clone https://github.com/mawlid1431/KotlinAPP.git
```

```bash
cd KotlinAPP/Kotlin_app
```

> **Important:** the Android project root is `Kotlin_app/`, **not** the repository root.
> In Android Studio choose **Open** and select the **`Kotlin_app`** folder.
> Opening the repo root instead is the number one cause of "no Gradle project found".

Folder map:

```
KotlinAPP/
├── README.md              ← architecture & full documentation
├── Kotlin_app/            ← ★ OPEN THIS FOLDER IN ANDROID STUDIO
│   ├── app/               ← Android module (Kotlin source, Compose UI)
│   ├── supabase/
│   │   └── schema.sql     ← run this in Supabase to create the backend
│   ├── apk/               ← prebuilt release APK
│   ├── secrets.defaults.properties   ← credential template
│   ├── local.properties   ← YOUR real keys (gitignored — you create this)
│   └── gradlew / gradlew.bat
└── landingPage/           ← React + Vite marketing site (separate project)
```

---

## 3. Backend setup — Supabase (database)

The app has **no custom server of its own**. Supabase *is* the backend: a hosted
PostgreSQL database exposed over a REST API, which the app calls directly through the
Supabase Kotlin SDK. You only need to create a project and run one SQL file.

### 3.1 Create the project

1. Go to <https://supabase.com/dashboard> → **New project**.
2. Name it e.g. `tdm-insight`, pick a region close to you (Singapore for Malaysia),
   set a database password, then **Create new project**. Wait ~2 minutes for provisioning.

### 3.2 Create the tables and seed data

1. In the left sidebar open **SQL Editor → New query**.
2. Open `Kotlin_app/supabase/schema.sql` from this repo, copy **the whole file**, paste it in.
3. Click **Run**.

That single script creates:

| Object | Purpose |
|---|---|
| `cases` table | One row per completed TDM calculation — patient inputs plus every PK result |
| `user_profiles` table | Pharmacist profile keyed by the Clerk user ID |
| `cases_user_created_idx` | Index for the "20 most recent cases for this user" lookup |
| RLS policies | Row-level security enabled, with open anon read/insert policies (academic project) |
| Seed rows | Demo profiles and 12 sample cases so the History screen is not empty |

Verify in **Table Editor** — you should now see `cases` and `user_profiles`.

### 3.3 Copy the two keys you need

**Project Settings → API**:

| Value | Looks like | Used as |
|---|---|---|
| **Project URL** | `https://xxxxxxxx.supabase.co` | `SUPABASE_URL` |
| **anon / public key** | `eyJhbGciOi...` (long JWT) | `SUPABASE_ANON_KEY` |

> ⚠️ Use the **anon** key only. **Never** put the `service_role` key in an Android app —
> it bypasses row-level security and would ship inside the APK.

### 3.4 (Optional) show the seed cases under your own account

The seed rows belong to `demo_user_001`. After you sign in once, swap in your real
Clerk user ID (see §10) so they show up in your History screen:

```sql
update cases set user_id = 'user_YOUR_CLERK_ID' where user_id = 'demo_user_001';
```

---

## 4. Auth setup — Clerk

Sign-in, sign-up, email verification and Google OAuth are handled by **Clerk**.
The app talks to Clerk's Frontend API directly over HTTP (`auth/ClerkAuthManager.kt`) —
no backend of your own is involved.

1. Go to <https://dashboard.clerk.com> → **Create application**.
2. Enable **Email + Password** under *User & Authentication → Email, Phone, Username*.
3. Enable **Google** under *User & Authentication → Social Connections → Google*.
   Skip this and the "Continue with Google" button comes back with a Clerk
   *"strategy is not enabled"* error.
4. *(Optional, for Supabase RLS)* **JWT Templates → New template**, name it `supabase`:
   ```json
   { "role": "authenticated", "email": "{{user.primary_email_address}}" }
   ```
5. **API Keys** → copy the **Publishable key** (`pk_test_…` or `pk_live_…`).
   That is your `CLERK_PUBLISHABLE_KEY`.

> ⚠️ Copy only the **publishable** key. The **secret key** (`sk_test_…`) is server-side
> only and must never go into the app.

**OAuth redirect:** the app registers the deep link `tdminsight://oauth-callback` in
`AndroidManifest.xml`, and Clerk redirects back to it after the Google flow. Nothing to
configure — but if you change that scheme, change
`ClerkAuthManager.OAUTH_REDIRECT_URL` to match.

---

## 5. Wire the keys into the app (local.properties)

Credentials are injected at **build time** from `local.properties` into `BuildConfig`,
so no real key ever appears in Kotlin source or in git history.

```
local.properties  (gitignored)
   │  read by app/build.gradle.kts at compile time
   ▼
BuildConfig.CLERK_PUBLISHABLE_KEY / SUPABASE_URL / SUPABASE_ANON_KEY
   │
   ▼
TdmApplication → ClerkAuthManager + SupabaseClientProvider
```

Copy the template and fill in the three values you collected above:

```bash
cp secrets.defaults.properties local.properties
```

Then edit `Kotlin_app/local.properties`:

```properties
CLERK_PUBLISHABLE_KEY=pk_test_your_key_here
SUPABASE_URL=https://your-project-id.supabase.co
SUPABASE_ANON_KEY=eyJhbGciOi...your_anon_key...
```

Android Studio also writes an `sdk.dir=...` line into this file — leave it alone.

> If you skip `local.properties` entirely, the build falls back to the demo project keys
> hard-coded in `app/build.gradle.kts`, so the app still runs — but you will be reading
> and writing the shared demo database instead of your own.

After editing, click **Sync Project with Gradle Files** (the elephant icon 🐘) so the
new `BuildConfig` values are regenerated.

---

## 6. Run on your phone from Android Studio

This is the path for someone who already has Android Studio installed.

### Step 1 — Open the project

**Android Studio → Open → select the `Kotlin_app` folder → OK.**
Wait for "Gradle sync finished" in the status bar. The first sync downloads all
dependencies and can take several minutes.

### Step 2 — Turn on Developer Options on the phone

1. **Settings → About phone**
2. Tap **Build number** **7 times** → *"You are now a developer!"*
3. Go back → **Settings → System → Developer options**
4. Turn on **USB debugging**
   *(on Xiaomi/Redmi also enable **Install via USB** and **USB debugging (Security settings)**)*

### Step 3 — Connect the phone

**Option A — USB cable (simplest)**

1. Plug the phone into the computer with a **data** USB cable.
2. On the phone, pull down the notification and set the USB mode to **File transfer (MTP)**.
3. A dialog appears: **"Allow USB debugging?"** → tick *Always allow from this computer* → **Allow**.
4. The device dropdown in Android Studio's toolbar now shows your phone
   (e.g. *Samsung SM-A536E*).

Confirm from a terminal:

```bash
adb devices
```

Expected — the phone listed as `device` (not `unauthorized`, not `offline`):

```
List of devices attached
R58MA0XXXXX     device
```

**Option B — Wireless, phone and computer on the same Wi-Fi network**

1. Both devices must be on the **same Wi-Fi**.
2. Phone: **Developer options → Wireless debugging → On → Pair device with pairing code**.
   The phone shows an IP:port and a 6-digit code.
3. Android Studio: device dropdown → **Pair Devices Using Wi-Fi** → *Pair using pairing code*
   → enter the code.

   Or from a terminal:

   ```bash
   adb pair 192.168.1.50:37021
   ```

   then

   ```bash
   adb connect 192.168.1.50:5555
   ```

### Step 4 — Run

1. In the toolbar select the **app** run configuration and your phone as the target device.
2. Press **Run ▶** (or `Shift + F10`).
3. Gradle builds the debug APK, installs it on the phone and launches it automatically.

---

## 7. Run from the terminal (Gradle + adb)

Use Android Studio's built-in terminal (**View → Tool Windows → Terminal**, `Alt+F12`) or
any shell — just make sure the working directory is `Kotlin_app/`.

Check the phone is visible:

```bash
adb devices
```

Build and install the debug build in one step:

```bash
./gradlew installDebug
```

On Windows PowerShell / CMD:

```bash
gradlew.bat installDebug
```

Then launch it:

```bash
adb shell am start -n com.aiu.tdminsight/.MainActivity
```

Or build the APK and install it manually:

```bash
./gradlew assembleDebug
```

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Watch the app's logs while it runs:

```bash
adb logcat -s ClerkAuthManager SupabaseRepo
```

Release build (unsigned):

```bash
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release-unsigned.apk`

---

## 8. Install the prebuilt APK (no Android Studio)

For a lecturer or tester who just wants to try the app:

1. Copy `Kotlin_app/apk/TdmInsight.apk` to the phone, or download it from the
   landing page's **Download APK** button.
2. Open it from the phone's Files app.
3. Android asks to allow installs from this source → **Settings → Allow from this source**.
4. Install, then open.

Requires Android 8.0 (API 26) or newer.

---

## 9. First launch — what to expect

1. **Splash screen** → disclaimer (*academic prototype, fictional data only*) → **Accept**.
2. **Sign up** with an email address, or **Continue with Google**.
   For email/password, Clerk emails a 6-digit verification code.
3. **Home → New Case**.
4. Enter a fictional patient: case ID, weight, height, age, sex, serum creatinine —
   CrCl is computed live with Cockcroft–Gault as you type.
5. Choose a workflow (**PRE**, **POST** or **PRE_POST**), then enter dose, infusion
   duration, interval and the concentration samples.
6. **Calculate** → the results screen shows the AUC₂₄ ring, the in-target verdict,
   the recommended dose and the full PK grid (ke, t½, Vd, Vd/kg, CL, Cmin, Cmax).
7. Tap **"How was this calculated?"** for the 4-phase formula walkthrough.
8. Open **History** — the case is there, loaded back from Supabase.

---

## 10. Verify the backend is really connected

**Supabase:** run a calculation, then open **Supabase → Table Editor → `cases`**.
A new row should appear with your inputs and computed results.

**Clerk:** open **Clerk Dashboard → Users**. Your account is listed after sign-up.
Click it and copy the **User ID** (`user_2ab…`) — that is the value written into
`cases.user_id`, and the one to use in the SQL from §3.4.

You can also read it from the logs:

```bash
adb logcat -s SupabaseRepo
```

---

## 11. Troubleshooting

| Symptom | Fix |
|---|---|
| Android Studio says *"no Gradle project"* | You opened the repo root — open the **`Kotlin_app`** folder instead. |
| `Unsupported class file major version` / JDK errors | Set **Gradle JDK = 21** in Settings → Build Tools → Gradle. |
| Phone not listed in `adb devices` | Charge-only cable, USB debugging off, or USB mode not set to *File transfer*. |
| Device shows as `unauthorized` | Unlock the phone and accept the **Allow USB debugging** dialog. Still stuck: `adb kill-server` then `adb start-server`. |
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | An older build with a different signature is installed: `adb uninstall com.aiu.tdminsight`, then reinstall. |
| Sign-in fails immediately | Wrong or missing `CLERK_PUBLISHABLE_KEY` — re-check §5 and re-sync Gradle. |
| *"strategy is not enabled"* on Google sign-in | Enable the **Google** social connection in the Clerk dashboard (§4, step 3). |
| History is empty after a calculation | Check `SUPABASE_URL` / `SUPABASE_ANON_KEY`, confirm `schema.sql` ran, and that the RLS policies exist on `cases`. |
| Network errors on the device | The app needs internet — check Wi-Fi/data. The `INTERNET` permission is already declared in the manifest. |
| Gradle sync fails after editing keys | **File → Sync Project with Gradle Files**, then **Build → Clean Project**. |

---

## 12. Useful commands

All run from `Kotlin_app/`.

| Task | Command |
|---|---|
| List connected devices | `adb devices` |
| Build debug APK | `./gradlew assembleDebug` |
| Build + install debug | `./gradlew installDebug` |
| Build release APK | `./gradlew assembleRelease` |
| Install an APK manually | `adb install -r app/build/outputs/apk/debug/app-debug.apk` |
| Uninstall the app | `adb uninstall com.aiu.tdminsight` |
| Launch the app | `adb shell am start -n com.aiu.tdminsight/.MainActivity` |
| Filtered logs | `adb logcat -s ClerkAuthManager SupabaseRepo` |
| Clean build | `./gradlew clean` |
| Pair over Wi-Fi | `adb pair <ip>:<port>` then `adb connect <ip>:5555` |

---

**Disclaimer:** TDM Insight is an educational tool built for coursework at
Albukhary International University (AIU). Use it with fictional patient data only.
All results must be reviewed by a qualified clinical pharmacist before any dosing decision.

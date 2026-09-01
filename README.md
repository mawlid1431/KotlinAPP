# TDM Insight — Vancomycin Therapeutic Drug Monitoring App

A native Android application for calculating vancomycin pharmacokinetic parameters using
AUC₂₄-guided therapeutic drug monitoring (TDM).  
Built with Kotlin · Jetpack Compose · Material 3 · MVVM · Supabase · Clerk Auth.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Architecture — Layer Diagram](#2-architecture--layer-diagram)
3. [Package Structure](#3-package-structure)
4. [Layer Descriptions](#4-layer-descriptions)
5. [Authentication Flow](#5-authentication-flow)
6. [Calculation Workflow](#6-calculation-workflow)
7. [Screen Navigation Flow](#7-screen-navigation-flow)
8. [Sequence Diagrams](#8-sequence-diagrams)
   - 8.1 App Startup & Auth Gate
   - 8.2 Sign-In
   - 8.3 Running a Calculation (PRE_POST)
   - 8.4 History Screen Load
9. [Database Schema](#9-database-schema)
10. [Credential System](#10-credential-system)
11. [Clinical Reference](#11-clinical-reference)
12. [How to Build & Run](#12-how-to-build--run)

---

## 1. Project Overview

TDM Insight helps clinical pharmacists and students calculate vancomycin pharmacokinetic
parameters at the bedside.  Vancomycin requires individualised dosing because its efficacy and
toxicity are tightly linked to the AUC₂₄/MIC ratio.  The 2020 Rybak guidelines recommend an
AUC₂₄ target of **400–600 mg·h/L**.

The app supports three clinical sampling workflows:

| Workflow | Samples required | Method |
|---|---|---|
| **PRE** | Trough only | Population Vd + Cockcroft–Gault CrCl |
| **POST** | Peak only | Newton–Raphson iterative fit |
| **PRE\_POST** | Trough + Peak | Sawchuk–Zaske two-point log-linear regression |

Every completed calculation is saved to a Supabase PostgreSQL database so the pharmacist
has a full audit trail, and recent cases load automatically in the History screen.

---

## 2. Architecture — Layer Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                        Android App                          │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐   │
│  │                    UI  Layer                         │   │
│  │  Jetpack Compose screens  ·  Material 3 theme        │   │
│  │  AuthScreens · Screens · NavGraph · UserPrefs        │   │
│  └────────────────────┬─────────────────────────────────┘   │
│                       │ observes StateFlow                   │
│  ┌────────────────────▼─────────────────────────────────┐   │
│  │               ViewModel  Layer                       │   │
│  │  AuthViewModel  ·  CaseViewModel  ·  HistoryViewModel│   │
│  └──────┬────────────────────────────────────┬──────────┘   │
│         │ calls                              │ calls        │
│  ┌──────▼──────────┐              ┌──────────▼───────────┐  │
│  │   Auth  Layer   │              │    Domain  Layer      │  │
│  │  ClerkAuthMgr   │              │    VancoEngine        │  │
│  │  AuthRepository │              │    InputValidator     │  │
│  │  AuthState      │              └──────────────────────┘  │
│  └──────┬──────────┘                                        │
│         │ HTTP (Ktor)              ┌──────────────────────┐  │
│         │                         │   Data  Layer        │  │
│  ┌──────▼──────────┐              │  SupabaseRepository  │  │
│  │  Clerk Frontend │              │  SupabaseModels(DTO) │  │
│  │  API  (cloud)   │              │  SupabaseClientProv. │  │
│  └─────────────────┘              └──────────┬───────────┘  │
│                                              │ postgrest-kt │
│                                   ┌──────────▼───────────┐  │
│                                   │  Supabase  (cloud)   │  │
│                                   │  PostgreSQL database │  │
│                                   └──────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

The architecture strictly follows **MVVM** (Model–View–ViewModel):
- The UI never touches the database or engine directly.
- ViewModels hold and expose state as `StateFlow`; screens collect it.
- The domain engine (`VancoEngine`) is pure Kotlin with zero Android imports — it can be
  unit-tested without a device.

---

## 3. Package Structure

```
com.aiu.tdminsight
│
├── TdmApplication.kt              Application singleton — lazy dependency host
├── MainActivity.kt                Single Activity; auth gate; NavGraph entry point
│
├── auth/
│   ├── AuthState.kt               Sealed class: Loading | Unauthenticated | Authenticated | Error
│   ├── ClerkAuthManager.kt        Clerk Frontend API calls via Ktor HTTP (sign-in, sign-up, token refresh)
│   └── AuthRepository.kt         Session persistence (SharedPreferences); facade over ClerkAuthManager
│
├── data/
│   ├── model/
│   │   ├── InputModels.kt         PatientInput · DosingInput · PreSampleInput · PostSampleInput
│   │   └── ResultModels.kt        PkResults · CalculationResult (Success/Failure) · Auc24Verdict
│   │
│   ├── validation/
│   │   ├── InputValidator.kt      Pure-Kotlin field + cross-field rules (errors + warnings)
│   │   └── ValidationResult.kt   FieldResult · ValidationReport
│   │
│   └── supabase/
│       ├── SupabaseClientProvider.kt   Creates SupabaseClient with anon key + Postgrest plugin
│       ├── SupabaseModels.kt           CaseDto (wire-format DTO) ↔ HistoryEntry conversion
│       └── SupabaseRepository.kt       saveCase() · loadRecentCases() (client-side user filter)
│
├── domain/
│   └── engine/
│       └── VancoEngine.kt         calculatePre() · calculatePost() · calculatePrePost()
│
├── viewmodel/
│   ├── AuthViewModel.kt           Exposes authState StateFlow; calls AuthRepository
│   ├── CaseViewModel.kt           Wizard state (CaseUiState); runs engine; fires Supabase save
│   └── HistoryViewModel.kt        Loads cases from Supabase; falls back to demo data
│
└── ui/
    ├── UserPrefs.kt               ThemePref (SYSTEM/LIGHT/DARK) + disclaimerAccepted state
    ├── theme/                     Color · Type · Shape · Theme (Material 3, dark cosmic palette)
    ├── components/
    │   └── Banners.kt             Reusable UI chips (AUC verdict, warning banners)
    ├── navigation/
    │   └── NavGraph.kt            Route constants + TdmNavGraph composable
    └── screens/
        ├── AuthScreens.kt         LoginScreen · SignUpScreen (dark cosmic design)
        └── Screens.kt             All 13 app screens (Splash → Results → History → Settings)
```

---

## 4. Layer Descriptions

### UI Layer
Built entirely with Jetpack Compose.  Every screen is a `@Composable` function that receives
a `NavController` and optionally a shared `ViewModel`.  The dark cosmic design uses a deep
space gradient background with purple/teal accent colours and white CTA buttons throughout.

**Screens (13 total):**

| Route | Screen | Purpose |
|---|---|---|
| `splash` | SplashScreen | 1.8 s animated logo → auto-navigate to Home |
| `home` | HomeScreen | Dashboard: quick start, history preview, settings |
| `new_case` | NewCaseScreen | Enter case ID and patient data |
| `medication_select` | MedicationSelectScreen | Confirm vancomycin selection |
| `workflow_select` | WorkflowSelectScreen | Choose PRE / POST / PRE\_POST |
| `input_form/{workflow}` | InputFormScreen | Enter dosing + concentration samples |
| `review` | ReviewScreen | Summary of all inputs before calculation |
| `calculating` | CalculatingScreen | Progress animation while engine runs |
| `results` | ResultsScreen | PK results, AUC₂₄ verdict chip, recommended dose |
| `explanation` | ExplanationScreen | Formula breakdown and clinical interpretation |
| `engine_error` | ErrorScreen | Friendly error with retry option |
| `history` | HistoryScreen | Last 20 cases loaded from Supabase |
| `settings` | SettingsScreen | Theme toggle (Light / Dark / System) |

### ViewModel Layer
Three `AndroidViewModel` subclasses, all accessing `TdmApplication` singletons without a DI
framework.  State is exposed as `StateFlow` so Compose can collect it with `collectAsState()`.

| ViewModel | Key state | Key actions |
|---|---|---|
| `AuthViewModel` | `authState: StateFlow<AuthState>` | `signIn()` · `signUp()` · `signOut()` |
| `CaseViewModel` | `uiState: StateFlow<CaseUiState>` | `updatePatient/Dosing/Pre/Post()` · `validate()` · `runCalculation()` |
| `HistoryViewModel` | `entries`, `isLoading`, `isLiveData` | `load()` — fetches Supabase, falls back to demo |

`CaseViewModel` is created once inside `TdmNavGraph` and passed explicitly to all wizard
screens, ensuring state survives back-stack navigation without re-creation.

### Domain Layer — `VancoEngine`
A pure Kotlin `object` (singleton) with three public functions — one per workflow.
Zero Android imports; zero coroutines; fully synchronous and unit-testable.

```
calculatePre(input: PreWorkflowInput)       → CalculationResult
calculatePost(input: PostWorkflowInput)     → CalculationResult
calculatePrePost(input: PrePostWorkflowInput) → CalculationResult
```

Each function:
1. Validates inputs via `InputValidator`
2. Runs the pharmacokinetic equations
3. Returns `CalculationResult.Success(PkResults)` or `CalculationResult.Failure(message)`

### Auth Layer — Clerk
The app calls the **Clerk Frontend API** directly over HTTPS using Ktor + OkHttp.
No Clerk Android SDK is used.  The publishable key (`pk_test_...`) is the only Clerk
credential in the Android binary — the secret key never leaves the server.

The `publishableKey` encodes the Frontend API domain in base64:
```
pk_test_cmVhbC1hc3AtNjI1NS5jbGVyay5hY2NvdW50cy5kZXYk
         └── base64 decode ──▶ real-asp-6255.clerk.accounts.dev
```

Session tokens (JWTs) are stored in private `SharedPreferences` and restored on the next
app launch so the user stays logged in.

### Data Layer — Supabase
Uses the official Supabase Kotlin SDK v2.6.1 with only the `postgrest-kt` plugin installed.
The app uses the **anon (public) key** exclusively — the service_role key never enters the
Android binary.

`SupabaseRepository` wraps all database calls:
- `saveCase()` — inserts a `CaseDto` row after a successful calculation
- `loadRecentCases()` — fetches the last 80 rows, filters client-side by `user_id`, returns 20

---

## 5. Authentication Flow

```
App Launch
    │
    ▼
TdmApplication.authRepository.savedSession()
    │
    ├── Session found in SharedPreferences
    │       └──▶ AuthState.Authenticated  ──▶  skip login gate
    │
    └── No session (first run / signed out)
            │
            ▼
        CLERK_PUBLISHABLE_KEY blank?
            │
            ├── YES  (local.properties not filled)
            │    └──▶ isConfigured = false  ──▶  bypass auth gate entirely
            │         (dev / test mode — app runs without login)
            │
            └── NO   (key is present)
                 └──▶ Show LoginScreen
                          │
                          ├── User taps "Sign Up"  ──▶  SignUpScreen
                          │       │
                          │       └── POST /v1/client/sign_ups (Clerk)
                          │               ├── Success ──▶ save session ──▶ TdmNavGraph
                          │               └── Failure ──▶ show error message
                          │
                          └── User taps "Sign In"
                                  │
                                  └── POST /v1/client/sign_ins (Clerk)
                                          ├── status = "complete"
                                          │     └── extract JWT + user_id + session_id
                                          │           └── SharedPreferences.save()
                                          │                 └── AuthState.Authenticated
                                          │                       └── TdmNavGraph
                                          └── status ≠ "complete"  ──▶  AuthState.Error
```

---

## 6. Calculation Workflow

```
WorkflowSelectScreen  ─── user picks PRE / POST / PRE_POST
         │
         ▼
InputFormScreen  ─── user enters dosing + concentration fields
         │
         ▼
ReviewScreen  ─── summary shown; user confirms
         │
         ▼
CaseViewModel.runCalculation()
         │
         ├─ 1. validate()  ──▶  InputValidator checks all fields
         │       └── errors?  ──▶  stay on ReviewScreen, show banner
         │
         ├─ 2. isCalculating = true  ──▶  CalculatingScreen shown
         │
         ├─ 3. VancoEngine.calculate[Pre|Post|PrePost]()
         │       │
         │       ├── PRE:      CrCl (Cockcroft–Gault) ──▶ CL ──▶ Vd (pop 0.7 L/kg)
         │       │              ──▶ ke · t½ · AUC₂₄ · recommended dose
         │       │
         │       ├── POST:     Newton–Raphson fit for ke from peak sample
         │       │              ──▶ Vd (pop) ──▶ CL ──▶ AUC₂₄ · recommended dose
         │       │
         │       └── PRE_POST: Sawchuk–Zaske  ke = ln(Cpost/Cpre) / Δt
         │                      ──▶ Vd (from infusion model) ──▶ CL ──▶ AUC₂₄
         │
         ├─ 4. CalculationResult.Success  ──▶  ResultsScreen
         │       └── (fire-and-forget) SupabaseRepository.saveCase()
         │
         └─ 5. CalculationResult.Failure  ──▶  ErrorScreen
```

---

## 7. Screen Navigation Flow

```
[First launch only]
        │
        ▼
  DisclaimerScreen ──(accepted)──▶ Auth Gate
                                       │
                              ┌────────▼────────┐
                              │   Auth Gate      │
                              │ (MainActivity)   │
                              └────────┬─────────┘
                                       │ authenticated / no key
                                       ▼
                                  SplashScreen (1.8 s)
                                       │
                                       ▼
                                  HomeScreen ◄────────────────────────┐
                                  /         \                         │
                            [New Case]   [History]   [Settings]       │
                                │             │           │           │
                                ▼             ▼           ▼           │
                         NewCaseScreen  HistoryScreen SettingsScreen  │
                                │                                     │
                                ▼                                     │
                       MedicationSelectScreen                         │
                                │                                     │
                                ▼                                     │
                       WorkflowSelectScreen                           │
                                │                                     │
                                ▼                                     │
                       InputFormScreen                                │
                                │                                     │
                                ▼                                     │
                       ReviewScreen                                   │
                                │                                     │
                                ▼                                     │
                       CalculatingScreen                              │
                          /         \                                 │
                    [Success]      [Failure]                          │
                        │               │                            │
                        ▼               ▼                            │
                  ResultsScreen    ErrorScreen ──[retry]──▶ Input    │
                        │                                            │
                  [Explanation]  ─────────────────────────────────── │
                        ▼                                            │
                 ExplanationScreen                                   │
                        │                                            │
                  [Back to Home] ─────────────────────────────────── ┘
```

---

## 8. Sequence Diagrams

### 8.1 — App Startup & Auth Gate

```
User         MainActivity      AuthViewModel      SharedPreferences    TdmNavGraph
  │               │                  │                   │                  │
  │──[launch]────►│                  │                   │                  │
  │               │──new()──────────►│                   │                  │
  │               │                  │──savedSession()──►│                  │
  │               │                  │◄──session/null────│                  │
  │               │◄──authState──────│                   │                  │
  │               │                  │                   │                  │
  │               │  [no session]    │                   │                  │
  │◄──LoginScreen─│                  │                   │                  │
  │               │                  │                   │                  │
  │  [has session]│                  │                   │                  │
  │               │─────────────────────────────────────────────────────────►│
  │◄──TdmNavGraph─│                  │                   │                  │
```

### 8.2 — Sign-In

```
User       LoginScreen    AuthViewModel    AuthRepository    ClerkAuthManager   Clerk API
  │              │               │                │                 │               │
  │──[email+pw]─►│               │                │                 │               │
  │              │──signIn()────►│                │                 │               │
  │              │               │──signIn()─────►│                 │               │
  │              │               │                │──signIn()──────►│               │
  │              │               │                │                 │─POST sign_ins─►│
  │              │               │                │                 │◄──response─────│
  │              │               │                │                 │                │
  │              │               │                │◄──ClerkResult.Success            │
  │              │               │                │  (userId,email,token,sessionId)  │
  │              │               │                │──saveSession()──►SharedPrefs     │
  │              │               │◄──Authenticated│                 │               │
  │              │◄──authState───│               │                 │               │
  │◄──TdmNavGraph│               │                │                 │               │
```

### 8.3 — Running a PRE_POST Calculation

```
User     InputFormScreen   CaseViewModel    InputValidator    VancoEngine    SupabaseRepository
  │             │                │                │                │                │
  │─[fill form]►│                │                │                │                │
  │             │──updatePre()──►│                │                │                │
  │             │──updatePost()─►│                │                │                │
  │             │                │                │                │                │
  │─[Calculate]►│                │                │                │                │
  │             │──runCalc()────►│                │                │                │
  │             │                │──validate()───►│                │                │
  │             │                │◄──report.OK────│                │                │
  │             │                │                │                │                │
  │             │                │──calculatePrePost()────────────►│                │
  │             │                │                │    ke = ln(Cpost/Cpre)/Δt       │
  │             │                │                │    Vd, CL, AUC₂₄, recDose      │
  │             │                │◄──Success(PkResults)────────────│                │
  │             │                │                │                │                │
  │             │◄──navigate to CalculatingScreen │                │                │
  │             │◄──navigate to ResultsScreen─────│                │                │
  │             │                │                │                │                │
  │             │                │──saveCase()─────────────────────────────────────►│
  │             │                │  (fire-and-forget, failure silent)               │
  │             │                │                │                │──INSERT cases──►Supabase
  │             │                │                │                │◄──OK────────────│
```

### 8.4 — History Screen Load

```
User      HistoryScreen    HistoryViewModel    SupabaseRepository    Supabase DB
  │             │                 │                   │                   │
  │─[navigate]─►│                 │                   │                   │
  │             │──init: load()──►│                   │                   │
  │             │                 │──loadRecentCases()►│                   │
  │             │                 │                   │──SELECT * FROM cases ORDER BY created_at DESC LIMIT 80─►│
  │             │                 │                   │◄──rows─────────────────────────────────────────────────│
  │             │                 │                   │  .filter { it.userId == loggedInUserId }               │
  │             │                 │                   │  .take(20)                                             │
  │             │                 │◄──List<HistoryEntry>│                   │
  │             │                 │  [if empty → DEMO_ENTRIES fallback]    │                   │
  │             │◄──entries StateFlow updated          │                   │
  │◄──case cards rendered          │                   │                   │
```

---

## 9. Database Schema

### Table: `cases`
Primary data store.  One row per completed calculation.  Written by `SupabaseRepository.saveCase()`.

| Column | Type | Description |
|---|---|---|
| `id` | uuid PK | Auto-generated |
| `user_id` | text | Clerk user ID |
| `created_at` | timestamptz | Timestamp of calculation |
| `case_label` | text | Patient/case identifier entered by the user |
| `workflow` | text | `PRE` / `POST` / `PRE_POST` |
| `weight_kg` | float8 | Patient weight kg |
| `age_years` | int | Patient age years |
| `is_male` | boolean | Biological sex (for Cockcroft–Gault) |
| `scr_umol_l` | float8 | Serum creatinine µmol/L |
| `dose_mg` | float8 | Prescribed dose mg |
| `interval_hours` | float8 | Dosing interval h |
| `infusion_duration_hours` | float8 | Infusion duration h |
| `pre_conc_mg_l` | float8? | Measured trough concentration mg/L |
| `pre_time_h` | float8? | Hours before next dose the trough was drawn |
| `post_conc_mg_l` | float8? | Measured peak concentration mg/L |
| `post_time_h` | float8? | Hours after end of infusion the peak was drawn |
| `ke_per_hour` | float8? | Elimination rate constant h⁻¹ |
| `half_life_hours` | float8? | t½ h |
| `vd_l` | float8? | Volume of distribution L |
| `vd_l_per_kg` | float8? | Vd L/kg |
| `clearance_l_per_hour` | float8? | Vancomycin clearance L/h |
| `auc24` | float8? | AUC₂₄ mg·h/L |
| `recommended_dose_mg` | float8? | Dose to achieve AUC₂₄ = 500 mg·h/L |
| `c_min` | float8? | Projected trough mg/L |
| `c_max` | float8? | Projected peak mg/L |

### Table: `user_profiles`
Optional pharmacist profile linked to Clerk user ID.  Not yet written by the app automatically.

| Column | Type | Description |
|---|---|---|
| `user_id` | text PK | Clerk user ID |
| `display_name` | text | Full name |
| `institution` | text | Hospital / university |
| `department` | text | Pharmacy / Clinical Pharmacy |
| `role` | text | `student` / `pharmacist` / `doctor` |
| `created_at` | timestamptz | Row creation time |
| `updated_at` | timestamptz | Last update time |

Row-level security is enabled on both tables.  The Android app uses only the anon key;
all reads and writes are permitted via open RLS policies (suitable for an academic project).

---

## 10. Credential System

Credentials are injected at **build time** from `local.properties` (gitignored) into
`BuildConfig` fields.  No real secret ever appears in Kotlin source or in git history.

```
local.properties  (gitignored — never committed)
    │
    │ read by app/build.gradle.kts at compile time
    ▼
BuildConfig.CLERK_PUBLISHABLE_KEY   ← safe for client apps (public key)
BuildConfig.SUPABASE_URL            ← safe (your project URL is not secret)
BuildConfig.SUPABASE_ANON_KEY       ← safe (read-only public key)
    │
    ▼
TdmApplication  (reads BuildConfig, creates singletons)
    ├── ClerkAuthManager(publishableKey)
    └── SupabaseClientProvider.create(url, anonKey)
```

`secrets.defaults.properties` is committed to git with placeholder values so the
project structure is visible to collaborators without exposing real keys.

**Keys that must NEVER enter the Android app:**
- Clerk secret key (`sk_test_...`) — server-side only
- Supabase service_role key — server-side only
- Clerk JWKS private key — server-side only

---

## 11. Clinical Reference

| Parameter | Formula / Source |
|---|---|
| CrCl (mL/min) | Cockcroft–Gault: `(140−age) × weight × sex_factor / (72 × SCr_mg/dL)` |
| Clearance (L/h) | `CrCl × 0.06` |
| Population Vd | `0.7 L/kg × weight` |
| ke (PRE) | `CL / Vd` |
| ke (POST) | Newton–Raphson iteration on one-compartment infusion model |
| ke (PRE_POST) | Sawchuk–Zaske: `ln(Cpost / Cpre) / (tpre − tpost)` |
| t½ | `ln(2) / ke` |
| AUC₂₄ | `(Dose / τ) × 24 / CL` |
| Recommended dose | `AUC₂₄_target × CL × τ / 24`  (target = 500 mg·h/L) |

**AUC₂₄ target:** 400–600 mg·h/L per Rybak MJ et al., *Am J Health-Syst Pharm* 2020.

> **Disclaimer:** This application is an educational tool.  All results must be reviewed
> by a qualified clinical pharmacist before any dosing decision is made.

---

## 12. How to Build & Run

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17
- Android device or emulator (API 26+)

### 1. Clone the repository
```bash
git clone <your-repo-url>
cd application/Kotlin_app
```

### 2. Add credentials to `local.properties`
Copy `secrets.defaults.properties` → `local.properties` and fill in real values:
```properties
CLERK_PUBLISHABLE_KEY=pk_test_...
SUPABASE_URL=https://your-project-id.supabase.co
SUPABASE_ANON_KEY=eyJ...
```

### 3. Run the database schema
Open the Supabase SQL Editor and run `supabase/schema.sql`.  This creates the `cases`
and `user_profiles` tables and inserts 12 seed cases for testing.

### 4. Build & install
```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

Or press **Run ▶** in Android Studio.

### 5. First launch
1. Accept the disclaimer
2. Sign up with an email address (Clerk handles verification)
3. Open the **New Case** wizard and run a PRE\_POST calculation
4. Check the **History** screen — your case will appear, loaded live from Supabase

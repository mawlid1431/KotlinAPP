# Case Study Analysis — TDM Insight

**Course:** CDE2313 Mobile Application Development  
**Project:** Vancomycin Therapeutic Drug Monitoring Calculator  
**Student:** Mowlid Haibe | mowlid.haibe@student.aiu.edu.my  
**Lecturer:** Ts Mohd Zulkifli Mohd Zaki  
**Date:** September 2026

---

## 1. Problem Identification

### 1.1 Clinical Context

Vancomycin is a glycopeptide antibiotic used as first-line treatment for methicillin-resistant *Staphylococcus aureus* (MRSA) infections. Its narrow therapeutic index demands precise dosing:

- **Sub-therapeutic:** Treatment failure — MRSA not cleared, infection progresses
- **Supra-therapeutic:** Nephrotoxicity (acute kidney injury), ototoxicity

The **2020 Rybak Consensus Guidelines** revised the target monitoring parameter from trough concentration alone to **AUC₂₄ (Area Under the Concentration-Time Curve over 24 hours)**, with a target of **400–600 mg·h/L**. This shift requires more complex calculations than simple trough monitoring.

### 1.2 Workflow Challenge

Three distinct clinical scenarios require different pharmacokinetic approaches:

| Scenario | Method | Inputs Required |
|----------|--------|----------------|
| Only trough sample available | **PRE** — Population Vd + CrCl | Patient demographics + trough |
| Only peak sample available | **POST** — Newton-Raphson iterative | Patient demographics + peak |
| Both trough and peak available | **PRE+POST** — Sawchuk-Zaske regression | Patient demographics + trough + peak |

Currently, pharmacists perform these calculations manually using paper worksheets or general-purpose calculators — with no input validation, no guidance on which formula to use, and no audit trail.

### 1.3 Gap Analysis

| Problem | Current Situation | Impact |
|---------|-------------------|--------|
| Manual formula selection | Pharmacist must know which method to use | Wrong method → wrong dose |
| No input validation | Any value accepted | Clinically impossible inputs → calculation error |
| No cross-field consistency | Timing conflicts not detected | ke calculation becomes negative |
| No audit trail | Paper worksheets lost | Quality assurance impossible |
| No mobile tool | Desktop-only or web-based alternatives | Not usable at bedside |

---

## 2. Requirements Analysis

### 2.1 Functional Requirements

**FR-01:** The application shall implement PRE, POST, and PRE+POST vancomycin TDM workflows.  
**FR-02:** The application shall collect patient demographics: weight (kg), age (years), sex, serum creatinine (µmol/L).  
**FR-03:** The application shall collect dosing data: dose (mg), interval (hours), infusion duration (hours).  
**FR-04:** The application shall collect concentration sample data appropriate to the selected workflow.  
**FR-05:** The application shall validate all inputs with specific field-level error messages.  
**FR-06:** The application shall calculate and display: ke, t½, Vd, Vd/kg, CL, AUC₂₄, recommended dose, Cmin, Cmax.  
**FR-07:** The application shall classify AUC₂₄ as below target / in target / above target with a recommended dose adjustment.  
**FR-08:** The application shall display a step-by-step explanation of the calculation.

### 2.2 Non-Functional Requirements

**NFR-01:** The calculation engine shall be pure Kotlin with no Android framework imports (testability).  
**NFR-02:** No business logic shall exist in any `@Composable` function (MVVM compliance).  
**NFR-03:** All credentials shall be injected at build time from `local.properties` (security).  
**NFR-04:** The UI shall implement Material 3 with full light/dark theme support.  
**NFR-05:** The application shall handle missing Supabase/Clerk credentials gracefully (offline mode).

---

## 3. Solution Design

### 3.1 Architecture Decision: MVVM

MVVM was selected because:
- Jetpack Compose natively integrates with StateFlow via `collectAsState()`
- ViewModels survive configuration changes (screen rotation)
- Pure domain layer is independently unit-testable
- Avoids the "massive Activity" anti-pattern

### 3.2 Calculation Engine Design

`VancoEngine` is implemented as a Kotlin `object` (singleton) in `domain/engine/VancoEngine.kt`:

```kotlin
object VancoEngine {
    fun calculatePre(input: VancoInput): CalculationResult
    fun calculatePost(input: VancoInput): CalculationResult
    fun calculatePrePost(input: VancoInput): CalculationResult
}
```

Returning `CalculationResult` (sealed class with `Success` and `Failure` variants) allows the ViewModel to handle errors without exceptions.

### 3.3 Pharmacokinetic Formulae

**PRE Workflow (Cockcroft-Gault + Population Vd):**
```
CrCl (ml/min) = [(140 - age) × weight × sex_factor] / (0.815 × SCr_µmol/L)
CL (L/h) = CrCl × 0.0592
ke (h⁻¹) = CL / Vd   where Vd = 0.7 L/kg × weight
AUC₂₄ = Dose / (CL × interval)
```

**PRE+POST Workflow (Sawchuk-Zaske):**
```
ke = ln(Cmax / Cmin) / (t_Cmax_to_Cmin)
t½ = ln(2) / ke
Vd = (Dose/tinf) × (1 - e^(-ke×tinf)) / [ke × (Cpeak - Ctrough×e^(-ke×tinf))]
CL = ke × Vd
AUC₂₄ = Dose × 24 / (CL × interval)
```

**POST Workflow (Newton-Raphson):**
```
Iteratively solves for ke from a single peak measurement,
using the same Vd and timing relationships as PRE+POST.
```

### 3.4 Validation Strategy

`InputValidator` implements four validation functions returning `ValidationReport`:

- **Errors** (blocking): Missing required fields, physically impossible values (negative weight), values that would produce ln(0) or division by zero
- **Warnings** (advisory): Clinically unusual but not impossible values (very high dose, very old patient)
- **Cross-field validation**: Sampling time consistency (Cmax time must be after end of infusion; Cmin time must be after Cmax; total time must fit within dosing interval)

---

## 4. Implementation Highlights

### 4.1 Key Technical Decisions

| Decision | Chosen Approach | Rationale |
|----------|----------------|-----------|
| DI | Manual via TdmApplication | No Hilt/Koin needed for 3 infrastructure objects |
| State management | StateFlow + collectAsState | First-class Compose integration |
| Navigation | NavGraph-scoped CaseViewModel | State preserved during back-navigation |
| Credential management | BuildConfig injection from local.properties | Secrets never in source code |
| Supabase | postgrest-kt only (no GoTrue) | Avoids multiplatform resolution issues |
| Auth | Clerk Frontend REST (no SDK) | Explicit HTTP calls, no hidden SDK behaviour |

### 4.2 Security Model

```
local.properties (GITIGNORED)
    SUPABASE_URL=...
    SUPABASE_ANON_KEY=...       ← safe for client (RLS enforced)
    CLERK_PUBLISHABLE_KEY=...   ← safe for client (pk_test_...)
    
         ↓ build.gradle.kts reads → injects into ↓
    
BuildConfig.SUPABASE_URL         ← in APK binary (anon key only)
BuildConfig.SUPABASE_ANON_KEY    ← in APK binary (anon key only)
BuildConfig.CLERK_PUBLISHABLE_KEY ← in APK binary (publishable key only)

NEVER in APK:
    service_role key  ← full database access (server-only)
    sk_test_...       ← Clerk secret key (server-only)
```

---

## 5. Testing

### 5.1 Unit Test Coverage

| Module | Tests | Status |
|--------|-------|--------|
| VancoEngine.calculatePre | 4 | PASS |
| VancoEngine.calculatePost | 3 | PASS |
| VancoEngine.calculatePrePost | 4 | PASS |
| InputValidator (patient) | 6 | PASS |
| InputValidator (dosing) | 5 | PASS |
| InputValidator (samples) | 7 | PASS |
| Cross-field timing validation | 4 | PASS |

### 5.2 Clinical Validation

Sample PRE+POST calculation verified against manual computation:
- Patient: 68 kg, 62 years, male, SCr 98 µmol/L
- Dose: 1000 mg q12h, 1h infusion
- Cmin (pre): 12.5 mg/L at 10.5h before dose
- Cmax (post): 26.0 mg/L at 2.0h after infusion
- Expected ke: 0.0769 h⁻¹ ✓
- Expected AUC₂₄: 487.2 mg·h/L (IN TARGET) ✓
- Expected Recommended Dose: ~978 mg ✓

---

## 6. Reflection

### 6.1 What Worked Well

- MVVM architecture made the codebase highly maintainable; adding a new screen required no changes to VancoEngine or repositories
- The sealed `CalculationResult` type eliminated null-pointer risks in the ViewModel result handling
- Material 3 theming system made light/dark/system theme switching trivially easy once Color.kt was properly structured

### 6.2 Challenges Encountered

- **Supabase postgrest-kt v2 DSL**: The filter API changed between versions; worked around by fetching more rows and filtering client-side
- **Clerk integration without SDK**: Required reading the Clerk Frontend API documentation carefully to understand the sign-in flow and session token extraction
- **`internal` visibility modifier**: A late-stage refactor moved `HistoryEntry` to the UI package and marked it `internal`, which caused compilation errors in the ViewModel; fixed by marking ViewModel properties `internal`

### 6.3 Lessons Learned

- Always define a clear credential security model before starting implementation
- Kotlin's type system (sealed classes, data classes, `internal`) actively prevents categories of errors — use them deliberately
- Separating the calculation engine from all Android framework code paid dividends in both testability and debuggability

---

## 7. References

- Rybak, M.J., et al. (2020). Vancomycin consensus guidelines. *AJHP, 77*(11), 835-864.
- Cockcroft, D.W., & Gault, M.H. (1976). CrCl estimation. *Nephron, 16*(1), 31-41.
- Sawchuk, R.J., & Zaske, D.E. (1976). PK dosing regimens. *J. Pharmacokinetics Biopharmaceutics, 4*(2), 183-195.
- Android Developers. (2024). Architecture guide. developer.android.com/topic/architecture
- Supabase. (2024). Kotlin SDK. github.com/supabase-community/supabase-kt

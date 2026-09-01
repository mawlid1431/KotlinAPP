# Build Prompt for Claude Code — TDM Insight

Paste this into Claude Code in your project's root folder (after `PROJECT_RULES.md` and your
design attachment are already in the repo, or attached to the session).

---

I'm building **TDM Insight**, a native Android app (Kotlin + Jetpack Compose + Material 3) for my
university mobile app development course. I've attached:

1. `PROJECT_RULES.md` — the full spec: tech stack, mandatory functional requirements from the
   lecturer's case study, the assessment rubric requirements, GitHub repo structure, and
   engineering rules.
2. My **design reference** (screens/mockups and/or a brand style guide) — this defines the exact
   visual style, colors, typography, spacing, and component look for the app in both light and
   dark mode.

**Read `PROJECT_RULES.md` in full before writing any code, and treat it as binding.** Then look at
the attached design reference and treat its visual choices as the authoritative brand/style system
for every screen you build — colors, type scale, spacing, corner radii, iconography, and component
styling must match what's shown there. If a screen or UI state isn't explicitly covered in the
design attachment, extend the same design language consistently rather than inventing a new style.

## What I need you to do, in order

1. **Set up the project structure first**, matching the repository layout in
   `PROJECT_RULES.md` Section 4 exactly (`app/`, `docs/`, `screenshots/`, `apk/`,
   `presentation/`, `ai/`, `assets/`, plus root `README.md`, `LICENCE`, `.gitignore`). Initialize
   git if it isn't already, and make an initial commit for the scaffold.

2. **Set up the Compose + Material 3 theme** from the attached design reference: create
   `Color.kt`, `Type.kt`, `Shape.kt`, and a `Theme.kt` with both light and dark color schemes.
   Confirm the theme renders correctly in both modes with a simple preview before moving on to
   real screens.

3. **Build the architecture skeleton** per `PROJECT_RULES.md` Section 1:
   `ui/`, `viewmodel/`, `domain/engine/`, `data/model/`, `data/validation/`. Set up the base
   navigation graph (Compose Navigation) connecting all screens as placeholders first, so the
   full flow is wired end-to-end before any screen is fully built out.

4. **Build the calculation engine and validators first**, before polishing any UI — this is the
   highest-weighted, highest-risk part of the project. For the Vancomycin Pre, Post, and
   Pre+Post workflows:
   - Implement the pharmacokinetic formulas using lecturer-approved, authoritative sources (do
     not invent formulas — ask me to confirm the exact equations/constants if you're not certain,
     and note the source in a code comment next to each formula).
   - Implement the full validator set: required fields, numeric/unit validation, range checks,
     cross-field checks (e.g., timing relationships), and protection against divide-by-zero /
     invalid log operations.
   - Write JUnit unit tests covering normal cases, edge cases, and the specific error-protection
     cases before moving on.

5. **Then build out each screen**, following the screen list and states from my UI design brief
   (splash, home, new case, medication selection, workflow selection, the three dynamic input
   forms with their error/warning/valid states, review, loading, results, the step-by-step
   explanation screen, settings, disclaimer/about, and empty/error states) — matching the attached
   design pixel-for-pixel where possible, and wiring each screen to its ViewModel and the engine.

6. **Enforce the "no logic in Composables" rule strictly** — if you catch yourself putting a
   calculation or validation rule inside a Composable or ViewModel, stop and move it into
   `domain/engine/` or `data/validation/` instead.

7. **Commit as you go**, in small, meaningful, individually-scoped commits with clear messages
   (per `PROJECT_RULES.md` Section 4) — not one giant commit at the end.

8. **Keep me updated on AI usage** — briefly flag any significant decision you made or code you
   generated that I should log in `ai/AI_Usage_Log.pdf` per Section 6, so I can keep that file
   current as we go rather than reconstructing it later.

## Working style

- Ask me directly if the case study, the assessment instructions, or the design reference leave
  something genuinely ambiguous (e.g., an exact clinical formula or constant) — don't guess on
  anything clinical.
- Keep the codebase clean and commented enough that I can explain any part of it in my individual
  technical viva.
- Prioritize getting the engine correct and the three workflows fully functional over adding any
  optional enhancements (history, camera/OCR, graphs, export) — only touch those after the core
  scope is solid, tested, and matches the design.

Let's start with step 1: set up the repository structure and initial commit, then move to the
Compose theme from my design reference.

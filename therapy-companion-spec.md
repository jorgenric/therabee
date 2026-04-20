# Therapy Companion

*A personal physical therapy management application*

**Software Specification · Version 1.3 · Android APK (Sideloaded)**

- **Classification:** Personal / Home Health
- **Platform:** Android 10+ (API 29+) · Sideloaded APK
- **Distribution:** Direct install, no Play Store
- **Status:** In development
- **Date:** April 2026 (v1.2 revision)

---

## Table of Contents

1. Purpose & Philosophy — §1
2. User Profile & Context — §2
3. Core Design Principles — §3
4. Application Architecture — §4
5. Screen & Feature Specifications — §5
6. Exercise & Session Engine — §6
7. Notifications & Reminders — §7
8. Data Model — §8
9. Motivation & Wellbeing Features — §9
   - 9.4 · Humble Brag
10. Backup, Restore & Update Safety — §10
11. Technical Requirements — §11
12. Out of Scope — §12
13. Future Considerations — §13
14. Revision Notes (v1.1, v1.2, v1.3) — §14

---

## §1 Purpose & Philosophy

### Why This App Exists

This application is built for a single person navigating a large, multi-system physical therapy regimen. The volume of prescribed exercises is not the problem — the *cognitive and emotional weight* of managing them is. When the list feels infinite and unstructured, the most common outcome is paralysis: nothing gets done.

> **Core Problem Statement**
>
> The patient has a large number of physical therapy exercises spanning multiple body systems. The sheer number feels overwhelming, causing her to avoid starting any of them. This app must reduce that barrier to zero — making it easier to start *something* than to do nothing.

The philosophy of this application can be summarized in one sentence: **make the next right step obvious and achievable, never overwhelming.** Every design decision should be evaluated against this principle.

---

## §2 User Profile & Context

### Understanding the User

#### Primary User

One specific individual managing chronic, multi-system health conditions with a significant prescribed home exercise program. She is comfortable with smartphones and apps. The app does not need to be dumbed down — but it must be *emotionally intelligent* in how it presents load and demand.

#### Secondary User (Optional)

A family member or caregiver may occasionally help configure the app (add exercises, adjust schedules) but is not the day-to-day user.

#### Key User Challenges

- **Overwhelm** — seeing the full list triggers avoidance. The app must never present the full scope all at once without buffering.
- **Memory / forgetting** — without reminders, exercises are simply not thought of. Proactive prompting is essential.
- **Motivation** — living with chronic conditions is already hard. The app must acknowledge effort, not just track deficits.
- **Fatigue & pain** — some days full completion is not realistic. The app must accommodate partial effort without penalizing it.

---

## §3 Core Design Principles

### How the App Must Behave

#### 1 · Show Less, Not More

The default view should never show the entire exercise library. Instead, show what is relevant *right now* — today's recommended items, or the next suggested exercise. The full list is accessible but not the first thing seen.

#### 2 · One Thing at a Time

When in an active session, the user sees exactly one exercise. No count of remaining items is shown prominently during a session. The goal is to eliminate the cognitive load of "how much is left."

#### 3 · Any Progress is Real Progress

The app must celebrate partial completion. Doing two exercises on a bad day is a success. There is no "failed day" — only a spectrum of effort. Streaks and completion percentages must be framed positively.

#### 4 · Gentle Honesty About the Program

The full scope of the program is available when the user wants to see it — but it is never thrust upon her. The app holds the full list so she doesn't have to hold it in her head.

#### 5 · Calm, Warm Visual Tone

The visual design should feel like a trusted, warm companion — not a clinical tracker or a gamified fitness app. No aggressive color schemes, no aggressive progress bars, no countdown timers shown prominently unless actively requested.

> **Design North Star**
>
> If a user opens the app on a bad day, feels overwhelmed, does one exercise, and closes it — that must feel like a *win*, not a failure.

---

## §4 Application Architecture

### Technical Structure

#### Platform

Android APK distributed via direct sideload. No Google Play Store listing required. The app must function entirely offline — no internet connection should be needed for any core functionality.

#### Recommended Stack

- **Language:** Kotlin (preferred) or Java
- **UI Framework:** Jetpack Compose (recommended) or XML Views
- **Database:** Room (SQLite abstraction layer)
- **Notifications:** Android WorkManager + AlarmManager
- **Min SDK:** API 29 (Android 10)
- **Target SDK:** API 34 (Android 14)
- **Architecture:** MVVM (Model-View-ViewModel)

*No external server. All data stored on-device only.*

#### Data Storage Philosophy

All data stays on the device. There is no cloud sync, no account creation, no login. The app stores everything in a local SQLite database via Room. An optional JSON export feature allows manual backup.

---

## §5 Screen & Feature Specifications

### Screens & Navigation

#### Navigation Structure

The app uses a bottom navigation bar with four primary destinations: **Today**, **All**, **Progress**, **Settings**.

### 5.1 · Home Screen ("Today")

The default landing screen. This is the most important screen in the app.

- Displays a curated, short list of exercises recommended for today — not the full program.
- The recommended daily set is drawn from the full exercise library using a scheduling algorithm (see §6).
- A warm, personalized greeting at the top (time-of-day aware: morning / afternoon / evening).
- Each exercise item shows: name, body system tag (color-coded), estimated duration.
- Completed exercises are visually checked off but remain visible — not hidden.
- A gentle summary line: "You've done 3 today — that's great." Not "3 of 9 remaining."
- A prominent **Start Next Exercise** button that launches the session view for the next uncompleted item.
- An optional **I need an easier day** button that swaps to a shorter curated list (see §9).
- On first app open of each calendar day, the Home screen may automatically surface the optional pain/energy check-in (see §9, *Check-In Presentation*).

### 5.2 · Exercise Library Screen ("All")

The complete list of all configured exercises. This is the reference view, not the daily driver.

- Full list of all exercises grouped by body system. Groups are derived from the distinct `body_system` values currently present in the library and listed alphabetically, case-insensitive — not from a hard-coded enum.
- Each group is collapsible — collapsed by default to reduce visual load.
- Tapping an exercise opens its detail view (instructions, notes, frequency, duration).
- Admin mode (see §5.5) allows adding, editing, and deleting exercises.
- Search bar to find an exercise by name or keyword.
- Filter by body system or by "not done recently."

### 5.3 · Session View

The active exercise experience. Opened when the user taps **Start** on any exercise.

- Full-screen, distraction-free layout showing one exercise at a time.
- Exercise name displayed in large, readable typography.
- Instruction text: step-by-step guidance, scrollable if long.
- Optional image or illustration slot (can be left blank or populated with a simple diagram).
- Notes field (therapist notes or personal reminders — e.g., "only do if pain is below 5/10").
- An optional self-timer (user-activated, not auto-started) — defaults to the exercise's target duration.
- Two completion options: **Done ✓** and **Skipped today** (both record the attempt; neither is penalized).
- After completion, a brief positive acknowledgment screen (one sentence, warm tone) then auto-advances or returns to Today.
- If the user came from the Today list, a soft prompt: "Next up: [exercise name]. Start now?" — not mandatory.
- A **Pause — I need a break** button that saves state and returns to the Home screen without marking as skipped.

### 5.4 · Progress Screen

A personal history and wellbeing view. Framed around accomplishment, not deficits.

- Weekly calendar view showing days with any exercise activity (dot or fill, not a number).
- "Exercises this week" count with a positive framing (e.g., "12 exercises completed this week").
- Streak display — but only shown if the streak is active and the user has not opted out.
- Body system coverage: a simple visual showing which systems have been worked recently (last 7 days). The set of systems displayed is drawn from the live library (distinct `body_system` values), not from a fixed enum, so it grows and shrinks as the library evolves.
- A history log: scrollable list of past sessions with date, exercise name, status (done / skipped / partial).
- No daily "failure" indicators. Days with no activity are simply blank — no red X, no negative language.
- Optional: pain and energy check-in using the FPS-R (Faces Pain Scale — Revised, rendered with a dog-face adaptation) with an expandable "Tell me more" layer — always dismissible, never required.

### 5.5 · Settings / Admin Screen

Configuration for the program. Intended for occasional use, not daily interaction.

- Exercise management: add, edit, delete exercises (or use a simple PIN/passcode to restrict to caregiver).
- Add/Edit exercise form: body system is a free-text field (not a fixed dropdown) with inline autocomplete suggestions sourced from the distinct `body_system` values already in the library. On save the value is trimmed, normalized to Title Case, and capped at 100 characters. This lets the user (or caregiver) coin new categories as the program evolves without a code change.
- Schedule configuration: set which exercises appear on which days of the week.
- Daily load setting: how many exercises to suggest per day (slider: 1–10).
- Notification management: see §7.
- Check-ins toggle: enables or disables the automatic daily pain/energy check-in prompt (see §9).
- Display name / greeting name customization.
- Export data as JSON file (manual backup) — see §10.1–§10.2 for contents and triggers.
- Import data from JSON (restore) — see §10.3 for merge strategies.
- Reset progress (requires typing "reset" to confirm; offers a one-tap export first — see §10.6).
- Theme: light / dark / system default.

---

## §6 Exercise & Session Engine

### How Exercises Are Scheduled & Presented

#### Exercise Configuration Fields

Each exercise in the library has the following configurable properties:

| Field | Type | Description |
| --- | --- | --- |
| `name` | String | Short name of the exercise. |
| `body_system` | String (free-text) | Category tag, stored as a free-form string (≤100 chars, Title-Case normalized on save). Authors may use conventional labels (Respiratory, Lower Extremity, Core, Neurological, Balance, Upper Extremity, Pelvic) or coin new ones. No fixed-enum validation; grouping and coverage views derive their category list from the distinct values currently in use. |
| `instructions` | Long text | Step-by-step how-to, written in plain language. |
| `duration_minutes` | Integer | Suggested time to spend on this exercise. |
| `frequency` | Enum | How often prescribed: Daily, 3×/week, 2×/week, As tolerated, Weekly. |
| `scheduled_days` | Bitmask / Array | Which days of the week this can appear (Mon–Sun). |
| `priority` | 1–3 | Therapist priority: 1 = essential, 2 = important, 3 = supplemental. |
| `notes` | Text | Caregiver/therapist notes (e.g., precautions, modifications). |
| `active` | Boolean | Whether this exercise is currently in the rotation. |
| `image_uri` | Optional URI | Optional local image for illustration. |

#### Daily Recommendation Algorithm

Each day, the app generates a recommended list using the following logic:

```
// Daily exercise selection logic

1. Filter: exercises where today matches scheduled_days AND active = true
2. Filter: exclude exercises completed within their recurrence window
   (e.g., a 3x/week exercise done 3 times already this week is excluded)
3. Sort by priority ASC (priority 1 first), then by days_since_last_done DESC
4. Select top N, where N = user's configured daily load setting (minimum 1)
5. Ensure at least 1 exercise from each body_system value represented in
   the active library if possible, to maintain multi-system coverage
6. Cap total estimated duration at user's configured session length
   (optional soft cap — advisory, not enforced)
```

#### Easier Day Mode

When the user selects **I need an easier day**, the algorithm repeats with:

- N reduced to half the normal daily load, floored at 1 — easier day mode never results in zero exercises.
- Priority 1 exercises only, unless there are none — then priority 2.
- Shortest duration exercises preferred.
- A brief affirming message: "Here's a lighter plan for today. Doing anything counts."

---

## §7 Notifications & Reminders

### Keeping Her Gently Reminded

> **Key Principle**
>
> Notifications must feel like a caring nudge from a friend, not an alarm or a guilt trip. Language matters enormously. Every notification should be written with warmth.

#### Notification Types

| Type | Priority | Description |
| --- | --- | --- |
| Morning Reminder | MUST | Configurable daily notification (default 9:00 AM). Warm, brief. Example: *"Good morning! Your therapy plan is ready when you are."* Tapping opens the Today screen. |
| Afternoon Check-In | SHOULD | Optional second notification if no exercises completed by mid-afternoon (configurable time). Example: *"No pressure — just a gentle reminder your exercises are waiting."* Only fires if zero exercises done that day. |
| Evening Encouragement | NICE | Optional end-of-day message. If exercises were done: *"You did something today. That matters."* If not done: *"Rest is also part of healing. See you tomorrow."* Never accusatory. |
| Custom Reminders | SHOULD | User can add up to 3 additional custom reminder times with custom short messages. |

#### Notification Behavior Rules

- All notifications are opt-in per type (individually toggleable).
- A "quiet hours" window can be configured (e.g., 9 PM – 8 AM) during which no notifications fire.
- Notifications are scheduled using Android WorkManager to persist across reboots.
- Notification text must never contain negative language: no "you haven't done," no "missed," no "overdue."
- Each notification has a configurable message — defaults are provided but user can customize.
- Tapping any notification opens the app directly to the Today screen.

---

## §8 Data Model

### How Data is Structured

```
-- EXERCISES table
Exercise {
  id: UUID
  name: String
  body_system: String        // Free-text, ≤100 chars, Title-Case normalized on save.
                             // No enum constraint — grouping derives from distinct live values.
  instructions: String
  duration_minutes: Int
  frequency: Enum            // Daily | 3xWeek | 2xWeek | AsTolerated | Weekly
  scheduled_days: Int        // Bitmask: Mon=1, Tue=2, Wed=4 … Sun=64
  priority: Int              // 1 | 2 | 3
  notes: String?
  active: Boolean
  image_uri: String?
  created_at: Timestamp
}

-- SESSIONS table (one row per exercise attempt)
Session {
  id: UUID
  exercise_id: UUID          // FK → Exercise
  date: Date
  status: Enum               // Completed | Skipped | Partial
  duration_actual: Int?      // seconds actually spent (optional)
  created_at: Timestamp
}

-- CHECK_INS table (one row per completed or dismissed daily check-in)
CheckIn {
  id: UUID
  date: Date                 // Used for the "one per day" DB gate
  pain_fps_r: Int?           // FPS-R score: 0 | 2 | 4 | 6 | 8 | 10
  energy_level: Int?         // 6-point scale: 0 | 2 | 4 | 6 | 8 | 10
  bpi_domain: Enum?          // Sleep | Mood | Mobility | Enjoyment | Concentration | Social | General
  bpi_score: Int?            // 0–10 interference score
  free_text_note: String?    // Sunday free-text field
  dismissed: Boolean         // true if the check-in was dismissed without any input
  created_at: Timestamp
}

-- SETTINGS (single row)
UserSettings {
  display_name: String
  daily_load: Int            // exercises per day (1–10)
  notifications_enabled: Boolean
  check_ins_enabled: Boolean // gates the daily auto-surfaced check-in prompt
  morning_reminder_time: LocalTime?
  afternoon_reminder_time: LocalTime?
  quiet_hours_start: LocalTime?
  quiet_hours_end: LocalTime?
  theme: Enum                // Light | Dark | System
  show_streaks: Boolean
}
```

*Database schema version: 2. The v1→v2 migration is a no-op on the `exercises` table because `body_system` was already a TEXT column; removing the enum constraint is a repository-layer change, not a column-type change.*

---

## §9 Motivation & Wellbeing Features

### Supporting the Whole Person

#### Language & Tone Standards

Every string of user-facing text must be reviewed against these standards:

| ✗ Never Use | ✓ Use Instead |
| --- | --- |
| "You missed your exercises" | "Ready when you are" |
| "0 of 9 completed" | "You've done 0 today — every day is a fresh start" |
| "Overdue" | "Available today" |
| "Failed" | "Skipped" or "Rested" |
| "You should have done…" | Omit entirely |
| "Incomplete" | "In progress" |

#### Completion Acknowledgment

After each exercise is marked **Done**, the app briefly shows a warm acknowledgment screen before returning to the list. These messages should rotate through a library of ~20 options. Examples:

- "Done. Your body heard that."
- "One more step forward."
- "That counts. Really."
- "You showed up today."
- "Small things add up."
- "Well done — rest a moment if you need."

#### Optional Pain / Energy Check-In

After completing a session, or as a standalone daily prompt, the app may optionally offer a check-in. It is always dismissible with a single tap and never required. It is structured in two layers: a fast primary question and an optional deeper follow-up.

#### Check-In Presentation (when it appears)

The check-in is presented automatically from the Today (Home) screen on the user's first app open each calendar day, subject to both gates below. It is never blocking — a single tap dismisses it back to Today.

- **Settings gate:** `check_ins_enabled` must be true. If the user has disabled check-ins in Settings, the prompt never surfaces.
- **Database gate:** on app open, `HomeViewModel` queries the `CheckIns` table for a row with today's date. If one exists — submitted or dismissed with input — the prompt is suppressed for the rest of the day.
- **Session suppression:** if the user dismisses the prompt with a single tap (no input written), it is not re-shown for the remainder of the current app session. On a subsequent cold start the same day it may reappear unless a `CheckIn` row has been written.
- **Submission** writes a `CheckIn` row and thereby suppresses further prompts for the day.
- The user can also invoke the check-in manually from the Progress screen at any time, independent of the automatic trigger.

#### Layer 1 · FPS-R (Faces Pain Scale — Revised, dog-face adaptation)

The primary pain prompt uses the scoring structure of the Faces Pain Scale — Revised (FPS-R), a clinically validated instrument for ongoing pain monitoring. It presents six faces scored 0, 2, 4, 6, 8, and 10 — anchored at "no pain at all" and "most pain possible."

**Art direction:** rather than the standard FPS-R human-face illustrations, Therapy Companion renders the six faces as stylized dog faces progressing from relaxed (0) to visibly distressed (10). The intent is to preserve the clinical scoring semantics of FPS-R while matching the app's warm, companion-style tone; the neutral-to-distressed progression avoids the smiling anchor used in Wong-Baker, which can feel alienating to someone rarely at a pain-free baseline. Because the illustrations are a stylistic adaptation, the stored numeric score — not the imagery — is what maps to the validated FPS-R instrument. Any clinician-facing export must label the scale as FPS-R so the clinical meaning is unambiguous.

Implementation notes:

- Six illustrations rendered as drawable resources (`pain_face_0`, `_2`, `_4`, `_6`, `_8`, `_10`) and loaded via `Image` + `painterResource` in the `FpsrScale` composable. Displayed in a horizontal row.
- Each face is a large, tappable touch target (minimum 56dp).
- Brief anchor labels below the first and last face only: "No pain" and "Most pain possible."
- No numeric labels shown by default — the score (0/2/4/6/8/10) is stored internally but not shown to the user unless she taps a face, at which point a small label may confirm her selection.
- A second question immediately below, same format: "How is your energy right now?" using a matching 6-point scale with a distinct illustration set (not repurposed pain faces).
- Both questions appear on the same screen — total interaction time under 10 seconds.
- A single **Done** button submits both; individual questions can be left blank.

#### Layer 2 · "Tell me more" (Optional Expansion)

Below the Done button, a secondary link reads: *"Tell me more about today →"*. Tapping it expands a second screen with one rotating follow-up question drawn from the BPI interference domains. Only one question appears per check-in — not all of them. The question rotates daily so that over a week, different dimensions of her experience are captured without any single check-in feeling burdensome.

| Day | Rotating Follow-Up Question | Scale |
| --- | --- | --- |
| Mon | "How much did pain affect your sleep last night?" | 0–10 slider |
| Tue | "How much did pain affect your mood today?" | 0–10 slider |
| Wed | "How much did pain limit your ability to move around?" | 0–10 slider |
| Thu | "How much did pain affect your enjoyment of activities?" | 0–10 slider |
| Fri | "How much did pain affect your concentration today?" | 0–10 slider |
| Sat | "How much did pain affect your relationships or social time?" | 0–10 slider |
| Sun | "Overall, how would you describe today?" | Free text, optional |

The slider for interference questions runs 0–10 with anchor labels "Did not interfere" and "Completely interfered." The slider thumb starts at the leftmost position (0) — not the midpoint — to avoid anchoring bias.

> **Clinical Note**
>
> The app makes no medical interpretation of any recorded values. It stores and displays trends only. The longitudinal data — FPS-R scores over weeks, interference domain patterns — is intended as a personal record she may choose to share with her care team. The app must never present a score as "good" or "bad," only as a data point. Because the FPS-R faces are rendered with a dog-face adaptation, any printed export intended for a clinician should label the scoring scale explicitly as FPS-R so the clinical meaning is unambiguous.

#### Data Stored Per Check-In

- Date and time of check-in.
- FPS-R pain score (0 / 2 / 4 / 6 / 8 / 10) — nullable if skipped.
- Energy scale score (0 / 2 / 4 / 6 / 8 / 10) — nullable if skipped.
- BPI interference domain recorded (which question was shown that day).
- BPI interference score (0–10) — nullable if "Tell me more" was not used.
- Free text note (Sunday prompt) — nullable.
- Whether check-in was dismissed without any input (boolean).

#### Trend Display on Progress Screen

- A simple line chart of FPS-R pain scores over the past 30 days.
- A second line overlaid for energy scores (differentiated by color).
- BPI interference scores shown as small secondary annotations, labeled by domain.
- No threshold lines, no "normal range" indicators — purely her own data relative to itself.
- Option to export check-in history as CSV for sharing with a physician or therapist.

#### Streak Handling

Streaks are shown only if the user enables them in Settings. If shown, they track consecutive days with *any* completed exercise (not a full program completion). A streak is never broken by a single missed day — a "grace day" of one skipped day is allowed. This is a deliberate, compassionate design choice for someone managing a chronic health condition.

### 9.4 · Humble Brag

#### Overview

The Humble Brag feature generates a short, warm, celebratory summary of the user's recent accomplishments and copies it to the clipboard for easy sharing in any app of her choosing. It is the equivalent of a proud note from a teacher written in the margin of a good test — specific, affirming, and brief enough to forward to a friend, family member, or care provider in a single tap.

> **Design Intent**
>
> The output must read like it was written by someone who is genuinely proud of the user — not a statistics report. It should feel personal, warm, and share-ready.

#### Trigger Conditions

The Humble Brag button is available in two contexts:

- **Anytime** — a persistent **Humble Brag** button is accessible from the Progress screen at any time. No milestone is required to use it.
- **On streak achievement** — when the user earns a streak milestone (see Streak Handling in §9), a prompt appears celebrating the achievement with an option to generate a Humble Brag. This is presented as a secondary call-to-action alongside the streak badge — not a blocking interruption.

#### Content Generation

The summary is generated from the user's logged activity data. The following signals inform the output:

| Signal | Description | Example contribution to output |
| --- | --- | --- |
| Current streak | Consecutive days with at least one completed exercise | "You've done something for yourself 12 days in a row." |
| Recent session count | Exercises completed in the past 7 days | "Seven sessions this week alone." |
| Lifetime session count | Total completed sessions since first use | "Over 60 sessions completed since you started." |
| Body systems worked recently | Distinct `body_system` values with a completed session in the last 7 days | "You've been taking care of your hands, your wrists, and your core." |
| Display name | User's configured name from Settings | Personalizes the greeting line |

The summary is generated by a local on-device language model call (see Implementation Notes below). It must always:

- Be **3–5 sentences maximum** — concise enough to paste into a text message without editing.
- Use a **warm, second-person, teacher-to-student tone** — proud, specific, never hollow.
- Reference at least one **concrete detail** from the data (a number, a body system, a streak length) so it reads as genuinely earned rather than generic.
- Avoid clinical language, percentages, or deficit framing. It should describe what she *did*, not what remains.

**Example output:**

> "Look at you — 12 days in a row, and this week you put in 7 sessions across your hands, wrists, and core. That kind of consistency is genuinely hard, especially on the harder days, and you've done it anyway. Your body is better for it. That's worth being proud of."

#### Share Behavior

1. User taps **Humble Brag** (from Progress screen or streak achievement prompt).
2. A brief loading state is shown while the summary is composed (target: under 2 seconds).
3. The generated text is displayed in a full-screen preview card with a warm background treatment.
4. Two actions are offered:
   - **Copy to clipboard** — copies the text and shows a brief confirmation toast ("Copied — go brag a little 🎉"). This is the primary action.
   - **Regenerate** — produces a new variation using the same underlying data, for users who want a different phrasing.
5. The user pastes the text into any app of their choosing (Messages, email, a social platform, etc.). The app does not send or post anything directly.

> **Privacy**
>
> No data leaves the device as part of this feature. The summary is generated locally and placed on the clipboard. The app has no knowledge of where it is pasted.

#### Implementation Notes

- Summary generation uses the Anthropic API via a lightweight local call, with the user's activity statistics passed as structured context. The model is prompted to produce exactly the tone and length described above.
- If the API call fails (e.g., no network at the time of request), the app falls back to a small library of pre-written template strings that are filled with the user's live data. The fallback must not be obviously mechanical — templates should be written with the same warmth as the generated output.
- The Humble Brag button is visually distinct but not prominent — it should feel like a treat to discover, not a core navigation item. A small trophy or star icon with a short label is appropriate.
- Generated text is not stored or logged. Each tap produces a fresh summary.

#### Data Model Impact

No new database tables are required. The feature reads from existing `Session`, `Exercise`, and `UserSettings` tables only. No new columns are needed.

---

## §10 Backup, Restore & Update Safety

### Why This Matters

Because the app is sideloaded without a Play Store update channel, the user replaces the APK by hand every time a new build is installed. Android preserves app data across an update *only* when three conditions all hold: the new APK uses the same `applicationId`, the new APK is signed with the same key, and the old version has not been uninstalled first. If any condition breaks — or if the device itself is lost, wiped, or replaced — the user must still be able to recover the two things she most cares about: the **record of exercises she has completed**, and the **library of exercises that have been imported and configured for her program**.

This section defines the contract for that recovery path. The design goal is that any exercise completion, check-in, or imported exercise entered into the app is recoverable without a cloud service, without an account, and without technical intervention beyond selecting a file.

### 10.1 · Export Contents

A full export is a single JSON file capturing the complete state of the user's program and history. The root object fields are:

| JSON field | Type | Contents |
| --- | --- | --- |
| `version` | Int | Backup format version (currently `1`). Incremented only if the JSON structure itself changes in a breaking way. |
| `schemaVersion` | Int | Room database schema version at export time. Import refuses files where this exceeds the installed app's schema version. |
| `exportedAt` | Long | UTC epoch milliseconds of export time. |
| `exercises` | Array | All rows from the `Exercise` table — including `active = false` rows. Retired exercises are preserved so old sessions still resolve to a name. |
| `sessions` | Array | Complete `Session` history — every Completed / Skipped / InProgress record. |
| `checkIns` | Array | Complete `CheckIn` history, including dismissed rows. Preserves FPS-R, energy, and BPI interference trends. |
| `userSettings` | Object or null | All fields from the `UserSettings` table. Restores daily load, notification schedule, toggles, and preferences. |

Images referenced by `imageFileName` are **not** inlined in the JSON — only the filename reference is kept. A disclaimer is shown in the export UI. If a future version supports image bundling, exports will be a `.zip` containing the JSON plus an `images/` directory.

### 10.2 · Export Triggers

- **Manual export** — Settings → "Export data." Opens the system share sheet (user can send to Drive, email, save to Downloads, etc.) and *also* writes a timestamped copy to the app's external files directory.
- **Before-update reminder** — when the user opens Settings, if the last export is more than 14 days old **and** at least one Session or CheckIn has been recorded since that last export, a gentle banner appears: *"It's been a while since your last backup. Export now?"* Dismissible, not blocking.
- **Automatic weekly snapshot (MUST)** — WorkManager writes a rolling JSON backup to the app's private external files directory (`/storage/emulated/0/Android/data/<package>/files/backups/`) once a week. The last 4 weekly snapshots are retained; older ones are pruned. This protects against update mishaps and accidental "Reset progress" taps. It does **not** protect against a full uninstall (Android removes that directory on uninstall), which is why the manual share-sheet export remains the durable path.

### 10.3 · Import Behavior

- Settings → "Import data" opens a file picker scoped to JSON files.
- The import screen first shows a summary of what *will* be imported: counts of exercises, sessions, check-ins, and the date range covered. Nothing is written until the user taps Confirm.
- Three merge strategies, presented as radio buttons with plain-language labels:
  1. **Replace everything** — wipes current data, restores the file as-is. Appropriate for a fresh reinstall after an uninstall or device swap.
  2. **Merge — keep both** (default) — adds everything from the file to what's already in the database, de-duplicating by `id` (UUID). This is the correct choice when importing a backup into an empty install after updating the APK.
  3. **Merge — prefer file** — same as above, but when a UUID collides, the file's version wins.
- Sessions and CheckIns are keyed by UUID; duplicates are silently skipped rather than surfaced as errors.
- If the file's `schema_version` is older than the current schema, the importer runs the same Room migrations against the imported data before inserting. If the `schema_version` is newer (i.e., exported from a future version of the app), import is refused with a clear message: *"This backup was made with a newer version of the app. Update the app before importing."*
- Imports are transactional — a failure partway through rolls back completely. The live database is never left in a half-migrated state.

### 10.4 · Update-Safety Workflow (User-Facing)

The Settings screen shows a one-time tooltip the first time the user visits it, titled *"Before you update the app."* It outlines the three-step routine:

1. **Export your data** (one tap — sends to share sheet and writes a local copy).
2. **Install the new APK — do not uninstall first.** Use `adb install -r`, or tap the APK file on-device. The signing key must match the previously installed build.
3. **Open the app and verify Today still shows your exercises.** If something looks wrong, use Import → *Replace everything* with the backup you just made.

This text is also available permanently from the Settings → About screen so it remains findable after the first-run tooltip is dismissed.

### 10.5 · CSV Export (View-Only)

Separately from the full JSON backup, the app supports two CSV exports oriented to sharing with a clinician rather than round-tripping data. Both are UTF-8 encoded, sorted most-recent-first.

**Session history CSV** — one row per session:

| Column | Contents |
| --- | --- |
| `id` | Session UUID |
| `exerciseId` | Exercise UUID (join key if used alongside an exercise export) |
| `date` | Local date/time of session start (`yyyy-MM-dd HH:mm:ss`) |
| `status` | `Completed` · `Skipped` · `InProgress` |
| `elapsedSeconds` | Actual time spent in seconds |
| `notes` | Session notes, if any |

**Check-in history CSV** — one row per check-in:

| Column | Contents |
| --- | --- |
| `id` | Check-in UUID |
| `date` | Local date/time of check-in (`yyyy-MM-dd HH:mm:ss`) |
| `painScore` | FPS-R score: `0` `2` `4` `6` `8` `10` — blank if not recorded |
| `energyScore` | Energy scale score: `0` `2` `4` `6` `8` `10` — blank if not recorded |
| `bpiDomain` | BPI interference domain shown that day — blank if Layer 2 not used |
| `bpiScore` | BPI interference score `0–10` — blank if not recorded |
| `freeText` | Free-text note (Sunday prompt) — blank if not entered |

CSV exports are a read-only view, not a backup. The importer does not accept them.

### 10.6 · Exercise CSV Import Format

Settings → "Import exercises" accepts a UTF-8 or Windows-1252 encoded `.csv` or `.tsv` file. The file is validated entirely before any row is committed (all-or-nothing). Lines beginning with `#` are treated as comments and ignored — the bundled template uses this to embed format hints inline.

**Required columns** (header row, case-insensitive):

| Column | Type | Accepted values |
| --- | --- | --- |
| `name` | String | Any non-blank string, ≤ 100 characters |
| `body_system` | String | Any non-blank string, ≤ 100 characters. No fixed list — any label is valid. |
| `instructions` | String | Any non-blank string. Wrap in `"..."` if it contains commas. |
| `duration` | Number | Integer or decimal minutes ≥ 1. Decimals are rounded up (`5.5` → `6`). |
| `frequency` | Enum | `Daily` · `3xWeek` · `2xWeek` · `AsTolerated` · `Weekly` |
| `days` | String | `Daily` · `Weekdays` · `Weekends` · or comma-separated day names |
| `priority` | Int or label | `1` / `high` / `essential` · `2` / `medium` / `important` · `3` / `low` / `supplemental` |

**Optional columns:**

| Column | Default | Accepted values |
| --- | --- | --- |
| `notes` | *(blank)* | Any string |
| `active` | `true` | `true` / `false` / `yes` / `no` / `0` |

**Flexible aliases** — the parser accepts common variants without requiring exact strings:

- `frequency`: `3x/Week`, `3 times a week`, `twice a week`, `as tolerated`, `PRN`, `once a week`, `every day`, etc.
- `days`: full names (`Monday`, `Tuesday`, …), single-letter abbreviations (`M` `Tu` `W` `Th` `F` `Sa` `Su`), or `Weekdays` / `Weekends` / `All`

**Encoding:** UTF-8 (with or without BOM), UTF-16 (with BOM), and Windows-1252 are all detected automatically. Files exported directly from Excel on Windows or Mac import without re-encoding.

### 10.7 · Failure Modes & Safeguards

- The export routine validates that the generated JSON parses cleanly before offering the share sheet; a malformed export is never handed to the user.
- Import writes to a staging database and atomically swaps it in only after a successful parse + migration. The live database is never partially overwritten.
- The **Reset progress** option in Settings (which wipes all history) must require the user to type the word "reset" to confirm, and must offer a one-tap export first.
- Every weekly automatic snapshot is verified by a round-trip parse before the previous snapshot is pruned — a corrupt snapshot never replaces a good one.

---

## §11 Technical Requirements

### Non-Functional Requirements

| ID | Requirement | Priority |
| --- | --- | --- |
| NFR-01 | App must function fully offline. No network access required for any feature. | MUST |
| NFR-02 | App must start and reach the Today screen in under 2 seconds on a mid-range Android device. | MUST |
| NFR-03 | Notifications must survive device reboots. WorkManager must be used to re-schedule on boot. | MUST |
| NFR-04 | All data must persist across app updates (database migration must be handled properly via Room migrations). | MUST |
| NFR-05 | The APK must be installable via sideload (Settings → Install unknown apps). No special signing beyond debug or self-signed required. | MUST |
| NFR-06 | Minimum font size for body text: 16sp. All touch targets minimum 48dp × 48dp. | MUST |
| NFR-07 | Support both light and dark themes. Respect system preference by default. | SHOULD |
| NFR-08 | APK size under 20 MB (excluding any bundled media assets). | SHOULD |
| NFR-09 | Support Android 10 (API 29) through Android 14 (API 34). | MUST |
| NFR-10 | Full backup export must be a single human-readable JSON file covering settings, exercises, sessions, and check-ins, per §10.1. Share-sheet and local-file outputs both required. | MUST |
| NFR-11 | CSV import of exercises must accept any non-empty string of ≤100 chars for `body_system`; no fixed-list validation. | SHOULD |
| NFR-12 | A rolling weekly JSON snapshot must be written to the app's external files directory via WorkManager, retaining the last 4 snapshots (§10.2). | MUST |
| NFR-13 | Import must be transactional and must refuse files whose `schema_version` is newer than the installed app's schema (§10.3). | MUST |
| NFR-14 | Release builds must be signed with a stable, documented keystore so that APK updates preserve app data on the device (§10.4). | MUST |

### Permissions Required

- `POST_NOTIFICATIONS` — for reminder notifications (Android 13+).
- `RECEIVE_BOOT_COMPLETED` — to reschedule notifications after reboot.
- `SCHEDULE_EXACT_ALARM` — for precise notification timing (Android 12+).
- `READ/WRITE_EXTERNAL_STORAGE` — optional, only for backup/restore to external storage.

---

## §12 Out of Scope

### What This App Is Not

> **Explicit Exclusions**
>
> These features are deliberately excluded from v1.0 to keep the app focused and deliverable.

- No cloud sync, no user accounts, no login.
- No video playback of exercises (images only, and optional).
- No communication with therapists or physicians.
- No AI-generated exercise recommendations.
- No wearable device integration.
- No multi-user profiles.
- No Google Play Store listing.
- No in-app purchases.
- No social features, sharing of progress externally.
- No camera or microphone access.

---

## §13 Future Considerations

### Possible v2.0 Features

These are out of scope for the initial build but worth designing around from the start so the architecture supports them later:

- **Cloud Backup** — optional Google Drive or iCloud-like sync for device migration, without requiring an account by default.
- **Exercise Videos** — short locally-stored video clips for exercises where technique is hard to describe in text.
- **Therapist Report** — a formatted PDF export of the past month's activity suitable for sharing at a medical appointment.
- **Symptom Tracking** — expanded daily health log beyond pain/energy (symptoms, medication, sleep) to correlate with exercise compliance.

---

## §14 Revision Notes

### v1.3 — Humble Brag feature

*Sections touched: §9 (new §9.4 · Humble Brag), TOC.*

Added §9.4 specifying the Humble Brag feature: a user-initiated, on-demand generator that produces a short, warm summary of the user's recent exercise accomplishments and places it on the clipboard for sharing. The feature is accessible from the Progress screen at any time and is additionally surfaced as a secondary prompt on streak milestone achievement. Output is 3–5 sentences, generated via Anthropic API with a warm teacher-to-student tone, referencing concrete data (streak length, session count, body systems worked). A fallback template library handles offline or error states. No new database tables or columns are required. No data leaves the device as part of this feature — generation is local and clipboard-only.

### v1.3 — Export format clarifications & exercise import specification

*Sections touched: §10.1, §10.5, new §10.6 (renumbered Failure Modes to §10.7).*

Updated §10.1 to document the actual JSON field names produced by the exporter (`schemaVersion`, `exportedAt`, `checkIns`, `userSettings`). Updated §10.5 CSV export column names to match implementation. Added §10.6 formally specifying the exercise CSV import format: required and optional columns, accepted type formats, flexible frequency/days/priority aliases, and auto-detected encoding support (UTF-8, UTF-16, Windows-1252). Lines beginning with `#` in import files are treated as comments and ignored.

### v1.2 — Backup, restore & update-safety

*Sections touched: §5.5 (export/import bullets now reference §10), new §10 "Backup, Restore & Update Safety", §11 (new NFR-10 upgraded to MUST, new NFR-12/13/14).*

Added a dedicated section defining how the user's exercise history and imported exercise library are preserved across sideloaded APK updates. Key additions: full JSON backup schema covering settings/exercises/sessions/check-ins; three-option import merge (Replace / Merge-keep-both / Merge-prefer-file); automatic weekly rolling snapshots written to external files via WorkManager (4-snapshot retention); before-update reminder banner in Settings after 14 days without export; a "Before you update the app" tooltip and About-screen note describing the export → install-with-matching-signature → verify workflow. Release builds must now be signed with a stable, documented keystore (NFR-14) so that APK replacement preserves on-device data rather than wiping it. CSV exports are explicitly scoped as view-only and not importable.

### v1.1 — Initial clarifications

This revision incorporated three functional clarifications based on early implementation progress.

#### FPS-R illustration set — dog-face adaptation

*Sections touched: §5.4, §9 (Layer 1).*

The six FPS-R faces are rendered as stylized dog-face illustrations rather than standard human FPS-R faces. The scoring structure (0/2/4/6/8/10, neutral→distressed anchors) is preserved; the art direction is an intentional tone choice. Clinician-facing exports must label the scale as FPS-R so the clinical meaning is unambiguous.

#### Body system — enum to free text

*Sections touched: §5.2, §5.4, §5.5, §6 (configuration table + algorithm step 5), §8 (data model), §11 (NFR-11; renumbered from §10 in v1.2).*

`body_system` is now a free-text string (≤100 chars, Title-Case normalized on save), not an enum. The library no longer validates category names against a fixed list. Grouping on the All screen, coverage on the Progress screen, and the "one per system" rule in the recommendation algorithm all derive their category set from the distinct values currently in the library. Add/Edit shows inline autocomplete sourced from existing values. Database schema version bumped to 2 with a no-op migration (column was already TEXT).

#### Check-in auto-surface trigger

*Sections touched: §5.1, §5.5 (new Check-ins toggle), §8 (new `check_ins_enabled` setting and new `CheckIns` table), §9 (new Check-In Presentation subsection).*

The pain/energy check-in bottom sheet is now auto-surfaced from the Today screen on the first app open of each calendar day, gated by (a) the `check_ins_enabled` setting and (b) a DB check against the `CheckIns` table for a row with today's date. Dismissing with no input suppresses re-prompting for the rest of the session; submitting writes a `CheckIn` row and thereby suppresses re-prompting for the day.

---

> **Final Note**
>
> This specification is a starting point. The most important thing this app can do is lower the barrier to starting — even on the hardest days. Every technical decision should be evaluated against that goal. The best feature is the one that makes the next exercise feel possible.

*Therapy Companion · Software Specification v1.3 · April 2026*
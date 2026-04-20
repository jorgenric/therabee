# Therapy Companion — Developer Guide

A reference for understanding, maintaining, and extending the codebase. Written for someone who did not build the app but needs to support or modify it.

---

## Table of Contents

1. [Stack & Architecture Primer](#1-stack--architecture-primer)
2. [Project Structure](#2-project-structure)
3. [Application Entry Points](#3-application-entry-points)
4. [Navigation](#4-navigation)
5. [Data Layer](#5-data-layer)
6. [Domain Layer — The Daily Scheduler](#6-domain-layer--the-daily-scheduler)
7. [UI Layer — Screens](#7-ui-layer--screens)
8. [Notification System](#8-notification-system)
9. [Backup & Restore](#9-backup--restore)
10. [Humble Brag Feature](#10-humble-brag-feature)
11. [Common Maintenance Tasks](#11-common-maintenance-tasks)

---

## 1. Stack & Architecture Primer

### Language: Kotlin

The entire app is written in Kotlin. It uses Kotlin coroutines extensively for all asynchronous work (database queries, file I/O). Familiarity with `suspend` functions, `Flow`, `CoroutineScope`, and `launch`/`collect` is required to work in most parts of the codebase.

### UI: Jetpack Compose

Jetpack Compose is Android's modern declarative UI toolkit. Instead of XML layout files, all UI is written as `@Composable` functions in Kotlin. Key concepts:

- **`@Composable` functions** — UI building blocks. They are re-invoked ("recomposed") whenever their inputs change. They cannot be called from non-composable code.
- **State hoisting** — data flows *down* as parameters; events flow *up* as lambda callbacks. ViewModels hold state; screens read it via `collectAsState()`.
- **`remember` / `rememberSaveable`** — short-lived state local to a composable. Not used for anything that needs to survive a screen rotation or navigation.
- **`LazyColumn`** — the Compose equivalent of `RecyclerView`. Items are rendered on demand.
- **Material3** — the design system used throughout. Components like `Card`, `OutlinedButton`, `TopAppBar`, `NavigationBar`, and `AlertDialog` all come from `androidx.compose.material3`.

### Architecture: MVVM

Every screen follows the same pattern:

```
Screen (Composable)
  └── ViewModel  ←→  Repository  ←→  DAO  ←→  Room/SQLite
```

- **Composable** reads a `StateFlow` from the ViewModel using `collectAsState()` and calls ViewModel methods in response to user events.
- **ViewModel** holds all screen state in a single `data class` wrapped in `MutableStateFlow`. It calls repositories on `Dispatchers.IO` and updates state with `.update { it.copy(...) }`.
- **Repository** provides a clean API over the DAO — callers never talk directly to Room.
- **DAO** is a Room-annotated interface that generates the SQL.

There is **no dependency injection framework** (no Hilt, no Koin). Repositories are instantiated manually as lazy properties on `TherapyCompanionApp` and passed to ViewModels through `ViewModelProvider.Factory` subclasses. Each screen's `@Composable` function reads the app instance from `LocalContext` and constructs its own ViewModel factory.

### Database: Room

Room is a SQLite abstraction library. It generates type-safe Kotlin from annotated interfaces at compile time using KSP (Kotlin Symbol Processing). The database is a single file on-device: `therapy_companion.db`. Schema version is tracked explicitly; migrations are never destructive.

### Background Work: WorkManager & AlarmManager

Two separate mechanisms handle background work:

- **`WorkManager`** — used for the weekly automatic backup (`BackupWorker`). It is battery-aware, persists across reboots, and survives process death. Appropriate for deferrable, non-time-critical tasks.
- **`AlarmManager.setExactAndAllowWhileIdle()`** — used for notification delivery. AlarmManager fires precise alarms even in Doze mode. Required because WorkManager cannot guarantee delivery at a specific wall-clock time.

These two mechanisms are not interchangeable. Notifications need AlarmManager; backup can use WorkManager.

### Reactive Data: Kotlin Flow

Repositories expose `Flow<T>` for live data and `suspend` functions for one-shot reads. `Flow` is the equivalent of LiveData — it emits a new value whenever the underlying database row changes. Screens subscribe via `collectAsState()` and re-render automatically.

Pattern used across the codebase:

```kotlin
// In ViewModel init block — runs for the lifetime of the ViewModel
viewModelScope.launch {
    repository.getSomething().collect { data ->
        _uiState.update { it.copy(thing = data) }
    }
}

// One-shot read on IO thread — used when you need data once, not reactively
viewModelScope.launch(Dispatchers.IO) {
    val data = repository.getSomethingOnce()
    _uiState.update { it.copy(thing = data) }
}
```

---

## 2. Project Structure

```
app/src/main/java/com/therapycompanion/
│
├── TherapyCompanionApp.kt          Application class — DI root, channel setup, notifications
├── MainActivity.kt                 Single Activity — theme, notification permission, NavGraph
│
├── ui/
│   ├── navigation/
│   │   ├── Screen.kt               Sealed class of all route strings
│   │   └── NavGraph.kt             NavHost wiring + bottom navigation bar
│   ├── theme/
│   │   ├── Color.kt                Material3 color tokens
│   │   ├── Type.kt                 Typography scale
│   │   └── Theme.kt                TherapyCompanionTheme composable
│   ├── home/
│   │   ├── HomeScreen.kt           Today screen
│   │   └── HomeViewModel.kt
│   ├── session/
│   │   ├── SessionScreen.kt        Active exercise view + timer + acknowledgment
│   │   └── SessionViewModel.kt
│   ├── library/
│   │   ├── LibraryScreen.kt        Exercise list with search/filter
│   │   ├── LibraryViewModel.kt
│   │   ├── ExerciseDetailScreen.kt
│   │   └── ExerciseEditScreen.kt
│   ├── progress/
│   │   ├── ProgressScreen.kt       Calendar, streak, coverage, history, Humble Brag
│   │   └── ProgressViewModel.kt
│   ├── settings/
│   │   ├── SettingsScreen.kt       All preferences, notification toggles, backup/restore
│   │   ├── SettingsViewModel.kt
│   │   └── ImportScreen.kt         CSV import flow
│   └── checkin/
│       └── CheckInBottomSheet.kt   FPS-R pain/energy check-in modal
│
├── data/
│   ├── model/                      Pure Kotlin domain models (no Room annotations)
│   │   ├── Exercise.kt
│   │   ├── Session.kt
│   │   ├── SessionStatus.kt
│   │   ├── CheckIn.kt
│   │   ├── UserSettings.kt
│   │   ├── Frequency.kt
│   │   └── DayBits.kt
│   ├── db/                         Room layer
│   │   ├── AppDatabase.kt          Database class + migrations
│   │   ├── ExerciseEntity.kt
│   │   ├── SessionEntity.kt
│   │   ├── CheckInEntity.kt
│   │   ├── UserSettingsEntity.kt
│   │   ├── ExerciseDao.kt
│   │   ├── SessionDao.kt
│   │   ├── CheckInDao.kt
│   │   ├── UserSettingsDao.kt
│   │   └── Mappers.kt              Entity ↔ domain model conversion functions
│   └── repository/
│       ├── ExerciseRepository.kt
│       ├── SessionRepository.kt
│       ├── CheckInRepository.kt
│       └── UserSettingsRepository.kt
│
├── domain/
│   └── scheduler/
│       └── DailyScheduler.kt       Pure function — selects today's exercises
│
├── notification/
│   ├── NotificationScheduler.kt    Schedules AlarmManager alarms
│   ├── NotificationReceiver.kt     BroadcastReceiver — fires + reschedules
│   └── BootReceiver.kt             Reschedules alarms after device restart
│
├── backup/
│   ├── BackupWorker.kt             WorkManager weekly snapshot
│   ├── JsonExporter.kt             Full JSON export
│   ├── JsonImporter.kt             JSON import with three merge strategies
│   └── CsvExporter.kt             Session + check-in CSV exports
│
├── import/
│   └── CsvImporter.kt             Exercise CSV import with validation and aliases
│
└── humbleBrag/
    └── HumbleBragGenerator.kt     20-phrase library; randomPhrase()
```

---

## 3. Application Entry Points

### `TherapyCompanionApp.kt`

Extends Android's `Application` class — it is instantiated once when the process starts and lives for the entire lifetime of the app. It is the manual dependency injection root.

**What it does on startup (`onCreate`):**

1. Creates the four notification channels (`CHANNEL_MORNING`, `CHANNEL_CHECKIN`, `CHANNEL_EVENING`, `CHANNEL_CUSTOM`). Channels are idempotent — safe to call every launch.
2. Calls `userSettingsRepository.initializeDefaults()` to insert the single `UserSettings` row if it does not already exist.
3. Collects the `UserSettings` Flow for the lifetime of the process and calls `NotificationScheduler.scheduleAll()` on every emission. This ensures alarms are always in sync with settings — including on first launch.
4. Enqueues `BackupWorker` (WorkManager deduplicates by name so this is safe to call on every launch).

**Repositories as lazy properties:**

```kotlin
val database by lazy { AppDatabase.getInstance(this) }
val exerciseRepository by lazy { ExerciseRepository(database.exerciseDao()) }
// ... etc.
```

Any code that needs a repository retrieves the Application instance from `Context` and accesses it directly. There is no service locator or injection container.

### `MainActivity.kt`

The single `Activity`. It:

1. Requests `POST_NOTIFICATIONS` permission on Android 13+ on first launch.
2. Reads an intent extra (`EXTRA_NAVIGATE_TO`) to handle notification deep-links — when a notification is tapped, the intent carries `"today"` and the NavGraph starts on the Today screen.
3. Collects `UserSettings` to read `themeMode` and passes `darkTheme: Boolean` to `TherapyCompanionTheme`. Theme changes apply reactively without restarting the Activity.
4. Renders the single `TherapyCompanionNavGraph` composable inside a `Surface`.

---

## 4. Navigation

### `Screen.kt` — Route Definitions

All navigation routes are defined as a `sealed class`. **Never use raw strings for navigation** — always use the constants in `Screen`.

```kotlin
Screen.Today.route          // "today"
Screen.Session.route(id)    // "session/<exerciseId>"
Screen.ExerciseEdit.route(id) // "exercise/<exerciseId>/edit"
```

Parameterized routes take `exerciseId` as a path segment. The ViewModel retrieves it from `SavedStateHandle` or from the `backStackEntry.arguments` map in the NavHost.

### `NavGraph.kt` — Navigation Wiring

`TherapyCompanionNavGraph` is a single `@Composable` that sets up:

- A `Scaffold` with a `NavigationBar` at the bottom. The bar is only shown on top-level destinations (Today, Library, Progress, Settings); it disappears when navigating into Session or Exercise detail.
- A `NavHost` containing all routes. Each route is a `composable { }` block that creates the screen.

**Back stack behaviour for Session:**

When a session completes (Done, Skip, or Cancel), the back stack pops back to `Today` using `popUpTo(Screen.Today.route, inclusive = false)`. This means pressing Back from the Today screen exits the app, not returns to the session. "Start next exercise" navigates to a new session while also clearing the previous session from the stack.

**Deep-link from notification:**

`MainActivity` reads `startDestination` from the intent and passes it to `TherapyCompanionNavGraph`. If it equals `"today"`, the NavHost starts there instead of the default.

---

## 5. Data Layer

### Domain Models vs. Entities

The data layer has two representations of every record:

| Layer | Class | Annotation | Lives in |
|---|---|---|---|
| Domain | `Exercise`, `Session`, etc. | None — pure Kotlin | `data/model/` |
| Database | `ExerciseEntity`, `SessionEntity`, etc. | Room `@Entity` | `data/db/` |

**Why two classes?** Domain models use proper Kotlin types (`Frequency` enum, `SessionStatus` enum). Entities store the same data as primitives that Room can persist — enums are stored as their `.name` string; booleans as integers; times as `"HH:mm"` strings.

`Mappers.kt` contains extension functions that convert between the two: `ExerciseEntity.toDomain()` and `Exercise.toEntity()`. Every repository method calls the appropriate mapper.

### Room Entities

#### `ExerciseEntity`

| Column | Type | Notes |
|---|---|---|
| `id` | TEXT (UUID) | Primary key. UUID chosen over auto-increment for safe merge on backup restore. |
| `name` | TEXT | |
| `body_system` | TEXT | Free-form; Title-Case normalized on save in the ViewModel. No enum validation. |
| `instructions` | TEXT | |
| `notes` | TEXT? | Optional therapist/caregiver notes. |
| `duration_minutes` | INTEGER | |
| `frequency` | TEXT | Stored as enum name: `"Daily"`, `"ThreePerWeek"`, etc. Deserialized by `Frequency.fromString()`. |
| `scheduled_days` | INTEGER | Bitmask. See `DayBits.kt` for constants (Mon=1, Tue=2, Wed=4…Sun=64). |
| `priority` | INTEGER | 1=essential, 2=important, 3=supplemental. |
| `active` | INTEGER (Boolean) | Inactive exercises are excluded from daily scheduling. |
| `image_file_name` | TEXT? | Filename only (not full path). Stored in `filesDir`. |
| `video_file_name` | TEXT? | Reserved for v2.0 — always null in v1.0. Column exists to avoid a future migration. |
| `created_at` / `updated_at` | INTEGER | UTC epoch milliseconds. |

#### `SessionEntity`

| Column | Type | Notes |
|---|---|---|
| `id` | TEXT (UUID) | |
| `exercise_id` | TEXT | Foreign key → `exercises.id`. Cascades on delete. |
| `started_at` | INTEGER | UTC epoch ms. Indexed for date-range queries. |
| `completed_at` | INTEGER? | Null for skipped or in-progress sessions. |
| `elapsed_seconds` | INTEGER | Actual time spent, derived from the countdown timer. |
| `status` | TEXT | `"InProgress"`, `"Completed"`, `"Skipped"`. Indexed. |
| `notes` | TEXT? | |

#### `CheckInEntity`

| Column | Type | Notes |
|---|---|---|
| `id` | TEXT (UUID) | |
| `checked_in_at` | INTEGER | UTC epoch ms. |
| `pain_score` | INTEGER? | FPS-R score: 0, 2, 4, 6, 8, or 10. |
| `energy_score` | INTEGER? | Same scale. |
| `bpi_domain` | TEXT? | Day-of-week BPI domain name (e.g., `"Sleep"`, `"Mood"`). |
| `bpi_score` | INTEGER? | 0–10 interference score. |
| `free_text` | TEXT? | Sunday free-text response. |
| `dismissed` | INTEGER (Boolean) | True if user tapped Dismiss with no input. |

#### `UserSettingsEntity`

Single-row table (primary key is always `id = 1`). Stores all user preferences. Times are stored as `"HH:mm"` strings. Custom reminders are stored as six flat nullable columns (`custom_reminder_1_time`, `custom_reminder_1_msg`, etc.) rather than a separate table — three pairs maximum.

### Schema Migrations

Defined in `AppDatabase.kt`. The database is currently at **version 5**. All migrations use `ALTER TABLE ... ADD COLUMN` with safe defaults; no existing data is ever dropped or rewritten.

| Migration | Change |
|---|---|
| 1 → 2 | No-op. Records removal of the `body_system` enum constraint (already TEXT in SQLite). |
| 2 → 3 | Adds `show_streaks INTEGER NOT NULL DEFAULT 0` to `user_settings`. |
| 3 → 4 | Adds `display_name TEXT NOT NULL DEFAULT ''` and `theme_mode TEXT NOT NULL DEFAULT 'System'`. |
| 4 → 5 | Adds six nullable TEXT columns for custom reminders (`custom_reminder_1_time`, `_msg`, through `_3`). |

**Rule:** Any time a Room entity is modified (column added, type changed), the database version must be bumped and a new `Migration` object must be added to the `addMigrations()` call in `buildDatabase()`. Failing to do this will crash the app on launch for existing installs. See §11 for the step-by-step procedure.

### Repositories

Repositories are thin wrappers over DAOs. Their responsibilities:

- Translate between DAO results (entities) and domain models using mappers.
- Provide a clean method surface that callers can understand without knowing Room internals.
- Expose `Flow<T>` for reactive subscriptions and `suspend` one-shot methods for batch operations.

No business logic lives in a repository. Query filtering and selection logic belongs in the `DailyScheduler` domain object.

---

## 6. Domain Layer — The Daily Scheduler

`DailyScheduler.kt` is the most algorithmically complex file in the codebase. It is a pure Kotlin `object` — no Android dependencies, no database access, no side effects. All data is passed in; a list is returned.

**Because it is a pure function, it is straightforwardly unit-testable** without mocking or a device.

### `selectDailyExercises()`

Takes the full exercise library, recent sessions, today's day bitmask, the daily load setting, and the easier-day flag. Returns the ordered list of exercises to show on the Today screen.

**Steps:**

1. **Filter to scheduled today** — checks `exercise.scheduledDays and todayDayBit != 0`.
2. **Filter frequency-exhausted** — `isFrequencyExhausted()` checks how many times each exercise has been completed this week (or today for Daily exercises). Skipped sessions do not count.
3. **Easier Day priority filter** — if `easierDay = true`, narrows to Priority 1 exercises. Falls back to Priority 2 if no P1 exists; falls back to all eligible if neither P1 nor P2 exists.
4. **Sort** — primary: priority ASC (1 first). Secondary: duration ASC (easier day only). Tertiary: days since last done DESC (never done = highest within tier).
5. **Apply daily load cap** — takes the top N exercises (halved, floored at 1, in easier day mode).
6. **Body-system diversity pass** — `applyBodySystemDiversity()`: if 2+ exercises from the same body system are in the selection AND there are eligible exercises from other systems, swaps the lower-priority duplicate for the best underrepresented alternative. This prevents an all-one-system day.

`currentWeekStart()` and `todayBoundaries()` are utility helpers used by both the scheduler and repositories.

---

## 7. UI Layer — Screens

All screens follow the same pattern: a `@Composable` function that reads from a ViewModel via `collectAsState()`, and a `ViewModel` that holds a single `UiState` data class.

### Home Screen (`ui/home/`)

**`HomeViewModel`** computes the daily exercise list using `DailyScheduler.selectDailyExercises()`. It also:
- Queries the `CheckIns` table to determine whether to surface the check-in bottom sheet on first open of the day.
- Manages the `easierDayEnabled` toggle (persisted to `UserSettings`).
- Exposes `markSkipped()` which writes a `Skipped` session record.

**`HomeScreen`** shows:
- Time-of-day greeting with optional display name.
- A full-width "Start: [Exercise Name]" button for the first uncompleted exercise.
- A `DailySummary` line using the "You've done N today" framing (no totals).
- An `ExerciseCard` per exercise with Start and Skip icon buttons. Completed and skipped cards show a strikethrough and status label.
- `EasierDayToggle` banner at the top when active.
- `CheckInBottomSheet` as a modal overlay when the check-in conditions are met.

### Session Screen (`ui/session/`)

**`SessionViewModel`** writes an `InProgress` session record to the database as soon as the session begins (navigation arrival). This is how "close = pause without skipping" works — tapping X calls `cancelSession()` which deletes the `InProgress` record, leaving nothing written.

Timer state: `remainingSeconds: Int`, `timerActive: Boolean`, `timerStarted: Boolean`. The timer is user-activated (not auto-started). `toggleTimer()` starts, pauses, and resumes. Elapsed time for the session record is computed as `totalSeconds - remainingSeconds`.

`markComplete()` updates the `InProgress` record to `Completed`. `markSkipped()` updates it to `Skipped`.

**`SessionScreen`** shows:
- Exercise name (large), body system label, instructions (scrollable), notes.
- Optional image via `AsyncImage` (Coil).
- Timer: hidden until first tap; shows `MM:SS` countdown once started.
- Done and Skip buttons.
- After Done: an acknowledgment screen with a randomly selected warm message from `strings.xml/acknowledgment_messages`. Then optionally: "Next up: [exercise name]. Start now?" prompt.

### Library Screen (`ui/library/`)

Displays the full exercise library grouped by `bodySystem`. Groups are collapsed by default.

**Search and filter:** `LibraryViewModel` maintains `searchQuery`, `selectedBodySystem`, and `showNotDoneRecently`. Filtering is computed in the ViewModel, not in SQL.

`ExerciseDetailScreen` is read-only. `ExerciseEditScreen` handles both new-exercise creation (when `exerciseId == null`) and editing. Body system input uses an autocomplete approach driven by the live set of distinct `bodySystem` values from the library.

### Progress Screen (`ui/progress/`)

**`ProgressViewModel`** loads a year's worth of sessions for the calendar and streak, 30 days of check-ins for the trend chart, 7-day body-system coverage, and all-time sessions for the lifetime count. All loaded on `Dispatchers.IO` in `loadData()`.

**Streak computation** (`computeStreak()`): walks backwards from today, counting consecutive days with at least one `Completed` session. One gap of exactly one day is forgiven (grace day). Returns 0 if today and yesterday both have no sessions.

**`HumbleBragState`** sealed class drives the Humble Brag overlay: `Idle` (nothing shown), `Ready(phrase: String)` (card visible). State is set by `generateHumbleBrag()` which calls `HumbleBragGenerator.randomPhrase()` — this is a synchronous, instant operation.

**`ProgressScreen`** shows (in order):
- Week summary ("N exercises this week").
- Calendar grid (month navigator, session days highlighted).
- Streak badge + optional milestone banner (at 3/7/14/21/30 and every 30 days after).
- Body-system coverage dots.
- Pain/energy trend chart (Canvas drawing, no third-party chart library).
- Session history log.
- `HumbleBragOverlay` as a `Dialog` when state is `Ready`.

### Settings Screen (`ui/settings/`)

All user preferences in one scrolling list. Sections: Profile (display name), Appearance (theme), Exercise Schedule (daily load slider), Notifications (morning/afternoon/evening toggles + time pickers + quiet hours + up to 3 custom reminders), Check-Ins toggle, Streak toggle, Data (export, import, CSV exports, reset).

**14-day backup reminder banner:** `SettingsViewModel` checks `lastExportAt` (stored in a SharedPreferences-equivalent field) against the current date and shows a banner if more than 14 days have elapsed and new sessions or check-ins exist.

**`ImportScreen`** handles the exercise CSV import flow: file picker → parse → preview counts → confirm → write.

### `CheckInBottomSheet`

A two-layer bottom sheet:

- **Layer 1:** FPS-R pain scale (6-point: 0/2/4/6/8/10) and energy scale displayed as tappable image slots. Currently uses emoji placeholders pending drawable assets (`pain_face_0` through `pain_face_10`).
- **Layer 2 (optional):** BPI interference question, rotating by day of week. Sunday shows a free-text field.

Both layers are always dismissible. The sheet writes a `CheckIn` row on submit.

---

## 8. Notification System

### Four Channels

| Channel ID | Importance | Purpose |
|---|---|---|
| `channel_morning` | DEFAULT | Daily exercise reminder |
| `channel_checkin` | LOW | Afternoon conditional prompt |
| `channel_evening` | MIN | End-of-day acknowledgment |
| `channel_custom` | DEFAULT | User-defined reminders |

Channels are created in `TherapyCompanionApp.onCreate()`. Users can individually silence or configure each channel in Android system settings.

### `NotificationScheduler`

Stateless `object`. `scheduleAll(context, settings)` cancels all existing alarms then re-schedules each enabled notification type using `AlarmManager.setExactAndAllowWhileIdle()`.

**Alarm delivery:** Each alarm fires `NotificationReceiver` as a broadcast. The receiver then re-schedules the same alarm for the next day. This "fire and reschedule" pattern means alarms repeat daily without needing `setRepeating()` (which is inexact and unsuitable for Doze mode).

**Quiet hours** are enforced at scheduling time by `applyQuietHours()`. If a scheduled time falls inside the quiet window, it is advanced to `quietEnd`. No post-fire suppression.

**Intent extras carried to the receiver:**

| Extra | Content |
|---|---|
| `EXTRA_TYPE` | `"morning"`, `"afternoon"`, `"evening"`, or `"custom"` |
| `EXTRA_CHANNEL_ID` | The notification channel to post on |
| `EXTRA_TITLE` / `EXTRA_BODY` | Pre-composed strings (overridden at fire time for evening) |
| `EXTRA_REQUEST_CODE` | Used as the notification ID so each alarm has a unique notification |

### `NotificationReceiver`

`BroadcastReceiver` that handles alarm delivery. Uses `goAsync()` to extend the receiver's lifetime for the coroutine that queries the database.

**Conditional logic at fire time:**

- `TYPE_AFTERNOON` — queries `SessionRepository` for today's completed sessions. Suppresses the notification silently if any exist.
- `TYPE_EVENING` — same query; picks between `"You did something today. That matters."` and `"Rest is also part of healing. See you tomorrow."`.
- `TYPE_MORNING` / `TYPE_CUSTOM` — always fire with the pre-composed body.

After handling, the receiver calls `NotificationScheduler.rescheduleAll()` which reads current settings and re-schedules all alarms for the next day.

### `BootReceiver`

Registered for `BOOT_COMPLETED` and `MY_PACKAGE_REPLACED`. AlarmManager alarms are lost on reboot. This receiver calls `NotificationScheduler.rescheduleAll()` after every boot to restore them.

---

## 9. Backup & Restore

### `BackupWorker`

`CoroutineWorker` scheduled weekly via `WorkManager.enqueueUniquePeriodicWork()` with `ExistingPeriodicWorkPolicy.KEEP`. The `KEEP` policy means calling `schedule()` on every app launch does not reset the timer.

It writes a `auto_backup_<date>.json` file to `getExternalFilesDir(null)/backups/`. This directory does not require storage permission on API 29+. It is removed by Android on uninstall — it is not a substitute for a manual share-sheet export.

After writing, it validates the file by round-tripping it through `JsonExporter.parse()`. If validation fails, the new file is deleted and `Result.retry()` is returned. Once validated, files beyond the newest 4 are pruned.

### `JsonExporter`

Produces a single JSON object:

```json
{
  "version": 1,
  "schemaVersion": 5,
  "exportedAt": 1714000000000,
  "exercises": [...],
  "sessions": [...],
  "checkIns": [...],
  "userSettings": {...}
}
```

All records from all four tables are included. `exportedAt` is a UTC epoch timestamp. `schemaVersion` is the Room database version at export time and is checked by the importer to refuse future-version files.

### `JsonImporter`

Parses the export JSON and writes to the database using one of three strategies chosen by the user:

| Strategy | Behaviour |
|---|---|
| Replace everything | Wipes all four tables, then inserts the file contents. |
| Merge — keep both (default) | Inserts records with new UUIDs; skips UUIDs already present. |
| Merge — prefer file | Same, but overwrites on UUID collision. |

The import is wrapped in a database transaction. A failure partway through rolls back completely.

Import refuses any file where `schemaVersion` exceeds the installed app's current schema version, displaying a message asking the user to update the app first.

### `CsvExporter`

Produces two read-only CSV files (not importable):
- **Session history** — one row per session with date, exercise name, status, elapsed time.
- **Check-in history** — one row per check-in with all FPS-R, energy, BPI, and free-text fields.

Both are UTF-8 encoded, sorted most-recent-first.

### `CsvImporter` (Exercise Import)

Handles Settings → "Import exercises" from a `.csv` or `.tsv` file. All rows are validated before any are committed (all-or-nothing). Flexible aliases are accepted for `frequency`, `days`, and `priority` columns. Auto-detects UTF-8 (with/without BOM), UTF-16 (with BOM), and Windows-1252 encodings. Lines beginning with `#` are treated as comments.

---

## 10. Humble Brag Feature

Located in `humbleBrag/HumbleBragGenerator.kt`. The feature is entirely self-contained and offline.

**`HumbleBragGenerator.randomPhrase()`** — selects one of 20 pre-written encouraging phrases at random. This is a pure function with no side effects.

**`HumbleBragState`** (in `ProgressViewModel.kt`) — sealed class with two states:
- `Idle` — no overlay shown.
- `Ready(phrase: String)` — overlay card is visible with the given phrase.

**`generateHumbleBrag()`** in `ProgressViewModel` — reads current state (streak, sessions this week, lifetime count, body systems) and calls `HumbleBragGenerator.randomPhrase()`. Sets state to `Ready` immediately (no network call, no coroutine needed).

**`HumbleBragOverlay`** composable renders a `Dialog` containing:
- Personalised header ("Sarah's Progress" or "Your Progress").
- Structured stats table: sessions this week, lifetime sessions, current streak (if enabled), body systems this week.
- Divider.
- Randomly selected phrase.
- "Copy" button — formats stats + phrase as plain text, places on clipboard via `LocalClipboardManager`, shows Toast.
- "New quote" button — calls `generateHumbleBrag()` again to pick a fresh phrase; stats are re-read from current state.

**`StreakMilestoneBrag`** composable — shown as an inline banner beneath the streak badge when `isStreakMilestone(streak)` returns true. Milestones: 3, 7, 14, 21, 30, and every 30 days thereafter. Taps into the same overlay via `viewModel.generateHumbleBrag()`.

---

## 11. Common Maintenance Tasks

### Adding a Column to an Existing Table

1. Add the field to the `*Entity` data class with a `@ColumnInfo` annotation and a safe default.
2. Add the corresponding field to the domain model class in `data/model/`.
3. Update `Mappers.kt` — both `toDomain()` and `toEntity()` for that type.
4. Bump `version` in `@Database` on `AppDatabase`.
5. Write a new `Migration` object using `ALTER TABLE ... ADD COLUMN ... DEFAULT ...`.
6. Add the migration to the `addMigrations(...)` call in `buildDatabase()`.
7. If the field is user-facing, update the relevant ViewModel, Repository, and Screen.

> Never use `fallbackToDestructiveMigration()`. It wipes user data.

### Adding a New Notification Type

1. Add a request code constant to `NotificationScheduler`.
2. Add a `TYPE_*` string constant.
3. Create a new `NotificationChannel` in `TherapyCompanionApp.createNotificationChannels()` and add a companion object constant for the channel ID.
4. Add a `schedule()` call in `NotificationScheduler.scheduleAll()`.
5. Add the new request code to `cancelAll()`.
6. Add handling for the new type in `NotificationReceiver.onReceive()`.
7. Add the setting toggle to `UserSettingsEntity`, `UserSettings`, and the migration.

### Adding a New Screen

1. Add a `object` route to `Screen.kt`.
2. Add a `composable(Screen.NewScreen.route) { ... }` block to `NavGraph.kt`.
3. If it should appear in the bottom nav, add it to the `bottomNavItems` list.
4. Create `NewScreen.kt` and `NewViewModel.kt` in a new `ui/newscreen/` package.
5. The ViewModel factory should follow the same `class Factory(...) : ViewModelProvider.Factory` pattern used in every other ViewModel.

### Changing the Encouragement Phrases

The Humble Brag phrases live in `HumbleBragGenerator.kt` as a plain `listOf(...)`. Add, remove, or rewrite entries freely — no other code needs to change.

The post-session acknowledgment messages live in `app/src/main/res/values/strings.xml` in the `acknowledgment_messages` string-array. Edit entries there — no code change required.

### Inspecting the Database

Use Android Studio's App Inspection tool (View → Tool Windows → App Inspection) to browse the live SQLite database on a connected device or emulator. The database is named `therapy_companion.db`.

For a production device, use the backup export feature (Settings → Export data) to get a JSON file, then inspect it directly — it contains all four tables in readable form.

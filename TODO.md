# Therabee — Open Items

Gaps identified against `therapy-companion-spec.md` v1.3. Check off items as they are completed.

---

## §5.1 Home Screen

- [ ] **Time-of-day greeting** — "Good morning/afternoon/evening, [Name]" in the TopAppBar title area. Requires display name from `UserSettings` (see Settings section below).
- [ ] **"You've done N today" summary line** — count of completed exercises shown below the greeting with positive framing.
- [ ] **"Start Next Exercise" primary button** — single prominent CTA that launches the next pending (non-completed, non-skipped) exercise, so the user doesn't have to find the Play button on a card.

---

## §5.3 Session Screen

- [x] **Fix X / Close button** — calls `cancelSession()` which deletes the InProgress record; returns Home with no session written.
- [x] **Full-screen acknowledgment after Done** — `AcknowledgmentScreen` composable shown in-place when `showAcknowledgment = true`; shows exercise name + encouragement message.
- [x] **"Next up: [exercise]" prompt** — `findNextExercise()` in SessionViewModel queries highest-priority incomplete exercise for today; "Start now" button navigates directly into next session.

---

## §5.4 Progress Screen

- [ ] **"Exercises this week: N" summary stat** — a simple count with positive framing at the top of the screen, above the calendar.
- [ ] **Session history log** — scrollable list of past sessions (date, exercise name, elapsed duration, status) below the trend chart.

---

## §5.5 Settings Screen

- [ ] **Display name / greeting name** — text field so users can set the name used in the Home greeting. Requires adding a `displayName` column to `UserSettings` → Room DB migration v3 → v4.
- [ ] **Theme toggle** — Light / Dark / System default. Also requires a `UserSettings` column change and the same DB migration.

---

## Completed

- [x] §5.2 Library screen — search bar, filter chips (body system + "Not done recently"), groups collapsed by default
- [x] §6 Exercise CSV import — parsing, validation, comment stripping, frequency/days/priority aliases, encoding auto-detection, template export
- [x] §10 Backup, restore & update safety — JSON export (share-sheet + file picker), three-strategy restore, weekly auto-backup with 4-file rolling retention, 14-day reminder banner, reset progress, session + check-in CSV export

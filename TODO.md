# Therabee — Open Items

Gaps identified against `therapy-companion-spec.md` v1.3. Check off items as they are completed.

---

## Open

### §5.1 Home Screen
- [ ] Summary line phrasing: change `"N of M done today"` → `"You've done N today — that's great."` No total count (spec is explicit: *"Not '3 of 9 remaining.'"*)

### §5.4 Progress Screen
- [ ] Body system coverage: simple visual showing which systems have been worked in the last 7 days (derived from live library values, not a fixed enum).
- [ ] Streak display: show only if `show_streaks` setting is true and streak is active; 1-day grace period. Add `show_streaks: Boolean` to `UserSettings`.
- [ ] Manual check-in: user can invoke the check-in from the Progress screen at any time, independent of the automatic daily prompt.

### §6 Easier Day Mode
- [ ] Priority filter: when Easier Day is on, select Priority 1 exercises only; if none qualify, fall back to Priority 2. Currently only halves load cap.
- [ ] Duration preference: when Easier Day is on, prefer shortest-duration exercises among those that qualify.

### §7 Notifications
- [ ] Afternoon notification: only fire if zero exercises completed that day. Currently always fires if the setting is on.
- [ ] Evening encouragement: two distinct messages — one if exercises were done (`"You did something today. That matters."`), one if not (`"Rest is also part of healing. See you tomorrow."`). Currently a single static message.
- [ ] Custom reminders: up to 3 additional user-defined reminder times with custom short messages (spec: SHOULD).

### §9 FPS-R Illustrations
- [ ] Replace emoji placeholders with actual drawable assets: `pain_face_0`, `pain_face_2`, `pain_face_4`, `pain_face_6`, `pain_face_8`, `pain_face_10` (dog-face adaptation). Also a distinct 6-illustration set for the energy scale.

### §10.4 Update-Safety Workflow
- [ ] "Before you update" one-time tooltip on first Settings visit.
- [ ] Permanent "Before you update" text in Settings → About screen.

---

## Completed

- [x] §5.1 Home screen — time-of-day greeting (with name when set), "N of M done today" summary line, "Start: [Exercise Name]" full-width button for the next pending exercise
- [x] §5.5 Settings screen — display name text field (Profile section), Light/Dark/System theme toggle (Appearance section); Room DB migration v3 → v4 adds display_name and theme_mode columns; theme applied reactively from MainActivity
- [x] §5.2 Library screen — search bar, filter chips (body system + "Not done recently"), groups collapsed by default
- [x] §5.3 Session screen — close/X acts as pause (deletes InProgress record, no skip written), full-screen acknowledgment after Done, "Next up" prompt with Start now button
- [x] §5.3 Session screen — optional user-activated countdown timer (Start/Pause/Resume); counts down from `durationMinutes × 60`; hidden until first tap; elapsed computed from countdown for session record
- [x] §5.4 Progress screen — "Exercises this week" count with positive framing above the calendar; session history log (date, name, duration, Done/Skipped) below the trend chart
- [x] §6 Exercise CSV import — parsing, validation, comment stripping, frequency/days/priority aliases, encoding auto-detection, template export
- [x] §10 Backup, restore & update safety — JSON export (share-sheet + file picker), three-strategy restore, weekly auto-backup with 4-file rolling retention, 14-day reminder banner, reset progress, session + check-in CSV export

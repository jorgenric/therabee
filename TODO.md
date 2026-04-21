# Therabee — Open Items

Gaps identified against `therapy-companion-spec.md` v1.3. Check off items as they are completed.

---

## Open


### §9 FPS-R Illustrations
- [ ] Replace emoji placeholders with actual drawable assets: `pain_face_0`, `pain_face_2`, `pain_face_4`, `pain_face_6`, `pain_face_8`, `pain_face_10` (dog-face adaptation). Also a distinct 6-illustration set for the energy scale.

### §10.4 Update-Safety Workflow
- [ ] "Before you update" one-time tooltip on first Settings visit.
- [ ] Permanent "Before you update" text in Settings → About screen.

---

## Completed

- [x] §5.2 Log unprompted completions — `SessionStatus.Partial` added; `Session.source` field (Prompted/Adhoc) with DB migration v5→v6; "I just did this" card on ExerciseDetailScreen (date picker, Done/Partial toggle, Save → "Logged!" → auto-back); `logAdhocCompletion()` in LibraryViewModel; CSV and JSON exports include `source` column

- [x] §5.1 Home screen — time-of-day greeting (with name when set), "Start: [Exercise Name]" full-width button for the next pending exercise; summary line shows `"You've done N today"` with no total count per spec language standards
- [x] §5.5 Settings screen — display name text field (Profile section), Light/Dark/System theme toggle (Appearance section); Room DB migration v3 → v4 adds display_name and theme_mode columns; theme applied reactively from MainActivity
- [x] §5.2 Library screen — search bar, filter chips (body system + "Not done recently"), groups collapsed by default
- [x] §5.3 Session screen — close/X acts as pause (deletes InProgress record, no skip written), full-screen acknowledgment after Done, "Next up" prompt with Start now button
- [x] §7 Notifications — afternoon suppressed when exercises done today; evening picks done vs. not-done message at fire time; up to 3 custom reminders with time + message, stored as flat DB columns (v4→v5 migration), scheduled on `CHANNEL_CUSTOM`
- [x] §5.3 Session screen — optional user-activated countdown timer (Start/Pause/Resume); counts down from `durationMinutes × 60`; hidden until first tap; elapsed computed from countdown for session record
- [x] §5.4 Progress screen — "Exercises this week" count with positive framing above the calendar; session history log (date, name, duration, Done/Skipped) below the trend chart
- [x] §5.4 Progress screen — body system coverage (last 7 days, live library values); streak badge with 1-day grace period (gated by `show_streaks` setting); manual check-in button in TopAppBar opens `CheckInBottomSheet` at any time
- [x] §6 Exercise CSV import — parsing, validation, comment stripping, frequency/days/priority aliases, encoding auto-detection, template export
- [x] §6 Easier Day Mode — priority filter (P1 only → fall back P2 → fall back all eligible); duration preference (shorter first within priority); body-system diversity pool scoped to priority-filtered candidates
- [x] §10 Backup, restore & update safety — JSON export (share-sheet + file picker), three-strategy restore, weekly auto-backup with 4-file rolling retention, 14-day reminder banner, reset progress, session + check-in CSV export
- [x] §9.4 Humble Brag — trophy button in Progress TopAppBar; stats overlay card (sessions this week, lifetime, streak, body systems); 20-phrase encouragement library (offline, no API); "New quote" cycles phrases; Copy formats stats + phrase to clipboard; streak milestone banner (at 3/7/14/21/30/60/90… days) with inline "Brag a little" CTA; spec updated to v1.3 to reflect offline-only approach

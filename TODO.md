# Therabee — Open Items

Gaps identified against `therapy-companion-spec.md` v1.3. Check off items as they are completed.

---

## Completed

- [x] §5.1 Home screen — time-of-day greeting (with name when set), "N of M done today" summary line, "Start: [Exercise Name]" full-width button for the next pending exercise
- [x] §5.5 Settings screen — display name text field (Profile section), Light/Dark/System theme toggle (Appearance section); Room DB migration v3 → v4 adds display_name and theme_mode columns; theme applied reactively from MainActivity
- [x] §5.2 Library screen — search bar, filter chips (body system + "Not done recently"), groups collapsed by default
- [x] §5.3 Session screen — close/X acts as pause (deletes InProgress record, no skip written), full-screen acknowledgment after Done, "Next up" prompt with Start now button
- [x] §5.4 Progress screen — "Exercises this week" count with positive framing above the calendar; session history log (date, name, duration, Done/Skipped) below the trend chart
- [x] §6 Exercise CSV import — parsing, validation, comment stripping, frequency/days/priority aliases, encoding auto-detection, template export
- [x] §10 Backup, restore & update safety — JSON export (share-sheet + file picker), three-strategy restore, weekly auto-backup with 4-file rolling retention, 14-day reminder banner, reset progress, session + check-in CSV export

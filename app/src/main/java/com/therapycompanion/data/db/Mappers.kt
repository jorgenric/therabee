package com.therapycompanion.data.db

import com.therapycompanion.data.model.CheckIn
import com.therapycompanion.data.model.Exercise
import com.therapycompanion.data.model.Frequency
import com.therapycompanion.data.model.Session
import com.therapycompanion.data.model.SessionStatus
import com.therapycompanion.data.model.UserSettings

// ── Exercise ──────────────────────────────────────────────────────────────────

fun ExerciseEntity.toDomain(): Exercise = Exercise(
    id = id,
    name = name,
    bodySystem = bodySystem,
    instructions = instructions,
    notes = notes,
    durationMinutes = durationMinutes,
    frequency = Frequency.fromString(frequency),
    scheduledDays = scheduledDays,
    priority = priority,
    active = active,
    imageFileName = imageFileName,
    videoFileName = videoFileName,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Exercise.toEntity(): ExerciseEntity = ExerciseEntity(
    id = id,
    name = name,
    bodySystem = bodySystem,
    instructions = instructions,
    notes = notes,
    durationMinutes = durationMinutes,
    frequency = frequency.name,
    scheduledDays = scheduledDays,
    priority = priority,
    active = active,
    imageFileName = imageFileName,
    videoFileName = videoFileName,
    createdAt = createdAt,
    updatedAt = updatedAt
)

// ── Session ───────────────────────────────────────────────────────────────────

fun SessionEntity.toDomain(): Session = Session(
    id = id,
    exerciseId = exerciseId,
    startedAt = startedAt,
    completedAt = completedAt,
    elapsedSeconds = elapsedSeconds,
    status = SessionStatus.fromString(status),
    notes = notes
)

fun Session.toEntity(): SessionEntity = SessionEntity(
    id = id,
    exerciseId = exerciseId,
    startedAt = startedAt,
    completedAt = completedAt,
    elapsedSeconds = elapsedSeconds,
    status = status.name,
    notes = notes
)

// ── CheckIn ───────────────────────────────────────────────────────────────────

fun CheckInEntity.toDomain(): CheckIn = CheckIn(
    id = id,
    checkedInAt = checkedInAt,
    painScore = painScore,
    energyScore = energyScore,
    bpiDomain = bpiDomain,
    bpiScore = bpiScore,
    freeText = freeText,
    dismissed = dismissed
)

fun CheckIn.toEntity(): CheckInEntity = CheckInEntity(
    id = id,
    checkedInAt = checkedInAt,
    painScore = painScore,
    energyScore = energyScore,
    bpiDomain = bpiDomain,
    bpiScore = bpiScore,
    freeText = freeText,
    dismissed = dismissed
)

// ── UserSettings ──────────────────────────────────────────────────────────────

fun UserSettingsEntity.toDomain(): UserSettings = UserSettings(
    dailyLoad = dailyLoad,
    easierDayEnabled = easierDayEnabled,
    morningReminderEnabled = morningReminderEnabled,
    morningReminderTime = morningReminderTime,
    afternoonCheckInEnabled = afternoonCheckInEnabled,
    afternoonCheckInTime = afternoonCheckInTime,
    eveningEncouragementEnabled = eveningEncouragementEnabled,
    eveningEncouragementTime = eveningEncouragementTime,
    quietHoursStart = quietHoursStart,
    quietHoursEnd = quietHoursEnd,
    checkInsEnabled = checkInsEnabled
)

fun UserSettings.toEntity(): UserSettingsEntity = UserSettingsEntity(
    id = 1,
    dailyLoad = dailyLoad,
    easierDayEnabled = easierDayEnabled,
    morningReminderEnabled = morningReminderEnabled,
    morningReminderTime = morningReminderTime,
    afternoonCheckInEnabled = afternoonCheckInEnabled,
    afternoonCheckInTime = afternoonCheckInTime,
    eveningEncouragementEnabled = eveningEncouragementEnabled,
    eveningEncouragementTime = eveningEncouragementTime,
    quietHoursStart = quietHoursStart,
    quietHoursEnd = quietHoursEnd,
    checkInsEnabled = checkInsEnabled
)

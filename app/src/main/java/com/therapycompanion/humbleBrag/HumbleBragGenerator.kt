package com.therapycompanion.humbleBrag

object HumbleBragGenerator {

    private val phrases = listOf(
        "Showing up is the hardest part — and you did it.",
        "Every session is a step toward feeling better.",
        "Consistency like yours is what real progress looks like.",
        "Your body is working hard, and so are you.",
        "Small efforts add up to big changes over time.",
        "You're building strength, one session at a time.",
        "Healing takes patience — you're practicing both.",
        "This is what commitment to your health looks like.",
        "Progress isn't always visible, but it's always real.",
        "You showed up for yourself. That matters.",
        "Every completed session is a gift to your future self.",
        "Rest is part of recovery, and so is this.",
        "You're doing something most people talk about but don't do.",
        "Your dedication to your health is something to be proud of.",
        "One session at a time — that's how it's done.",
        "Therapy is hard. You're doing it anyway.",
        "The work you're putting in now is investing in tomorrow.",
        "Your consistency is your superpower.",
        "Being kind to your body is a practice. You're practicing.",
        "You're further along than you were yesterday."
    )

    fun randomPhrase(): String = phrases.random()
}

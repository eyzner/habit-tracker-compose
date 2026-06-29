package ru.netology.habittracker.model

data class Habit(
    val id: Long,
    val title: String,
    val completedDays: Set<Int> = emptySet(),
) {
    val isCompletedToday: Boolean
        get() = completedDays.contains(todayIndex())

    val progress: Float
        get() = completedDays.size / DAYS_IN_WEEK.toFloat()

    companion object {
        const val DAYS_IN_WEEK = 7

        fun todayIndex(): Int {
            val calendar = java.util.Calendar.getInstance()
            val day = calendar.get(java.util.Calendar.DAY_OF_WEEK)
            return when (day) {
                java.util.Calendar.MONDAY -> 0
                java.util.Calendar.TUESDAY -> 1
                java.util.Calendar.WEDNESDAY -> 2
                java.util.Calendar.THURSDAY -> 3
                java.util.Calendar.FRIDAY -> 4
                java.util.Calendar.SATURDAY -> 5
                else -> 6
            }
        }
    }
}

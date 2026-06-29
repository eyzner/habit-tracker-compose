package ru.netology.habittracker.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.netology.habittracker.model.Habit

class HabitViewModel : ViewModel() {
    private var nextId = 1L

    private val _habits = MutableStateFlow(emptyList<Habit>())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    private val _draftTitle = MutableStateFlow("")
    val draftTitle: StateFlow<String> = _draftTitle.asStateFlow()

    fun updateDraftTitle(title: String) {
        _draftTitle.value = title
    }

    fun createHabit(): Boolean {
        val title = _draftTitle.value.trim()
        if (title.isBlank()) return false

        _habits.value = _habits.value + Habit(id = nextId++, title = title)
        _draftTitle.value = ""
        return true
    }

    fun clearDraft() {
        _draftTitle.value = ""
    }

    fun toggleToday(habitId: Long) {
        val today = Habit.todayIndex()
        _habits.value = _habits.value.map { habit ->
            if (habit.id != habitId) {
                habit
            } else {
                val newDays = if (habit.completedDays.contains(today)) {
                    habit.completedDays - today
                } else {
                    habit.completedDays + today
                }
                habit.copy(completedDays = newDays)
            }
        }
    }

    fun deleteHabit(habitId: Long) {
        _habits.value = _habits.value.filterNot { it.id == habitId }
    }

    fun progressForDay(dayIndex: Int): Float {
        val list = _habits.value
        if (list.isEmpty()) return 0f
        val completed = list.count { it.completedDays.contains(dayIndex) }
        return completed / list.size.toFloat()
    }
}

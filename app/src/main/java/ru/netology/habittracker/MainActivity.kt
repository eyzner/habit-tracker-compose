package ru.netology.habittracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.netology.habittracker.model.Habit
import ru.netology.habittracker.ui.theme.HabitTrackerTheme
import ru.netology.habittracker.viewmodel.HabitViewModel
import kotlin.math.abs
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HabitTrackerTheme {
                val viewModel: HabitViewModel = viewModel()
                var screen by rememberSaveable { mutableStateOf(AppScreen.List.name) }

                BackHandler(enabled = screen == AppScreen.Create.name) {
                    viewModel.clearDraft()
                    screen = AppScreen.List.name
                }

                AnimatedContent(
                    targetState = screen,
                    label = "screen"
                ) { target ->
                    when (target) {
                        AppScreen.Create.name -> CreateHabitScreen(
                            viewModel = viewModel,
                            onBack = {
                                viewModel.clearDraft()
                                screen = AppScreen.List.name
                            },
                            onSaved = {
                                screen = AppScreen.List.name
                            }
                        )

                        else -> HabitListScreen(
                            viewModel = viewModel,
                            onCreateClick = {
                                screen = AppScreen.Create.name
                            }
                        )
                    }
                }
            }
        }
    }
}

private enum class AppScreen {
    List,
    Create
}

private val weekDays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HabitListScreen(
    viewModel: HabitViewModel,
    onCreateClick: () -> Unit,
) {
    val habits by viewModel.habits.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Трекер привычек") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateClick) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            WeekProgress(
                habits = habits,
                progressForDay = viewModel::progressForDay
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (habits.isEmpty()) {
                EmptyState(onCreateClick = onCreateClick)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        bottom = 96.dp + WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding()
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = habits,
                        key = { it.id }
                    ) { habit ->
                        SwipeHabitCard(
                            habit = habit,
                            onToggleToday = { viewModel.toggleToday(habit.id) },
                            onDelete = { viewModel.deleteHabit(habit.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekProgress(
    habits: List<Habit>,
    progressForDay: (Int) -> Float,
) {
    val today = Habit.todayIndex()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Прогресс по дням недели",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekDays.forEachIndexed { index, day ->
                    val progress = progressForDay(index)
                    WeekDayProgressItem(
                        day = day,
                        percent = (progress * 100).roundToInt(),
                        isToday = index == today,
                        hasHabits = habits.isNotEmpty()
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekDayProgressItem(
    day: String,
    percent: Int,
    isToday: Boolean,
    hasHabits: Boolean,
) {
    val color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (isToday) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surface
                )
                .border(1.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = if (hasHabits) "$percent%" else "0%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SwipeHabitCard(
    habit: Habit,
    onToggleToday: () -> Unit,
    onDelete: () -> Unit,
) {
    var dragAmount by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .pointerInput(habit.id) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (abs(dragAmount) > 150f) {
                            onDelete()
                        }
                        dragAmount = 0f
                    },
                    onHorizontalDrag = { _, drag ->
                        dragAmount += drag
                    }
                )
            }
    ) {
        Text(
            text = "Свайп — удалить",
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 24.dp),
            color = MaterialTheme.colorScheme.onErrorContainer,
            fontWeight = FontWeight.Bold
        )

        HabitCard(
            habit = habit,
            onToggleToday = onToggleToday
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HabitCard(
    habit: Habit,
    onToggleToday: () -> Unit,
) {
    val progressPercent = (habit.progress * 100).roundToInt()
    val today = Habit.todayIndex()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = habit.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (habit.isCompletedToday) "Сегодня: выполнено" else "Сегодня: не выполнено",
                style = MaterialTheme.typography.bodyMedium,
                color = if (habit.isCompletedToday) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { habit.progress },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Прогресс привычки: $progressPercent%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                weekDays.forEachIndexed { index, day ->
                    HabitDayChip(
                        day = day,
                        checked = habit.completedDays.contains(index),
                        enabled = index == today,
                        isToday = index == today,
                        onClick = onToggleToday
                    )
                }
            }
        }
    }
}

@Composable
private fun HabitDayChip(
    day: String,
    checked: Boolean,
    enabled: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
) {
    val background = when {
        checked -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = when {
        checked -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(enabled = enabled, onClick = onClick),
        color = background,
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = day,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = textColor,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun EmptyState(onCreateClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Пока нет привычек",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Добавьте первую привычку и отмечайте выполнение каждый день.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = onCreateClick) {
            Text("Добавить привычку")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateHabitScreen(
    viewModel: HabitViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val title by viewModel.draftTitle.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Создание привычки") },
                navigationIcon = {
                    Text(
                        text = "Назад",
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .clickable(onClick = onBack),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = viewModel::updateDraftTitle,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Название привычки") },
                placeholder = { Text("Например, читать 20 минут") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                )
            )

            Button(
                onClick = {
                    if (viewModel.createHabit()) {
                        onSaved()
                    }
                },
                enabled = title.trim().isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сохранить")
            }
        }
    }
}


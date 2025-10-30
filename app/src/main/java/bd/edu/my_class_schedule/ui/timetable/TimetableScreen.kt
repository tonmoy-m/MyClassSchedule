package bd.edu.my_class_schedule.ui.timetable

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import bd.edu.my_class_schedule.data.db.entities.Session

@Composable
fun TimetableRoute() {
    // No factory needed for AndroidViewModel; Compose will use the Activity’s default factory.
    val vm: TimetableViewModel = viewModel()
    TimetableScreen(vm = vm)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(vm: TimetableViewModel) {
    val sessionItems by vm.sessionItems.collectAsState()
    val selectedDay by vm.selectedDay.collectAsState()
    val selectedWeekType by vm.selectedWeekType.collectAsState()
    val showAddDialog by vm.showAddDialog.collectAsState()

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(title = { Text(text = "Timetable") })
                DayTabs(selectedDay = selectedDay, onSelect = vm::selectDay)
                WeekTypeChips(selected = selectedWeekType, onSelect = vm::selectWeekType)
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = vm::openAddDialog) { Text("+") }
        }
    ) { padding ->
        if (sessionItems.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No sessions yet")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sessionItems, key = { it.id }) { s ->
                    SessionCard(item = s, onDelete = { vm.deleteSession(s.id) })
                }
            }
        }
    }

    if (showAddDialog) {
        AddSessionDialog(vm = vm, onDismiss = vm::closeAddDialog, onSave = vm::saveNewSession)
    }
}

@Composable
private fun DayTabs(selectedDay: Int, onSelect: (Int) -> Unit) {
    val days = listOf("Mon" to 1, "Tue" to 2, "Wed" to 3, "Thu" to 4, "Fri" to 5, "Sat" to 6, "Sun" to 7)
    ScrollableTabRow(selectedTabIndex = days.indexOfFirst { it.second == selectedDay }) {
        days.forEach { (label, iso) ->
            Tab(
                selected = selectedDay == iso,
                onClick = { onSelect(iso) },
                text = { Text(label) }
            )
        }
    }
}

@Composable
private fun WeekTypeChips(selected: Int, onSelect: (Int) -> Unit) {
    Row(
        Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(selected = selected == Session.BOTH, onClick = { onSelect(Session.BOTH) }, label = { Text("Both") })
        FilterChip(selected = selected == Session.ODD, onClick = { onSelect(Session.ODD) }, label = { Text("Odd") })
        FilterChip(selected = selected == Session.EVEN, onClick = { onSelect(Session.EVEN) }, label = { Text("Even") })
    }
}

@Composable
private fun SessionCard(item: SessionUi, onDelete: () -> Unit) {
    val tf = remember { DateTimeFormatter.ofPattern("HH:mm") }
    ElevatedCard {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = item.subjectTitle, style = MaterialTheme.typography.titleMedium)
            val subtitle = buildString {
                append(item.start.format(tf)).append(" - ").append(item.end.format(tf))
                if (!item.room.isNullOrBlank()) append(" • Room: ").append(item.room)
                item.teacherName?.let { append(" • ").append(it) }
                when (item.weekType) {
                    Session.ODD -> append(" • Odd weeks")
                    Session.EVEN -> append(" • Even weeks")
                }
            }
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onDelete) { Text("Delete") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSessionDialog(vm: TimetableViewModel, onDismiss: () -> Unit, onSave: () -> Unit) {
    val subjects by vm.subjects.collectAsState()
    val teachers by vm.teachers.collectAsState()
    val subjectId by vm.newSubjectId.collectAsState()
    val teacherId by vm.newTeacherId.collectAsState()
    val room by vm.newRoom.collectAsState()
    val start by vm.newStart.collectAsState()
    val end by vm.newEnd.collectAsState()
    val weekType by vm.newWeekType.collectAsState()

    val tf = remember { DateTimeFormatter.ofPattern("HH:mm") }
    var startText by remember(start) { mutableStateOf(TextFieldValue(start.format(tf))) }
    var endText by remember(end) { mutableStateOf(TextFieldValue(end.format(tf))) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                parseTimeOrNull(startText.text)?.let { vm.setNewStart(it) }
                parseTimeOrNull(endText.text)?.let { vm.setNewEnd(it) }
                onSave()
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Add Session") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBoxWrapper(
                    label = "Subject",
                    items = subjects.map { it.title to it.id },
                    selectedId = subjectId,
                    onSelected = vm::setNewSubject
                )
                ExposedDropdownMenuBoxWrapper(
                    label = "Teacher",
                    items = teachers.map { it.name to it.id },
                    selectedId = teacherId,
                    onSelected = vm::setNewTeacher,
                    allowNull = true
                )
                OutlinedTextField(
                    value = room,
                    onValueChange = vm::setNewRoom,
                    label = { Text("Room") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = startText,
                    onValueChange = { startText = it },
                    label = { Text("Start (HH:mm)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = endText,
                    onValueChange = { endText = it },
                    label = { Text("End (HH:mm)") },
                    singleLine = true
                )
                WeekTypeChips(selected = weekType, onSelect = vm::setNewWeekType)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExposedDropdownMenuBoxWrapper(
    label: String,
    items: List<Pair<String, Long>>,
    selectedId: Long?,
    onSelected: (Long?) -> Unit,
    allowNull: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = items.firstOrNull { it.second == selectedId }?.first
        ?: if (allowNull) "None" else "Select"

    Column {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    .fillMaxWidth(),
                value = selectedLabel,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                if (allowNull) {
                    DropdownMenuItem(
                        text = { Text("None") },
                        onClick = {
                            onSelected(null)
                            expanded = false
                        }
                    )
                }
                items.forEach { (labelText, id) ->
                    DropdownMenuItem(
                        text = { Text(labelText) },
                        onClick = {
                            onSelected(id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

private fun parseTimeOrNull(text: String): LocalTime? = runCatching {
    LocalTime.parse(text, DateTimeFormatter.ofPattern("HH:mm"))
}.getOrNull()
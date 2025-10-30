@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package bd.edu.my_class_schedule.ui.homework

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import bd.edu.my_class_schedule.data.db.entities.Homework
import bd.edu.my_class_schedule.data.db.entities.HomeworkType

@Composable
fun HomeworkRoute(vm: HomeworkViewModel = viewModel()) {
    HomeworkScreen(vm)
}

@Composable
fun HomeworkScreen(vm: HomeworkViewModel) {
    val list by vm.filtered.collectAsState()
    val query by vm.query.collectAsState()

    val showDialog by vm.showDialog.collectAsState()
    val title by vm.title.collectAsState()
    val subject by vm.subject.collectAsState()
    val description by vm.description.collectAsState()
    val dueDateMillis by vm.dueDateMillis.collectAsState()
    val isCompleted by vm.isCompleted.collectAsState()
    val type by vm.type.collectAsState()

    val subjects by vm.subjects.collectAsState()

    val snackbar = remember { SnackbarHostState() }
    val showDatePicker = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(title = { Text("Homework") })
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    value = query,
                    onValueChange = vm::setQuery,
                    label = { Text("Search (title, subject, description)") },
                    singleLine = true
                )
            }
        },
        floatingActionButton = { FloatingActionButton(onClick = vm::openAdd) { Text("+") } },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        if (list.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No homework yet")
            }
        } else {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(list, key = { it.id }) { hw ->
                    HomeworkCard(
                        hw = hw,
                        onToggleCompleted = { vm.toggleCompleted(hw.id) },
                        onEdit = { vm.openEdit(hw) },
                        onDelete = { vm.delete(hw.id) }
                    )
                }
            }
        }
    }

    if (showDialog) {
        val isTitleError = title.isBlank()
        AlertDialog(
            onDismissRequest = vm::closeDialog,
            title = { Text(if (isCompleted) "Homework (completed)" else "Homework") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = vm::setTitle,
                        label = { Text("Title*") },
                        singleLine = true,
                        isError = isTitleError,
                        supportingText = { if (isTitleError) Text("Title is required") }
                    )

                    // Type selection
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TypeChip(current = type, target = HomeworkType.HOMEWORK, onSelect = vm::setType, label = "Homework")
                        TypeChip(current = type, target = HomeworkType.ASSIGNMENT, onSelect = vm::setType, label = "Assignment")
                        TypeChip(current = type, target = HomeworkType.LAB_REPORT, onSelect = vm::setType, label = "Lab report")
                    }

                    // Subject dropdown (like Timetable)
                    val items = subjects.map { it.title to it.id }
                    val selectedId = remember(subjects, subject) {
                        items.firstOrNull { it.first.equals(subject, ignoreCase = true) }?.second
                    }

                    SubjectDropdown(
                        label = "Subject",
                        items = items,
                        selectedId = selectedId,
                        onSelected = { chosenId ->
                            val name = items.firstOrNull { it.second == chosenId }?.first
                            vm.setSubject(name ?: "")
                        },
                        allowNull = true,
                        onClear = { vm.setSubject("") }
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = vm::setDescription,
                        label = { Text("Description") },
                        minLines = 2
                    )

                    val dueText = HomeworkViewModel.formatDate(dueDateMillis) ?: "No due date"
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Due: $dueText", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.width(12.dp))
                        FilledTonalButton(onClick = { showDatePicker.value = true }) { Text("Pick date") }
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = { vm.setDueDateMillis(null) }) { Text("Clear") }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AssistChip(
                            onClick = { vm.setCompleted(!isCompleted) },
                            label = { Text(if (isCompleted) "Completed" else "Pending") }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { vm.save() }, enabled = !isTitleError) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = vm::closeDialog) { Text("Cancel") } }
        )
    }

    if (showDatePicker.value) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker.value = false },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showDatePicker.value = false }) { Text("Cancel") } }
        ) {
            val pickerState = rememberDatePickerState(initialSelectedDateMillis = dueDateMillis)
            Column {
                DatePicker(state = pickerState)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        val selected = pickerState.selectedDateMillis
                        vm.setDueDateMillis(selected)
                        showDatePicker.value = false
                    }) { Text("OK") }
                }
            }
        }
    }
}

@Composable
private fun TypeChip(
    current: HomeworkType,
    target: HomeworkType,
    onSelect: (HomeworkType) -> Unit,
    label: String
) {
    FilterChip(
        selected = current == target,
        onClick = { onSelect(target) },
        label = { Text(label) }
    )
}

@Composable
private fun SubjectDropdown(
    label: String,
    items: List<Pair<String, Long>>,
    selectedId: Long?,
    onSelected: (Long?) -> Unit,
    allowNull: Boolean = false,
    onClear: (() -> Unit)? = null
) {
    val expanded = remember { mutableStateOf(false) }
    val selectedLabel = items.firstOrNull { it.second == selectedId }?.first
        ?: if (allowNull) "None" else "Select"

    Column {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        ExposedDropdownMenuBox(
            expanded = expanded.value,
            onExpandedChange = { expanded.value = it }
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    .fillMaxWidth(),
                value = selectedLabel,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded.value) }
            )
            DropdownMenu(
                expanded = expanded.value,
                onDismissRequest = { expanded.value = false }
            ) {
                if (allowNull) {
                    DropdownMenuItem(
                        text = { Text("None") },
                        onClick = {
                            onSelected(null)
                            onClear?.invoke()
                            expanded.value = false
                        }
                    )
                }
                items.forEach { (labelText, id) ->
                    DropdownMenuItem(
                        text = { Text(labelText) },
                        onClick = {
                            onSelected(id)
                            expanded.value = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeworkCard(
    hw: Homework,
    onToggleCompleted: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    hw.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                AssistChip(
                    onClick = onToggleCompleted,
                    label = { Text(if (hw.isCompleted) "Done" else "Mark done") }
                )
            }

            val meta = listOfNotNull(
                when (hw.type) {
                    HomeworkType.HOMEWORK -> "Homework"
                    HomeworkType.ASSIGNMENT -> "Assignment"
                    HomeworkType.LAB_REPORT -> "Lab report"
                },
                hw.subject?.takeIf { it.isNotBlank() },
                HomeworkViewModel.formatDate(hw.dueDateMillis)?.let { "Due: $it" }
            ).joinToString(" • ")
            if (meta.isNotBlank()) {
                Text(meta, style = MaterialTheme.typography.bodyMedium)
            }

            if (!hw.description.isNullOrBlank()) {
                Text(hw.description, style = MaterialTheme.typography.bodySmall)
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onEdit) { Text("Edit") }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onDelete) { Text("Delete") }
            }
        }
    }
}
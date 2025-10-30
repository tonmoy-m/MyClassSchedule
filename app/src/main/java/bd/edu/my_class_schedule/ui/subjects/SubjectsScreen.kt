@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package bd.edu.my_class_schedule.ui.subjects

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import bd.edu.my_class_schedule.data.db.entities.Subject

@Composable
fun SubjectsRoute() {
    val context = LocalContext.current
    val vm: SubjectsViewModel = viewModel(factory = SubjectsViewModel.provideFactory(context))
    SubjectsScreen(vm)
}

@Composable
fun SubjectsScreen(vm: SubjectsViewModel) {
    val list by vm.filtered.collectAsState()
    val query by vm.query.collectAsState()

    val showDialog by vm.showDialog.collectAsState()
    val code by vm.code.collectAsState()
    val title by vm.title.collectAsState()
    val creditText by vm.creditText.collectAsState()
    val shortName by vm.shortName.collectAsState()

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(title = { Text("Subjects") })
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    value = query,
                    onValueChange = vm::setQuery,
                    label = { Text("Search (code, title, short name)") },
                    singleLine = true
                )
            }
        },
        floatingActionButton = { FloatingActionButton(onClick = vm::openAdd) { Text("+") } }
    ) { padding ->
        if (list.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No subjects yet")
            }
        } else {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(list, key = { it.id }) { s ->
                    SubjectCard(
                        s = s,
                        onEdit = { vm.openEdit(s) },
                        onDelete = { vm.delete(s) }
                    )
                }
            }
        }
    }

    if (showDialog) {
        val isCodeError = code.isBlank()
        val isTitleError = title.isBlank()
        val credit = creditText.toDoubleOrNull()
        val isCreditError = credit == null || credit <= 0.0

        AlertDialog(
            onDismissRequest = vm::closeDialog,
            title = { Text("Subject") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = vm::setCode,
                        label = { Text("Course code*") },
                        singleLine = true,
                        isError = isCodeError,
                        supportingText = { if (isCodeError) Text("Code is required") }
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = vm::setTitle,
                        label = { Text("Course title*") },
                        singleLine = true,
                        isError = isTitleError,
                        supportingText = { if (isTitleError) Text("Title is required") }
                    )
                    OutlinedTextField(
                        value = creditText,
                        onValueChange = vm::setCreditText,
                        label = { Text("Credit*") },
                        singleLine = true,
                        isError = isCreditError,
                        supportingText = { if (isCreditError) Text("Enter a positive number") }
                    )
                    OutlinedTextField(
                        value = shortName,
                        onValueChange = vm::setShortName,
                        label = { Text("Short name (optional)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { vm.save() },
                    enabled = !isCodeError && !isTitleError && !isCreditError
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = vm::closeDialog) { Text("Cancel") } }
        )
    }
}

@Composable
private fun SubjectCard(
    s: Subject,
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
                    "${s.code} — ${s.title}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text("${s.credit}", style = MaterialTheme.typography.bodyMedium)
            }
            if (!s.shortName.isNullOrBlank()) {
                Text("Short: ${s.shortName}", style = MaterialTheme.typography.bodySmall)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onEdit) { Text("Edit") }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onDelete) { Text("Delete") }
            }
        }
    }
}
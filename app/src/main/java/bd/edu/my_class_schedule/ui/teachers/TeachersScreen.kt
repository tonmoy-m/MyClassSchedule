@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package bd.edu.my_class_schedule.ui.teachers

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@Composable
fun TeachersRoute(vm: TeachersViewModel = viewModel()) {
    TeachersScreen(vm)
}

@Composable
fun TeachersScreen(vm: TeachersViewModel) {
    val teachers by vm.filteredTeachers.collectAsState()
    val query by vm.query.collectAsState()

    val showDialog by vm.showDialog.collectAsState()
    val name by vm.name.collectAsState()
    val designation by vm.designation.collectAsState()
    val dept by vm.dept.collectAsState()
    val personalEmail by vm.personalEmail.collectAsState()
    val officialEmail by vm.officialEmail.collectAsState()
    val personalPhone by vm.personalPhone.collectAsState()
    val officialPhone by vm.officialPhone.collectAsState()
    val notes by vm.notes.collectAsState()

    val snackBarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current

    fun showSnack(message: String) {
        scope.launch { snackBarHost.showSnackbar(message) }
    }

    fun dial(number: String) {
        if (number.isBlank()) return
        val uri = "tel:${Uri.encode(number)}".toUri()
        val intent = Intent(Intent.ACTION_DIAL, uri)
        runCatching { context.startActivity(intent) }
            .onFailure { showSnack("No dialer available") }
    }

    fun email(address: String) {
        if (address.isBlank()) return
        val uri = "mailto:$address".toUri()
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        runCatching { context.startActivity(intent) }
            .onFailure { showSnack("No email app available") }
    }

    fun copy(text: String, label: String) {
        @Suppress("DEPRECATION")
        run { clipboard.setText(AnnotatedString(text)) }
        showSnack("$label copied")
    }

    @Composable
    fun ContactRow(
        label: String,
        value: String,
        primaryIcon: @Composable () -> Unit,
        onPrimaryAction: () -> Unit,
        onCopy: () -> Unit,
        contentDescriptionPrimary: String,
        contentDescriptionCopy: String,
        modifier: Modifier = Modifier
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: $value",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
                    .clickable(onClick = onPrimaryAction)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onPrimaryAction) {
                    primaryIcon()
                }
                IconButton(onClick = onCopy) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = contentDescriptionCopy
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(title = { Text("Teachers") })
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    value = query,
                    onValueChange = vm::setQuery,
                    label = { Text("Search (name, dept, email, phone)") },
                    singleLine = true
                )
            }
        },
        floatingActionButton = { FloatingActionButton(onClick = vm::openAdd) { Text("+") } },
        snackbarHost = { SnackbarHost(snackBarHost) }
    ) { padding ->
        if (teachers.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No teachers yet")
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(teachers, key = { it.id }) { t ->
                    ElevatedCard {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(t.name, style = MaterialTheme.typography.titleMedium)

                            val details = listOfNotNull(
                                t.designation?.takeIf { it.isNotBlank() },
                                t.dept?.takeIf { it.isNotBlank() }
                            ).joinToString(" • ")
                            if (details.isNotBlank()) {
                                Text(details, style = MaterialTheme.typography.bodyMedium)
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (!t.officialPhone.isNullOrBlank()) {
                                    ContactRow(
                                        label = "Official ph",
                                        value = t.officialPhone,
                                        primaryIcon = { Icon(Icons.Filled.Phone, contentDescription = "Call") },
                                        onPrimaryAction = { dial(t.officialPhone) },
                                        onCopy = { copy(t.officialPhone, "Phone") },
                                        contentDescriptionPrimary = "Call",
                                        contentDescriptionCopy = "Copy phone"
                                    )
                                }
                                if (!t.personalPhone.isNullOrBlank()) {
                                    ContactRow(
                                        label = "Personal ph",
                                        value = t.personalPhone,
                                        primaryIcon = { Icon(Icons.Filled.Phone, contentDescription = "Call") },
                                        onPrimaryAction = { dial(t.personalPhone) },
                                        onCopy = { copy(t.personalPhone, "Phone") },
                                        contentDescriptionPrimary = "Call",
                                        contentDescriptionCopy = "Copy phone"
                                    )
                                }
                                if (!t.officialEmail.isNullOrBlank()) {
                                    ContactRow(
                                        label = "Official email",
                                        value = t.officialEmail,
                                        primaryIcon = { Icon(Icons.Filled.Email, contentDescription = "Email") },
                                        onPrimaryAction = { email(t.officialEmail) },
                                        onCopy = { copy(t.officialEmail, "Email") },
                                        contentDescriptionPrimary = "Send email",
                                        contentDescriptionCopy = "Copy email"
                                    )
                                }
                                if (!t.personalEmail.isNullOrBlank()) {
                                    ContactRow(
                                        label = "Personal email",
                                        value = t.personalEmail,
                                        primaryIcon = { Icon(Icons.Filled.Email, contentDescription = "Email") },
                                        onPrimaryAction = { email(t.personalEmail) },
                                        onCopy = { copy(t.personalEmail, "Email") },
                                        contentDescriptionPrimary = "Send email",
                                        contentDescriptionCopy = "Copy email"
                                    )
                                }
                            }

                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = { vm.openEdit(t) }) { Text("Edit") }
                                Spacer(Modifier.width(8.dp))
                                TextButton(onClick = { vm.delete(t.id) }) { Text("Delete") }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        val isNameError = name.isBlank()
        AlertDialog(
            onDismissRequest = vm::closeDialog,
            title = { Text("Teacher") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = vm::setName,
                        label = { Text("Name*") },
                        singleLine = true,
                        isError = isNameError,
                        supportingText = {
                            if (isNameError) Text("Name is required")
                        }
                    )
                    OutlinedTextField(
                        value = designation,
                        onValueChange = vm::setDesignation,
                        label = { Text("Designation") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = dept,
                        onValueChange = vm::setDept,
                        label = { Text("Department") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = officialEmail,
                        onValueChange = vm::setOfficialEmail,
                        label = { Text("Official email") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = personalEmail,
                        onValueChange = vm::setPersonalEmail,
                        label = { Text("Personal email") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = officialPhone,
                        onValueChange = vm::setOfficialPhone,
                        label = { Text("Official phone") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = personalPhone,
                        onValueChange = vm::setPersonalPhone,
                        label = { Text("Personal phone") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = vm::setNotes,
                        label = { Text("Notes") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { vm.save() },
                    enabled = !isNameError
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = vm::closeDialog) { Text("Cancel") } }
        )
    }
}
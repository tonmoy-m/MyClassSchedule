package bd.edu.my_class_schedule.ui.teachers

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import bd.edu.my_class_schedule.data.db.DatabaseProvider
import bd.edu.my_class_schedule.data.db.entities.Teacher
import bd.edu.my_class_schedule.data.repo.TeacherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class TeachersViewModel(app: Application) : AndroidViewModel(app) {
    private val db by lazy { DatabaseProvider.get(getApplication()) }
    private val repo by lazy { TeacherRepository(db.teacherDao()) }

    // Raw list from DB
    val teachers = repo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Search query
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()
    fun setQuery(v: String) { _query.value = v }

    // Filtered + sorted list for UI
    val filteredTeachers: StateFlow<List<Teacher>> = combine(teachers, _query) { list, q ->
        val s = q.trim().lowercase(Locale.ROOT)
        val filtered = if (s.isEmpty()) {
            list
        } else {
            list.filter { t ->
                fun hit(x: String?) = x?.lowercase(Locale.ROOT)?.contains(s) == true
                hit(t.name) ||
                        hit(t.dept) ||
                        hit(t.designation) ||
                        hit(t.personalEmail) ||
                        hit(t.officialEmail) ||
                        hit(t.personalPhone) ||
                        hit(t.officialPhone)
            }
        }
        filtered.sortedBy { it.name.lowercase(Locale.ROOT) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    private val _editingId = MutableStateFlow<Long?>(null)

    private val _name = MutableStateFlow("")
    private val _designation = MutableStateFlow("")
    private val _dept = MutableStateFlow("")
    private val _personalEmail = MutableStateFlow("")
    private val _officialEmail = MutableStateFlow("")
    private val _personalPhone = MutableStateFlow("")
    private val _officialPhone = MutableStateFlow("")
    private val _notes = MutableStateFlow("")

    val name: StateFlow<String> = _name.asStateFlow()
    val designation: StateFlow<String> = _designation.asStateFlow()
    val dept: StateFlow<String> = _dept.asStateFlow()
    val personalEmail: StateFlow<String> = _personalEmail.asStateFlow()
    val officialEmail: StateFlow<String> = _officialEmail.asStateFlow()
    val personalPhone: StateFlow<String> = _personalPhone.asStateFlow()
    val officialPhone: StateFlow<String> = _officialPhone.asStateFlow()
    val notes: StateFlow<String> = _notes.asStateFlow()

    fun openAdd() {
        _editingId.value = null
        _name.value = ""
        _designation.value = ""
        _dept.value = ""
        _personalEmail.value = ""
        _officialEmail.value = ""
        _personalPhone.value = ""
        _officialPhone.value = ""
        _notes.value = ""
        _showDialog.value = true
    }

    fun openEdit(t: Teacher) {
        _editingId.value = t.id
        _name.value = t.name
        _designation.value = t.designation.orEmpty()
        _dept.value = t.dept.orEmpty()
        _personalEmail.value = t.personalEmail.orEmpty()
        _officialEmail.value = t.officialEmail.orEmpty()
        _personalPhone.value = t.personalPhone.orEmpty()
        _officialPhone.value = t.officialPhone.orEmpty()
        _notes.value = t.notes.orEmpty()
        _showDialog.value = true
    }

    fun closeDialog() { _showDialog.value = false }

    fun setName(v: String) { _name.value = v }
    fun setDesignation(v: String) { _designation.value = v }
    fun setDept(v: String) { _dept.value = v }
    fun setPersonalEmail(v: String) { _personalEmail.value = v }
    fun setOfficialEmail(v: String) { _officialEmail.value = v }
    fun setPersonalPhone(v: String) { _personalPhone.value = v }
    fun setOfficialPhone(v: String) { _officialPhone.value = v }
    fun setNotes(v: String) { _notes.value = v }

    fun save() {
        val name = _name.value.trim()
        if (name.isBlank()) return
        viewModelScope.launch {
            val teacher = Teacher(
                id = _editingId.value ?: 0L,
                name = name,
                designation = _designation.value.ifBlank { null },
                dept = _dept.value.ifBlank { null },
                personalEmail = _personalEmail.value.ifBlank { null },
                officialEmail = _officialEmail.value.ifBlank { null },
                personalPhone = _personalPhone.value.ifBlank { null },
                officialPhone = _officialPhone.value.ifBlank { null },
                notes = _notes.value.ifBlank { null }
            )
            repo.upsert(teacher)
            closeDialog()
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            val t = teachers.value.firstOrNull { it.id == id } ?: repo.getById(id) ?: return@launch
            repo.delete(t)
        }
    }
}
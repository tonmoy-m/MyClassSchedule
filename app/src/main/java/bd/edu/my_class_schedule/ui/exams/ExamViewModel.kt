package bd.edu.my_class_schedule.ui.exams

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import bd.edu.my_class_schedule.data.db.DatabaseProvider
import bd.edu.my_class_schedule.data.db.entities.Exam
import bd.edu.my_class_schedule.data.db.entities.Subject
import bd.edu.my_class_schedule.data.repo.ExamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class ExamViewModel(app: Application) : AndroidViewModel(app) {
    private val db by lazy { DatabaseProvider.get(getApplication()) }
    private val repo by lazy { ExamRepository(db.examDao()) }

    val all: StateFlow<List<Exam>> = repo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val subjects: StateFlow<List<Subject>> = db.subjectDao()
        .observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()
    fun setQuery(v: String) { _query.value = v }

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    private val _editingId = MutableStateFlow<Long?>(null)

    private val _title = MutableStateFlow("")
    private val _subject = MutableStateFlow("")
    private val _location = MutableStateFlow("")
    private val _notes = MutableStateFlow("")

    private val _date = MutableStateFlow<LocalDate?>(null)      // NEW
    private val _start = MutableStateFlow<LocalTime?>(null)     // NEW
    private val _end = MutableStateFlow<LocalTime?>(null)       // NEW

    val title: StateFlow<String> = _title.asStateFlow()
    val subject: StateFlow<String> = _subject.asStateFlow()
    val location: StateFlow<String> = _location.asStateFlow()
    val notes: StateFlow<String> = _notes.asStateFlow()

    val date: StateFlow<LocalDate?> = _date.asStateFlow()
    val start: StateFlow<LocalTime?> = _start.asStateFlow()
    val end: StateFlow<LocalTime?> = _end.asStateFlow()

    val filtered: StateFlow<List<Exam>> = combine(all, _query) { list, q ->
        val s = q.trim().lowercase(Locale.ROOT)
        if (s.isEmpty()) list else list.filter { e ->
            fun hit(x: String?) = x?.lowercase(Locale.ROOT)?.contains(s) == true
            hit(e.title) || hit(e.subject) || hit(e.location) || hit(e.notes)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun openAdd() {
        _editingId.value = null
        _title.value = ""
        _subject.value = ""
        _location.value = ""
        _notes.value = ""
        _date.value = null
        _start.value = null
        _end.value = null
        _showDialog.value = true
    }

    fun openEdit(e: Exam) {
        _editingId.value = e.id
        _title.value = e.title
        _subject.value = e.subject.orEmpty()
        _location.value = e.location.orEmpty()
        _notes.value = e.notes.orEmpty()
        _date.value = e.date
        _start.value = e.startTime
        _end.value = e.endTime
        _showDialog.value = true
    }

    fun closeDialog() { _showDialog.value = false }

    fun setTitle(v: String) { _title.value = v }
    fun setSubject(v: String) { _subject.value = v }
    fun setLocation(v: String) { _location.value = v }
    fun setNotes(v: String) { _notes.value = v }

    fun setDate(v: LocalDate?) { _date.value = v }
    fun setStart(v: LocalTime?) { _start.value = v }
    fun setEnd(v: LocalTime?) { _end.value = v }

    fun save() {
        val title = _title.value.trim()
        if (title.isBlank()) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val entity = Exam(
                id = _editingId.value ?: 0L,
                title = title,
                subject = _subject.value.ifBlank { null },
                location = _location.value.ifBlank { null },
                notes = _notes.value.ifBlank { null },
                date = _date.value,
                startTime = _start.value,
                endTime = _end.value,
                createdAt = if (_editingId.value == null) {
                    now
                } else {
                    all.value.firstOrNull { it.id == _editingId.value }?.createdAt ?: now
                },
                updatedAt = now
            )
            repo.upsert(entity)
            closeDialog()
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            val e = all.value.firstOrNull { it.id == id } ?: repo.getById(id) ?: return@launch
            repo.delete(e)
        }
    }

    companion object {
        private val TF = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")

        fun formatDate(date: LocalDate?): String? = date?.format(TF)
    }
}
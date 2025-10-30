package bd.edu.my_class_schedule.ui.homework

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import bd.edu.my_class_schedule.data.db.DatabaseProvider
import bd.edu.my_class_schedule.data.db.entities.Homework
import bd.edu.my_class_schedule.data.db.entities.HomeworkType
import bd.edu.my_class_schedule.data.db.entities.Subject
import bd.edu.my_class_schedule.data.repo.HomeworkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class HomeworkViewModel(app: Application) : AndroidViewModel(app) {
    private val db by lazy { DatabaseProvider.get(getApplication()) }
    private val repo by lazy { HomeworkRepository(db.homeworkDao()) }

    val all: StateFlow<List<Homework>> = repo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Subject list for dropdown
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
    private val _description = MutableStateFlow("")
    private val _dueDateMillis = MutableStateFlow<Long?>(null)
    private val _isCompleted = MutableStateFlow(false)
    private val _type = MutableStateFlow(HomeworkType.HOMEWORK) // NEW

    val title: StateFlow<String> = _title.asStateFlow()
    val subject: StateFlow<String> = _subject.asStateFlow()
    val description: StateFlow<String> = _description.asStateFlow()
    val dueDateMillis: StateFlow<Long?> = _dueDateMillis.asStateFlow()
    val isCompleted: StateFlow<Boolean> = _isCompleted.asStateFlow()
    val type: StateFlow<HomeworkType> = _type.asStateFlow()

    val filtered: StateFlow<List<Homework>> = combine(all, _query) { list, q ->
        val s = q.trim().lowercase(Locale.ROOT)
        if (s.isEmpty()) list else list.filter { h ->
            fun hit(x: String?) = x?.lowercase(Locale.ROOT)?.contains(s) == true
            hit(h.title) || hit(h.subject) || hit(h.description)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun openAdd() {
        _editingId.value = null
        _title.value = ""
        _subject.value = ""
        _description.value = ""
        _dueDateMillis.value = null
        _isCompleted.value = false
        _type.value = HomeworkType.HOMEWORK
        _showDialog.value = true
    }

    fun openEdit(h: Homework) {
        _editingId.value = h.id
        _title.value = h.title
        _subject.value = h.subject.orEmpty()
        _description.value = h.description.orEmpty()
        _dueDateMillis.value = h.dueDateMillis
        _isCompleted.value = h.isCompleted
        _type.value = h.type
        _showDialog.value = true
    }

    fun closeDialog() { _showDialog.value = false }

    fun setTitle(v: String) { _title.value = v }
    fun setSubject(v: String) { _subject.value = v }
    fun setDescription(v: String) { _description.value = v }
    fun setDueDateMillis(v: Long?) { _dueDateMillis.value = v }
    fun setCompleted(v: Boolean) { _isCompleted.value = v }
    fun setType(v: HomeworkType) { _type.value = v }

    fun save() {
        val title = _title.value.trim()
        if (title.isBlank()) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val entity = Homework(
                id = _editingId.value ?: 0L,
                title = title,
                subject = _subject.value.ifBlank { null },
                description = _description.value.ifBlank { null },
                dueDateMillis = _dueDateMillis.value,
                isCompleted = _isCompleted.value,
                type = _type.value,
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
            val h = all.value.firstOrNull { it.id == id } ?: repo.getById(id) ?: return@launch
            repo.delete(h)
        }
    }

    fun toggleCompleted(id: Long) {
        viewModelScope.launch {
            val h = all.value.firstOrNull { it.id == id } ?: repo.getById(id) ?: return@launch
            repo.upsert(h.copy(isCompleted = !h.isCompleted))
        }
    }

    companion object {
        fun formatDate(millis: Long?): String? {
            millis ?: return null
            val fmt = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
            fmt.timeZone = TimeZone.getDefault()
            return fmt.format(millis)
        }
    }
}
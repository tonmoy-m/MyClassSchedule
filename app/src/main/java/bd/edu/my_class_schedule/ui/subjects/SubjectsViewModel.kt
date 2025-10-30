package bd.edu.my_class_schedule.ui.subjects

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import bd.edu.my_class_schedule.data.db.DatabaseProvider
import bd.edu.my_class_schedule.data.db.dao.SubjectDao
import bd.edu.my_class_schedule.data.db.entities.Subject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SubjectsViewModel(private val dao: SubjectDao) : ViewModel() {

    val query = MutableStateFlow("")

    val showDialog = MutableStateFlow(false)
    private val editingId = MutableStateFlow<Long?>(null)
    val code = MutableStateFlow("")
    val title = MutableStateFlow("")
    val creditText = MutableStateFlow("")
    val shortName = MutableStateFlow("")

    private val all = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val filtered = combine(all, query) { list, q ->
        val qq = q.trim().lowercase()
        if (qq.isEmpty()) list
        else list.filter {
            it.code.lowercase().contains(qq) ||
                    it.title.lowercase().contains(qq) ||
                    (it.shortName?.lowercase()?.contains(qq) == true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun openAdd() {
        editingId.value = null
        code.value = ""
        title.value = ""
        creditText.value = ""
        shortName.value = ""
        showDialog.value = true
    }

    fun openEdit(s: Subject) {
        editingId.value = s.id
        code.value = s.code
        title.value = s.title
        creditText.value = s.credit.toString()
        shortName.value = s.shortName.orEmpty()
        showDialog.value = true
    }

    fun closeDialog() { showDialog.value = false }

    fun setQuery(v: String) { query.value = v }
    fun setCode(v: String) { code.value = v }
    fun setTitle(v: String) { title.value = v }
    fun setCreditText(v: String) { creditText.value = v }
    fun setShortName(v: String) { shortName.value = v }

    fun save() {
        val codeStr = code.value.trim()
        val titleStr = title.value.trim()
        val credit = creditText.value.trim().toDoubleOrNull()
        val short = shortName.value.trim().ifBlank { null }

        if (codeStr.isBlank() || titleStr.isBlank() || credit == null || credit <= 0.0) return

        viewModelScope.launch {
            val id = editingId.value
            if (id == null) {
                dao.insert(Subject(code = codeStr, title = titleStr, credit = credit, shortName = short))
            } else {
                dao.update(Subject(id = id, code = codeStr, title = titleStr, credit = credit, shortName = short))
            }
            showDialog.value = false
        }
    }

    fun delete(s: Subject) {
        viewModelScope.launch { dao.delete(s) }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val dao = DatabaseProvider.get(context.applicationContext).subjectDao()
                    return SubjectsViewModel(dao) as T
                }
            }
    }
}
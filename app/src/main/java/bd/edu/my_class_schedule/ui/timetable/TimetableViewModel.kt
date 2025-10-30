package bd.edu.my_class_schedule.ui.timetable

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import bd.edu.my_class_schedule.data.db.DatabaseProvider
import bd.edu.my_class_schedule.data.db.entities.Session
import bd.edu.my_class_schedule.data.repo.TimetableRepository
import java.time.LocalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SessionUi(
    val id: Long,
    val subjectTitle: String,
    val teacherName: String?,
    val room: String?,
    val start: LocalTime,
    val end: LocalTime,
    val weekType: Int
)

@OptIn(ExperimentalCoroutinesApi::class)
class TimetableViewModel(app: Application) : AndroidViewModel(app) {
    private val _selectedDay = MutableStateFlow(1) // ISO 1=Mon..7=Sun
    val selectedDay: StateFlow<Int> = _selectedDay.asStateFlow()

    private val _selectedWeekType = MutableStateFlow(Session.BOTH)
    val selectedWeekType: StateFlow<Int> = _selectedWeekType.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private val _newSubjectId = MutableStateFlow<Long?>(null)
    private val _newTeacherId = MutableStateFlow<Long?>(null)
    private val _newRoom = MutableStateFlow("")
    private val _newStart = MutableStateFlow(LocalTime.of(9, 0))
    private val _newEnd = MutableStateFlow(LocalTime.of(10, 0))
    private val _newWeekType = MutableStateFlow(Session.BOTH)

    val newSubjectId: StateFlow<Long?> = _newSubjectId.asStateFlow()
    val newTeacherId: StateFlow<Long?> = _newTeacherId.asStateFlow()
    val newRoom: StateFlow<String> = _newRoom.asStateFlow()
    val newStart: StateFlow<LocalTime> = _newStart.asStateFlow()
    val newEnd: StateFlow<LocalTime> = _newEnd.asStateFlow()
    val newWeekType: StateFlow<Int> = _newWeekType.asStateFlow()

    private val db by lazy { DatabaseProvider.get(getApplication()) }
    private val repo by lazy { TimetableRepository(db.sessionDao(), db.subjectDao(), db.teacherDao()) }

    val subjects = repo.observeSubjects().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val teachers = repo.observeTeachers().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val sessions = combine(_selectedDay, _selectedWeekType) { day, week ->
        day to week
    }.flatMapLatest { (day, week) ->
        repo.observeSessions(day, week)
    }

    val sessionItems: StateFlow<List<SessionUi>> =
        combine(sessions, subjects, teachers) { sessions, subjects, teachers ->
            val subjectMap = subjects.associateBy { it.id }
            val teacherMap = teachers.associateBy { it.id }
            sessions.map { s ->
                SessionUi(
                    id = s.id,
                    subjectTitle = subjectMap[s.subjectId]?.title ?: "—",
                    teacherName = s.teacherId?.let { teacherMap[it]?.name },
                    room = s.room,
                    start = s.startTime,
                    end = s.endTime,
                    weekType = s.weekType
                )
            }.sortedBy { it.start }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectDay(dayIso: Int) { _selectedDay.value = dayIso }
    fun selectWeekType(type: Int) { _selectedWeekType.value = type }

    fun openAddDialog() {
        viewModelScope.launch {
            val (start, end) = repo.suggestDefaultTimes(_selectedDay.value, _selectedWeekType.value)
            _newStart.value = start
            _newEnd.value = end
            _newSubjectId.value = subjects.value.firstOrNull()?.id
            _newTeacherId.value = teachers.value.firstOrNull()?.id
            _newRoom.value = ""
            _newWeekType.value = _selectedWeekType.value
            _showAddDialog.value = true
        }
    }

    fun closeAddDialog() { _showAddDialog.value = false }

    fun setNewSubject(id: Long?) { _newSubjectId.value = id }
    fun setNewTeacher(id: Long?) { _newTeacherId.value = id }
    fun setNewRoom(text: String) { _newRoom.value = text }
    fun setNewStart(time: LocalTime) { _newStart.value = time }
    fun setNewEnd(time: LocalTime) { _newEnd.value = time }
    fun setNewWeekType(type: Int) { _newWeekType.value = type }

    fun saveNewSession() {
        val subjectId = _newSubjectId.value ?: return
        viewModelScope.launch {
            repo.saveSession(
                Session(
                    subjectId = subjectId,
                    teacherId = _newTeacherId.value,
                    dayOfWeek = _selectedDay.value,
                    startTime = _newStart.value,
                    endTime = _newEnd.value,
                    weekType = _newWeekType.value,
                    room = _newRoom.value.ifBlank { null },
                    reminderMinutesBefore = null,
                    silenceDnd = false
                )
            )
            closeAddDialog()
        }
    }

    fun deleteSession(id: Long) {
        viewModelScope.launch {
            val current = sessions.first().firstOrNull { it.id == id } ?: return@launch
            repo.deleteSession(current)
        }
    }
}
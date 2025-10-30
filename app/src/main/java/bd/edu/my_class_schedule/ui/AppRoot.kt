package bd.edu.my_class_schedule.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import bd.edu.my_class_schedule.ui.homework.HomeworkRoute
import bd.edu.my_class_schedule.ui.timetable.TimetableRoute
import bd.edu.my_class_schedule.ui.teachers.TeachersRoute
import bd.edu.my_class_schedule.ui.subjects.SubjectsRoute
import bd.edu.my_class_schedule.ui.exams.ExamsRoute // <-- add this import

private object Routes {
    const val Timetable = "timetable"
    const val Subjects = "subjects"
    const val Homework = "homework"
    const val Teachers = "teachers"
    const val Exams = "exams" // <-- add Exams route
}

@Composable
fun AppRoot() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                listOf(
                    Routes.Timetable to "Timetable",
                    Routes.Subjects to "Subjects",
                    Routes.Homework to "Homework",
                    Routes.Exams to "Exams", // <-- add Exams tab
                    Routes.Teachers to "Teachers"
                ).forEach { (route, label) ->
                    val selected = currentRoute == route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (!selected) {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Spacer(Modifier) },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Timetable,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.Timetable) { TimetableRoute() }
            composable(Routes.Subjects) { SubjectsRoute() }
            composable(Routes.Homework) { HomeworkRoute() }
            composable(Routes.Teachers) { TeachersRoute() }
            composable(Routes.Exams) { ExamsRoute() } // <-- add Exams destination
        }
    }
}
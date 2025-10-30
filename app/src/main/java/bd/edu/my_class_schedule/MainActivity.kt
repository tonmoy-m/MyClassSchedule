package bd.edu.my_class_schedule

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import bd.edu.my_class_schedule.ui.AppRoot
import bd.edu.my_class_schedule.ui.theme.MyClassScheduleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyClassScheduleTheme {
                // Single app entry: NavHost + bottom bar live inside AppRoot
                AppRoot()
            }
        }
    }
}
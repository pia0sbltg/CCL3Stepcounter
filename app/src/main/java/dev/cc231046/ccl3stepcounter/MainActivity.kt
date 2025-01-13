package dev.cc231046.ccl3stepcounter

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import dev.cc231046.ccl3stepcounter.data.GoalsDao
import dev.cc231046.ccl3stepcounter.data.StepsDao
import dev.cc231046.ccl3stepcounter.data.StepsDatabase
import dev.cc231046.ccl3stepcounter.ui.AppNavigation
import dev.cc231046.ccl3stepcounter.ui.theme.CCL3StepcounterTheme

class MainActivity : ComponentActivity() {
    private val REQUEST_ACTIVITY_RECOGNITION = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Check for permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), REQUEST_ACTIVITY_RECOGNITION)
        }

        val database = StepsDatabase.getDatabase(applicationContext)
        val stepsDao = database.stepsDao()
        val goalsDao = database.goalsDao()


        enableEdgeToEdge()
        setContent {
            CCL3StepcounterTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(stepsDao = stepsDao, goalsDao = goalsDao, modifier= Modifier.padding(innerPadding))
                }
            }
        }
    }


}



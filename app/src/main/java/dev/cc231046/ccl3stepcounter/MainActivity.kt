package dev.cc231046.ccl3stepcounter

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.os.PowerMonitor
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import dev.cc231046.ccl3stepcounter.data.StepsDatabase
import dev.cc231046.ccl3stepcounter.ui.AppNavigation
import dev.cc231046.ccl3stepcounter.ui.theme.CCL3StepcounterTheme
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private val REQUEST_ACTIVITY_RECOGNITION = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var attributionContext = createAttributionContext("MoveMate")

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), REQUEST_ACTIVITY_RECOGNITION)
        }

      //  val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
       // startActivity(intent)


        scheduleStepTrackingJob(this)

        val database = StepsDatabase.getDatabase(applicationContext)
        val stepsDao = database.stepsDao()
        val goalsDao = database.goalsDao()


        enableEdgeToEdge()
        setContent {
            CCL3StepcounterTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(stepsDao = stepsDao, goalsDao = goalsDao, navController= navController, modifier = Modifier.padding(innerPadding))                }
            }
        }
    }


}



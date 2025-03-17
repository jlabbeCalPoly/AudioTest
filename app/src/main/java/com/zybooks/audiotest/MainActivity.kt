package com.zybooks.audiotest

import android.app.Activity
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zybooks.audiotest.ui.theme.AudioTestTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.math.absoluteValue
import com.zybooks.audiotest.GraphicalViewModel
import com.zybooks.audiotest.GraphicalScreen
import com.zybooks.audiotest.ui.theme.AudioTestTheme

private val graphicalViewModel = GraphicalViewModel()

class MainActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent{
            AudioTestTheme(dynamicColor = false) {
                Surface (
                    modifier = Modifier.fillMaxSize()
                ) {
                    GraphicalScreen(graphicalViewModel = graphicalViewModel, activity = this)
                }
            }
        }
    }

    // Runs each time the activity restarts
    // Useful since it's ran each time a config change occurs
    override fun onStart() {
        super.onStart()
        graphicalViewModel.setOnStart()
    }
}
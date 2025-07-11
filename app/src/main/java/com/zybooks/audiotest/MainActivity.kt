package com.zybooks.audiotest

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.zybooks.audiotest.ui.theme.AudioTestTheme
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
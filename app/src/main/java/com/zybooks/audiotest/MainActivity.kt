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


class MainActivity: ComponentActivity() {
    private val graphicalViewModel = GraphicalViewModel()

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
}

/*
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AudioTestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                    TestButton(this)
                }
            }
        }
        // check if the user has enabled audio recording permissions
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            val permissionArray = arrayOf(android.Manifest.permission.RECORD_AUDIO)
            ActivityCompat.requestPermissions(this,
                permissionArray,
                1)
        }


    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Composable
fun TestButton(runContext: Activity) {
    var amplitude by remember{ mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    Column(
           modifier = Modifier.fillMaxSize(),
           horizontalAlignment = Alignment.CenterHorizontally,
           verticalArrangement = Arrangement.SpaceEvenly) {
        Button(
            onClick = {
                if (ContextCompat.checkSelfPermission(runContext, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    val permissionArray = arrayOf(android.Manifest.permission.RECORD_AUDIO)
                    ActivityCompat.requestPermissions(runContext, permissionArray, 1)
                } else {
                    // audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes
                    /*
                    * NOTES:
                    *   44100 Hz: Best practice for audio recordings (ie. voice recordings)
                    *   ENCODING_PCM_16BIT provides larger, more precise ranking of amplitudes that ENCODING-PCM_8BIT
                    *
                    * */
                    val buffer = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
                    val recorder = AudioRecord( MediaRecorder.AudioSource.MIC,
                        44100,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        buffer
                    )
                    val shortArray = ShortArray(buffer)

                    coroutineScope.launch(Dispatchers.IO) {
                        recorder.startRecording()

                        while(true) {
                            try {
                                recorder.read(shortArray, 0, buffer)
                            } catch(e: Exception) {
                                e.message?.let { Log.e("Error:", it)}
                            }

                            var avg = 0
                            shortArray.forEach {
                                avg += it
                            }
                            avg /= buffer
                            amplitude = avg

                            delay(10)
                        }
                    }
                }
            }
        ) {
            Text("Avg Amp.: $amplitude")
        }

    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AudioTestTheme {
        Greeting("Android")
    }
}
*/
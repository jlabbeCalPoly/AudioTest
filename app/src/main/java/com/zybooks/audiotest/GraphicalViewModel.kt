package com.zybooks.audiotest

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.ui.graphics.Path
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext
import kotlin.math.max
import kotlin.math.min

class GraphicalViewModel : ViewModel() {
    // include ? after Job type since graphicalJob may also be null
    private var graphicalJob : Job? = null

    // is new audio data being streamed in or not
    var isRunning by mutableStateOf(false)
        private set

    // path generated by the audioData
    var path: Path? by mutableStateOf(null)
        private set
    var width: Float by mutableFloatStateOf(0f)
    var height: Float by mutableFloatStateOf(0f)

    // Y-axis range values for dynamic scaling
    var currentMaxAmplitude: Float = 1f
    var currentMinAmplitude: Float = -1f

    // Number of divisions for Y-axis labels (excluding zero)
    val yAxisDivisions = 4

    fun setGraphSize(width: Float, height: Float) {
        this.width = width
        this.height = height
    }

    @SuppressLint("MissingPermission")
    fun startGraphical() {
        // debounce if the graphical display is already running
        if(!isRunning) {
            isRunning = true
            // Reset amplitude values
            currentMaxAmplitude = 1f
            currentMinAmplitude = -1f

            val buffer = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
            val recorder = AudioRecord( MediaRecorder.AudioSource.MIC,
                44100,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                buffer
            )
            val shortArray = ShortArray(buffer)

            fun smoothData(data: ShortArray, readSize: Int): List<Float> {
                val chunkSize = readSize / 200
                val smoothedData = data.toList().chunked(chunkSize).map { chunk ->
                    val filtered = chunk.filter { it > 5 }
                    if (filtered.isNotEmpty()) {
                        filtered.average().toFloat()
                    } else {
                        0f
                    }
                }

                // Apply moving average to further smooth data
                val windowSize = 5  // Number of points to average
                return smoothedData.mapIndexed { index, _ ->
                    smoothedData.slice(index until min(index + windowSize, smoothedData.size)).average().toFloat()
                }
            }

            fun buildPath(readSize: Int) {
                var xOffset = 0f
                val smoothedData = smoothData(shortArray, readSize)

                // Find the max amplitude in the smoothed data
                val dataMax = smoothedData.maxOrNull() ?: 1f
                val dataMin = smoothedData.minOrNull() ?: -1f

                // Update the max and min amplitudes with smoothing
                // Use a damping factor to gradually adjust the range (prevents too rapid changes)
                val dampingFactor = 0.7f
                currentMaxAmplitude = max(dataMax, currentMaxAmplitude * dampingFactor)
                currentMinAmplitude = min(dataMin, currentMinAmplitude * dampingFactor)

                // Ensure we have a minimum range to prevent division by zero
                val range = max(currentMaxAmplitude - currentMinAmplitude, 10f)

                // Calculate scale to fit in view
                val amplitudeScale = height / range
                val lineWidth = width / smoothedData.size

                path = Path().apply {
                    reset()
                    moveTo(0f, height / 2) // Start from the middle

                    for (data in smoothedData) {
                        // Scale relative to the dynamic range
                        val normalizedData = (data - currentMinAmplitude) / range
                        val yOffset = height - (normalizedData * height)
                        xOffset += lineWidth

                        lineTo(xOffset, yOffset)
                    }
                }
            }

            fun readData(): Int? {
                var readSize : Int? = null
                try {
                    readSize = recorder.read(shortArray, 0, buffer)
                } catch(e: Exception) {
                    e.message?.let { Log.e("Error:", it) }
                    return null
                }
                return readSize
            }

            graphicalJob = viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    recorder.startRecording()
                    try {
                        while(isRunning) {
                            val readData = readData()
                            if (readData != null && readData > 0) {
                                Log.d("Reading", ""+readData)
                                buildPath(readData)
                            }
                        }
                    } catch(e: Exception) {
                        e.message?.let { Log.e("Error:", it)}
                    } finally {
                        recorder.stop()
                        recorder.release()
                    }
                }
            }
        }
    }

    fun cancelGraphical() {
        if(isRunning) {
            graphicalJob?.cancel()
            isRunning = false
            // Reset amplitude range when stopped
            currentMaxAmplitude = 1f
            currentMinAmplitude = -1f
        }
    }

    // Helper function to get Y-axis label values
    fun getYAxisLabelValues(): List<Float> {
        val range = currentMaxAmplitude - currentMinAmplitude
        val step = range / yAxisDivisions
        val labels = mutableListOf<Float>()
        for (i in 0..yAxisDivisions) {
            labels.add(currentMinAmplitude + (step * i))
        }
        return labels
    }
}
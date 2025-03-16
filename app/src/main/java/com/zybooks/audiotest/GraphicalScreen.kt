package com.zybooks.audiotest

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.roundToInt
import com.zybooks.audiotest.ui.theme.Blue as BLUE_COLOR

@Composable
fun GraphicalScreen (
    modifier: Modifier = Modifier,
    graphicalViewModel: GraphicalViewModel = viewModel(),
    activity: Activity
) {
    Column (
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Y-axis labels column
            Column(
                modifier = Modifier
                    .width(50.dp)
                    .height(200.dp)
                    .padding(end = 20.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                    if (graphicalViewModel.yAxisLabels != null) {
                        //val yAxisLabels = graphicalViewModel.yAxisLabels
                        // Display labels from top to bottom
                        graphicalViewModel.yAxisLabels!!.map { value ->
                            Text(
                                text = String.format("%.2f", value),
                                style = TextStyle(fontSize = 10.sp, color = Color.DarkGray),
                                textAlign = TextAlign.End,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    } else {
                        Log.d("yAxisLabels", "yAxisLabels are null")
                    }
                }

            // Graph area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(200.dp)
                  //  .background(Color.LightGray)
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    graphicalViewModel.setGraphSize(size.width, size.height)
                    // Draw horizontal grid lines
                    //if (graphicalViewModel.isRunning) {
                    //    val yAxisLabels = graphicalViewModel.getYAxisLabelValues()
                    //    val step = size.height / (yAxisLabels.size - 1)

                        // Draw grid lines for each y-axis point
                    //    for (i in yAxisLabels.indices) {
                    //        val y = size.height - (i * step)
                    //        drawLine(
                    //            color = Color.Gray.copy(alpha = 0.5f),
                    //            start = Offset(0f, y),
                    //            end = Offset(size.width, y),
                    //            strokeWidth = 1f
                    //        )
                    //    }
                    //}

                    // Draw the waveform path
                    if(graphicalViewModel.path != null) {
                        drawPath(
                            graphicalViewModel.path!!,
                            color = BLUE_COLOR,
                            style = Stroke(width = 3f)
                        )
                    } else {
                        Log.d("Debug graph", "Path is null")
                    }
                }
            }
        }

        Button(
            onClick = { startGraphical(activity = activity, graphicalViewModel = graphicalViewModel) }
        ) {
            Text(if (graphicalViewModel.isRunning) "Stop" else "Start")
        }
    }
}

fun startGraphical(activity: Activity, graphicalViewModel: GraphicalViewModel) {
    if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
        Log.d("CheckPermission", "Need permission")
        val permissionArray = arrayOf(android.Manifest.permission.RECORD_AUDIO)
        ActivityCompat.requestPermissions(activity, permissionArray, 1)
    } else {
        if(graphicalViewModel.isRunning) {
            Log.d("PermissionGranted", "Cancelling graphical")
            graphicalViewModel.cancelGraphical()
        } else {
            Log.d("PermissionGranted", "Starting graphical")
            graphicalViewModel.startGraphical()
        }
    }
}
package com.zybooks.audiotest

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.platform.LocalConfiguration
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
import com.zybooks.audiotest.ui.theme.White as WHITE_COLOR

@Composable
fun GraphicalScreen (
    modifier: Modifier = Modifier,
    graphicalViewModel: GraphicalViewModel = viewModel(),
    activity: Activity
) {
    val config = LocalConfiguration.current
    if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
        PortraitOrientationLayout(modifier, graphicalViewModel, activity)
    } else { // The orientation is in landscape mode
        LandscapeOrientationLayout(modifier, graphicalViewModel, activity)
    }
}

// Orientation layouts
@Composable
fun PortraitOrientationLayout(
    modifier: Modifier = Modifier,
    graphicalViewModel: GraphicalViewModel,
    activity: Activity
) {
    Column (
        modifier = Modifier.fillMaxHeight().padding(top = 50.dp, bottom = 10.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(10.dp).weight(0.5f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // The y-axis labels on the left side of the graph
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(60.dp)
                    .padding(end = 20.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                if (graphicalViewModel.yAxisLabels != null) {
                    graphicalViewModel.yAxisLabels!!.map { value ->
                        Text(
                            text = String.format("%.2f", value),
                            style = TextStyle(fontSize = 10.sp, color = Color.DarkGray),
                            textAlign = TextAlign.End,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
            // the graph
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .border(2.dp, WHITE_COLOR)
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // determine the size of the graph, needed to ensure that
                    // the generated paths stay within the bounds
                    graphicalViewModel.setGraphSize(size.width, size.height)

                    // Draw the waveform path
                    if(graphicalViewModel.path != null) {
                        drawPath(
                            graphicalViewModel.path!!,
                            color = BLUE_COLOR,
                            style = Stroke(width = 3f)
                        )
                    }
                }
            }
        }
        Column (
            Modifier.padding(bottom = 20.dp, start = 20.dp, end = 20.dp).fillMaxHeight().weight(1f),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp).weight(0.25f),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { startGraphical(activity = activity, graphicalViewModel = graphicalViewModel) }
                ) {
                    Text(if (graphicalViewModel.isRunning) "Stop" else "Start")
                }
            }
            // the box displaying the selected points
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Text (
                    modifier = Modifier.fillMaxSize().weight(0.1f),

                    style = TextStyle(fontSize = 15.sp, color = WHITE_COLOR),
                    text = "Data points selected"
                )
                Box(
                    modifier = Modifier.fillMaxSize().weight(2f)
                        .border(2.dp, WHITE_COLOR)
                ) {

                }
            }
        }
    }
}

@Composable
fun LandscapeOrientationLayout(
    modifier: Modifier = Modifier,
    graphicalViewModel: GraphicalViewModel = viewModel(),
    activity: Activity
) {
    Row (
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(200.dp).weight(0.25f),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = { startGraphical(activity = activity, graphicalViewModel = graphicalViewModel) }
            ) {
                Text(if (graphicalViewModel.isRunning) "Stop" else "Start")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().height(200.dp).padding(10.dp).weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // The y-axis labels on the left side of the graph
            Column(
                modifier = Modifier
                    .width(50.dp)
                    .height(200.dp)
                    .padding(end = 20.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                if (graphicalViewModel.yAxisLabels != null) {
                    graphicalViewModel.yAxisLabels!!.map { value ->
                        Text(
                            text = String.format("%.2f", value),
                            style = TextStyle(fontSize = 10.sp, color = Color.DarkGray),
                            textAlign = TextAlign.End,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
            // the graph
            Box(
                modifier = Modifier
                    .height(200.dp)
                    .border(2.dp, WHITE_COLOR)
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // determine the size of the graph, needed to ensure that
                    // the generated paths stay within the bounds
                    graphicalViewModel.setGraphSize(size.width, size.height)

                    // Draw the waveform path
                    if(graphicalViewModel.path != null) {
                        drawPath(
                            graphicalViewModel.path!!,
                            color = BLUE_COLOR,
                            style = Stroke(width = 3f)
                        )
                    }
                }
            }
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
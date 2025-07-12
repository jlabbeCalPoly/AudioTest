package com.zybooks.audiotest

import android.app.Activity
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel

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
    val path by graphicalViewModel.data.collectAsState()

    Column (
        modifier = Modifier.fillMaxHeight().padding(top = 50.dp, bottom = 10.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { startGraphical(activity = activity, graphicalViewModel = graphicalViewModel) }
        ) {
            Text(if (graphicalViewModel.isRecording) "Stop" else "Start")
        }
        Graph(path)
        Text("HELLO IM ON THE BOTTOM")
    }
}

@Composable
fun LandscapeOrientationLayout(
    modifier: Modifier = Modifier,
    graphicalViewModel: GraphicalViewModel = viewModel(),
    activity: Activity
) {
    val path by graphicalViewModel.data.collectAsState()

    Row (
        modifier = Modifier.padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("turn sideways")
    }
}

fun startGraphical(activity: Activity, graphicalViewModel: GraphicalViewModel) {
    if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
        Log.d("CheckPermission", "Need permission")
        val permissionArray = arrayOf(android.Manifest.permission.RECORD_AUDIO)
        ActivityCompat.requestPermissions(activity, permissionArray, 1)
    } else {
        if(graphicalViewModel.isRecording) {
            Log.d("PermissionGranted", "Cancelling graphical")
            graphicalViewModel.cancelGraphical()
        } else {
            Log.d("PermissionGranted", "Starting graphical")
            graphicalViewModel.startDataAcquisition(activity, 20)
        }
    }
}
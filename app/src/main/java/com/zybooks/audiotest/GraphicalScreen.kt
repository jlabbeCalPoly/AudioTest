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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
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
    Column (
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(200.dp).background(Color.LightGray)
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                    graphicalViewModel.setGraphSize(size.width, size.height)
                  /*  val path: Path =
                        Path().apply {
                            reset()
                            moveTo(0f, size.height/2)
                            lineTo(size.width, size.height/2)
                            close()
                        } */

                    if(graphicalViewModel.path != null) {
                        drawPath(
                            graphicalViewModel.path!!,
                            color = Color.Blue, style = Stroke(width=5f)
                        )
                    }
                }
        }
        Button(
            onClick = { startGraphical(activity = activity, graphicalViewModel = graphicalViewModel) }
        ) {
            Text("Test Button")
        }
    }
}

fun startGraphical(activity: Activity, graphicalViewModel: GraphicalViewModel) {
    // check for permissions
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
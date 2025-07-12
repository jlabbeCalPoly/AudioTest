package com.zybooks.audiotest

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log
import kotlin.math.pow
import kotlin.math.roundToInt

/*
This is kind of a mess, but it's what I have right now.
Things I want to do:
    - display information about the selected points
    - improve axis label positioning
    - fix the floating point precision issues with the axis labels
    - move some stuff into other files/classes and organize better
        - maybe use Pair for graph coordinates and Offset for pixel coordinates?
    - see how graph looks with real data and implement path smoothing if needed
    - look into better caching/state management to reduce recompositions?
        - (I'm still not super familiar with Compose's state apis)
 */

@Composable
fun Graph(data: ShortArray) {
    // do some math to make them cross nicely?
    val dottedLineEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 15f))
    val textMeasurer = rememberTextMeasurer()
    var xMax by remember { mutableStateOf(22f) }
    var xMin by remember { mutableStateOf(-5f) }
    var yMax by remember { mutableStateOf(1500f) }
    var yMin by remember { mutableStateOf(-1200f) }

    var initialSelectOffset by remember { mutableStateOf(Offset.Unspecified) }
    var currentSelectOffset by remember { mutableStateOf(Offset.Unspecified) }
    var isSelecting by remember { mutableStateOf(false) }

    val graphPadding = 30.dp

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Canvas(
            Modifier
                .fillMaxSize()
                .clipToBounds()
                .pointerInput(Unit) { // pan and zoom gesture
                    awaitEachGesture {
                        val firstDown = awaitFirstDown()
                        firstDown.consume()
                        val pointerId1 = firstDown.id

                        var secondDown: PointerInputChange? = null

                        // while only one finger down
                        while (secondDown == null) {
                            val event = awaitPointerEvent()
                            secondDown = event.changes.firstOrNull {
                                it.changedToDown() && it.id != firstDown.id
                            }

                            secondDown?.consume()

                            if (event.changes.any { it.id == firstDown.id && it.changedToUpIgnoreConsumed() }) {
                                return@awaitEachGesture
                            }

                            // panning
                            val change = event.changes.firstOrNull { it.id == pointerId1 }
                            if (change != null && !change.isConsumed) {
                                change.consume()
                                val diffPx = change.position - change.previousPosition
                                val diff = Offset(
                                    pxToUnits(diffPx.x, size.width.toFloat(), xMin, xMax),
                                    pxToUnits(diffPx.y, size.height.toFloat(), yMin, yMax)
                                )

                                // dragging right -> decreasing x bounds
                                // dragging down -> decreasing y bounds
                                xMin -= diff.x
                                xMax -= diff.x
                                yMin += diff.y
                                yMax += diff.y
                            }
                        }

                        val pointerId2 = secondDown.id

                        // figuring out how to base calculations on the initial points at gesture start would be best
                        // would fix some issues with drifting (ex: zooming through the dead zone)
                        // i think we could just store initial bounds and finger pointer positions and do math from those
                        while (true) {
                            val event = awaitPointerEvent()
                            val change1 = event.changes.firstOrNull { it.id == pointerId1 }
                            val change2 = event.changes.firstOrNull { it.id == pointerId2 }

                            // need to make sure the drags aren't consumed by selection gesture
                            if (change1 == null || change2 == null ||
                                change1.isConsumed || change2.isConsumed ||
                                change1.changedToUpIgnoreConsumed() || change2.changedToUpIgnoreConsumed()
                            ) {
                                break
                            }

                            change1.consume()
                            change2.consume()

                            val cl =
                                CoordinateLocator(xMin, xMax, yMin, yMax, size.height.toFloat(), size.width.toFloat())

                            val centerPx = Offset(
                                (change1.position.x + change2.position.x) / 2,
                                (change1.position.y + change2.position.y) / 2
                            )
                            val centerCoords = Offset(
                                cl.locToCoord(centerPx.x, CoordinateLocator.Axis.X),
                                cl.locToCoord(centerPx.y, CoordinateLocator.Axis.Y)
                            )

                            val prevCenterPx = Offset(
                                (change1.previousPosition.x + change2.previousPosition.x) / 2,
                                (change1.previousPosition.y + change2.previousPosition.y) / 2
                            )
                            val prevCenterCoords = Offset(
                                cl.locToCoord(prevCenterPx.x, CoordinateLocator.Axis.X),
                                cl.locToCoord(prevCenterPx.y, CoordinateLocator.Axis.Y)
                            )

                            val prevDistanceX = abs(change1.previousPosition.x - change2.previousPosition.x)
                            val prevDistanceY = abs(change1.previousPosition.y - change2.previousPosition.y)

                            val distanceX = abs(change1.position.x - change2.position.x)
                            val distanceY = abs(change1.position.y - change2.position.y)

                            // zooms in a little if you swipe your fingers through the "dead zone" too fast
                            // using the avg of the distances makes it better but it can still happen
                            val minFingerDistancePx = 40.dp.toPx()
                            val zoomFactorX =
                                if (prevDistanceX != 0f && ((distanceX + prevDistanceX) / 2) > minFingerDistancePx) {
                                    (distanceX / prevDistanceX)
                                } else {
                                    1f
                                }

                            val zoomFactorY =
                                if (prevDistanceY != 0f && ((distanceY + prevDistanceY) / 2) > minFingerDistancePx) {
                                    (distanceY / prevDistanceY)
                                } else {
                                    1f
                                }

                            val widthX = xMax - xMin
                            val centerProportionX = (centerCoords.x - xMin) / widthX
                            val heightY = yMax - yMin
                            val centerProportionY = (centerCoords.y - yMin) / heightY

                            val newWidthX = widthX / zoomFactorX
                            val newHeightY = heightY / zoomFactorY

                            var newMinX = centerCoords.x - (centerProportionX * newWidthX)
                            var newMaxX = centerCoords.x + ((1 - centerProportionX) * newWidthX)
                            var newMinY = centerCoords.y - (centerProportionY * newHeightY)
                            var newMaxY = centerCoords.y + ((1 - centerProportionY) * newHeightY)

                            // do panning based on change in center
                            newMinX += prevCenterCoords.x - centerCoords.x
                            newMaxX += prevCenterCoords.x - centerCoords.x
                            newMaxY += prevCenterCoords.y - centerCoords.y
                            newMinY += prevCenterCoords.y - centerCoords.y

                            xMin = newMinX
                            xMax = newMaxX
                            yMin = newMinY
                            yMax = newMaxY
                        }
                    }
                }
                .pointerInput(Unit) { // selection gesture
                    // this runs before the above handler for some reason
                    detectDragGesturesAfterLongPress(
                        // idk if i can look at gestures relative to the inset
                        // for now we just subtract the padding
                        onDragStart = { offset ->
                            // offset is relative to outer canvas, so we need to subtract the padding
                            initialSelectOffset = Offset(
                                x = offset.x - graphPadding.toPx(),
                                y = offset.y - graphPadding.toPx()
                            )
                            currentSelectOffset = initialSelectOffset
                            isSelecting = true
                        },
                        onDrag = { change, dragAmount ->
                            currentSelectOffset = Offset(
                                x = change.position.x - graphPadding.toPx(),
                                y = change.position.y - graphPadding.toPx()
                            )
                        },
                        onDragEnd = {
                            isSelecting = false
                            initialSelectOffset = Offset.Unspecified
                            currentSelectOffset = Offset.Unspecified
                        }
                    )
                }
        ) {
            val measuredX = textMeasurer.measure(
                "Time (seconds)",
                style = TextStyle(color = Color.White)
            )

            drawText(
                measuredX,
                topLeft = Offset(
                    x = (size.width - measuredX.size.width) / 2,
                    y = size.height - ((measuredX.size.height + graphPadding.toPx()) / 2) // minus half of inset
                )
            )

            rotate(-90f) {
                val measuredY = textMeasurer.measure(
                    "Amplitude",
                    style = TextStyle(color = Color.White)
                )

                drawText(
                    measuredY,
                    // just trust the math don't worry about it
                    // could have been better to rotate around (0, 0) or something
                    topLeft = Offset(
                        x = (size.width - measuredY.size.width) / 2,
                        y = (size.height - size.width - measuredY.size.height + graphPadding.toPx()) / 2
                    )
                )
            }

            // make space for axis labels
            inset(inset = graphPadding.toPx()) {
                clipRect {
                    val cl = CoordinateLocator(xMin, xMax, yMin, yMax, size.height, size.width)

                    drawLine(
                        Color.DarkGray,
                        start = Offset(cl.coordToLoc(0f, CoordinateLocator.Axis.X), 0f),
                        end = Offset(cl.coordToLoc(0f, CoordinateLocator.Axis.X), size.height),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = dottedLineEffect
                    )

                    drawLine(
                        Color.DarkGray,
                        start = Offset(0f, cl.coordToLoc(0f, CoordinateLocator.Axis.Y)),
                        end = Offset(size.width, cl.coordToLoc(0f, CoordinateLocator.Axis.Y)),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = dottedLineEffect
                    )

                    // draws lines and numbers for axes
                    drawAxisMarkers(
                        min = xMin,
                        max = xMax,
                        axis = CoordinateLocator.Axis.X,
                        cl = cl,
                        textMeasurer = textMeasurer,
                    )

                    drawAxisMarkers(
                        min = yMin,
                        max = yMax,
                        axis = CoordinateLocator.Axis.Y,
                        cl = cl,
                        textMeasurer = textMeasurer,
                    )

                    if (data.isNotEmpty()) {
                        drawPath(
                            path = generatePath(cl, xMin, xMax, data), Color.Green, style = Stroke(2.dp.toPx())
                        )
                    }

                    drawRect(
                        color = Color.Gray,
                        style = Stroke(width = 1.dp.toPx()),
                        size = size
                    )

                    if (isSelecting) {
                        val initialPointerX = cl.locToCoord(initialSelectOffset.x, CoordinateLocator.Axis.X)
                        val currentPointerX = cl.locToCoord(currentSelectOffset.x, CoordinateLocator.Axis.X)

                        val initialNearestPoint = Offset(
                            // this is evil
                            indexToXCoord(xCoordToIndex(initialPointerX, data.size), data.size),
                            getValueFromXCoord(initialPointerX, data).toFloat()
                        )
                        val currentNearestPoint = Offset(
                            indexToXCoord(xCoordToIndex(currentPointerX, data.size), data.size),
                            getValueFromXCoord(currentPointerX, data).toFloat()
                        )

                        val initialNearestOffset = cl.coordToLoc(initialNearestPoint)
                        val currentNearestOffset = cl.coordToLoc(currentNearestPoint)

                        // line between dots
                        drawLine(
                            Color.LightGray,
                            start = initialNearestOffset,
                            end = currentNearestOffset,
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = dottedLineEffect
                        )

                        // initial selection location
                        drawLine(
                            Color.White,
                            start = Offset(initialNearestOffset.x, 0f),
                            end = Offset(initialNearestOffset.x, size.height),
                            strokeWidth = 2.dp.toPx(),
                        )

                        drawCircle(
                            Color.White,
                            radius = 5.dp.toPx(),
                            center = initialNearestOffset
                        )

                        // current selection location
                        drawLine(
                            Color.White,
                            start = Offset(currentNearestOffset.x, 0f),
                            end = Offset(currentNearestOffset.x, size.height),
                            strokeWidth = 2.dp.toPx(),
                        )

                        drawCircle(
                            Color.White,
                            radius = 5.dp.toPx(),
                            center = currentNearestOffset
                        )
                    }
                }
            }
        }
    }
}

class CoordinateLocator(
    val xMin: Float,
    val xMax: Float,
    val yMin: Float,
    val yMax: Float,
    val height: Float,
    val width: Float
) {
    enum class Axis {
        X, Y
    }

    fun coordToLoc(coord: Offset) = Offset(
        coordToLoc(coord.x, Axis.X),
        coordToLoc(coord.y, Axis.Y)
    )

    fun coordToLoc(coord: Float, axis: Axis): Float {
        return when (axis) {
            Axis.X -> coordToLoc(coord, xMin, xMax, width)
            Axis.Y -> height - coordToLoc(coord, yMin, yMax, height) // flip y-axis (top is 0)
        }
    }

    private fun coordToLoc(coord: Float, min: Float, max: Float, axisLength: Float): Float {
        return (coord - min) * axisLength / (max - min)
    }

    fun locToCoord(loc: Offset) = Offset(
        locToCoord(loc.x, Axis.X),
        locToCoord(loc.y, Axis.Y)
    )

    fun locToCoord(loc: Float, axis: Axis): Float {
        return when (axis) {
            Axis.X -> locToCoord(loc, xMin, xMax, width)
            Axis.Y -> locToCoord(height - loc, yMin, yMax, height) // flip y-axis (top is 0)
        }
    }

    private fun locToCoord(loc: Float, min: Float, max: Float, axisLength: Float): Float {
        return loc * (max - min) / axisLength + min
    }
}

fun DrawScope.drawAxisMarkers(
    min: Float,
    max: Float,
    axis: CoordinateLocator.Axis,
    cl: CoordinateLocator,
    textMeasurer: TextMeasurer,
) {
    val dottedLineEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 15f))

    val axisNums = getAxisNums(min, max)

    for (num in axisNums) {
        val loc = cl.coordToLoc(num, axis)
        val start = when (axis) {
            CoordinateLocator.Axis.X -> Offset(loc, 0f)
            CoordinateLocator.Axis.Y -> Offset(0f, loc)
        }
        val end = when (axis) {
            CoordinateLocator.Axis.X -> Offset(loc, size.height)
            CoordinateLocator.Axis.Y -> Offset(size.width, loc)
        }

        drawLine(
            Color.DarkGray,
            start = start,
            end = end,
            strokeWidth = 1.dp.toPx(),
            pathEffect = dottedLineEffect
        )

        val textLayoutResult = textMeasurer.measure(
            text = num.toString(),
            style = TextStyle(color = Color.White, background = Color.Black)
        )

        val textOffset = when (axis) {
            CoordinateLocator.Axis.X -> Offset(
                loc - (textLayoutResult.size.width / 2),
                size.height - textLayoutResult.size.height - 5.dp.toPx()
            )

            CoordinateLocator.Axis.Y -> Offset(
                5.dp.toPx(),
                loc - (textLayoutResult.size.height / 2)
            )
        }

        drawText(textLayoutResult, topLeft = textOffset)
    }
}

// TODO: could use refinement, apparently i tried to invent "nice numbers"
// i suppose i should consult the research papers on this stuff
fun getAxisNums(min: Float, max: Float): FloatArray {
    val width = max - min

    val powOf10 = floor(log(width, 10f))
    val factor = 10f.pow(powOf10) // factor that normalizes values to a range of 0..10

    val fakeMin = min / factor
    val fakeMax = max / factor
    val fakeWidth = width / factor

    val rawStep = fakeWidth / 5
    val stepSize = when {
        rawStep <= 1f -> 1
        rawStep <= 2f -> 2
        rawStep <= 5f -> 5
        else -> 10
    }

    val fakeCeilMin = ceil(fakeMin).toInt()
    val fakeFloorMax = floor(fakeMax).toInt()

    return (fakeCeilMin..fakeFloorMax)
        .filter { it % stepSize == 0 } // only keep multiples of the step size
        .map { it * factor } // convert back to the original scale
        .toFloatArray()
}

// convert distance in pixels to graph units
fun pxToUnits(px: Float, axisWidthPx: Float, min: Float, max: Float): Float {
    val axisWidth = max - min
    return px * axisWidth / axisWidthPx
}

/*
// convert graph units to distance in pixels
fun unitsToPx(units: Float, axisWidthPx: Float, min: Float, max: Float): Float {
    val axisWidth = max - min
    return units * axisWidthPx / axisWidth
}
*/


// copilot chose this scaling lol
// this will change
fun xCoordToIndex(coord: Float, dataSize: Int): Int {
    // convert coordinate to index in the data array
    return (coord / 1000 * dataSize).roundToInt().coerceIn(0, dataSize - 1)
}

fun indexToXCoord(index: Int, dataSize: Int): Float {
    // convert index in the data array to coordinate
    return (index.toFloat() / dataSize * 1000)
}

fun getValueFromXCoord(x: Float, data: ShortArray): Short {
    // convert x coordinate to index in the data array and get the value
    val index = xCoordToIndex(x, data.size)
    return data.getOrNull(index) ?: 0
}

fun generatePath(cl: CoordinateLocator, xMin: Float, xMax: Float, data: ShortArray): Path {
    val path = Path()

    // was coming up one short of the walls so we add one and then coerce again (sorry)
    val minIndexX = (xCoordToIndex(xMin, data.size) - 1).coerceAtLeast(0)
    val maxIndexX = (xCoordToIndex(xMax, data.size) + 1).coerceAtMost(data.lastIndex)

    for (i in minIndexX..maxIndexX) {
        val coordX = indexToXCoord(i, data.size)
        val coordY = data[i].toFloat()

        val locX = cl.coordToLoc(coordX, CoordinateLocator.Axis.X)
        val locY = cl.coordToLoc(coordY, CoordinateLocator.Axis.Y)

        if (i == minIndexX) {
            path.moveTo(locX, locY)
        } else {
            path.lineTo(locX, locY)
        }
    }

    return path
}


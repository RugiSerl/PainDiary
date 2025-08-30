package com.rugiserl.paindiary

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.util.Calendar
import kotlin.Int


data class GraphDataState(
    val data: List<Pair<Float, Float>> = listOf<Pair<Float, Float>>(),
)

class GraphDataViewModel : ViewModel() {

    // Expose screen UI state
    private val _uiState = MutableStateFlow(GraphDataState())
    val uiState: StateFlow<GraphDataState> = _uiState.asStateFlow()

    // Handle business logic
    fun addElement(point: Pair<Float, Float>) {
        _uiState.update { currentState ->
            currentState.copy(
                data = currentState.data + point
            )
        }
    }
}


@Composable
fun NormalGraph(data: List<Pair<Float, Float>>, modifier : Modifier = Modifier ) {
    if (data.isEmpty()) return
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val sorted = data.sortedBy { it.first }
        val width = sorted.last().first - sorted.first().first
        val xOffset = sorted.first().first
        val min = sorted.minBy { it.second }
        val max = sorted.maxBy { it.second }

        for (i in 0..(data.size-2)) {
            val start = Offset(x = (sorted[i].first-xOffset)/width*canvasWidth, y = canvasHeight - (sorted[i].second - min.second)/(max.second-min.second)*canvasHeight)
            val end = Offset(x = (sorted[i+1].first-xOffset)/width*canvasWidth, y = canvasHeight - (sorted[i+1].second - min.second)/(max.second-min.second)*canvasHeight)
            drawLine(
                start = start,
                end = end,
                color = Color.Blue,
                strokeWidth = 5.dp.toPx()
            )
            drawCircle(
                color = Color.Blue,
                radius = 5.dp.toPx(),
                center = end
            )
        }
    }
}

@Composable
fun PainGraph(data: MutableList<Pair<Long, Float>>, modifier : Modifier = Modifier) {
    var normalData = mutableListOf<Pair<Float, Float>>()
    for (i in 0..(data.size-1)) {
        normalData.add(Pair(data[i].first.toFloat(), data[i].second))
    }
    NormalGraph(normalData, modifier)
}
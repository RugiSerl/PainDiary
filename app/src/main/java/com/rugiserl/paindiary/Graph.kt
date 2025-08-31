package com.rugiserl.paindiary

import android.icu.util.Calendar
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update



data class GraphDataState(
    val data: List<Pair<Calendar, Float>> = listOf<Pair<Calendar, Float>>(),
)

class GraphDataViewModel : ViewModel() {

    // Expose screen UI state
    private val _uiState = MutableStateFlow(GraphDataState())
    val uiState: StateFlow<GraphDataState> = _uiState.asStateFlow()

    // Handle business logic
    fun addElement(point: Pair<Calendar, Float>) {
        _uiState.update { currentState ->
            currentState.copy(
                data = currentState.data + point
            )
        }
    }

    fun getAverage(): Double {
        if (_uiState.value.data.isNotEmpty()) { // avoid division by 0
            return _uiState.value.data.sumOf {it.second.toDouble()} / _uiState.value.data.size
        } else {
            return 0.0
        }
    }
}


@Composable
fun NormalGraph(data: List<Pair<Float, Float>>, graphColor: Color, modifier : Modifier = Modifier) {

    Card (
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ){
            Canvas(
                modifier = modifier
            ) {
                if (!data.isEmpty()) {
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
                            color = graphColor,
                            strokeWidth = 5.dp.toPx()
                        )
                        drawCircle(
                            color = graphColor,
                            radius = 5.dp.toPx(),
                            center = end
                        )
                    }
                }

            }

    }

}

@Composable
fun DayGraph(data: List<Pair<Calendar, Float>>, graphColor: Color, modifier : Modifier) {
    Card (
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ){
        Canvas(
            modifier = modifier
        ) {
            if (!data.isEmpty()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val msInADay: Float = 1000.0f * 60.0f * 60.0f * 24.0f

                for (i in 0..(data.size-2)) {
                    if (data[i].first.get(Calendar.DATE) == Calendar.getInstance().get(Calendar.DATE)) {
                        println(data[i].first.get(Calendar.MILLISECONDS_IN_DAY).toFloat()/msInADay)
                        val start = Offset(x = data[i].first.get(Calendar.MILLISECONDS_IN_DAY)/msInADay*canvasWidth, y = canvasHeight - (data[i].second)/10*canvasHeight)
                        val end = Offset(x = data[i+1].first.get(Calendar.MILLISECONDS_IN_DAY)/msInADay*canvasWidth, y = canvasHeight - (data[i+1].second)/10*canvasHeight)
                        drawLine(
                            start = start,
                            end = end,
                            color = graphColor,
                            strokeWidth = 5.dp.toPx()
                        )
                        drawCircle(
                            color = graphColor,
                            radius = 5.dp.toPx(),
                            center = start
                        )
                        if (i==data.size-2) {
                            drawCircle(
                                color = graphColor,
                                radius = 5.dp.toPx(),
                                center = end
                            )
                        }


                    }

                }
            }
        }


    }
}

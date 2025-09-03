package com.rugiserl.paindiary

import android.content.Context
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
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.sql.Date

/**
 * Converting between long and Calendar to store Calendar in the database
 */
object CalendarConverter {
    @TypeConverter
    fun toCalendar(dateLong: Long?): Calendar? {
         if (dateLong == null) {
             return null
         }
         else {
             var c = Calendar.getInstance()
             c.time = Date(dateLong)
             return c
         }
    }

    @TypeConverter
    fun fromCalendar(calendar: Calendar?): Long? {
        return calendar?.time?.time // ugly
    }
}


@Entity

@TypeConverters(CalendarConverter::class)
data class GraphEntry(
    @PrimaryKey
    @ColumnInfo(name = "date") val date: Calendar,
    @ColumnInfo(name = "pain_level") val painLevel: Float
)

@Dao
interface GraphEntryDao {
    @Query("SELECT * FROM GraphEntry")
    fun getAll(): List<GraphEntry>

    @Insert
    fun insertAll(vararg entries: GraphEntry)

    @Delete
    fun delete(entry: GraphEntry)
}

/**
 * The DAO for our database
 */
@Database(entities = [GraphEntry::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): GraphEntryDao
}

/**
 * Row of the table
 */
data class GraphDataState(
    var db: AppDatabase
)

/**
 * Viewmodel to access database within jetpack compose
 */
class GraphDataViewModel(
    private val context: Context,
) : ViewModel() {


    // Expose screen UI state
    private val _uiState = MutableStateFlow(GraphDataState(
        db = Room.databaseBuilder(
            context = context,
            AppDatabase::class.java, "painGraph"
        ).allowMainThreadQueries().build()
    ))
    val uiState: StateFlow<GraphDataState> = _uiState.asStateFlow()


    fun addElement(point: Pair<Calendar, Float>) {
        _uiState.value.db.userDao().insertAll(
            GraphEntry(
                date = point.first,
                painLevel = point.second
            )
        )
    }

    fun getAverage(): Double {
        if (!_uiState.value.db.userDao().getAll().isEmpty()) { // avoid division by 0
            return _uiState.value.db.userDao().getAll().sumOf {it.painLevel.toDouble()} / _uiState.value.db.userDao().getAll().size
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
fun DayGraph(data: List<GraphEntry>, graphColor: Color, modifier : Modifier) {
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
                    if (data[i].date.get(Calendar.DATE) == Calendar.getInstance().get(Calendar.DATE)) {
                        println(data[i].date.get(Calendar.MILLISECONDS_IN_DAY).toFloat()/msInADay)
                        val start = Offset(x = data[i].date.get(Calendar.MILLISECONDS_IN_DAY)/msInADay*canvasWidth, y = canvasHeight - (data[i].painLevel)/10*canvasHeight)
                        val end = Offset(x = data[i+1].date.get(Calendar.MILLISECONDS_IN_DAY)/msInADay*canvasWidth, y = canvasHeight - (data[i+1].painLevel)/10*canvasHeight)
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


                    }

                }
            }
        }


    }
}

package com.rugiserl.paindiary

import android.content.Context
import android.icu.util.Calendar
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import java.sql.Date

/**
 * Converting between long and Calendar to store Calendar in the database
 */
object CalendarConverter {
    @TypeConverter
    fun toCalendar(dateLong: Long): Calendar {
        var c = Calendar.getInstance()
        c.time = Date(dateLong)
        return c
    }

    @TypeConverter
    fun fromCalendar(calendar: Calendar): Long {
        return calendar.time.time // ugly
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
    private val _uiState = MutableStateFlow(
        GraphDataState(
            db = Room.databaseBuilder(
                context = context,
                AppDatabase::class.java, "painGraph"
            ).allowMainThreadQueries().build()
        )
    )
    val uiState: StateFlow<GraphDataState> = _uiState.asStateFlow()


    fun addElement(point: Pair<Calendar, Float>) {
        _uiState.value.db.userDao().insertAll(
            GraphEntry(
                date = point.first,
                painLevel = point.second
            )
        )
    }

    fun getAllEntries(): List<GraphEntry> {
        return _uiState.value.db.userDao().getAll()
    }


    fun getAverageByDate(day: Calendar = Calendar.getInstance()): Double {
        var filteredList = getAllEntries().filter {
            it.date.get(Calendar.DAY_OF_YEAR) == day.get(Calendar.DAY_OF_YEAR) && it.date.get(
                Calendar.YEAR
            ) == day.get(Calendar.YEAR)
        }
        return if (filteredList.isNotEmpty()) {
            filteredList.sumOf { it.painLevel.toDouble() } / filteredList.size
        } else {
            0.0
        }
    }

    fun getAverageByMonth(day: Calendar = Calendar.getInstance()): Double {
        var filteredList = getAllEntries().filter {
            it.date.get(Calendar.MONTH) == day.get(Calendar.MONTH) && it.date.get(Calendar.YEAR) == day.get(
                Calendar.YEAR
            )
        }
        return if (filteredList.isNotEmpty()) {
            filteredList.sumOf { it.painLevel.toDouble() } / filteredList.size
        } else {
            0.0
        }
    }
}

@Composable
fun DayGraph(data: List<GraphEntry>, graphColor: Color, modifier: Modifier) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        Column {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp)
            ) {
                Text(
                    text = "Today's pain graph"
                )
            }
            Canvas(
                modifier = modifier
            ) {
                var todayData = data.filter {
                    it.date.get(Calendar.DATE) == Calendar.getInstance().get(Calendar.DATE)
                }.sortedBy { it.date.get(Calendar.MILLISECONDS_IN_DAY) }
                if (!todayData.isEmpty()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val msInADay: Float = 1000.0f * 60.0f * 60.0f * 24.0f


                    // Draw the initial circle
                    drawCircle(
                        color = graphColor,
                        radius = 5.dp.toPx(),
                        center = Offset(
                            x = todayData[0].date.get(Calendar.MILLISECONDS_IN_DAY) / msInADay * canvasWidth,
                            y = canvasHeight - (todayData[0].painLevel) / 10 * canvasHeight
                        )
                    )

                    for (i in 0..(todayData.size - 2)) {
                        val start = Offset(
                            x = todayData[i].date.get(Calendar.MILLISECONDS_IN_DAY) / msInADay * canvasWidth,
                            y = canvasHeight - (todayData[i].painLevel) / 10 * canvasHeight
                        )
                        val end = Offset(
                            x = todayData[i + 1].date.get(Calendar.MILLISECONDS_IN_DAY) / msInADay * canvasWidth,
                            y = canvasHeight - (todayData[i + 1].painLevel) / 10 * canvasHeight
                        )
                        drawLine(
                            start = start,
                            end = end,
                            color = graphColor,
                            strokeWidth = 5.dp.toPx()
                        )
                        // Draw the end of every segment. The initial circle is already drowned earlier.
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

package com.rugiserl.paindiary

import android.app.Activity
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rugiserl.paindiary.ui.theme.PainDiaryTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val viewModel: GraphDataViewModel = GraphDataViewModel(baseContext)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    setContent {
                        PainDiaryTheme {
                            App(viewModel)
                        }
                    }

                }
            }
        }

    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun App(viewModel: GraphDataViewModel = GraphDataViewModel(LocalContext.current)) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle() // allow storing of the data even when the device is rotating
    val activity = LocalActivity.current as Activity
    var addingEntry by remember { mutableStateOf(false) }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Pain diary") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) {innerPadding ->
        Box (
            modifier = Modifier.padding(innerPadding)
        ) {

            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ){
                Text(
                    text = "Your average pain today is "+viewModel.getAverageByDate().roundToInt().toString(),
                    fontSize = 20.sp,
                    modifier = Modifier
                        .padding(20.dp)
                )
            }
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {

                DayGraph(
                    data = uiState.db.userDao().getAll(),
                    graphColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .height(300.dp)
                        .fillMaxWidth()
                        .padding(50.dp),

                )
                Row {
                    MainMenuButton(
                        onClick = {
                            activity.startActivity(Intent(activity, StatsActivity::class.java))
                        },
                        imageVector = Icons.AutoMirrored.Filled.List,
                        text = "View stats",
                        tonal = true
                    )
                    MainMenuButton(
                        onClick = {
                            addingEntry = true
                        },
                        imageVector = Icons.Default.Add,
                        text = "Add entry",
                        tonal = false
                    )

                }

            }

            if (addingEntry) {
                AddEntryDialog(
                    onDismissRequest = {
                        addingEntry = false
                    },
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun AddEntryDialog(onDismissRequest: () -> Unit, viewModel: GraphDataViewModel = GraphDataViewModel(LocalContext.current)) {
    var entryToAdd by rememberSaveable {mutableStateOf(0.0f)}
    val sliderColor = getGreenRedGradient(entryToAdd)
    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Card (
            modifier = Modifier
                .requiredSize(300.dp, 200.dp)
        ) {
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxSize()
            ) {
                Text(
                    text = "How would you rate your pain from 0 to 10 ?",
                    textAlign = TextAlign.Center
                )
                Slider(
                    value = entryToAdd,
                    valueRange = 0.0f..10.0f,
                    steps = 9,
                    onValueChange = {entryToAdd = it},
                    colors = SliderDefaults.colors(
                        thumbColor = sliderColor,
                        activeTrackColor = sliderColor,
                        inactiveTickColor = sliderColor
                    )
                )
                Text(entryToAdd.roundToInt().toString()+"/10")
                Row (
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxSize()
                ){
                    TextButton(
                        onClick = {
                            viewModel.addElement (Pair<Calendar, Float> (
                                first = Calendar.getInstance(),
                                second = entryToAdd
                            ))
                            onDismissRequest()
                        }
                    ) {
                        Text("Add entry")
                    }
                }
            }
        }
    }
}

@Composable
fun MainMenuButton(onClick: () -> Unit, imageVector: ImageVector, text: String, tonal: Boolean) {
    val content = @Composable{
        Column (
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector =  imageVector,
                contentDescription = text
            )
            Text(text)
        }
    }
    if (tonal) {
        FilledTonalButton (
            onClick = onClick,
            modifier = Modifier
                .padding(10.dp)
        ){
            content()
        }
    } else {
        Button (
            onClick = onClick,
            modifier = Modifier
                .padding(10.dp)
        ) {
            content()
        }
    }



}

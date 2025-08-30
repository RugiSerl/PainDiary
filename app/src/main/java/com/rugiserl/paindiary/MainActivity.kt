package com.rugiserl.paindiary

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rugiserl.paindiary.ui.theme.PainDiaryTheme
import kotlin.random.Random
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val viewModel: GraphDataViewModel by viewModels()

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
fun App(viewModel: GraphDataViewModel = GraphDataViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current as Activity
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Pain diary") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(
                        onClick = {
                            activity.startActivity(Intent(activity, SettingActivity::class.java))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "settings"
                        )
                    }
                }
            )


        }
    ) {innerPadding ->
        Box (
            modifier = Modifier.padding(innerPadding)
        ) {
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {

                Text(
                    text = "Your average pain today is ..",
                    fontSize = 20.sp,
                    modifier = Modifier
                        .padding(50.dp)
                )

                NormalGraph(
                    data = uiState.data,
                    modifier = Modifier.height(300.dp).fillMaxWidth().padding(50.dp)

                )
                Button(
                    onClick = {
                         viewModel.addElement (Pair<Float, Float> (
                            first = (System.currentTimeMillis()%31557600).toFloat(),
                            second = Random.nextFloat()
                        ))
                        println(uiState.data.size)
                    }
                ) {
                    Text("Click me.")
                }
            }
        }
    }
}

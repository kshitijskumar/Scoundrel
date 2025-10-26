package org.example.scoundrel

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.example.scoundrel.ui.scoundrel.ScoundrelGameScreen
import org.example.scoundrel.ui.scoundrel.ScoundrelIntent
import org.example.scoundrel.ui.scoundrel.ScoundrelViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .navigationBarsPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val vm = remember { ScoundrelViewModel() }
            LaunchedEffect(vm) {
                vm.processIntent(ScoundrelIntent.InitialisationIntent)
            }
            ScoundrelGameScreen(vm)
        }
    }
}
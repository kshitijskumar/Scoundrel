package org.example.scoundrel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.example.scoundrel.ui.scoundrel.ScoundrelGameScreen
import org.example.scoundrel.ui.scoundrel.ScoundrelIntent
import org.example.scoundrel.ui.scoundrel.ScoundrelViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import scoundrel.composeapp.generated.resources.Res
import scoundrel.composeapp.generated.resources.compose_multiplatform

@Composable
@Preview
fun App() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .safeContentPadding()
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
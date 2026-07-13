package org.onion.agro

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

import io.github.vinceglb.filekit.FileKit

fun main() {
    FileKit.init(BuildConfig.APP_NAME)
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = BuildConfig.APP_NAME,
            state = rememberWindowState(width = 1200.dp, height = 800.dp)
        ) {
            App()
        }
    }
}
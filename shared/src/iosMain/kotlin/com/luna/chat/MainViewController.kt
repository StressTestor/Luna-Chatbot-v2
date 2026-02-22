package com.luna.chat

import androidx.compose.ui.window.ComposeUIViewController
import com.luna.chat.di.initKoin
import com.luna.chat.presentation.App

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
    }
) {
    App()
}

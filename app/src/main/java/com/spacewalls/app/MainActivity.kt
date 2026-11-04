package com.spacewalls.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.spacewalls.app.di.LocalAppContainer
import com.spacewalls.app.ui.nav.SpaceWallsRoot
import com.spacewalls.app.ui.theme.SpaceColors
import com.spacewalls.app.ui.theme.SpaceWallsTheme
import androidx.compose.foundation.layout.Box

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as SpaceWallsApp).container

        setContent {
            CompositionLocalProvider(LocalAppContainer provides container) {
                SpaceWallsTheme {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SpaceColors.Black)
                    ) {
                        SpaceWallsRoot()
                    }
                }
            }
        }
    }
}

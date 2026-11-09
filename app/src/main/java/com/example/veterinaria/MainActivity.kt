package com.example.veterinaria

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.example.veterinaria.di.AppModule
import com.example.veterinaria.presentation.navigation.Routes
import com.example.veterinaria.presentation.navigation.VeterinariaNavGraph
import com.example.veterinaria.ui.theme.VeterinariaAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            VeterinariaAppTheme {
                val startDestination = remember {
                    if (AppModule.tokenManager.isLoggedIn()) {
                        Routes.HOME
                    } else {
                        Routes.LOGIN
                    }
                }

                VeterinariaNavGraph(startDestination = startDestination)
            }
        }
    }
}

package com.example.carshowroom.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.carshowroom.viewmodel.CarShowroomIntent
import com.example.carshowroom.viewmodel.CarShowroomViewModel

@Composable
fun CarShowroomApp() {
    val navController = rememberNavController()
    // Hoist the ViewModel to the NavHost level so both screens share the exact same state instance
    val viewModel: CarShowroomViewModel = hiltViewModel()

    // Global Lifecycle Observer for Audio
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_PAUSE) {
                viewModel.handleIntent(CarShowroomIntent.PauseAudio)
            } else if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.handleIntent(CarShowroomIntent.ResumeAudio)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    NavHost(
        navController = navController,
        startDestination = "showroom",
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(500)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(500)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(500)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(500)
            )
        }
    ) {
        composable("showroom") {
            VirtualShowroomScreen(
                viewModel = viewModel,
                onNavigateToAR = {
                    navController.navigate("ar_view")
                }
            )
        }
        
        composable("ar_view") {
            ArSceneScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

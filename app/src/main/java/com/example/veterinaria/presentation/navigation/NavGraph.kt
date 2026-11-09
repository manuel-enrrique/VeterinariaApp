package com.example.veterinaria.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.veterinaria.presentation.auth.LoginRoute
import com.example.veterinaria.presentation.auth.RegisterRoute
import com.example.veterinaria.presentation.home.HomeRoute
import com.example.veterinaria.presentation.home.MascotaDetailRoute
import com.example.veterinaria.presentation.home.MascotaFormRoute

/** Rutas centralizadas de la aplicación. */
object Routes {

    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"

    const val MASCOTA_FORM = "mascota_form?mascotaId={mascotaId}"
    const val MASCOTA_DETAIL = "mascota_detalle/{mascotaId}"

    const val NO_ID_ARG = -1L

    fun mascotaForm(mascotaId: Long? = null): String =
        if (mascotaId == null) "mascota_form" else "mascota_form?mascotaId=$mascotaId"

    fun mascotaDetail(mascotaId: Long): String = "mascota_detalle/$mascotaId"
}

@Composable
fun VeterinariaNavGraph(startDestination: String = Routes.LOGIN) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.LOGIN) {
            LoginRoute(
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                },
                onLoginSuccess = {
                    navController.navigateToHome()
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterRoute(
                onNavigateToLogin = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.LOGIN) {
                            launchSingleTop = true
                        }
                    }
                },
                onRegisterSuccess = {
                    navController.navigateToHome()
                }
            )
        }

        composable(Routes.HOME) {
            HomeRoute(
                onAddMascota = {
                    navController.navigate(Routes.mascotaForm())
                },
                onMascotaClick = { mascotaId ->
                    navController.navigate(Routes.mascotaDetail(mascotaId))
                },
                onEditMascota = { mascotaId ->
                    navController.navigate(Routes.mascotaForm(mascotaId))
                },
                onLoggedOut = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Routes.MASCOTA_FORM,
            arguments = listOf(
                navArgument("mascotaId") {
                    type = NavType.LongType
                    defaultValue = Routes.NO_ID_ARG
                }
            )
        ) { backStackEntry ->
            val mascotaId = backStackEntry.arguments
                ?.getLong("mascotaId")
                ?.takeIf { it > 0L }

            MascotaFormRoute(
                mascotaId = mascotaId,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.MASCOTA_DETAIL,
            arguments = listOf(
                navArgument("mascotaId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val mascotaId = backStackEntry.arguments?.getLong("mascotaId")
                ?: return@composable

            MascotaDetailRoute(
                mascotaId = mascotaId,
                onBack = { navController.popBackStack() },
                onEdit = { id ->
                    navController.navigate(Routes.mascotaForm(id))
                }
            )
        }
    }
}

private fun NavHostController.navigateToHome() {
    navigate(Routes.HOME) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

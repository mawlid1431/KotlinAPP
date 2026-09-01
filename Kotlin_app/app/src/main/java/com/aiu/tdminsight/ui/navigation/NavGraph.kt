package com.aiu.tdminsight.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aiu.tdminsight.ui.screens.*
import com.aiu.tdminsight.viewmodel.CaseViewModel

object Routes {
    const val SPLASH            = "splash"
    const val HOME              = "home"
    const val NEW_CASE          = "new_case"
    const val MEDICATION_SELECT = "medication_select"
    const val WORKFLOW_SELECT   = "workflow_select"
    const val INPUT_FORM        = "input_form"        // {workflow} arg
    const val REVIEW            = "review"
    const val CALCULATING       = "calculating"
    const val RESULTS           = "results"
    const val EXPLANATION       = "explanation"
    const val ERROR             = "engine_error"
    const val HISTORY           = "history"
    const val SETTINGS          = "settings"
    const val DISCLAIMER        = "disclaimer"
}

@Composable
fun TdmNavGraph() {
    val navController = rememberNavController()
    // Single VM instance shared across all wizard screens so user edits
    // are not lost when navigating between destinations.
    val vm: CaseViewModel = viewModel()
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
    ) {
        composable(Routes.SPLASH)            { SplashScreen(navController) }
        composable(Routes.HOME)              { HomeScreen(navController) }
        composable(Routes.NEW_CASE)          { NewCaseScreen(navController, vm = vm) }
        composable(Routes.MEDICATION_SELECT) { MedicationSelectScreen(navController, vm = vm) }
        composable(Routes.WORKFLOW_SELECT)   { WorkflowSelectScreen(navController, vm = vm) }
        composable(
            route = "${Routes.INPUT_FORM}/{workflow}",
            arguments = listOf(navArgument("workflow") { type = NavType.StringType }),
        ) { backStack ->
            val workflow = backStack.arguments?.getString("workflow") ?: "pre"
            InputFormScreen(navController, workflow, vm = vm)
        }
        composable(Routes.REVIEW)      { ReviewScreen(navController, vm = vm) }
        composable(Routes.CALCULATING) { CalculatingScreen(navController, vm = vm) }
        composable(Routes.RESULTS)     { ResultsScreen(navController, vm = vm) }
        composable(Routes.EXPLANATION) { ExplanationScreen(navController, vm = vm) }
        composable(Routes.ERROR)       { ErrorScreen(navController, vm = vm) }
        composable(Routes.HISTORY)     { HistoryScreen(navController) }
        composable(Routes.SETTINGS)    { SettingsScreen(navController) }
        composable(Routes.DISCLAIMER)  { DisclaimerScreen(navController) }
    }
}

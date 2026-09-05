package com.aiu.tdminsight.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aiu.tdminsight.auth.AuthState
import com.aiu.tdminsight.ui.screens.*
import com.aiu.tdminsight.viewmodel.AuthViewModel
import com.aiu.tdminsight.viewmodel.CaseViewModel
import com.aiu.tdminsight.viewmodel.HistoryViewModel

object Routes {
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
    const val PROFILE           = "profile"
    const val DISCLAIMER        = "disclaimer"
}

/**
 * @param authVm the Activity-scoped AuthViewModel. It is passed in rather than
 * obtained with `viewModel()` because inside a NavHost that call resolves to the
 * NavBackStackEntry and would create a second, unobserved instance — which is
 * what previously made "Sign out" appear to do nothing.
 */
@Composable
fun TdmNavGraph(authVm: AuthViewModel) {
    val navController = rememberNavController()

    // These two ViewModels are Activity-scoped (this call site sits outside the
    // NavHost), so without a key they would OUTLIVE a sign-out and the next
    // person to sign in would inherit the previous user's wizard state and case
    // history. Keying them by Clerk user ID gives each account its own instance.
    val userKey = (authVm.authState.value as? AuthState.Authenticated)?.userId ?: "anonymous"
    val vm: CaseViewModel           = viewModel(key = "case-$userKey")
    val historyVm: HistoryViewModel = viewModel(key = "history-$userKey")

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
    ) {
        composable(Routes.HOME)              { HomeScreen(navController, historyVm = historyVm) }
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
        composable(Routes.HISTORY)     { HistoryScreen(navController, vm = historyVm) }
        composable(Routes.SETTINGS)    { SettingsScreen(navController, historyVm = historyVm, authVm = authVm) }
        composable(Routes.PROFILE)     { ProfileScreen(navController, authVm = authVm) }
        composable(Routes.DISCLAIMER)  { DisclaimerScreen(navController) }
    }
}

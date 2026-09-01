package com.aiu.tdminsight.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aiu.tdminsight.TdmApplication
import com.aiu.tdminsight.data.model.VancoWorkflow
import com.aiu.tdminsight.ui.screens.HistoryEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val supabaseRepo = (application as TdmApplication).supabaseRepository

    private val _isLoading  = MutableStateFlow(true)
    internal val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // true  = data came from Supabase (real saved cases)
    // false = fallback demo entries shown while no cases have been saved yet
    private val _isLiveData = MutableStateFlow(false)
    internal val isLiveData: StateFlow<Boolean> = _isLiveData.asStateFlow()

    private val _entries = MutableStateFlow<List<HistoryEntry>>(emptyList())
    internal val entries: StateFlow<List<HistoryEntry>> = _entries.asStateFlow()

    init { load() }

    /** Re-fetch — called when the screen is re-entered or user triggers a refresh. */
    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            val live = supabaseRepo.loadRecentCases()
            if (live.isNotEmpty()) {
                _entries.value    = live
                _isLiveData.value = true
            } else {
                _entries.value    = DEMO_ENTRIES
                _isLiveData.value = false
            }
            _isLoading.value = false
        }
    }

    companion object {
        // Shown when the user has no saved cases yet (or Supabase is unreachable).
        internal val DEMO_ENTRIES = listOf(
            HistoryEntry("MDH-001", "26 Aug 2026", VancoWorkflow.PRE_POST, 1000.0, 12.0, 1.0,  487.2,  978.0, 0.0769,  9.01, 34.8, 2.68),
            HistoryEntry("MDH-002", "24 Aug 2026", VancoWorkflow.PRE,       750.0,  8.0, 1.0,  621.4,  611.0, 0.0521, 13.30, 48.3, 2.51),
            HistoryEntry("MDH-003", "22 Aug 2026", VancoWorkflow.POST,     1250.0, 12.0, 1.0,  358.8, 1196.0, 0.1012,  6.85, 29.4, 2.98),
            HistoryEntry("MDH-004", "19 Aug 2026", VancoWorkflow.PRE_POST, 1000.0, 12.0, 1.0,  512.1, 1023.0, 0.0831,  8.34, 31.6, 2.63),
        )
    }
}

package com.aiu.tdminsight.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aiu.tdminsight.TdmApplication
import com.aiu.tdminsight.data.model.HistoryEntry
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

    /**
     * Deletes every saved case for the signed-in user, then refreshes.
     * Reports failure instead of optimistically emptying the list, so a failed
     * delete leaves the cases visible rather than faking success.
     */
    fun clearAllCases(onResult: (Boolean) -> Unit = {}) {
        val userId = (getApplication<android.app.Application>() as TdmApplication)
            .authRepository.savedSession()?.userId
        if (userId == null) {
            onResult(false)
            return
        }
        viewModelScope.launch {
            val ok = supabaseRepo.deleteAllCases(userId)
            if (ok) load()
            onResult(ok)
        }
    }

    /** Re-fetch — called when the screen is re-entered or user triggers a refresh. */
    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            val live = supabaseRepo.loadRecentCases()
            _entries.value    = live
            _isLiveData.value = live.isNotEmpty()
            _isLoading.value  = false
        }
    }
}

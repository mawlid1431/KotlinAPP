package com.aiu.tdminsight.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aiu.tdminsight.TdmApplication
import com.aiu.tdminsight.data.model.HistoryEntry
import com.aiu.tdminsight.ui.export.CaseReportPdf
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

    // The case the user tapped, shown on the detail screen. Held here rather
    // than passed through the route so the full record travels without being
    // serialised into a URL.
    private val _selected = MutableStateFlow<HistoryEntry?>(null)
    internal val selected: StateFlow<HistoryEntry?> = _selected.asStateFlow()

    fun select(entry: HistoryEntry) { _selected.value = entry }

    /**
     * The signed-in Clerk user, printed on an exported report as the person who
     * created the case. History is filtered to this user's own rows server-side,
     * so the owner of every visible case is this account.
     */
    fun reportAuthor(): CaseReportPdf.Author? =
        (getApplication<android.app.Application>() as TdmApplication)
            .authRepository.savedSession()?.let {
                CaseReportPdf.Author(
                    name   = it.fullName,
                    email  = it.email,
                    userId = it.userId,
                )
            }

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
            // Keep the open detail in sync with a refresh, and drop it if the
            // case is gone (e.g. after "clear all cases").
            _selected.value = _selected.value?.let { sel ->
                live.firstOrNull { it.rowId != null && it.rowId == sel.rowId }
                    ?: live.firstOrNull { it.caseId == sel.caseId && it.date == sel.date }
            }
            _isLiveData.value = live.isNotEmpty()
            _isLoading.value  = false
        }
    }
}

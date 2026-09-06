package com.aiu.tdminsight.ui.export

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import com.aiu.tdminsight.data.model.HistoryEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ShareCaseReport — builds the PDF for a saved case and hands the real file to
 * Android's share sheet, so WhatsApp, Gmail, Drive, Files and any other target
 * receive an actual application/pdf attachment (not a link and not a screenshot).
 */
object ShareCaseReport {

    private const val TAG = "ShareCaseReport"

    /**
     * Generates the report and opens the chooser.
     *
     * PDF generation is real work, so it runs on [Dispatchers.Default]; only the
     * chooser is started on the caller's (main) dispatcher.
     *
     * @return true when the chooser was opened, false when anything failed —
     * the caller shows a message rather than leaving the user with no feedback.
     */
    suspend fun share(context: Context, entry: HistoryEntry, author: CaseReportPdf.Author?): Boolean {
        return try {
            val file = withContext(Dispatchers.Default) {
                CaseReportPdf.build(context, entry, author)
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file,
            )

            val send = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "TDM Insight report — ${entry.caseId}")
                putExtra(Intent.EXTRA_TEXT, summaryText(entry))
                // Grants the receiving app read access to the cached file for
                // the lifetime of that intent. Without this the attachment
                // arrives as a permission error in WhatsApp and Gmail.
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                clipData = android.content.ClipData.newRawUri("report", uri)
            }

            val chooser = Intent.createChooser(send, "Share report").apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            Log.w(TAG, "share failed: ${e.message}", e)
            false
        }
    }

    /** Short body text that travels with the attachment. */
    private fun summaryText(e: HistoryEntry): String = buildString {
        appendLine("TDM Insight — vancomycin TDM report")
        appendLine("Case: ${e.caseId}  (${e.date})")
        appendLine("Workflow: ${CaseReportPdf.workflowLabel(e.workflow)}")
        appendLine()
        appendLine("AUC24: ${"%.0f".format(e.auc24)} mg.h/L")
        appendLine("Recommended dose: ${"%.0f".format(e.recDoseMg)} mg every ${"%.0f".format(e.intervalH)} h")
        appendLine()
        append("Full report attached as PDF. Academic prototype — fictional data only.")
    }
}

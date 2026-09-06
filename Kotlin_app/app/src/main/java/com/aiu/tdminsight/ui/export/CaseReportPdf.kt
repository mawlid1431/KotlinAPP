package com.aiu.tdminsight.ui.export

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.aiu.tdminsight.data.model.Auc24Verdict
import com.aiu.tdminsight.data.model.HistoryEntry
import com.aiu.tdminsight.data.model.VancoWorkflow
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.math.exp
import kotlin.math.max

/**
 * CaseReportPdf — renders a saved case as a structured, multi-page A4 PDF.
 *
 * This draws the actual data and re-plots the charts with android.graphics; it
 * is never a screenshot of the UI. Everything is laid out through [Doc], a tiny
 * flowing-layout helper that starts a new page whenever the next block would
 * not fit, so nothing is ever clipped at the page edge.
 *
 * Uses android.graphics.pdf.PdfDocument from the platform — no new dependency.
 */
object CaseReportPdf {

    // ── Page geometry: A4 at 72 dpi (points) ──────────────────────────────
    private const val PAGE_W = 595
    private const val PAGE_H = 842
    private const val MARGIN = 44f
    private const val CONTENT_W = PAGE_W - MARGIN * 2      // 507
    private const val FOOTER_TOP = PAGE_H - 54f            // nothing may cross this

    // ── Brand palette (matches the app's Material 3 scheme) ───────────────
    private const val NAVY    = 0xFF073A6B.toInt()
    private const val PRIMARY = 0xFF1464A8.toInt()
    private const val TINT    = 0xFFE3F0FF.toInt()
    private const val SURFACE = 0xFFF5F5F5.toInt()
    private const val TEXT    = 0xFF0F0F0F.toInt()
    private const val MUTED   = 0xFF6B6B6B.toInt()
    private const val LINE    = 0xFFE2E2E2.toInt()
    private const val SUCCESS = 0xFF1E7A46.toInt()
    private const val WARNING = 0xFF9A5F00.toInt()
    private const val ERROR   = 0xFFCC2929.toInt()

    private val SANS      = Typeface.create("sans-serif", Typeface.NORMAL)
    private val SANS_BOLD = Typeface.create("sans-serif", Typeface.BOLD)
    private val MONO      = Typeface.create("monospace", Typeface.NORMAL)
    private val MONO_BOLD = Typeface.create("monospace", Typeface.BOLD)

    /**
     * Identifies the person the report is about. Every field is read from the
     * live Clerk session, never typed in or invented.
     */
    data class Author(
        val name: String?,
        val email: String?,
        val userId: String?,
    )

    /**
     * Builds the report and returns the written file.
     *
     * @param author the signed-in Clerk user — the person who created the case.
     */
    fun build(context: Context, e: HistoryEntry, author: Author?): File {
        // Two passes: the first only counts pages so the second can print
        // "Page 1 of 3". PdfDocument cannot revisit a finished page, and the
        // layout is deterministic, so the two passes always agree.
        val probe = PdfDocument()
        val pageCount = render(probe, e, author, totalPages = 0)
        probe.close()

        val pdf = PdfDocument()
        render(pdf, e, author, totalPages = pageCount)

        val dir = File(context.cacheDir, "shared_reports").apply { mkdirs() }
        // One file per case, overwritten on re-share, so the cache cannot grow
        // without bound as the user shares the same case repeatedly.
        val file = File(dir, "TDM-Insight-${safeName(e.caseId)}.pdf")
        FileOutputStream(file).use { pdf.writeTo(it) }
        pdf.close()
        return file
    }

    /** Lays the whole report out into [pdf]; returns the number of pages used. */
    private fun render(
        pdf: PdfDocument,
        e: HistoryEntry,
        author: Author?,
        totalPages: Int,
    ): Int {
        val doc = Doc(pdf, totalPages)
        doc.newPage()
        drawHeaderBand(doc, e, author)
        sectionSummary(doc, e)
        sectionTargetBand(doc, e)
        sectionPatient(doc, e)
        sectionRegimen(doc, e)
        sectionSamples(doc, e)
        sectionParameters(doc, e)
        sectionCurve(doc, e)
        sectionMethod(doc, e)
        sectionDisclaimer(doc)
        doc.finishPage()
        return doc.pageNo
    }

    /** Trims text with a trailing ellipsis until it fits [maxW] points. */
    private fun ellipsize(text: String, p: Paint, maxW: Float): String {
        if (p.measureText(text) <= maxW) return text
        var cut = text.length
        while (cut > 1 && p.measureText(text.take(cut) + "…") > maxW) cut--
        return text.take(cut) + "…"
    }

    private fun safeName(raw: String): String {
        val cleaned = raw.trim().replace(Regex("[^A-Za-z0-9._-]"), "-").trim('-')
        return if (cleaned.isBlank()) "case" else cleaned.take(48)
    }

    // ══════════════════════════════════════════════════════════════════════
    // Flowing document: pages, cursor, automatic page breaks
    // ══════════════════════════════════════════════════════════════════════
    private class Doc(val pdf: PdfDocument, val totalPages: Int) {
        var page: PdfDocument.Page? = null
        lateinit var canvas: Canvas
        var y = 0f
        var pageNo = 0

        fun newPage() {
            finishPage()
            pageNo++
            val info = PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, pageNo).create()
            page = pdf.startPage(info)
            canvas = page!!.canvas
            canvas.drawColor(Color.WHITE)
            y = MARGIN
        }

        /** Stamps the footer, then closes the current page. */
        fun finishPage() {
            val p = page ?: return
            drawFooter(p.canvas, pageNo, totalPages)
            pdf.finishPage(p)
            page = null
        }

        /** Starts a new page unless [h] points still fit above the footer. */
        fun ensure(h: Float) {
            if (y + h > FOOTER_TOP) {
                newPage()
                // Continuation pages get a slim running head so a shared PDF
                // still identifies itself on page 2 and beyond.
                val p = paint(SANS, 8f, MUTED)
                canvas.drawText("TDM Insight — vancomycin TDM report (continued)", MARGIN, y + 8f, p)
                y += 22f
                rule(this)
                y += 12f
            }
        }

        fun space(h: Float) { y += h }
    }

    private fun drawFooter(c: Canvas, pageNo: Int, totalPages: Int) {
        c.drawLine(MARGIN, FOOTER_TOP + 14f, PAGE_W - MARGIN, FOOTER_TOP + 14f, stroke(LINE, 0.8f))
        c.drawText(
            "TDM Insight · academic prototype (CDE2313, AIU) · fictional patient data only",
            MARGIN, FOOTER_TOP + 28f, paint(SANS, 8f, MUTED)
        )
        val label = if (totalPages > 0) "Page $pageNo of $totalPages" else "Page $pageNo"
        val r = paint(SANS, 8f, MUTED).apply { textAlign = Paint.Align.RIGHT }
        c.drawText(label, PAGE_W - MARGIN, FOOTER_TOP + 28f, r)
    }

    private fun paint(tf: Typeface, size: Float, color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = tf
        textSize = size
        this.color = color
    }

    private fun fill(color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        this.color = color
    }

    private fun stroke(color: Int, w: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = w
        this.color = color
    }

    private fun rule(doc: Doc) {
        doc.canvas.drawLine(MARGIN, doc.y, PAGE_W - MARGIN, doc.y, stroke(LINE, 0.8f))
    }

    // ══════════════════════════════════════════════════════════════════════
    // Blocks
    // ══════════════════════════════════════════════════════════════════════

    private fun drawHeaderBand(doc: Doc, e: HistoryEntry, author: Author?) {
        val h = 74f
        doc.canvas.drawRoundRect(
            RectF(MARGIN, doc.y, PAGE_W - MARGIN, doc.y + h), 8f, 8f, fill(NAVY)
        )
        doc.canvas.drawText("TDM Insight", MARGIN + 16f, doc.y + 28f, paint(SANS_BOLD, 17f, Color.WHITE))
        doc.canvas.drawText(
            "Vancomycin therapeutic drug monitoring report",
            MARGIN + 16f, doc.y + 45f, paint(SANS, 10f, 0xFFA9C2DA.toInt())
        )
        doc.canvas.drawText(
            "Albukhary International University (AIU) · CDE2313",
            MARGIN + 16f, doc.y + 60f, paint(SANS, 8.5f, 0xFF7E97AE.toInt())
        )

        val right = paint(SANS, 9f, 0xFFCFE3F5.toInt()).apply { textAlign = Paint.Align.RIGHT }
        val stamp = SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
        doc.canvas.drawText("Report generated $stamp", PAGE_W - MARGIN - 16f, doc.y + 28f, right)
        doc.canvas.drawText(
            "Report ID ${e.rowId?.take(8) ?: "—"}",
            PAGE_W - MARGIN - 16f, doc.y + 44f, paint(SANS, 8f, 0xFF7E97AE.toInt()).apply {
                textAlign = Paint.Align.RIGHT
            }
        )
        doc.y += h + 18f

        // ── Case identity ────────────────────────────────────────────────
        // Name, real creation timestamp and real creator: the case label and
        // created_at come from the saved database row, the person comes from
        // the live Clerk session that owns it.
        val titlePaint = paint(SANS_BOLD, 15f, TEXT)
        doc.canvas.drawText(
            ellipsize(e.caseId, titlePaint, CONTENT_W - 150f), MARGIN, doc.y + 14f, titlePaint
        )
        doc.canvas.drawText(
            workflowLabel(e.workflow), MARGIN, doc.y + 30f, paint(SANS, 9.5f, MUTED)
        )
        doc.y += 42f

        val rows = mutableListOf<Pair<String, String>>()
        rows += "Created" to createdAtText(e)
        author?.name?.takeIf { it.isNotBlank() }?.let { rows += "Created by" to it }
        author?.email?.takeIf { it.isNotBlank() }?.let { rows += "Account" to it }
        author?.userId?.takeIf { it.isNotBlank() }?.let { rows += "User ID" to it }

        val boxH = 18f * rows.size + 16f
        doc.canvas.drawRoundRect(
            RectF(MARGIN, doc.y, PAGE_W - MARGIN, doc.y + boxH), 6f, 6f, fill(SURFACE)
        )
        rows.forEachIndexed { i, (k, v) ->
            val baseline = doc.y + 21f + i * 18f
            doc.canvas.drawText(k, MARGIN + 12f, baseline, paint(SANS, 8.5f, MUTED))
            val vp = paint(SANS_BOLD, 9f, TEXT)
            doc.canvas.drawText(
                ellipsize(v, vp, CONTENT_W - 110f), MARGIN + 92f, baseline, vp
            )
        }
        doc.y += boxH + 18f
    }

    /**
     * The case's real creation time, taken from the database row's created_at
     * and shown in the reader's own time zone. Falls back to the date-only
     * value already carried on the entry if the timestamp cannot be parsed.
     */
    private fun createdAtText(e: HistoryEntry): String {
        val iso = e.createdAtIso
        if (!iso.isNullOrBlank()) {
            try {
                val local = OffsetDateTime.parse(iso).atZoneSameInstant(ZoneId.systemDefault())
                return local.format(DateTimeFormatter.ofPattern("d MMM yyyy 'at' HH:mm", Locale.getDefault()))
            } catch (_: Exception) {
                // Some PostgREST builds return a 2-digit offset ("+00"), which
                // OffsetDateTime rejects; show the raw date and time instead.
                return iso.take(16).replace('T', ' ')
            }
        }
        return e.date
    }

    /**
     * @param keepWith points of content that must fit under the heading. Without
     * it a heading can be printed at the foot of a page with its table starting
     * on the next one, which reads as an orphaned title.
     */
    private fun sectionTitle(doc: Doc, title: String, keepWith: Float = 62f) {
        doc.ensure(34f + keepWith)
        doc.canvas.drawText(title.uppercase(Locale.ROOT), MARGIN, doc.y + 10f, paint(SANS_BOLD, 9f, PRIMARY))
        doc.y += 16f
        rule(doc)
        doc.y += 12f
    }

    /** Three headline numbers: AUC24, verdict, recommended dose. */
    private fun sectionSummary(doc: Doc, e: HistoryEntry) {
        sectionTitle(doc, "Result summary")
        val h = 68f
        doc.ensure(h + 10f)
        val gap = 12f
        val w = (CONTENT_W - gap * 2) / 3f
        val vc = verdictColor(e.verdict)

        data class Box(val label: String, val value: String, val unit: String, val color: Int)
        val boxes = listOf(
            Box("AUC₂₄ (24-hour exposure)", fmt(e.auc24, 0), "mg·h/L", vc),
            Box("Verdict vs 400–600 band", verdictLabel(e.verdict), "", vc),
            Box("Recommended dose", fmt(e.recDoseMg, 0), "mg every ${fmt(e.intervalH, 0)} h", TEXT),
        )
        boxes.forEachIndexed { i, b ->
            val x = MARGIN + i * (w + gap)
            doc.canvas.drawRoundRect(RectF(x, doc.y, x + w, doc.y + h), 6f, 6f, fill(if (i == 1) TINT else SURFACE))
            doc.canvas.drawText(b.label, x + 12f, doc.y + 16f, paint(SANS, 8f, MUTED))
            val big = if (b.value.length > 8) 14f else 21f
            doc.canvas.drawText(b.value, x + 12f, doc.y + 42f, paint(MONO_BOLD, big, b.color))
            if (b.unit.isNotBlank()) {
                doc.canvas.drawText(b.unit, x + 12f, doc.y + 57f, paint(SANS, 8.5f, MUTED))
            }
        }
        doc.y += h + 18f
    }

    /** Horizontal target-band chart with the patient's AUC24 marked on it. */
    private fun sectionTargetBand(doc: Doc, e: HistoryEntry) {
        sectionTitle(doc, "AUC₂₄ against the therapeutic band")
        val h = 62f
        doc.ensure(h + 16f)

        val axisMin = 0.0
        val axisMax = max(800.0, e.auc24 * 1.15)
        val barY = doc.y + 22f
        val barH = 16f
        fun px(v: Double) = MARGIN + (CONTENT_W * ((v - axisMin) / (axisMax - axisMin))).toFloat()

        // Full range, then the in-target window highlighted
        doc.canvas.drawRoundRect(RectF(MARGIN, barY, MARGIN + CONTENT_W, barY + barH), 4f, 4f, fill(SURFACE))
        doc.canvas.drawRect(RectF(px(400.0), barY, px(600.0), barY + barH), fill(0xFFD6EFE0.toInt()))
        doc.canvas.drawLine(px(400.0), barY, px(400.0), barY + barH, stroke(SUCCESS, 1f))
        doc.canvas.drawLine(px(600.0), barY, px(600.0), barY + barH, stroke(SUCCESS, 1f))

        // Marker for this case
        val mx = px(e.auc24).coerceIn(MARGIN, MARGIN + CONTENT_W)
        val vc = verdictColor(e.verdict)
        doc.canvas.drawLine(mx, barY - 7f, mx, barY + barH + 7f, stroke(vc, 2.2f))
        val tri = Path().apply {
            moveTo(mx, barY - 7f); lineTo(mx - 5f, barY - 15f); lineTo(mx + 5f, barY - 15f); close()
        }
        doc.canvas.drawPath(tri, fill(vc))

        // Scale labels, clamped inside the content box so nothing runs off page
        val lab = paint(SANS, 7.5f, MUTED).apply { textAlign = Paint.Align.CENTER }
        listOf(0.0, 400.0, 600.0, axisMax).forEach { v ->
            val x = px(v).coerceIn(MARGIN + 12f, MARGIN + CONTENT_W - 12f)
            doc.canvas.drawText(fmt(v, 0), x, barY + barH + 20f, lab)
        }
        val valueLab = paint(MONO_BOLD, 8.5f, vc).apply { textAlign = Paint.Align.CENTER }
        val vx = mx.coerceIn(MARGIN + 26f, MARGIN + CONTENT_W - 26f)
        doc.canvas.drawText("${fmt(e.auc24, 0)} mg·h/L", vx, barY - 19f, valueLab)

        doc.canvas.drawText(
            "Target 400–600 mg·h/L (Rybak MJ et al., Am J Health-Syst Pharm, 2020)",
            MARGIN, barY + barH + 36f, paint(SANS, 8f, MUTED)
        )
        doc.y += h + 22f
    }

    private fun sectionPatient(doc: Doc, e: HistoryEntry) {
        sectionTitle(doc, "Patient")
        table(doc, listOf(
            Triple("Weight", fmt(e.weightKg, 1), "kg"),
            Triple("Age", e.ageYears.toString(), "years"),
            Triple("Sex", if (e.isMale) "Male" else "Female", ""),
            Triple("Serum creatinine", fmt(e.scrUmolL, 1), "µmol/L"),
        ))
    }

    private fun sectionRegimen(doc: Doc, e: HistoryEntry) {
        sectionTitle(doc, "Dosing regimen as prescribed")
        table(doc, listOf(
            Triple("Dose", fmt(e.doseMg, 0), "mg"),
            Triple("Dosing interval (τ)", fmt(e.intervalH, 1), "h"),
            Triple("Infusion duration", fmt(e.tInfH, 2), "h"),
            Triple("Sampling workflow", shortWorkflow(e.workflow), ""),
        ))
    }

    private fun sectionSamples(doc: Doc, e: HistoryEntry) {
        val rows = mutableListOf<Triple<String, String, String>>()
        e.preConcMgL?.let { rows += Triple("Pre-dose (trough) concentration", fmt(it, 2), "mg/L") }
        e.preTimeH?.let { rows += Triple("Trough sample time", fmt(it, 2), "h after dose start") }
        e.postConcMgL?.let { rows += Triple("Post-dose (peak) concentration", fmt(it, 2), "mg/L") }
        e.postTimeH?.let { rows += Triple("Peak sample time", fmt(it, 2), "h after dose start") }
        if (rows.isEmpty()) return
        sectionTitle(doc, "Measured concentrations")
        table(doc, rows)
    }

    private fun sectionParameters(doc: Doc, e: HistoryEntry) {
        sectionTitle(doc, "Pharmacokinetic parameters")
        val rows = mutableListOf(
            Triple("Elimination rate constant (ke)", fmt(e.ke, 4), "h⁻¹"),
            Triple("Elimination half-life (t½)", fmt(e.t12, 2), "h"),
            Triple("Volume of distribution (Vd)", fmt(e.vdL, 2), "L"),
            Triple("Vd per kg", fmt(e.vdLPerKg, 3), "L/kg"),
            Triple("Clearance (CL)", fmt(e.clLH, 3), "L/h"),
            Triple("AUC₂₄", fmt(e.auc24, 1), "mg·h/L"),
            Triple("Recommended dose", fmt(e.recDoseMg, 0), "mg every ${fmt(e.intervalH, 0)} h"),
        )
        e.cmin?.let { rows += Triple("Projected trough (Cmin)", fmt(it, 2), "mg/L") }
        e.cmax?.let { rows += Triple("Projected peak (Cmax)", fmt(it, 2), "mg/L") }
        table(doc, rows)
    }

    /**
     * Full concentration-time curve with axes, gridlines and labels.
     * Uses the same one-compartment infusion model as the app's chart.
     */
    private fun sectionCurve(doc: Doc, e: HistoryEntry) {
        sectionTitle(doc, "Predicted concentration–time curve")
        val plotH = 180f
        val blockH = plotH + 62f
        // Keep the whole chart on one page: never split a graph across a break.
        doc.ensure(blockH)

        val left = MARGIN + 34f
        val right = PAGE_W - MARGIN - 6f
        val top = doc.y + 8f
        val bottom = top + plotH
        val plotW = right - left

        val tau = if (e.intervalH > 0) e.intervalH else 12.0
        val tInf = if (e.tInfH > 0) e.tInfH else 1.0

        fun conc(t: Double): Double {
            if (e.ke <= 1e-9 || e.vdL <= 1e-9) return 0.0
            val peak = (e.doseMg / (e.ke * e.vdL * tInf)) * (1.0 - exp(-e.ke * tInf))
            return if (t <= tInf) (e.doseMg / (e.ke * e.vdL * tInf)) * (1.0 - exp(-e.ke * t))
            else peak * exp(-e.ke * (t - tInf))
        }

        val steps = 160
        val pts = (0..steps).map { i -> val t = tau * i / steps; t to conc(t) }
        val cMax = max(pts.maxOf { it.second }, 1.0)
        val yMax = niceCeil(cMax * 1.15)

        fun px(t: Double) = left + (plotW * (t / tau)).toFloat()
        fun py(c: Double) = bottom - (plotH * (c / yMax)).toFloat()

        // Plot frame
        doc.canvas.drawRect(RectF(left, top, right, bottom), fill(0xFFFCFDFE.toInt()))

        // Y gridlines + labels
        val yLab = paint(SANS, 7.5f, MUTED).apply { textAlign = Paint.Align.RIGHT }
        val gridPaint = stroke(LINE, 0.7f)
        for (i in 0..4) {
            val v = yMax * i / 4.0
            val yy = py(v)
            doc.canvas.drawLine(left, yy, right, yy, gridPaint)
            doc.canvas.drawText(fmt(v, if (yMax < 10) 1 else 0), left - 6f, yy + 3f, yLab)
        }

        // X ticks + labels
        val xLab = paint(SANS, 7.5f, MUTED).apply { textAlign = Paint.Align.CENTER }
        val xTicks = 6
        for (i in 0..xTicks) {
            val t = tau * i / xTicks
            val xx = px(t)
            doc.canvas.drawLine(xx, bottom, xx, bottom + 4f, gridPaint)
            doc.canvas.drawText(fmt(t, if (tau <= 6) 1 else 0), xx, bottom + 15f, xLab)
        }

        // Area under the curve, then the curve itself
        val area = Path().apply {
            moveTo(px(0.0), bottom)
            pts.forEach { (t, c) -> lineTo(px(t), py(c)) }
            lineTo(px(tau), bottom)
            close()
        }
        doc.canvas.drawPath(area, fill(0x141464A8))
        val line = Path().apply {
            moveTo(px(pts.first().first), py(pts.first().second))
            pts.drop(1).forEach { (t, c) -> lineTo(px(t), py(c)) }
        }
        doc.canvas.drawPath(line, stroke(PRIMARY, 2f))

        // End-of-infusion marker
        if (tInf < tau) {
            val ix = px(tInf)
            val dash = stroke(0xFF9FB4C8.toInt(), 1f).apply {
                pathEffect = android.graphics.DashPathEffect(floatArrayOf(4f, 4f), 0f)
            }
            doc.canvas.drawLine(ix, top, ix, bottom, dash)
            doc.canvas.drawText("end of infusion", ix + 4f, top + 11f, paint(SANS, 7f, MUTED))
        }

        // Peak / trough call-outs, drawn inside the plot so they cannot clip
        val peakC = pts.maxByOrNull { it.second }
        if (peakC != null) {
            doc.canvas.drawCircle(px(peakC.first), py(peakC.second), 2.6f, fill(PRIMARY))
            val t = paint(MONO_BOLD, 7.5f, PRIMARY)
            val label = "peak ${fmt(peakC.second, 1)} mg/L"
            val tx = (px(peakC.first) + 6f).coerceAtMost(right - t.measureText(label) - 4f)
            doc.canvas.drawText(label, tx, (py(peakC.second) - 6f).coerceAtLeast(top + 10f), t)
        }
        val troughC = pts.last()
        doc.canvas.drawCircle(px(troughC.first), py(troughC.second), 2.6f, fill(PRIMARY))
        val tp = paint(MONO_BOLD, 7.5f, PRIMARY).apply { textAlign = Paint.Align.RIGHT }
        doc.canvas.drawText(
            "trough ${fmt(troughC.second, 1)} mg/L",
            right - 4f, (py(troughC.second) - 8f).coerceAtLeast(top + 10f), tp
        )

        // Axis frame last, so it sits above the fill
        doc.canvas.drawRect(RectF(left, top, right, bottom), stroke(0xFFC9D4DF.toInt(), 1f))

        // Axis titles
        val rot = paint(SANS, 8f, MUTED)
        doc.canvas.save()
        doc.canvas.rotate(-90f, MARGIN + 4f, (top + bottom) / 2f)
        rot.textAlign = Paint.Align.CENTER
        doc.canvas.drawText("Concentration (mg/L)", MARGIN + 4f, (top + bottom) / 2f, rot)
        doc.canvas.restore()
        doc.canvas.drawText(
            "Hours after the start of the dose (one dosing interval, τ = ${fmt(tau, 0)} h)",
            left, bottom + 30f, paint(SANS, 8f, MUTED)
        )
        doc.y = bottom + 46f
    }

    private fun sectionMethod(doc: Doc, e: HistoryEntry) {
        sectionTitle(doc, "Method")
        val method = when (e.workflow) {
            VancoWorkflow.PRE ->
                "PRE (trough only): CrCl by Cockcroft–Gault, CL = CrCl × 0.06, Vd = 0.7 L/kg, ke = CL / Vd."
            VancoWorkflow.POST ->
                "POST (peak only): ke fitted to the measured level by Newton–Raphson on the infusion model."
            VancoWorkflow.PRE_POST ->
                "PRE + POST: Sawchuk–Zaske, ke = ln(Cpeak / Ctrough) / Δt, Vd from the infusion model."
        }
        paragraph(doc, method)
        paragraph(
            doc,
            "AUC₂₄ = (Dose / τ) × 24 / CL.  Recommended dose = AUC_target × CL × τ / 24, " +
            "AUC_target = 500 mg·h/L (centre of the 400–600 band)."
        )
    }

    private fun sectionDisclaimer(doc: Doc) {
        doc.ensure(38f)
        doc.space(4f)
        val h = 32f
        doc.canvas.drawRoundRect(RectF(MARGIN, doc.y, PAGE_W - MARGIN, doc.y + h), 6f, 6f, fill(0xFFFFF4E5.toInt()))
        doc.canvas.drawText("Disclaimer", MARGIN + 12f, doc.y + 14f, paint(SANS_BOLD, 9f, WARNING))
        doc.canvas.drawText(
            "Educational tool, fictional patient data. Every result must be reviewed by a qualified clinical pharmacist.",
            MARGIN + 12f, doc.y + 26f, paint(SANS, 8.5f, 0xFF7A5510.toInt())
        )
        doc.y += h + 4f
    }

    // ── Layout atoms ──────────────────────────────────────────────────────

    /** Two-column label/value table with zebra rows and aligned units. */
    private fun table(doc: Doc, rows: List<Triple<String, String, String>>) {
        val rowH = 18f
        rows.forEachIndexed { i, (label, value, unit) ->
            doc.ensure(rowH)
            if (i % 2 == 0) {
                doc.canvas.drawRect(RectF(MARGIN, doc.y, PAGE_W - MARGIN, doc.y + rowH), fill(0xFFFAFBFC.toInt()))
            }
            val baseline = doc.y + 12.5f
            // Value is right-aligned at a fixed column so every number lines up.
            val valueRight = PAGE_W - MARGIN - 96f
            val labelPaint = paint(SANS, 9.5f, TEXT)
            val labelMaxW = valueRight - (MARGIN + 8f) - 60f
            doc.canvas.drawText(ellipsize(label, labelPaint, labelMaxW), MARGIN + 8f, baseline, labelPaint)
            val vp = paint(MONO_BOLD, 9.5f, NAVY).apply { textAlign = Paint.Align.RIGHT }
            doc.canvas.drawText(value, valueRight, baseline, vp)
            doc.canvas.drawText(unit, valueRight + 8f, baseline, paint(SANS, 8.5f, MUTED))
            doc.y += rowH
        }
        doc.y += 12f
    }

    /** Word-wrapped paragraph that breaks across pages if it has to. */
    private fun paragraph(doc: Doc, text: String) {
        val p = paint(SANS, 9f, MUTED)
        val maxW = CONTENT_W - 8f
        val words = text.split(" ")
        var line = StringBuilder()
        val lines = mutableListOf<String>()
        words.forEach { w ->
            val candidate = if (line.isEmpty()) w else "$line $w"
            if (p.measureText(candidate) > maxW) {
                lines += line.toString(); line = StringBuilder(w)
            } else line = StringBuilder(candidate)
        }
        if (line.isNotEmpty()) lines += line.toString()

        lines.forEach { l ->
            doc.ensure(13f)
            doc.canvas.drawText(l, MARGIN + 4f, doc.y + 9f, p)
            doc.y += 13f
        }
        doc.y += 8f
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private fun fmt(v: Double, decimals: Int): String =
        if (v.isNaN() || v.isInfinite()) "—" else String.format(Locale.US, "%.${decimals}f", v)

    private fun niceCeil(v: Double): Double {
        if (v <= 0) return 1.0
        val steps = listOf(1.0, 2.0, 2.5, 5.0, 10.0)
        var mag = 1.0
        while (mag * 10 < v) mag *= 10
        steps.forEach { s -> if (mag * s >= v) return mag * s }
        return mag * 10
    }

    private fun verdictColor(v: Auc24Verdict) = when (v) {
        Auc24Verdict.IN_TARGET    -> SUCCESS
        Auc24Verdict.ABOVE_TARGET -> ERROR
        Auc24Verdict.BELOW_TARGET -> WARNING
        Auc24Verdict.INVALID      -> MUTED
    }

    private fun verdictLabel(v: Auc24Verdict) = when (v) {
        Auc24Verdict.IN_TARGET    -> "In target"
        Auc24Verdict.ABOVE_TARGET -> "Above target"
        Auc24Verdict.BELOW_TARGET -> "Below target"
        Auc24Verdict.INVALID      -> "—"
    }

    private fun shortWorkflow(w: VancoWorkflow) = when (w) {
        VancoWorkflow.PRE      -> "PRE"
        VancoWorkflow.POST     -> "POST"
        VancoWorkflow.PRE_POST -> "PRE + POST"
    }

    internal fun workflowLabel(w: VancoWorkflow) = when (w) {
        VancoWorkflow.PRE      -> "Pre-dose (trough only)"
        VancoWorkflow.POST     -> "Post-dose (peak only)"
        VancoWorkflow.PRE_POST -> "Pre + Post (trough and peak)"
    }
}

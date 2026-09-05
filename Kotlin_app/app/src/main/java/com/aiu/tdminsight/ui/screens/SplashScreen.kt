package com.aiu.tdminsight.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.aiu.tdminsight.data.model.*
import com.aiu.tdminsight.ui.LocalUserPrefs
import com.aiu.tdminsight.ui.components.*
import com.aiu.tdminsight.ui.navigation.Routes
import com.aiu.tdminsight.ui.theme.tdm
import com.aiu.tdminsight.ui.theme.TdmNumericLarge
import com.aiu.tdminsight.ui.theme.TdmNumericMedium
import com.aiu.tdminsight.ui.theme.TdmNumericSmall
import com.aiu.tdminsight.ui.theme.TdmNumericMono
import com.aiu.tdminsight.viewmodel.CaseViewModel
import com.aiu.tdminsight.viewmodel.HistoryViewModel

// ==========================================================================
// SplashScreen — Launch screen shown by MainActivity before the auth gate.
// ==========================================================================

// ════════════════════════════════════════════════════════════════════════════
// 01 · SPLASH
// ════════════════════════════════════════════════════════════════════════════
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1800)
        onFinished()
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Bottom-right decorative circle (ZEN. style abstract shape)
        Box(
            Modifier
                .size(280.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                .align(Alignment.BottomEnd)
                .offset(x = 60.dp, y = 60.dp)
        )
        Box(
            Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                .align(Alignment.BottomEnd)
                .offset(x = 20.dp, y = 20.dp)
        )

        // "TDM" chip top-left (ZEN. "ZEN." label)
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(24.dp, 56.dp)
        ) {
            Text(
                "TDM",
                style = TdmNumericMono.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    letterSpacing = 1.5.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
            )
        }

        // Hero text bottom-left (ZEN. "Find your inner calm.")
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(start = 28.dp, bottom = 100.dp)
        ) {
            Text(
                "START YOUR JOURNEY",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Vancomycin\nTherapeutic\nDrug Monitoring",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 44.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "TDM Insight",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Loading bar at bottom
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .align(Alignment.BottomCenter),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )

        Text(
            "ACADEMIC PROTOTYPE · FICTIONAL DATA",
            style = TdmNumericMono.copy(letterSpacing = 0.8.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 14.dp)
        )
    }
}

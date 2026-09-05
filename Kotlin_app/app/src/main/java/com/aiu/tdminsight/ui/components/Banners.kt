package com.aiu.tdminsight.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiu.tdminsight.ui.theme.tdm

/** Academic prototype notice shown at the top of every input screen. */
@Composable
fun FictionalDataBanner(modifier: Modifier = Modifier) {
    Banner(
        icon        = Icons.Outlined.Info,
        iconTint    = MaterialTheme.colorScheme.onSurfaceVariant,
        title       = "Academic prototype",
        body        = "Fictional data only — not a prescribing decision.",
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor   = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier    = modifier,
    )
}

/** Blocking error — red errorContainer. */
@Composable
fun ErrorBanner(title: String, body: String, modifier: Modifier = Modifier) {
    Banner(
        icon        = Icons.Filled.Error,
        iconTint    = MaterialTheme.colorScheme.error,
        title       = title,
        body        = body,
        containerColor = MaterialTheme.colorScheme.errorContainer,
        contentColor   = MaterialTheme.colorScheme.onErrorContainer,
        modifier    = modifier,
    )
}

/** Non-blocking warning — amber warningContainer. */
@Composable
fun WarningBanner(title: String, body: String, modifier: Modifier = Modifier) {
    val tdm = MaterialTheme.tdm
    Banner(
        icon        = Icons.Filled.Warning,
        iconTint    = tdm.warning,
        title       = title,
        body        = body,
        containerColor = tdm.warningContainer,
        contentColor   = tdm.onWarningContainer,
        modifier    = modifier,
    )
}

/** In-target success — green successContainer. */
@Composable
private fun Banner(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    body: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        contentColor = contentColor,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.padding(14.dp, 14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = containerColor
            ) {
                Icon(icon, contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp))
            }
            Column {
                Text(title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                    color = contentColor)
                Text(body,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.85f),
                    modifier = Modifier.padding(top = 3.dp))
            }
        }
    }
}

/**
 * Step-progress bar — ZEN. onboarding style.
 * Filled segments = completed steps (using primary color).
 */
@Composable
fun StepIndicator(current: Int, total: Int = 5, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        repeat(total) { i ->
            val filled = i < current
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(2.dp),
                    color = if (filled) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxSize()
                ) {}
            }
        }
    }
}

/** Full-width primary pill CTA — clean solid black, Uniwind style. */
@Composable
fun PrimaryPillButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(50),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.onBackground,
            contentColor   = MaterialTheme.colorScheme.background,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            disabledContentColor   = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
        )
    ) {
        Text(
            text,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                letterSpacing = 0.2.sp
            )
        )
    }
}

/** Outlined secondary CTA. */

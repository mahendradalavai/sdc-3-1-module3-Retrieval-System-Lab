package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RedError
import com.example.ui.theme.Slate700

@Composable
fun SimilarityGauge(
    score: Float, // -1f to 1f
    angleDegrees: Float,
    modifier: Modifier = Modifier
) {
    val clampedScore = score.coerceIn(0f, 1f)
    val percentage = (clampedScore * 100).toInt()

    val gaugeColor = when {
        score >= 0.70f -> CyanPrimary
        score >= 0.40f -> GreenSuccess
        score >= 0.20f -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.outline
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "${"%.3f".format(score)}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    color = gaugeColor
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "($percentage%)",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Mini bar
        Box(
            modifier = Modifier
                .width(90.dp)
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Slate700)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(clampedScore)
                    .clip(RoundedCornerShape(3.dp))
                    .background(gaugeColor)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "θ = ${"%.1f".format(angleDegrees)}°",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

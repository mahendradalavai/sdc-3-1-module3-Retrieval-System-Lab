package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ProjectedNode
import com.example.ui.theme.CategoryAiColor
import com.example.ui.theme.CategoryBioColor
import com.example.ui.theme.CategoryCulinaryColor
import com.example.ui.theme.CategoryDefaultColor
import com.example.ui.theme.CategoryEnergyColor
import com.example.ui.theme.CategorySpaceColor
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import kotlin.math.sqrt

fun categoryToColor(category: String): Color {
    return when {
        category.contains("AI", ignoreCase = true) || category.contains("Computing", ignoreCase = true) -> CategoryAiColor
        category.contains("Space", ignoreCase = true) || category.contains("Astronomy", ignoreCase = true) -> CategorySpaceColor
        category.contains("Culinary", ignoreCase = true) || category.contains("Bread", ignoreCase = true) -> CategoryCulinaryColor
        category.contains("Energy", ignoreCase = true) || category.contains("Solar", ignoreCase = true) -> CategoryEnergyColor
        category.contains("Bio", ignoreCase = true) || category.contains("Gene", ignoreCase = true) -> CategoryBioColor
        category.contains("Query", ignoreCase = true) -> CyanPrimary
        else -> CategoryDefaultColor
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VectorSpaceCanvas(
    nodes: List<ProjectedNode>,
    queryText: String,
    modifier: Modifier = Modifier,
    onNodeSelected: ((ProjectedNode) -> Unit)? = null
) {
    var selectedNode by remember { mutableStateOf<ProjectedNode?>(null) }

    // Pulsing animation for the query vector node
    val infiniteTransition = rememberInfiniteTransition(label = "query_pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 16f,
        targetValue = 34f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_radius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Slate900)
            .border(1.dp, Slate700, RoundedCornerShape(16.dp))
            .padding(12.dp)
            .testTag("vector_space_container")
    ) {
        // Legend & Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "2D Semantic Vector Space (PCA)",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = "Closer points = Higher Cosine Similarity",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Category Badges FlowRow
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(
                "Query" to CyanPrimary,
                "AI/ML" to CategoryAiColor,
                "Astronomy" to CategorySpaceColor,
                "Culinary" to CategoryCulinaryColor,
                "Energy" to CategoryEnergyColor,
                "Biomed" to CategoryBioColor
            ).forEach { (label, color) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Canvas Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF070B14))
                .border(1.dp, Slate800, RoundedCornerShape(12.dp))
        ) {
            val queryNode = nodes.find { it.isQuery }
            val docNodes = nodes.filter { !it.isQuery }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("vector_space_canvas")
                    .pointerInput(nodes) {
                        detectTapGestures { offset ->
                            val width = size.width
                            val height = size.height
                            val centerX = width / 2f
                            val centerY = height / 2f
                            val scale = minOf(width, height) * 0.42f

                            var nearest: ProjectedNode? = null
                            var nearestDist = Float.MAX_VALUE

                            for (node in nodes) {
                                val nodeX = centerX + node.x * scale
                                val nodeY = centerY + node.y * scale
                                val dx = offset.x - nodeX
                                val dy = offset.y - nodeY
                                val dist = sqrt(dx * dx + dy * dy)
                                if (dist < 40f && dist < nearestDist) {
                                    nearest = node
                                    nearestDist = dist
                                }
                            }
                            selectedNode = nearest
                            if (nearest != null) {
                                onNodeSelected?.invoke(nearest)
                            }
                        }
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val centerX = canvasWidth / 2f
                val centerY = canvasHeight / 2f
                val scale = minOf(canvasWidth, canvasHeight) * 0.42f

                // Draw Coordinate Axes Grid
                val gridColor = Color(0x22334155)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, centerY),
                    end = Offset(canvasWidth, centerY),
                    strokeWidth = 1.5f
                )
                drawLine(
                    color = gridColor,
                    start = Offset(centerX, 0f),
                    end = Offset(centerX, canvasHeight),
                    strokeWidth = 1.5f
                )

                // Concentric circles representing similarity radii
                drawCircle(
                    color = gridColor,
                    radius = scale * 0.5f,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
                )
                drawCircle(
                    color = gridColor,
                    radius = scale,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
                )

                // If query exists, draw vector rays from query to top-3 matching documents
                if (queryNode != null) {
                    val qOffset = Offset(centerX + queryNode.x * scale, centerY + queryNode.y * scale)
                    val topMatches = docNodes.sortedByDescending { it.similarityToQuery }.take(3)

                    for (match in topMatches) {
                        if (match.similarityToQuery > 0.1f) {
                            val mOffset = Offset(centerX + match.x * scale, centerY + match.y * scale)
                            val alpha = match.similarityToQuery.coerceIn(0.25f, 0.85f)
                            drawLine(
                                color = CyanPrimary.copy(alpha = alpha),
                                start = qOffset,
                                end = mOffset,
                                strokeWidth = 2f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
                            )
                        }
                    }
                }

                // Draw Document Nodes
                for (doc in docNodes) {
                    val nodeOffset = Offset(centerX + doc.x * scale, centerY + doc.y * scale)
                    val color = categoryToColor(doc.category)
                    val isSelected = selectedNode?.id == doc.id
                    val radius = if (isSelected) 10f else 6.5f

                    if (isSelected) {
                        drawCircle(
                            color = color.copy(alpha = 0.35f),
                            radius = radius + 6f,
                            center = nodeOffset
                        )
                    }

                    drawCircle(
                        color = color,
                        radius = radius,
                        center = nodeOffset
                    )

                    // Draw outer border for node
                    drawCircle(
                        color = Color.White.copy(alpha = 0.8f),
                        radius = radius,
                        center = nodeOffset,
                        style = Stroke(width = 1.5f)
                    )
                }

                // Draw Query Node (Glowing pulsing beacon)
                if (queryNode != null) {
                    val qOffset = Offset(centerX + queryNode.x * scale, centerY + queryNode.y * scale)

                    // Pulsing animated wave
                    drawCircle(
                        color = CyanPrimary.copy(alpha = pulseAlpha),
                        radius = pulseRadius,
                        center = qOffset
                    )

                    // Core Query Dot
                    drawCircle(
                        color = CyanPrimary,
                        radius = 9f,
                        center = qOffset
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4f,
                        center = qOffset
                    )
                }
            }

            // Overlay Hint if no query
            if (queryNode == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Text(
                        text = "Enter a search query to plot the query vector & similarity rays",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Slate700,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        // Inspection tooltip card for selected node
        if (selectedNode != null) {
            val node = selectedNode!!
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("selected_node_card"),
                colors = CardDefaults.cardColors(containerColor = Slate800),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(categoryToColor(node.category))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = node.title,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                maxLines = 1
                            )
                            Text(
                                text = node.category,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    if (!node.isQuery && queryText.isNotBlank()) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Cosine Sim",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Text(
                                text = "%.3f".format(node.similarityToQuery),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanPrimary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

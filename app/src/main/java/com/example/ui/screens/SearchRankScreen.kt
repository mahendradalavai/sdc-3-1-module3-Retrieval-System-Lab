package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RetrievalResult
import com.example.engine.DefaultCorpus
import com.example.ui.RetrievalUiState
import com.example.ui.components.CosineFormulaCard
import com.example.ui.components.MathInspectorDialog
import com.example.ui.components.SimilarityGauge
import com.example.ui.components.categoryToColor
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@Composable
fun SearchRankScreen(
    state: RetrievalUiState,
    onQueryChanged: (String) -> Unit,
    onThresholdChanged: (Float) -> Unit,
    onTopKChanged: (Int) -> Unit,
    onSelectForMath: (RetrievalResult) -> Unit,
    onDismissMath: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showFilterControls by remember { mutableStateOf(false) }

    // Filter results according to threshold and topK
    val filteredResults = remember(state.rankedResults, state.threshold, state.topK) {
        val aboveThreshold = state.rankedResults.filter { it.score >= state.threshold }
        if (state.topK > 0) aboveThreshold.take(state.topK) else aboveThreshold
    }

    val topMatch = state.rankedResults.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("search_rank_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))

            // Search Text Field
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_input_field"),
                placeholder = {
                    Text(
                        text = "Enter query (e.g. space telescope, sourdough yeast)...",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    if (state.isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = CyanPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = CyanPrimary
                        )
                    }
                },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(
                            onClick = { onQueryChanged("") },
                            modifier = Modifier.testTag("clear_query_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = Slate700,
                    focusedContainerColor = Slate900,
                    unfocusedContainerColor = Slate900,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                singleLine = true
            )
        }

        // Suggested Queries Horizontal Row
        item {
            Column {
                Text(
                    text = "Benchmark Test Queries",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DefaultCorpus.SUGGESTED_TEST_QUERIES.forEach { querySuggestion ->
                        AssistChip(
                            onClick = { onQueryChanged(querySuggestion) },
                            label = {
                                Text(
                                    text = querySuggestion,
                                    fontSize = 12.sp,
                                    color = if (state.query == querySuggestion) CyanPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (state.query == querySuggestion) Slate800 else Slate900
                            ),
                            border = AssistChipDefaults.assistChipBorder(
                                enabled = true,
                                borderColor = if (state.query == querySuggestion) CyanPrimary else Slate700
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("suggested_query_${querySuggestion.take(8)}")
                        )
                    }
                }
            }
        }

        // Mathematical Formula Card
        item {
            CosineFormulaCard(
                activeBreakdown = topMatch?.similarityBreakdown,
                docTitle = topMatch?.document?.title
            )
        }

        // Search Controls & Stats Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (state.query.isBlank()) "Corpus Ready (${state.corpus.size} docs)" else "${filteredResults.size} Ranked Matches",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    if (state.searchDurationMs > 0 && state.query.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Slate800)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${state.searchDurationMs}ms",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = CyanPrimary
                                )
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showFilterControls = !showFilterControls }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filters",
                        tint = if (showFilterControls) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Threshold & K",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (showFilterControls) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }

        // Expandable Filter Controls
        item {
            AnimatedVisibility(visible = showFilterControls) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("filter_controls_card"),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate700))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        // Top K selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Top-K Results:",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(3, 5, 10, -1).forEach { k ->
                                    val isSelected = state.topK == k
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { onTopKChanged(k) },
                                        label = {
                                            Text(
                                                text = if (k == -1) "All" else "$k",
                                                fontSize = 11.sp
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = CyanPrimary,
                                            selectedLabelColor = Slate900,
                                            containerColor = Slate800,
                                            labelColor = MaterialTheme.colorScheme.onSurface
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = isSelected,
                                            borderColor = if (isSelected) CyanPrimary else Slate700
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Minimum Similarity Threshold
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Min Similarity Threshold:",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = "≥ ${"%.2f".format(state.threshold)}",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary,
                                fontSize = 12.sp
                            )
                        }

                        Slider(
                            value = state.threshold,
                            onValueChange = onThresholdChanged,
                            valueRange = 0.0f..0.8f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                thumbColor = CyanPrimary,
                                activeTrackColor = CyanPrimary,
                                inactiveTrackColor = Slate700
                            ),
                            modifier = Modifier.testTag("threshold_slider")
                        )
                    }
                }
            }
        }

        // Empty state when no query
        if (state.query.isBlank()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Slate900)
                        .border(1.dp, Slate800, RoundedCornerShape(16.dp))
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(CyanPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Test Vector Retrieval",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Type any natural language query or tap one of the suggested benchmark chips above to calculate embeddings and rank matching documents using cosine similarity.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else if (filteredResults.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Slate900)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No documents met threshold (≥ ${"%.2f".format(state.threshold)})",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Lower the threshold slider to view lower similarity results.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate600
                            )
                        )
                    }
                }
            }
        } else {
            // Ranked Results List
            items(filteredResults, key = { it.document.id }) { result ->
                RankedDocumentCard(
                    result = result,
                    onInspectMath = { onSelectForMath(result) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Math Inspector Dialog
    if (state.selectedResultForMath != null) {
        MathInspectorDialog(
            result = state.selectedResultForMath,
            queryText = state.query,
            onDismiss = onDismissMath
        )
    }
}

@Composable
fun RankedDocumentCard(
    result: RetrievalResult,
    onInspectMath: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rank = result.rank
    val isTopRank = rank == 1

    val cardBorderColor = when (rank) {
        1 -> CyanPrimary
        2 -> Color(0xFF38BDF8)
        3 -> Color(0xFF818CF8)
        else -> Slate700
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ranked_doc_card_${result.document.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isTopRank) Slate800 else Slate900
        ),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(cardBorderColor.copy(alpha = if (isTopRank) 0.8f else 0.4f))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Rank badge, Category, and Similarity Gauge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Rank Badge
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (isTopRank) CyanPrimary else Slate700),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#$rank",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isTopRank) Slate900 else MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Category Badge
                    val catColor = categoryToColor(result.document.category)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(catColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = result.document.category,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = catColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                // Similarity Gauge
                SimilarityGauge(
                    score = result.score,
                    angleDegrees = result.similarityBreakdown.angleDegrees
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = result.document.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Content Snippet
            Text(
                text = result.document.content,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                ),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Actions: Math Inspector Trigger
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "q·d = ${"%.3f".format(result.similarityBreakdown.dotProduct)}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = Slate600
                    )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onInspectMath() }
                        .background(Slate800)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("inspect_math_button_${result.document.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Functions,
                        contentDescription = "Inspect",
                        tint = CyanPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Inspect Math",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CyanPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

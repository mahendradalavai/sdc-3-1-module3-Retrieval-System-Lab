package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.RetrievalUiState
import com.example.ui.RetrievalViewModel
import com.example.ui.screens.BenchmarkScreen
import com.example.ui.screens.CorpusScreen
import com.example.ui.screens.SearchRankScreen
import com.example.ui.screens.VectorSpaceScreen
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

sealed class LabNavTab(
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    object Search : LabNavTab("Search & Rank", Icons.Default.Search, "tab_search")
    object VectorSpace : LabNavTab("Vector Space", Icons.Default.Hub, "tab_vector_space")
    object Corpus : LabNavTab("Corpus", Icons.Default.Storage, "tab_corpus")
    object Benchmark : LabNavTab("Benchmark", Icons.Default.Assessment, "tab_benchmark")
}

class MainActivity : ComponentActivity() {

    private val viewModel: RetrievalViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(uiState.statusMessage) {
                    uiState.statusMessage?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
                        viewModel.clearStatusMessage()
                    }
                }

                RetrievalLabApp(
                    state = uiState,
                    snackbarHostState = snackbarHostState,
                    onQueryChanged = viewModel::onQueryChange,
                    onThresholdChanged = viewModel::setThreshold,
                    onTopKChanged = viewModel::setTopK,
                    onSelectForMath = viewModel::selectResultForMath,
                    onDismissMath = viewModel::dismissMathDialog,
                    onSwitchEngine = viewModel::switchEmbeddingType,
                    onAddDocument = viewModel::addCustomDocument,
                    onDeleteDocument = viewModel::deleteDocument,
                    onResetCorpus = viewModel::resetCorpus,
                    onRunBenchmark = viewModel::runBenchmark
                )
            }
        }
    }
}

@Composable
fun RetrievalLabApp(
    state: RetrievalUiState,
    snackbarHostState: SnackbarHostState,
    onQueryChanged: (String) -> Unit,
    onThresholdChanged: (Float) -> Unit,
    onTopKChanged: (Int) -> Unit,
    onSelectForMath: (com.example.data.model.RetrievalResult) -> Unit,
    onDismissMath: () -> Unit,
    onSwitchEngine: (String) -> Unit,
    onAddDocument: (String, String, String) -> Unit,
    onDeleteDocument: (Int) -> Unit,
    onResetCorpus: () -> Unit,
    onRunBenchmark: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        LabNavTab.Search,
        LabNavTab.VectorSpace,
        LabNavTab.Corpus,
        LabNavTab.Benchmark
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("retrieval_lab_root"),
        containerColor = Slate950,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            LabTopBar(
                embeddingType = state.embeddingType,
                isGeminiConfigured = state.isGeminiKeyConfigured,
                onToggleEngine = {
                    val nextType = if (state.embeddingType == "LOCAL") "GEMINI" else "LOCAL"
                    onSwitchEngine(nextType)
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Slate900,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("lab_navigation_bar")
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTabIndex == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTabIndex = index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyanPrimary,
                            selectedTextColor = CyanPrimary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = Slate800
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTabIndex) {
                0 -> SearchRankScreen(
                    state = state,
                    onQueryChanged = onQueryChanged,
                    onThresholdChanged = onThresholdChanged,
                    onTopKChanged = onTopKChanged,
                    onSelectForMath = onSelectForMath,
                    onDismissMath = onDismissMath
                )
                1 -> VectorSpaceScreen(
                    state = state
                )
                2 -> CorpusScreen(
                    state = state,
                    onAddDocument = onAddDocument,
                    onDeleteDocument = onDeleteDocument,
                    onResetCorpus = onResetCorpus
                )
                3 -> BenchmarkScreen(
                    state = state,
                    onRunBenchmark = onRunBenchmark
                )
            }
        }
    }
}

@Composable
fun LabTopBar(
    embeddingType: String,
    isGeminiConfigured: Boolean,
    onToggleEngine: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars),
        color = Slate900,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(CyanPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "RL",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Slate900
                        )
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Retrieval Lab",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "Embeddings & Cosine Ranking",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CyanPrimary,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Engine Mode Switch Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onToggleEngine() }
                    .background(Slate800)
                    .border(1.dp, Slate700, RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .testTag("engine_toggle_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Engine Mode",
                    tint = CyanPrimary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = if (embeddingType == "LOCAL") "Local (64D)" else "Gemini API",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }
    }
}

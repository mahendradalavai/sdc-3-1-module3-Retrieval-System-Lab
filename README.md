# Retrieval Lab 🔍📐

**Retrieval Lab** is an interactive Android application and educational workbench built to explore, visualize, and benchmark how semantic search and information retrieval (IR) systems retrieve and rank relevant documents for natural language queries using vector embeddings and cosine similarity.

---

## 🌟 Highlights & Features

### 1. 🔎 Real-Time Semantic Search & Ranking
- **Instant Query Retrieval**: Type any query or tap quick benchmark chips to retrieve documents ranked by semantic relevance in real time.
- **Top-K & Threshold Filters**: Dynamically adjust minimum cosine similarity cutoffs ($\ge 0.0$ to $0.8$) and limit result lists (Top 3, Top 5, Top 10, or All).
- **Ranked Match Cards**: Displays ranking badges (#1 Top Match, #2, #3), semantic categories, cosine similarity percentage gauge, and angular separation ($\theta$ in degrees).
- **Interactive Formula Inspector**: Breaks down live values for the cosine formula:
  $$\text{Cosine Similarity} = \cos(\theta) = \frac{\mathbf{q} \cdot \mathbf{d}}{\|\mathbf{q}\|_2 \cdot \|\mathbf{d}\|_2}$$
  Inspect dot products, L2 Euclidean vector lengths, and dimension-by-dimension vector projections.

### 2. 🌌 2D Vector Space Visualizer (PCA)
- **Principal Component Analysis (PCA) Canvas**: Projects high-dimensional embedding vectors into an interactive 2D coordinate space.
- **Query Beacon & Proximity Rings**: Features concentric distance rings and pulsating query beacons connecting to top-matching documents via similarity rays.
- **Pairwise Vector Comparator**: Select any two corpus documents to compute their dot product, cosine angle, angular distance, and Euclidean distance side by side.

### 3. 📊 Information Retrieval (IR) Benchmarks
- **Quantitative IR Metrics**: Measures real-world retrieval quality using standard information retrieval metrics:
  - **Hit@1 / Precision@1**: Did the top-ranked result match the ground-truth domain?
  - **Hit@3 / Precision@3**: Was a relevant document retrieved within the top 3 spots?
  - **Mean Reciprocal Rank (MRR)**: Evaluates the average reciprocal rank $\frac{1}{\text{rank}_i}$ across all queries.
- **Multi-Domain Benchmark Set**: Pre-loaded test cases spanning Computing & AI, Astrophysics, Culinary Science, Renewable Energy, and Biomedicine.

### 4. 📚 Document Corpus Management
- **Room Database Persistence**: Indexed documents and their computed vector embeddings are cached and stored locally using Android Room.
- **Vector Inspection**: View document text snippets, category metadata, dimension lengths, and sample vector coordinates.
- **Custom Document Addition**: Index custom user text into the corpus on the fly and re-benchmark retrieval performance.
- **Corpus Reset**: Restore default curated domain documents with a single tap.

### 5. ⚙️ Dual Embedding Engines
- **Built-in Local Embedding Engine**: A deterministic 64-dimensional semantic anchor engine that works entirely offline with zero configuration or API keys required.
- **Google Gemini API Embedding Engine**: Optional integration with Gemini's state-of-the-art `text-embedding-004` (768 dimensions) via REST API.

---

## 🏗️ Architecture & Tech Stack

- **UI Framework**: Modern declarative UI with **Jetpack Compose** and **Material Design 3**.
- **Architecture Pattern**: MVVM (Model-View-ViewModel) utilizing Kotlin Coroutines, `StateFlow`, and unidirectional data flow.
- **Local Persistence**: **Room Database** (v2.6) with KSP and custom TypeConverters for vector storage.
- **Networking**: **Retrofit 2** + **Moshi** + **OkHttp 4** for Google Gemini Embedding API calls.
- **Vector Mathematics (`VectorMath`)**:
  - Dot product calculation: $\sum_{i=1}^n q_i \cdot d_i$
  - L2 Euclidean norm calculation: $\sqrt{\sum_{i=1}^n x_i^2}$
  - Cosine similarity: $\frac{\mathbf{u} \cdot \mathbf{v}}{\|\mathbf{u}\| \|\mathbf{v}\|}$
  - Angular distance: $\arccos(\text{clamp}(\text{similarity}, -1, 1)) \times \frac{180}{\pi}$
  - 2D PCA projection for dimensional reduction and visualization.

---

## 📂 Project Structure

```
app/src/main/java/com/example/
├── MainActivity.kt               # Main entry point with Edge-to-Edge Navigation Suite
├── data/
│   ├── Document.kt               # Document data models and search result entities
│   ├── DocumentDao.kt            # Room DAO for corpus CRUD operations
│   └── DocumentDatabase.kt       # Room Database with FloatArray type converters
├── engine/
│   ├── BenchmarkEvaluator.kt     # Benchmark runner & Hit@K / MRR metrics calculator
│   ├── DefaultCorpus.kt          # Curated multi-domain document corpus
│   ├── GeminiEmbeddingService.kt # Retrofit client for Gemini text-embedding-004
│   ├── LocalEmbeddingEngine.kt   # Offline deterministic 64D semantic vector engine
│   └── VectorMath.kt             # High-performance vector math operations
└── ui/
    ├── RetrievalViewModel.kt     # Central state management for retrieval, search, and benchmarks
    ├── components/               # Custom UI components (Formula cards, gauges, math inspector)
    ├── screens/
    │   ├── SearchRankScreen.kt   # Live query search, threshold controls, and ranked cards
    │   ├── VectorSpaceScreen.kt  # 2D PCA space visualizer & pairwise comparator
    │   ├── CorpusScreen.kt       # Document manager, vector inspector, & custom indexer
    │   └── BenchmarkScreen.kt    # Evaluation suite for Hit@1, Hit@3, and MRR
    └── theme/                    # Material 3 color palettes and typography
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** Ladybug (2024.2.1) or newer
- **JDK 17** or newer
- Android device or emulator running **API 26 (Android 8.0)** or higher

### Building the Project
Clone the repository and build the debug APK using Gradle:

```bash
# Build the application
gradle assembleDebug

# Run unit tests
gradle :app:testDebugUnitTest
```

### (Optional) Enabling Gemini Embeddings
To use Gemini `text-embedding-004` rather than the built-in offline engine:
1. Obtain an API key from [Google AI Studio](https://aistudio.google.com/).
2. Add your key into `.env`:
   ```properties
   GEMINI_API_KEY=your_actual_api_key_here
   ```
3. Rebuild the app. In the app's top bar or settings, toggle the embedding engine from **Local (64D)** to **Gemini API (768D)**.

---

## 📖 License

This project is licensed under the Apache License, Version 2.0.

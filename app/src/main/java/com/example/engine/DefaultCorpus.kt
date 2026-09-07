package com.example.engine

data class CorpusItem(
    val title: String,
    val category: String,
    val content: String
)

data class BenchmarkQuery(
    val queryText: String,
    val targetCategory: String,
    val description: String
)

object DefaultCorpus {

    val DOCUMENTS = listOf(
        CorpusItem(
            title = "Attention Mechanisms & Transformer Neural Networks",
            category = "AI & Computing",
            content = "The Transformer architecture relies entirely on self-attention mechanisms to compute representations of input tokens without recurrent sequence alignments. Multi-head attention allows neural models to jointly attend to information from different representation subspaces at different positions, enabling massive parallel training on modern GPU clusters."
        ),
        CorpusItem(
            title = "Backpropagation and Gradient Descent Optimization",
            category = "AI & Computing",
            content = "Deep learning models adjust internal parameter weights via backpropagation, which computes partial derivatives of the loss function using the calculus chain rule. Optimizers like Adam and SGD with momentum iteratively step down the gradient landscape to minimize prediction error on training datasets."
        ),
        CorpusItem(
            title = "Vector Embeddings and Semantic Latent Spaces",
            category = "AI & Computing",
            content = "Dense vector embeddings represent textual tokens and documents in continuous geometric spaces. By calculating cosine similarity between high-dimensional vector representations, retrieval systems can identify semantically related queries and documents even when they share zero exact vocabulary words."
        ),
        CorpusItem(
            title = "James Webb Space Telescope Infrared Observations",
            category = "Astronomy & Space",
            content = "The James Webb Space Telescope utilizes gold-coated beryllium primary mirrors and cryogenic infrared spectrographs to observe the earliest primordial galaxies formed after the Big Bang. Its high angular resolution penetrates dense cosmic dust clouds to image stellar nurseries and exoplanet atmospheric compositions."
        ),
        CorpusItem(
            title = "Hubble Space Telescope Deep Field Imaging",
            category = "Astronomy & Space",
            content = "Orbiting above Earth's atmospheric distortion, the Hubble Space Telescope captured long-exposure optical and ultraviolet surveys revealing thousands of interacting galaxies across billions of lightyears of cosmic history, measuring cosmic expansion and the Hubble constant."
        ),
        CorpusItem(
            title = "Supermassive Black Holes and Event Horizons",
            category = "Astronomy & Space",
            content = "Gravitational collapse produces spacetime singularities where escape velocity exceeds the speed of light. The Event Horizon Telescope combined radio interferometry arrays across planet Earth to resolve photon rings around the supermassive black hole in galaxy M87 and Sagittarius A*."
        ),
        CorpusItem(
            title = "Sourdough Fermentation & Wild Yeast Ecology",
            category = "Culinary Science",
            content = "Artisan sourdough bread relies on a symbiotic culture of wild yeasts such as Candida humilis and lactic acid bacteria like Fructilactobacillus sanfranciscensis. The bacteria metabolize maltose into lactic and acetic acids, creating distinctive tangy flavor profiles while yeasts produce carbon dioxide gas bubbles to leaven the dough."
        ),
        CorpusItem(
            title = "Gluten Matrix Formation and Hydration Kinetics",
            category = "Culinary Science",
            content = "Wheat flour proteins glutenin and gliadin bond with water molecules during dough hydration, creating an elastic gluten network that traps gas during proofing. Autolyse rest periods and controlled stretch-and-fold kneading techniques optimize crumb structure and oven spring."
        ),
        CorpusItem(
            title = "Maillard Reaction and Crust Caramelization",
            category = "Culinary Science",
            content = "When baking dough at high oven temperatures exceeding 140°C, reducing sugars react with amino acids in the Maillard cascade. This non-enzymatic browning reaction yields complex aromatic pyrazines, furans, and crisp caramelized crust coloration."
        ),
        CorpusItem(
            title = "Silicon Photovoltaic Solar Cell Physics",
            category = "Renewable Energy",
            content = "Photovoltaic solar panels convert incident sunlight photons into direct electric current through the p-n junction photovoltaic effect. Doped crystalline silicon wafers release valence electrons when energized by solar photons, which grid inverters then convert into alternating current power."
        ),
        CorpusItem(
            title = "Aerodynamic Wind Turbine Generator Design",
            category = "Renewable Energy",
            content = "Modern utility-scale wind turbines use composite airfoil blades to generate aerodynamic lift from prevailing wind currents. A direct-drive synchronous generator converts mechanical rotational torque into high-voltage electrical energy for transmission across decarbonized power grids."
        ),
        CorpusItem(
            title = "Lithium-Ion Battery Storage & Grid Balancing",
            category = "Renewable Energy",
            content = "Grid-scale battery energy storage systems (BESS) stabilize intermittent renewable generation from solar and wind farms. Lithium iron phosphate (LFP) chemistry provides high cycle life, thermal stability, and rapid sub-second dispatch to regulate grid frequency."
        ),
        CorpusItem(
            title = "CRISPR-Cas9 Gene Editing Therapeutics",
            category = "Biomedicine & Genetics",
            content = "The bacterial adaptive immune system provides RNA-guided Cas9 endonucleases that introduce targeted double-strand breaks at specific DNA genomic sequences. Cellular repair pathways enable precise gene knockouts and genetic corrections for sickle cell disease."
        ),
        CorpusItem(
            title = "mRNA Vaccine Synthesis and Lipid Nanoparticle Delivery",
            category = "Biomedicine & Genetics",
            content = "Messenger RNA therapeutics encode viral antigen spike proteins that human ribosomes translate to stimulate neutralizing antibody and T-cell immune responses. Ionizable lipid nanoparticles protect the delicate mRNA molecules from enzymatic degradation during cellular uptake."
        )
    )

    val BENCHMARK_QUERIES = listOf(
        BenchmarkQuery(
            queryText = "how do attention layers and transformer models process tokens?",
            targetCategory = "AI & Computing",
            description = "Tests transformer & self-attention neural retrieval"
        ),
        BenchmarkQuery(
            queryText = "space telescope observing distant galaxies with infrared mirrors",
            targetCategory = "Astronomy & Space",
            description = "Tests deep space optics & telescope retrieval"
        ),
        BenchmarkQuery(
            queryText = "wild yeast and lactic acid bacteria fermenting bread dough",
            targetCategory = "Culinary Science",
            description = "Tests microbiology & sourdough bread retrieval"
        ),
        BenchmarkQuery(
            queryText = "converting sunlight to electricity using silicon solar panels",
            targetCategory = "Renewable Energy",
            description = "Tests photovoltaic clean energy retrieval"
        ),
        BenchmarkQuery(
            queryText = "lipid nanoparticles delivering messenger RNA vaccine antigens",
            targetCategory = "Biomedicine & Genetics",
            description = "Tests mRNA therapeutics and immunology retrieval"
        )
    )

    val SUGGESTED_TEST_QUERIES = listOf(
        "neural network gradient descent weights",
        "distant infrared galaxies space telescope",
        "wild yeast fermentation sourdough baking",
        "solar photovoltaic panels clean energy",
        "CRISPR gene editing DNA repair",
        "semantic vector embeddings cosine similarity"
    )
}

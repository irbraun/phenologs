# Computable Descriptions for Identifying Phenologs
The contents of this repository consists of code for evaluating the utility of using automated methods to generate computable representations of biological phenotypes descriptions, in this case in plant species. Computable representations of phenotypes include EQ statements comprised of ontology terms, or numerical vectors generated using natural language processing (NLP) tools such as `doc2vec`.

### Phenotype Description Dataset
The dataset of phenotype descriptions for this work is taken from "An ontology approach to comparative phenomics in plants" (Oellrich, Walls *et al.*, 2015). In that work, each phenotype description was distilled into individual atomized statements relating to individual aspects (phenes) of each phenotype. In addition, the atomized statements were translated into EQ statements composed of ontology terms. 

### EQ Statements
A combination of different methods and approaches were used to generate EQ statements in an automated fashion. Semantic annotation was done using NCBO Annotator (bioportal.bioontology.org/annotator), NOBLE Coder (http://noble-tools.dbmi.pitt.edu/), and a naive-Bayes text classifier. EQ statements were generated from ontology term annotations using a combination of a rule-based approach and analysis of dependency-graph path lengths between terms within predicted statements.

### Document Embeddings
Embeddings of phenotype descriptions were generated using `doc2vec` (Le *et al.*, 2014). Both models pre-trained using the English Wikipedia corpus and models built using domain-specific articles were used. 

### Citations
Publication in preparation.
# Computable descriptions for identifying phenologs

### Project description
Phenologs are defined as similar phenotypes with hypothesized shared genetic basis. Identifying phenotypes mentioned in literature and predicting their similarity to other phenotypes enables candidate gene prediction in order to discover new genotype to phenotype relationships. In order to calculate the similarity between two phenotype descriptions, the descriptions first need to be converted into a computable format. The contents of this repository consists of code for evaluating the utility of using automated methods to generate computable representations of biological phenotypes descriptions, in this case in plant species. Computable representations of phenotypes include EQ statements comprised of ontology terms, or embeddings into numerical vectors.

### Data this work is based on 
The dataset of phenotype descriptions for this work is taken from "[An ontology approach to comparative phenomics in plants][4]" (Oellrich, Walls et al., 2015). In that work, each phenotype description was translated into EQ statement(s) composed of ontology terms by curators.

### Generating computable representations
Both EQ statements and numerical vectors were used to as computable representations for phenotype descriptions to be generated without curators. EQ statement generation was performed using a pipeline that combines semantic annotation tools [NOBLE Coder][2], [NCBO Annotator][1], machine learning methods, and a rule-based approach. Descriptions were additionally embedded into numerical vectors using [Doc2Vec][6] models and bag of words methods.

### Reproducing results
Download and unzip `phenologs_main` available at the data repository [here][7]. This directory contains a jar file with dependencies used by the pipeline, python modules for running the pipeline, semantic annotation tools, the main pipeline script, and R scripts used to analyze the outputs of the pipeline. 


The SLURM script `pipeline.sb` contains commands to generate datasets and run the analysis discussed in the publication. The commands in the script for running the pipeline and subsequent analysis are:
```
# Generate annotation files for phene and phenotype descriptions in the dataset, and 
# build similarity networks between both phene and phenotypes using each applicable
# type of input text description.
python pipeline.py -d phene -c config/config_set_ph_ene/
python pipeline.py -d phenotype -c config_set_ph_full/
python pipeline.py -d split_phenotype -c config_set_ph_split/

# Look at the within-group and between-group average phenotype similarities using
# an existing hierarchical functional categorization with using both phenotype and
# phene descriptions as the data source.
Rscript r/subsets_approach1.R -p1
Rscript r/subsets_approach1.R -p2

# Look at the performance of each type of computationally generated phenotype 
# representation on a functional classification task using either phenotype or
# phene descriptions as the data source.
Rscript r/subsets_approach2.R -p1
Rscript r/subsets_approach2.R -p2

# Compare the network built using curated phenotype representations to the networks
# built computationally using each tool, with phenotype or phene descriptions as the 
# data source.
Rscript r/network_comparison.R -p1
Rscript r/network_comparison.R -p2

```
1. Output files related to semantic annotations with different methods are in `phenologs_main/annotators/`.
2. Output files summarizing the annotation results are in `phenologs_main/output/`.
3. Output network files are in `phenologs_main/networks/`.
4. Output analysis files are in `phenologs_main/r/output/`.

### References
Publication in preparation.

### Feedback
Send any feedback, questions, or suggestions to irbraun at iastate dot edu.





[1]: http://bioportal.bioontology.org/annotator
[2]: http://noble-tools.dbmi.pitt.edu/
[3]: https://github.com/jhlau/doc2vec
[4]: https://plantmethods.biomedcentral.com/articles/10.1186/s13007-015-0053-y
[5]: https://stanfordnlp.github.io/CoreNLP/
[6]: https://cs.stanford.edu/~quocle/paragraph_vector.pdf
[7]: https://doi.org/10.5281/zenodo.3255020

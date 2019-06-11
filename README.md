# Computable descriptions for identifying phenologs

### Project description
Phenologs are defined as similar phenotypes with hypothesized shared genetic basis. Identifying phenotypes mentioned in literature and predicting their similarity to other phenotypes enables candidate gene prediction in order to discover new genotype to phenotype relationships. In order to calculate the similarity between two phenotype descriptions, the descriptions first need to be converted into a computable format. The contents of this repository consists of code for evaluating the utility of using automated methods to generate computable representations of biological phenotypes descriptions, in this case in plant species. Computable representations of phenotypes include EQ statements comprised of ontology terms, or embeddings into numerical vectors.

### Plant PhenomeNET
The dataset of phenotype descriptions for this work is taken from "[An ontology approach to comparative phenomics in plants][4]" (Oellrich, Walls et al., 2015). In that work, each phenotype description was translated into EQ statement(s) composed of ontology terms by curators.

### Generating computable representations
Both EQ statements and numerical vectors were used to as computable representations for phenotype descriptions to be generated without curators. EQ statement generation was performed using a pipeline that combines semantic annotation tools [NOBLE Coder][2], [NCBO Annotator][1], machine learning methods, and a rule-based approach. Descriptions were additionally embedded into numerical vectors using [Doc2Vec][6] models and bag of words methods.

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
library(ggplot2)
library(tidyr)
library(dplyr)
library(data.table)
library(car)
library(data.table)
library(hashmap)

source("/Users/irbraun/NetBeansProjects/term-mapping/r/analysis/utils.R")
source("/Users/irbraun/NetBeansProjects/term-mapping/r/analysis/utils_for_subsets.R")




# Network files.
NETWORKS_DIR <- "/Users/irbraun/Desktop/droplet/path/networks/"
PHENOTYPE_EDGES_FILE <- "phenotype_network_modified.csv"
# Function categorization files.
SUBSETS_DIR <- "/Users/irbraun/Desktop/"
SUBSETS_FILENAME <- "out.csv"
# Define properties of the output files.
OUT_PATH_CURATEDEQ <- "/Users/irbraun/Desktop/summaries_cureq.csv"
OUT_PATH_PREDEQ <- "/Users/irbraun/Desktop/summaries_predeq.csv"
OUT_PATH_DOC2VEC <- "/Users/irbraun/Desktop/summaries_doc2vec.csv"
OUT_FILE_COLUMNS <- c("group","class","subset","num_in","num_out","mean_within","mean_between","p_value","greater","significant")


# Read in the phenotype and phene network files output from the pipeline.
phenotype_network <- read(NETWORKS_DIR,PHENOTYPE_EDGES_FILE)

# Read in the categorization file for functional subsets of Arabidopsis genes.
subsets_df <- read(SUBSETS_DIR, SUBSETS_FILENAME)
subset_name_list <- unique(subsets_df$subset)


# Define mappings between subsets and groups and classes.
mappings_df <- subsets_df %>% distinct(subset, .keep_all=TRUE)
subset2group <- hashmap(mappings_df$subset, mappings_df$group)
subset2class <- hashmap(mappings_df$subset, mappings_df$class)




# Produce table iterating through each loci, using curated EQ statemnts.
phenotype_network$value_to_use <- phenotype_network$c_edge
table <- get_empty_table(OUT_FILE_COLUMNS)
for (subset in subset_name_list){
  results <- mean_similarity_within_and_between(phenotype_network, subsets_df, subset)
  table[nrow(table)+1,] <- c(subset2group[[subset]], subset2class[[subset]], subset, results)
}
write.csv(table, file=OUT_PATH_CURATEDEQ, row.names=F)




# Produce table iterating through each loci, using predicted EQ statements.
# Note: Edges with a value of -1 indicate that no edge was calculated due to missing data.
# These are replaced here with 0 to indicate minimal semantic similarity.
phenotype_network$value_to_use <- phenotype_network$p_edge
phenotype_network$value_to_use <- pmax(phenotype_network$value_to_use, 0.000)
table <- get_empty_table(OUT_FILE_COLUMNS)
for (subset in subset_name_list){
  results <- mean_similarity_within_and_between(phenotype_network, subsets_df, subset)
  table[nrow(table)+1,] <- c(subset2group[[subset]], subset2class[[subset]], subset, results)
}
write.csv(table, file=OUT_PATH_PREDEQ, row.names=F)




# Produce table iterating through each loci, using document embeddings from doc2vec.
phenotype_network$value_to_use <- range01((1/phenotype_network$enwiki_dbow))
table <- get_empty_table(OUT_FILE_COLUMNS)
for (subset in subset_name_list){
  results <- mean_similarity_within_and_between(phenotype_network, subsets_df, subset)
  table[nrow(table)+1,] <- c(subset2group[[subset]], subset2class[[subset]], subset, results)
}
write.csv(table, file=OUT_PATH_DOC2VEC, row.names=F)








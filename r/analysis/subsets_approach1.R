library(ggplot2)
library(tidyr)
library(dplyr)
library(data.table)
library(car)
library(bootstrap)
library(DAAG)
library(kSamples)
library(data.table)

source("/Users/irbraun/NetBeansProjects/term-mapping/r/analysis/utils.R")
source("/Users/irbraun/NetBeansProjects/term-mapping/r/analysis/subset_functions.R")




# Read in the phenotype and phene network files output from the pipeline.
dir <- "/Users/irbraun/Desktop/droplet/path/networks/"
phenotype_edges_file <- "phenotype_network_modified.csv"
phene_edges_file <- "phene_network_modified.csv"
phenotype_network <- read(dir,phenotype_edges_file)
phene_network <- read(dir,phene_edges_file)



# Read in the categorization file for functional subsets of Arabidopsis genes.
dir <- "/Users/irbraun/Desktop/"
subsets_filename <- "out_phenotype.csv"
subsets_df <- read(dir, subsets_filename)
subset_name_list <- unique(subsets_df$subset)



# Define the output filenames.
output_path_curated_eq <- "/Users/irbraun/Desktop/summaries_cureq.csv"
output_path_predicted_eq <- "/Users/irbraun/Desktop/summaries_predeq.csv"
output_path_doc2vec <- "/Users/irbraun/Desktop/summaries_doc2vec.csv"
output_file_columns <- c("subset","num_in","num_out","mean_within","mean_between","p_value","greater","significant")





# Produce table iterating through each loci, using curated EQ statemnts.
phenotype_network$value_to_use <- phenotype_network$c_edge
table <- get_empty_table(output_file_columns)
for (subset in subset_name_list){
  results <- mean_similarity_within_and_between(phenotype_network, subsets_df, subset)
  table[nrow(table)+1,] <- c(subset, results)
}
write.csv(table, file=output_path_curated_eq, row.names=F)



# Produce table iterating through each loci, using predicted EQ statements.
# Note: Edges with a value of -1 indicate that no edge was calculated due to missing data.
# These are replaced here with 0 to indicate minimal semantic similarity.
phenotype_network$value_to_use <- phenotype_network$p_edge
phenotype_network$value_to_use <- pmax(phenotype_network$value_to_use, 0.000)
table <- get_empty_table(output_file_columns)
for (subset in subset_name_list){
  results <- mean_similarity_within_and_between(phenotype_network, subsets_df, subset)
  table[nrow(table)+1,] <- c(subset, results)
}
write.csv(table, file=output_path_predicted_eq, row.names=F)




# Produce table iterating through each loci, using document embeddings from doc2vec.
phenotype_network$value_to_use <- (1/phenotype_network$enwiki_dbow)
table <- get_empty_table(output_file_columns)
for (subset in subset_name_list){
  results <- mean_similarity_within_and_between(phenotype_network, subsets_df, subset)
  table[nrow(table)+1,] <- c(subset, results)
}
write.csv(table, file=output_path_doc2vec, row.names=F)








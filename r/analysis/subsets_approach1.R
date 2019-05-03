library(ggplot2)
library(tidyr)
library(dplyr)
library(data.table)
library(car)
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
OUT_PATH_SUMMARY <- "/Users/irbraun/Desktop/subsets_approach1_summary.csv"
OUT_PATH_CURATEDEQ <- "/Users/irbraun/Desktop/subsets_approach1_cureq.csv"
OUT_PATH_PREDEQ <- "/Users/irbraun/Desktop/subsets_approach1_predeq.csv"
OUT_PATH_DOC2VEC <- "/Users/irbraun/Desktop/summary_table_doc2vec.csv"
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







# Will use df$value_to_use to evaluate similarities.
summarize_method <- function(df, output_path){
  table <- get_empty_table(OUT_FILE_COLUMNS)
  for (subset in subset_name_list){
    results <- mean_similarity_within_and_between(phenotype_network, subsets_df, subset)
    table[nrow(table)+1,] <- c(subset2group[[subset]], subset2class[[subset]], subset, results)
  }
  write.csv(table, file=output_path, row.names=F)
  return(table)
}










# Produce table iterating through each loci, using curated EQ statemnts.
phenotype_network$value_to_use <- phenotype_network$c_edge
table1 <- summarize_method(phenotype_network, OUT_PATH_CURATEDEQ)



# Produce table iterating through each loci, using predicted EQ statements.
# Note: Edges with a value of -1 indicate that no edge was calculated due to missing data.
# These are replaced here with 0 to indicate minimal semantic similarity.
phenotype_network$value_to_use <- phenotype_network$p_edge
phenotype_network$value_to_use <- pmax(phenotype_network$value_to_use, 0.000)
table2 <- summarize_method(phenotype_network, OUT_PATH_PREDEQ)


# Produce table iterating through each loci, using document embeddings from doc2vec.
phenotype_network$value_to_use <- range01((1/phenotype_network$enwiki_dbow))
table3 <- summarize_method(phenotype_network, OUT_PATH_DOC2VEC)







# Update suffixes in the output tables so the merged table makes sense.
cols_to_update <- c("num_in","num_out","mean_within","mean_between","p_value","greater","significant")
new_names <- sapply(cols_to_update, paste, "cureq", sep=".")
setnames(table1, old=cols_to_update, new=new_names)
new_names <- sapply(cols_to_update, paste, "predeq", sep=".")
setnames(table2, old=cols_to_update, new=new_names)
new_names <- sapply(cols_to_update, paste, "doc2vec", sep=".")
setnames(table3, old=cols_to_update, new=new_names)


# Include the full names of each group and class.
names_df <-read("/Users/irbraun/NetBeansProjects/term-mapping/data/original_datasets/","subset_names_cleaned.csv")
group_names_map <- hashmap(unique(names_df$Group.Symbol), unique(names_df$Group.Name))
class_names_map <- hashmap(unique(names_df$Class.Symbol), unique(names_df$Class.Name))
f_group <- function(x){return(group_names_map[[x]])}
f_class <- function(x){return(class_names_map[[x]])}


# Generate a table that summarizes the overall results.
summary <- Reduce(function(x,y) merge(x = x, y = y, by=c("group","class","subset")), list(table1,table2,table3))
summary$group <- f_group(summary$group)
summary$class <- f_class(summary$class)
write.csv(summary, file=OUT_PATH_SUMMARY, row.names=F)














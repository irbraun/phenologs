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



# Define which similarity metric from the network edge file to use.
phenotype_network$value_to_use <- (1/phenotype_network$enwiki_dbow)



# Get the phenotype ID's that refer to phenotypes for genes in Arabidopsis.
phenotype_ids <- unique(subsets_df$chunk)
# Reduce the number of phenotypes for testing purposes.
phenotype_ids <- phenotype_ids[1:20]
phenotype_network_table <- data.table(phenotype_network)




# Get a dataframe of the similarity predictions dropping out one phenotype at a time. This is the performance bottleneck.
all_predictions_df <- get_empty_table(c("phenotype_id",unique(subsets_df$subset)))
for (p in phenotype_ids){
  # Apply the similarity to cluster function to each subset for this phenotype.
  scores_for_each_subset <- sapply(subset_name_list, get_similarity_to_cluster, network_df=phenotype_network_table, subsets_df=subsets_df, phenotype_id=p)
  row_values <- c(p, scores_for_each_subset)
  all_predictions_df[nrow(all_predictions_df)+1,] <- row_values
}


# Get a dataframe of the existing categorizations that are the targets for prediction.
all_targets_df <- get_empty_table(c("phenotype_id",unique(subsets_df$subset)))
for (p in phenotype_ids){
  subset_membership <- subsets_df[subsets_df$chunk == p,]$subset
  membership_for_each_subset <- sapply(subset_name_list, membership_func, subset_membership=subset_membership)
  row_values <- c(p, membership_for_each_subset)
  all_targets_df[nrow(all_targets_df)+1,] <- row_values
}





# Loop through the the phenotypes, dropping them out and treating remainder as training data.
final_predictions_table <- get_empty_table(c("phenotype_id",unique(subsets_df$subset)))
final_thresholds_table <- get_empty_table(c("phenotype_id","threshold","f1"))
for (p in phenotype_ids){
  
  #Get portions of the prediction and target dataframes that don't have this phenotype in them.
  pred_dropped <- all_predictions_df[all_predictions_df$phenotype_id != p,]
  truth_dropped <- all_targets_df[all_targets_df$phenotype_id != p,]
  
  # Estimate the best threshold from the training data for this phenotype.
  best_threshold = 1.000
  best_f1 = 0.000
  thresholds_to_test = seq(0,1,0.01)
  for (t in thresholds_to_test){
    
    # What are the binary predictions at this threshold?
    binary_pred <- sapply(pred_dropped[,2:ncol(pred_dropped)], get_binary_decision, threshold=t)
    binary_pred <- data.frame(binary_pred)
    binary_pred_matrix <- data.matrix(binary_pred)
    
    # What are the true categories for these genes?
    binary_target_matrix <- data.matrix(truth_dropped[,2:ncol(truth_dropped)])
    
    # Get the resulting F-score and update.
    f1 <- get_f_score(binary_pred_matrix, binary_target_matrix)
    best_f1 <- ifelse(f1>=best_f1, f1, best_f1)
    best_threshold <- ifelse(f1>=best_f1, t, best_threshold)
  }
  
  # Remember the threshold that was learned when dropping this phenotype, and best F-score obtained.
  final_thresholds_table[nrow(final_thresholds_table)+1,] <- c(p,best_threshold,best_f1)
  
  # Using this learned threshold, make binary predictions for this particular phenotype alone.
  pred_alone <- all_predictions_df[all_predictions_df$phenotype_id == p,]
  binary_pred <- sapply(pred_alone[,2:ncol(pred_alone)], get_binary_decision, threshold=t)
  #binary_pred <- data.frame(binary_pred)
  binary_pred_matrix <- data.matrix(binary_pred)
  row_values <- c(p, binary_pred)
  final_predictions_table[nrow(final_predictions_table)+1,] <- row_values
}

# Results of estimating the threshold for each phenotype and predicting subset membership.
final_thresholds_table
final_predictions_table 

# What is the performance when combining each membership prediction?
final_binary_target_matrix <- data.matrix(all_targets_df[,2:ncol(all_targets_df)])
final_binary_pred_matrix <- data.matrix(final_predictions_table[,2:ncol(final_predictions_table)])
final_f1 <- get_f_score(final_binary_pred_matrix, final_binary_target_matrix)





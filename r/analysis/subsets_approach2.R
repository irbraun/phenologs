library(ggplot2)
library(tidyr)
library(dplyr)
library(data.table)
library(car)
library(parallel)




source("/work/dillpicl/irbraun/term-mapping/path/r/utils.R")
source("/work/dillpicl/irbraun/term-mapping/path/r/utils_for_subsets.R")


# Specify column in the network file to be used as predicted values. Has to match the csv file.
PRED_COLUMN_NAMES <- c("cur_m1_edge", "cur_m2_edge", "pre_m1_edge", "pre_m2_edge","jaccard", "cosine")

# Network files.
NETWORKS_DIR <- "/work/dillpicl/irbraun/term-mapping/path/networks/"
PHENOTYPE_EDGES_FILE <- "phenotype_network_modified_NEW.csv"
# Function categorization files.
SUBSETS_DIR <- "/work/dillpicl/irbraun/term-mapping/path/r/"
SUBSETS_FILENAME <- "classifications.csv"
# Output files.
OUTPUT_DIR <- "/work/dillpicl/irbraun/term-mapping/path/r/output/"

# Get the number of cores available for parallelization.
numCores <- detectCores()
cat(paste(numCores,"cores available"))







run_one_method_class_level <- function(pred_column_name, phenotype_network, subsets_df, class_name_list){
  
  out_thresholds_file <- paste(OUTPUT_DIR,"class",pred_column_name,"_thresholds.csv",sep="")
  out_predictions_file <- paste(OUTPUT_DIR,"class",pred_column_name,"_predictions.csv",sep="")
  out_text_file <- paste(OUTPUT_DIR,"class",pred_column_name,"_f1.csv",sep="")
  
  phenotype_network$value_to_use <- phenotype_network[,pred_column_name]
  
  phenotype_ids <- unique(subsets_df$chunk)
  phenotype_network_table <- data.table(phenotype_network)
  
  all_predictions_df <- get_empty_table(c("phenotype_id",unique(subsets_df$class)))
  for (p in phenotype_ids){
    scores_for_each_class <- sapply(class_name_list, get_similarity_to_class, network_df=phenotype_network_table, subsets_df=subsets_df, phenotype_id=p)
    row_values <- c(p, scores_for_each_class)
    all_predictions_df[nrow(all_predictions_df)+1,] <- row_values
  }
  
  all_targets_df <- get_empty_table(c("phenotype_id",unique(subsets_df$class)))
  for (p in phenotype_ids){
    class_membership <- subsets_df[subsets_df$chunk == p,]$class
    membership_for_each_class <- sapply(class_name_list, membership_func, subset_membership=class_membership)
    row_values <- c(p, membership_for_each_class)
    all_targets_df[nrow(all_targets_df)+1,] <- row_values
  }
  
  final_predictions_table <- get_empty_table(c("phenotype_id",unique(subsets_df$class)))
  final_thresholds_table <- get_empty_table(c("phenotype_id","threshold","f1"))
  for (p in phenotype_ids){
    
    pred_dropped <- all_predictions_df[all_predictions_df$phenotype_id != p,]
    truth_dropped <- all_targets_df[all_targets_df$phenotype_id != p,]
    best_threshold = 1.000
    best_f1 = 0.000
    thresholds_to_test = seq(0,1,0.01)
    for (t in thresholds_to_test){
      
      binary_pred <- sapply(pred_dropped[,2:ncol(pred_dropped)], get_binary_decision, threshold=t)
      binary_pred <- data.frame(binary_pred)
      binary_pred_matrix <- data.matrix(binary_pred)
      binary_target_matrix <- data.matrix(truth_dropped[,2:ncol(truth_dropped)])
      f1 <- get_f_score(binary_pred_matrix, binary_target_matrix)
      best_f1 <- ifelse(f1>=best_f1, f1, best_f1)
      best_threshold <- ifelse(f1>=best_f1, t, best_threshold)
    }
    final_thresholds_table[nrow(final_thresholds_table)+1,] <- c(p,best_threshold,best_f1)
    
    pred_alone <- all_predictions_df[all_predictions_df$phenotype_id == p,]
    binary_pred <- sapply(pred_alone[,2:ncol(pred_alone)], get_binary_decision, threshold=best_threshold)
    binary_pred_matrix <- data.matrix(binary_pred)
    row_values <- c(p, binary_pred)
    final_predictions_table[nrow(final_predictions_table)+1,] <- row_values
  }
  
  final_binary_target_matrix <- data.matrix(all_targets_df[,2:ncol(all_targets_df)])
  final_binary_pred_matrix <- data.matrix(final_predictions_table[,2:ncol(final_predictions_table)])
  final_f1 <- get_f_score(final_binary_pred_matrix, final_binary_target_matrix)
  write.csv(final_thresholds_table, file=out_thresholds_file, row.names=F)
  write.csv(final_predictions_table, file=out_predictions_file, row.names=F)
  sink(out_text_file)
  cat(final_f1)
  closeAllConnections()
}







run_one_method_subset_level <- function(pred_column_name, phenotype_network, subsets_df, subset_name_list){
  
  # Generate method specific output files.
  out_thresholds_file <- paste(OUTPUT_DIR,"subset",pred_column_name,"_thresholds.csv",sep="")
  out_predictions_file <- paste(OUTPUT_DIR,"subset",pred_column_name,"_predictions.csv",sep="")
  out_text_file <- paste(OUTPUT_DIR,"subset",pred_column_name,"_f1.csv",sep="")
  
  
  # Define which similarity metric from the network edge file to use.
  phenotype_network$value_to_use <- phenotype_network[,pred_column_name]
  
  # Get the phenotype ID's that refer to phenotypes for genes in Arabidopsis.
  phenotype_ids <- unique(subsets_df$chunk)
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
    binary_pred <- sapply(pred_alone[,2:ncol(pred_alone)], get_binary_decision, threshold=best_threshold)
    binary_pred_matrix <- data.matrix(binary_pred)
    row_values <- c(p, binary_pred)
    final_predictions_table[nrow(final_predictions_table)+1,] <- row_values
  }
  
  # What is the performance when combining each membership prediction?
  final_binary_target_matrix <- data.matrix(all_targets_df[,2:ncol(all_targets_df)])
  final_binary_pred_matrix <- data.matrix(final_predictions_table[,2:ncol(final_predictions_table)])
  final_f1 <- get_f_score(final_binary_pred_matrix, final_binary_target_matrix)
  
  # Results of estimating the threshold for each phenotype and predicting subset membership.
  write.csv(final_thresholds_table, file=out_thresholds_file, row.names=F)
  write.csv(final_predictions_table, file=out_predictions_file, row.names=F)
  sink(out_text_file)
  cat(final_f1)
  closeAllConnections()
}








# Read in the phenotype and phene network files output from the pipeline.
phenotype_network <- read(NETWORKS_DIR,PHENOTYPE_EDGES_FILE)

# Read in the categorization file for functional subsets of Arabidopsis genes.
subsets_df <- read(SUBSETS_DIR, SUBSETS_FILENAME)
subset_name_list <- unique(subsets_df$subset)
class_name_list <- unique(subsets_df$class)


# Check the performance of each method on the classification task in parallel.
mclapply(PRED_COLUMN_NAMES, run_one_method_subset_level, phenotype_network=phenotype_network, subsets_df=subsets_df, subset_name_list=subset_name_list, mc.cores=numCores)
mclapply(PRED_COLUMN_NAMES, run_one_method_class_level, phenotype_network=phenotype_network, subsets_df=subsets_df, class_name_list=class_name_list, mc.cores=numCores)






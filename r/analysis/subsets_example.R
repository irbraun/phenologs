library(ggplot2)
library(tidyr)
library(dplyr)
library(data.table)
library(car)
library(bootstrap)
library(DAAG)
library(kSamples)
library(data.table)


read <- function(dir,filename){
  d <- read.csv(file=paste(dir,filename,sep=""), header=T, sep=",", stringsAsFactors=FALSE)
  return(d)
}



mean_similarity_within_and_between <- function(network_df, subsets_df, subset_name){
  
  # Identify the chunk IDs which are within or outside this subset category.
  # Note that 'within' means the gene was mapped in this subset, 'without' means the gene was never mapped to this subset.
  # Genes can belong to more than one subset, which is why the removal of the intersection is included here.
  inside_df <- subsets_df[subsets_df$subset %in% c(subset_name),]
  outside_df <- subsets_df[!(subsets_df$subset %in% c(subset_name)),]
  inside_chunk_ids <- inside_df$chunk
  outside_chunk_ids <- outside_df$chunk
  intersecting_chunk_ids<- intersect(inside_chunk_ids,outside_chunk_ids)
  outside_chunk_ids <- setdiff(outside_chunk_ids, intersecting_chunk_ids)
  
  # Calculating average similarity within the subset.
  relevant_slice <- network_df[network_df$phenotype_1 %in% inside_chunk_ids & network_df$phenotype_2 %in% inside_chunk_ids,]
  dist_within <- relevant_slice$value_to_use
  average_within_similarity <- mean(relevant_slice$value_to_use)
  
  # Calculating average similarity between this and other subsets.
  relevant_slice <- network_df[(network_df$phenotype_1 %in% inside_chunk_ids & network_df$phenotype_2 %in% outside_chunk_ids) | (network_df$phenotype_1 %in% outside_chunk_ids & network_df$phenotype_2 %in% inside_chunk_ids),]
  dist_between <- relevant_slice$value_to_use
  average_between_similarity <- mean(relevant_slice$value_to_use)
  
  # What were the sample sizes of chunks considered within and outside this subset?
  n_in <- length(inside_chunk_ids)
  n_out <- length(outside_chunk_ids)
  
  # Kolmogorov–Smirnov test, TODO figure out whether this is appropriate or how else these distributions should be treated/compared. 
  p_value <- ks.test(dist_within,dist_between)$p.value

  # Values to make the output easier to understand in a table row.
  greater_similarity_grouping <- ifelse(average_within_similarity > average_between_similarity, "within", "between")
  significance_threshold <- 0.05
  significant <- ifelse(p_value <= significance_threshold, "yes", "no")
  
  # Things to return.
  result <- c(n_in, n_out, round(average_within_similarity,3), round(average_between_similarity,3), round(p_value,3), greater_similarity_grouping, significant)
  return(result)
}


# Setup for the tables that are output for the loci subset analysis (approach 1).
get_new_table <- function(){
  # Setup for the tables to output the results of this loci subset analysis. 
  cols <- c("subset","num_in","num_out","mean_within","mean_between","p_value","greater","significant")
  table <- data.frame(matrix(ncol=length(cols), nrow=0))
  colnames(table) <- cols
  return(table)
}

# Setup for the tables that are output for the loci subset analysis (approach 2).
get_new_subsets_table <- function(){
  cols <- c("phenotype_id",unique(categories$subset))
  table <- data.frame(matrix(ncol=length(cols), nrow=0))
  colnames(table) <- cols
  return(table)
}


membership_func <- function(subset,subset_membership){
  return (ifelse(subset %in% subset_membership, 1, 0))
}


# In
# subset-->p_number mapping df
# pairwise similarity df
# phenotype that's dropped out
# subset (name of subset/cluster)
# Out
# Sim(this phenotype, that cluster)
get_similarity_to_cluster <- function(network_df, subsets_df, subset, phenotype_id){
  
  # Get the list of phenotype IDs that are in this subset/cluster.
  phenotype_ids_in_subset <- subsets_df[subsets_df$subset %in% c(subset),]$chunk
  
  # Get the network edges connecting this phenotype's node to any node in the subset/cluster.
  slice <- network_df[(network_df$phenotype_1 == phenotype_id & network_df$phenotype_2 %in% phenotype_ids_in_subset) | (network_df$phenotype_2 == phenotype_id & network_df$phenotype_1 %in% phenotype_ids_in_subset),]
  
  # Measure similarity between the phenotype and the cluster. Could do mean to all nodes or maximum to any node?
  similarity <- mean(slice$value_to_use)
  return(similarity)
}


# function to get binary decisions from the pred df given a threshold.
get_binary_decisions <- function(value,threshold){
  output <- ifelse(value>=threshold,1,0)
  return(output)
}

# function to get F-score given binary decision matrix.
get_f_score <- function(pred_matrix, target_matrix){
  tp <- length(which(pred_matrix==1 & target_matrix==1))
  fp <- length(which(pred_matrix==1 & target_matrix==0))
  fn <- length(which(pred_matrix==0 & target_matrix==1))
  tn <- length(which(pred_matrix==0 & target_matrix==0))
  prec <- tp/(tp+fp)
  rec <- tp/(tp+fn)
  f1 <- (2*prec*rec)/(prec+rec)
  return(f1)
}




######################################################### Approach 1



# Read in the categorization file for subsets of loci in Arabidopsis.
categories <- read("/Users/irbraun/Desktop/","out_phenotype.csv")
subsets <- unique(categories$subset)


# Read in the phenotype and phene network files output from the pipeline.
dir <- "/Users/irbraun/Desktop/droplet/path/networks/"
phenotype_edges_file <- "phenotype_network_modified.csv"
phene_edges_file <- "phene_network_modified.csv"
phenotype_network <- read(dir,phenotype_edges_file)
phene_network <- read(dir,phene_edges_file)




# Produce table iterating through each loci, using curated EQ statemnts.
output_path_eqp = "/Users/irbraun/Desktop/summaries_cureq.csv"
phenotype_network$value_to_use <- phenotype_network$c_edge
table <- get_new_table()
for (subset in subsets){
  results <- mean_similarity_within_and_between(phenotype_network, categories, subset)
  table[nrow(table)+1,] <- c(subset, results)
}
write.csv(table, file=output_path_eqp, row.names=F)


# Produce table iterating through each loci, using predicted EQ statements.
output_path_eqp = "/Users/irbraun/Desktop/summaries_predeq.csv"
phenotype_network$value_to_use <- phenotype_network$p_edge

# Edges with a value of -1 indicate that no edge was calculated due to missing data.
# These should be replaced with 0 to indicate minimal semantic similarity.
phenotype_network$value_to_use <- pmax(phenotype_network$value_to_use, 0.000)

table <- get_new_table()
for (subset in subsets){
  results <- mean_similarity_within_and_between(phenotype_network, categories, subset)
  table[nrow(table)+1,] <- c(subset, results)
}
write.csv(table, file=output_path_eqp, row.names=F)

# Produce table iterating through each loci, using doc2vec.
output_path_eqp = "/Users/irbraun/Desktop/summaries_doc2vec.csv"
phenotype_network$value_to_use <- (1/phenotype_network$enwiki_dbow)
table <- get_new_table()
for (subset in subsets){
  results <- mean_similarity_within_and_between(phenotype_network, categories, subset)
  table[nrow(table)+1,] <- c(subset, results)
}
write.csv(table, file=output_path_eqp, row.names=F)







######################################################### Approach 2





phenotype_network$value_to_use <- phenotype_network$enwiki_dbow

# Get the phenotype ID's that refer to phenotypes for genes in Arabidopsis.
phenotype_ids <- unique(categories$chunk)

# for testing
phenotype_ids <- phenotype_ids[1:5]
phenotype_network_table <- data.table(phenotype_network)




# Get a dataframe of the similarity predictions dropping out one phenotype at a time.
table <- get_new_subsets_table()
for (p in phenotype_ids){
  # Apply the similarity to cluster function to each subset for this phenotype.
  scores_for_each_subset <- sapply(subsets, get_similarity_to_cluster, network_df=phenotype_network_table, subsets_df=categories, phenotype_id=p)
  row_values <- c(p, scores_for_each_subset)
  table[nrow(table)+1,] <- row_values
}
pred <- table



# Get a dataframe of the existing categorizations that are the targets for prediction.
table <- get_new_subsets_table()
for (p in phenotype_ids){
  subset_membership <- categories[categories$chunk == p,]$subset
  membership_for_each_subset <- sapply(subsets, membership_func, subset_membership=subset_membership)
  row_values <- c(p, membership_for_each_subset)
  table[nrow(table)+1,] <- row_values
}
truth <- table




# Loop through the the phenotypes, dropping them out and treating remainder as training data.
table <- get_new_subsets_table()
for (p in phenotype_ids){
  
  #Get version of the pred and target matrices that don't have this phenotype in them.
  
  
  
  # Estimate the best threshold from the training data.
  best_threshold = 1.000
  best_f1 = 0.000
  thresholds_to_test = seq(0.5,1.0,0.1)
  for (t in thresholds_to_test){
    
    # What are the binary predictions at this threshold?
    binary_pred <- sapply(pred[,2:ncol(pred)], get_binary_decisions, threshold=t)
    binary_pred <- data.frame(binary_pred)
    binary_pred_matrix <- data.matrix(binary_pred)
    
    # What are the true categories for these genes?
    binary_target_matrix <- data.matrix(truth[,2:ncol(truth)])
    
    # Get the resulting F-score and update.
    f1 <- get_f_score(binary_pred_matrix, binary_target_matrix)
    best_f1 <- ifelse(f1>=best_f1, f1, best_f1)
    best_threshold <- ifelse(f1>=best_f1, threshold, best_threshold)
  }
  
  
  
  
}






# Get a matrix from the target data.
m1 <- data.matrix(truth[,2:ncol(truth)])

# Get a matrix from the predicted data.
binary_pred <- sapply(pred[,2:ncol(pred)], get_binary_decisions, threshold=0.5)
binary_pred <- data.frame(binary_pred)
m2 <- data.matrix(binary_pred)









# For each phenotype, estimate the optimal threshold from the training portion of the data.























library(ggplot2)
library(tidyr)
library(dplyr)
library(data.table)
library(car)
library(bootstrap)
library(DAAG)
library(kSamples)


read <- function(dir,filename){
  d <- read.csv(file=paste(dir,filename,sep=""), header=T, sep=",")
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



get_new_table <- function(){
  # Setup for the tables to output the results of this loci subset analysis. 
  cols <- c("subset","num_in","num_out","mean_within","mean_between","p_value","greater","significant")
  table <- data.frame(matrix(ncol=length(cols), nrow=0))
  colnames(table) <- cols
  return(table)
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

# generate a table that looks like
# rows = phenotype/gene ID
# columns = subset names
# internal cells = the predicted scores

# generate a second table that looks like 
# rows = phenotype/gene ID
# columns = subset names
# internal cells = 0 if not made, 1 is that's what Menke said.


d <- phenotype_network
pk <- 1
scores = c()
for (subset in subsets){
  
  dropped_network <- phenotype_network[!(phenotype_network$phenotype_1 %in% c(subset)) & !(phenotype_network$phenotype_2 %in% c(subset)),]
  score <- get_similarity_to_cluster(dropped_network, categories, subset)
  
  
  
  
  
  
  
  
  # Need a score specific to placing this pk within this subset.
  # Drop the lines related to pk from 
  score <- get_similarity_to_cluster()
  # add a row that has {subset name, score}
  
  # What are the true subsets that this pk belongs in?
  # look in the subsets df
}


# So that predicted table is made by dropping out one pk at a time.
# Now estimate the threshold that would be found by dropping out that same on pk at a time.
for (t in thresholds){
  # calculate F1 given those matrices minus pk.
  # use where F1 maxed to threshold the predictions for pk.
  # (but take atleast one) if that results in no predictions being made.
}


# or instead of doing that, just generate precision recall graphs for each of the methods. wont work because 
for (t in thresholds){
  get precision and recall values for these points.
  
  )
}
















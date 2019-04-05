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
  dist_within <- relevant_slice$p_edge
  average_within_similarity <- mean(relevant_slice$p_edge)
  
  # Calculating average similarity between this and other subsets.
  relevant_slice <- network_df[(network_df$phenotype_1 %in% inside_chunk_ids & network_df$phenotype_2 %in% outside_chunk_ids) | (network_df$phenotype_1 %in% outside_chunk_ids & network_df$phenotype_2 %in% inside_chunk_ids),]
  dist_between <- relevant_slice$p_edge
  average_between_similarity <- mean(relevant_slice$p_edge)
  
  # What were the sample sizes of chunks considered within and outside this subset?
  n_in <- length(inside_chunk_ids)
  n_out <- length(outside_chunk_ids)
  
  # Kolmogorov–Smirnov test, TODO figure out whether this is appropriate or how else these distributions should be treated/compared. 
  p_value <- ks.test(dist_within,dist_between)$p.value

  result <- c(n_in, n_out, average_within_similarity, average_between_similarity, p_value)
  return(result)
}





# Read in the categorization file for subsets of loci in Arabidopsis.
categories <- read("/Users/irbraun/Desktop/","out.csv")
subsets <- unique(categories$subset)


# Read in the phenotype and phene network files output from the pipeline.
dir <- "/Users/irbraun/Desktop/droplet/path/networks/"
phenotype_edges_file <- "phenotype_network.csv"
phene_edges_file <- "phene_network.csv"
phenotype_network <- read(dir,phenotype_edges_file)
phene_network <- read(dir,phene_edges_file)


# Setup for the tables to output the results of this pathway example.
cols <- c("subset","n1","n2","mean1","mean2","p")
table <- data.frame(matrix(ncol=length(cols), nrow=0))
colnames(table) <- cols
output_path_eqp = "/Users/irbraun/Desktop/summaries.csv"
# Produce the table, iterating through each loci subset.
for (subset in subsets){
  results <- mean_similarity_within_and_between(phenotype_network, categories, subset)
  table[nrow(table)+1,] <- c(subset, results)
}
write.csv(table, file=output_path_eqp, row.names=F)











df <- read("/Users/irbraun/Desktop/","out.csv")
subset <- "HRM"
inside_df <- df[df$subset %in% c(subset),]
outside_df <- df[!(df$subset %in% c(subset)),]
inside_chunk_ids <- inside_df$chunk
outside_chunk_ids <- inside_df$chunk










# Returns the similarity value between two phenotypes.
# df: phenotype network file
# p1: phenotype ID1
# p2: phenotype ID2
get_phenotype_similarity <- function(df,p1,p2){
  relevant_ids <- c(p1,p2)
  x <- df[df$phenotype_1 %in% relevant_ids & df$phenotype_2 %in% relevant_ids,]
  return(x[1,]$value_used)
}

# Returns maximum similarity between any of p1's phenes and p2's phenes.
# df: a phene network file
# p1: phenotype ID1
# p2: phenotype ID2
get_max_phene_similarity <- function(df,p1,p2){
  relevant_ids <- c(p1,p2)
  x <- df[df$phenotype_1 %in% relevant_ids & df$phenotype_2 %in% relevant_ids,]
  return(max(x$value_used))
}

# Returns the number of genes that have greater similarity to p1.
# df: either a phenotype or phene network.
# p1: phenotype ID1
# sim: similarity value between p1 and something else.
get_gene_ranking <- function(df,p1,sim){
  relevant_ids <- c(p1)
  relevant_slice <- df[df$phenotype_1 %in% relevant_ids | df$phenotype_2 %in% relevant_ids,]
  x <- relevant_slice[relevant_slice$value_used>sim,]
  ranked_ahead <- length(unique(c(x$phenotype_1,x$phenotype_2)))
  return(ranked_ahead+1)
}

# Returns the number of nodes which are visited before this similarity is used.
# df: either a phenotype or phene network.
# p1: phenotype ID1
# sim: similarity value between p1 and something else.
get_node_ranking <- function(df,p1,sim){
  relevant_ids <- c(p1)
  relevant_slice <- df[df$phenotype_1 %in% relevant_ids | df$phenotype_2 %in% relevant_ids,]
  ranked_ahead <- nrow(relevant_slice[relevant_slice$value_used>sim,])
  return(ranked_ahead+1)
}





# Read in the phenotype and phene network files output from the pipeline.
dir <- "/Users/irbraun/Desktop/droplet/path/networks/"
phenotype_edges_file <- "phenotype_network.csv"
phene_edges_file <- "phene_network.csv"
phenotype_network <- read(dir,phenotype_edges_file)
phene_network <- read(dir,phene_edges_file)


# Notes about the c2 pathway example being used here.
# c2: phenotype 1262, one phene is 2544
# c1: phenotype 1261, one phene is 2543   
# r1: phenotype 2599, one phene is 2545       
# b1: phenotype 1584, one phene is 3304     not picking aleurone layer


# Setup for the tables to output the results of this pathway example.
table <- data.frame(matrix(ncol=8, nrow=0))
cols <- c("query","gene","ph_sim","ph_noderank","ph_generank","p_sim","p_noderank","p_generank")
colnames(table) <- cols
output_path_eqp = "/Users/irbraun/Desktop/table1.csv"
output_path_d2v = "/Users/irbraun/Desktop/table2.csv"
query_id <- 1262
other_ids <- c(1262,1261,2599,1584)



# Using the similarities obtained by running the eqpipe.
phenotype_network$value_used <- phenotype_network$p_edge
phene_network$value_used <- phene_network$p_edge
for (id in other_ids){
  # Using the phenotype network
  sim <- get_phenotype_similarity(phenotype_network,query_id,id)
  gene_rank <- get_gene_ranking(phenotype_network,query_id,sim)
  node_rank <- get_node_ranking(phenotype_network,query_id,sim)
  phenotype_network_results <- c(sim,gene_rank,node_rank)
  # Using the phene network
  sim <- get_max_phene_similarity(phene_network,query_id,id)
  gene_rank <- get_gene_ranking(phene_network,query_id,sim)
  node_rank <- get_node_ranking(phene_network,query_id,sim)
  phene_network_results <- c(sim,gene_rank,node_rank)
  # Report the output of searching the network to table row.
  table[nrow(table)+1,] <- c(query_id,id,phenotype_network_results,phene_network_results)
}
write.csv(table, file=output_path_eqp, row.names=F)



# Using the similarities obtained through sentence embedding.
phenotype_network$value_used <- (1.00/phenotype_network$enwiki_dbow)
phene_network$value_used <- (1.00/phene_network$enwiki_dbow)
for (id in other_ids){
  # Using the phenotype network
  sim <- get_phenotype_similarity(phenotype_network,query_id,id)
  gene_rank <- get_gene_ranking(phenotype_network,query_id,sim)
  node_rank <- get_node_ranking(phenotype_network,query_id,sim)
  phenotype_network_results <- c(sim,gene_rank,node_rank)
  # Using the phene network
  sim <- get_max_phene_similarity(phene_network,query_id,id)
  gene_rank <- get_gene_ranking(phene_network,query_id,sim)
  node_rank <- get_node_ranking(phene_network,query_id,sim)
  phene_network_results <- c(sim,gene_rank,node_rank)
  # Report the output of searching the network to table row.
  table[nrow(table)+1,] <- c(query_id,id,phenotype_network_results,phene_network_results)
}
write.csv(table, file=output_path_d2v, row.names=F)
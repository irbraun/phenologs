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
# b1: phenotype 1584, one phene is 3304     


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








# # Using the phenotype network.
# # Ranks here mean 'how many phenotypes of other genes are more similar to the phenotype of c2 than this gene are?'
# c1_sim <- get_phenotype_similarity(phenotype_network,1262,1261)
# c1_rank <- get_gene_ranking(phenotype_network,1262,c1_sim)
# r1_sim <- get_phenotype_similarity(phenotype_network,1262,2599)
# r1_rank <- get_gene_ranking(phenotype_network,1262,r1_sim)
# b1_sim <- get_phenotype_similarity(phenotype_network,1262,1584)
# b1_rank <- get_gene_ranking(phenotype_network,1262,b1_sim)
# n_phenotypes <- length(unique(c(phenotype_network$phenotype_1,phenotype_network$phenotype_2)))
# 
# # Using the phene network
# # Node ranks here mean 'how many phenes outside of c2 are more similar to any phene in c2 than any phene from this gene are?'
# # Gene ranks here mean 'if I use the phenes of c2 as a query in the phene network, how many genes do I see before this one?'
# c1_sim <- get_max_phene_similarity(phene_network,1262,1261)
# c1_node_rank <- get_node_ranking(phene_network,1262,c1_sim)
# c1_gene_rank <- get_gene_ranking(phene_network,1262,c1_sim)
# r1_sim <- get_max_phene_similarity(phene_network,1262,2599)
# r1_node_rank <- get_node_ranking(phene_network,1262,r1_sim)
# r1_gene_rank <- get_gene_ranking(phene_network,1262,r1_sim)
# b1_sim <- get_max_phene_similarity(phene_network,1262,1584)
# b1_node_rank <- get_node_ranking(phene_network,1262,b1_sim)
# b1_gene_rank <- get_gene_ranking(phene_network,1262,b1_sim)
# n_phenes <- length(unique(c(phene_network$phene_1,phene_network$phene_2)))


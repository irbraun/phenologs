library(ggplot2)
library(tidyr)
library(dplyr)
library(data.table)
library(car)
library(bootstrap)
library(DAAG)
library(kSamples)
library(parallel)



source("/Users/irbraun/NetBeansProjects/term-mapping/r/analysis/utils.R")




# Network files.
NETWORKS_DIR <- "/Users/irbraun/Desktop/droplet/path/networks/"
PHENOTYPE_EDGES_FILE <- "phene_text_phene_network.csv"
# Define properties of the output files.
OUTPUT_DIR <- "/Users/irbraun/Desktop/temp/"
OUT_FILE_COLUMNS <- c("method","rank","sim","rank","sim","rank","sim","rank","sim")

# The column names in the network file for each predictive method.
#PRED_COLUMN_NAMES <- c("predefined", "cur_m1_edge", "cur_m2_edge", "pre_m1_edge", "pre_m2_edge", "enwiki_dbow", "jaccard", "cosine")
PRED_COLUMN_NAMES <- c("cur_m1_edge", "cur_m2_edge", "pre_m1_edge", "pre_m2_edge", "enwiki_dbow", "jaccard", "cosine")

# Get the number of cores available for parallelization.
numCores <- detectCores()
cat(paste(numCores,"cores available"))








read <- function(dir,filename){
  d <- read.csv(file=paste(dir,filename,sep=""), header=T, sep=",")
  return(d)
}

# Returns the similarity value between two phenotypes.
# df: phenotype network file
# p1: phenotype ID1
# p2: phenotype ID2
get_phenotype_similarity <- function(df,p1,p2){
  if(p1==p2){
    return(1.00)
  }
  relevant_ids <- c(p1,p2)
  x <- df[df$phenotype_1 %in% relevant_ids & df$phenotype_2 %in% relevant_ids,]
  return(x[1,]$value_used)
}

# Returns maximum similarity between any of p1's phenes and p2's phenes.
# df: a phene network file
# p1: phenotype ID1
# p2: phenotype ID2
get_max_phene_similarity <- function(df,p1,p2){
  if(p1==p2){
    return(1.00)
  }
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
phenotype_network <- read(NETWORKS_DIR,PHENOTYPE_EDGES_FILE)

# Notes about the c2 pathway example being used here.
# c2: phenotype 1262, one phene is 2544
# c1: phenotype 1261, one phene is 2543   
# r1: phenotype 2599, one phene is 2545       
# b1: phenotype 1584, one phene is 3304




# IDs for the phenotypes and corresponding genes in the pathway.
# Ordering of the other IDs is: c2(identity), c1, r1, b1.
query_id <- 1262
other_ids <- c(1262,1261,2599,1584)




# Method to produce the df for a single method with network.
get_df_for_one_method <- function(column_to_use, df, query_id, other_ids, phenotype_nodes){
  df$value_used <- df[,column_to_use]
  table <- get_empty_table(OUT_FILE_COLUMNS)
  results <- c()
  for (id in other_ids){
    # Using the phenotype network
    sim <- ifelse(phenotype_nodes==T, get_phenotype_similarity(df,query_id,id), get_max_phene_similarity(df,query_id,id))
    gene_rank <- get_gene_ranking(df,query_id,sim)
    results <- c(results,gene_rank,sim)
  }
  table[nrow(table)+1,] <- c(column_to_use,results)
  return(table)
}



dfs <- mclapply(PRED_COLUMN_NAMES, get_df_for_one_method, df=phenotype_network, query_id=query_id, other_ids=other_ids, phenotype_nodes==FALSE, mc.cores=numCores)
combined_df <- do.call("rbind", dfs)
write.csv(combined_df, file=paste(OUTPUT_DIR,"table11111.csv",sep=""), row.names=F)









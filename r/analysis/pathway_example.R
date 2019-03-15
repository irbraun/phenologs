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

# Finds the row where similarity between two phenotype is specified and returns it.
get_phenotype_similarity <- function(df,p1,p2){
  relevant_ids <- c(p1,p2)
  x <- df[df$phenotype_1 %in% relevant_ids & df$phenotype_2 %in% relevant_ids,]
  return(x[1,]$c_edge)
}

# Figures out how many phenotypes or phenes (depending on df) are observed before this phenotype (or its best phene) when querying with c2. 
get_node_ranking <- function(df,p1,sim){
  relevant_ids <- c(p1)
  relevant_slice <- df[df$phenotype_1 %in% relevant_ids | df$phenotype_2 %in% relevant_ids,]
  ranked_ahead <- nrow(relevant_slice[relevant_slice$c_edge>sim,])
  return(ranked_ahead+1)
}


# Figures out how many genes are observed before this gene when querying with c2. 
get_gene_ranking <- function(df,p1,sim){
  relevant_ids <- c(p1)
  relevant_slice <- df[df$phenotype_1 %in% relevant_ids | df$phenotype_2 %in% relevant_ids,]
  x <- relevant_slice[relevant_slice$c_edge>sim,]
  ranked_ahead <- length(unique(c(x$phenotype_1,x$phenotype_2)))
  return(ranked_ahead+1)
}
                             
# Notes aobut the get_node_ranking function.
# Determines how many other found similarities to this phenotype are greater than this similarity.
# Note that this refers to number of similarities to the phenotype, even when using phene network.
# ________________________
# Case 1 (phenotypes
# The relevant slice for phenotype network is each row where any phenotype is compared to phenotype p1.
# Case 2 (phenes)
# The relevant slice for phene network is each row where any phene from any phenotype is compared to any phene from phenotype p1.
# In this case, the similarity used is already the top edge between any phenes involved in two phenotypes interest.
# Therefore, anything ranked ahead is similarity between some phene from another phenotype and any phene from phenotype p1.
# Should actually then take unique() from the genes that are ranked ahead, because some phene from another phenotype could show up >1 time?
# (This would occur if that phene matched multiple phenes from p1 at a higher similarity than the passed in value)





# Finds the row where similarity between two phenotypes is maximized and returns it.
# Needs the max because two phenotypes can be compared multiple times when using the phene network.
get_max_phene_similarity <- function(df,p1,p2){
  relevant_ids <- c(p1,p2)
  x <- df[df$phenotype_1 %in% relevant_ids & df$phenotype_2 %in% relevant_ids,]
  return(max(x$c_edge))
}



dir <- "/Users/irbraun/Desktop/droplet/path/networks/"
phenotype_edges_file <- "phenotype_network_modified.csv"
phene_edges_file <- "phene_network.csv"

phenotype_network <- read(dir,phenotype_edges_file)
phene_network <- read(dir,phene_edges_file)


# Notes about the c2 pathway example being used here.
# c2 = phene 2544, phenotype 1262
# c1 = phene 2543, phenotype 1261   
# r1 = phene 2545, phenotype 2599     
# b1 = phene 3304, phenotype 1584     

# Using the phenotype network.
# Ranks here mean 'how many phenotypes of other genes are more similar to the phenotype of c2 than this gene are?'
c1_sim <- get_phenotype_similarity(phenotype_network,1262,1261)
c1_rank <- get_ranking(phenotype_network,1262,c1_sim)
r1_sim <- get_phenotype_similarity(phenotype_network,1262,2599)
r1_rank <- get_ranking(phenotype_network,1262,r1_sim)
b1_sim <- get_phenotype_similarity(phenotype_network,1262,1584)
b1_rank <- get_ranking(phenotype_network,1262,b1_sim)
n_phenotypes <- length(unique(c(phenotype_network$phenotype_1,phenotype_network$phenotype_2)))

# Using the phene network
# Node ranks here mean 'how many phenes outside of c2 are more similar to any phene in c2 than any phene from this gene are?'
# Gene ranks here mean 'if I use the phenes of c2 as a query in the phene network, how many genes do I see before this one?'
c1_sim <- get_max_phene_similarity(phene_network,1262,1261)
c1_node_rank <- get_node_ranking(phene_network,1262,c1_sim)
c1_gene_rank <- get_gene_ranking(phene_network,1262,c1_sim)
r1_sim <- get_max_phene_similarity(phene_network,1262,2599)
r1_node_rank <- get_node_ranking(phene_network,1262,r1_sim)
r1_gene_rank <- get_gene_ranking(phene_network,1262,r1_sim)
b1_sim <- get_max_phene_similarity(phene_network,1262,1584)
b1_node_rank <- get_node_ranking(phene_network,1262,b1_sim)
b1_gene_rank <- get_gene_ranking(phene_network,1262,b1_sim)
n_phenes <- length(unique(c(phene_network$phene_1,phene_network$phene_2)))


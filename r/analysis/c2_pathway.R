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

get_phenotype_similarity <- function(df,p1,p2){
  relevant_ids <- c(p1,p2)
  x <- df[df$phenotype_1 %in% relevant_ids & df$phenotype_2 %in% relevant_ids,]
  return(x[1,]$c_edge)
}

get_ranking <- function(df,p1,sim){
  relevant_ids <- c(p1)
  x <- df[df$phenotype_1 %in% relevant_ids | df$phenotype_2 %in% relevant_ids,]
  ranked_ahead <- nrow(x[x$c_edge>sim,])
  return(ranked_ahead+1)
}

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


# notes for the example
#c2 = phene 2544, phenotype 1262
#c1 = phene 2543, phenotype 1261   
#r1 = phene 2545, phenotype 2599     
#b1 = phene 3304, phenotype 1584     

# using the phenotype network.
c1_sim <- get_phenotype_similarity(phenotype_network,1262,1261)
c1_rank <- get_ranking(phenotype_network,1262,c1_sim)
r1_sim <- get_phenotype_similarity(phenotype_network,1262,2599)
r1_rank <- get_ranking(phenotype_network,1262,r1_sim)
b1_sim <- get_phenotype_similarity(phenotype_network,1262,1584)
b1_rank <- get_ranking(phenotype_network,1262,b1_sim)
n <- length(unique(c(phenotype_network$phenotype_1,phenotype_network$phenotype_2)))

# using the phene network
c1_sim <- get_max_phene_similarity(phene_network,1262,1261)
c1_rank <- get_ranking(phene_network,1262,c1_sim)
r1_sim <- get_max_phene_similarity(phene_network,1262,2599)
r1_rank <- get_ranking(phene_network,1262,r1_sim)
b1_sim <- get_max_phene_similarity(phene_network,1262,1584)
b1_rank <- get_ranking(phene_network,1262,b1_sim)
n <- length(unique(c(phene_network$phene_1,phene_network$phene_2)))







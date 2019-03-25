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


# Produces a file of results for evaluating one network against another at a range of thresholds.
# Arguments
# d: the dataframe describing the network edges
# output_path: path to the output file
# retain_edge_quantity: if true, don't apply each threshold to second network, just take top k edges where the threshold left k edges in the first network.
# doc2vec: use the sentence embedding generated network as the second network.
check_edges <- function(d, output_path, retain_edge_quantity=F, doc2vec=F){
  
  edges <- data.frame(matrix(ncol = 8, nrow = 0))
  cols <- c("threshold","n_cur","n_pre","overlap","jaccard","precision","recall","f1")
  colnames(edges) <- cols
  
  thresholds <- seq(1,0.5,-0.05)
  for (t in thresholds){
    sub_cur <- d[d$c_edge >= t,]$id
  
    # What happens if using any cutoff that assumes same number of edges?
    # The doc2vec edges are distances rather than similarities, so 'order(-x)' rather than 'order(x)'. 
    if(retain_edge_quantity==T){
      num_to_retain = length(sub_cur)
      if(doc2vec==T) sub_pre <- d[order(d$d2v),][1:num_to_retain,]$id
      else sub_pre <- d[order(-d$p_edge),][1:num_to_retain,]$id
    } 
    # What happens if we use that same threshold regradless of how many edges that retains?
    # The doc2vec edges are distances rather than similarities, so <= t instead of >= t.
    else{
      if(doc2vec==T) sub_pre <- d[d$d2v <= t,]$id
      else sub_pre <- d[d$p_edge >= t,]$id
    }
    
    i <- length(intersect(sub_cur,sub_pre))
    u <- length(union(sub_cur,sub_pre))
    jac <- i/u
    num_cur <- length(sub_cur)
    num_pred <- length(sub_pre)
    
    tp <- i
    fp <- num_pred-i
    fn <- num_cur-i
    precision <- tp/(tp+fp)
    recall <- tp/(tp+fn)
    f1 <- (2*precision*recall)/(precision+recall)
    
    jac <- round(jac,3)
    precision <- round(precision,3)
    recall <- round(recall,3)
    f1 <- round(f1,3)
    
    edges[nrow(edges)+1,] <- c(t, num_cur, num_pred, i, jac, precision, recall, f1)
  }
  write.csv(edges, file=output_path, row.names=F)
  
}





# The file specifying the network edges.
dir <- "/Users/irbraun/Desktop/droplet/path/networks/"
file <- "output_for_split_phenotypes/phenotype_network_modified.csv"

# Read it in and assign unique IDs to each row.
d <- read(dir,file)
d$id <- row.names(d)

# Which doc2vec model should be used?
colnames(d)[colnames(d)=="enwiki_dbow"] <- "d2v"

# Use different predicted sets of edges and check performance.
check_edges(d, "/Users/irbraun/Desktop/droplet/path/networks/results_regular_edges.csv", retain_edge_quantity=F, doc2vec=F)
check_edges(d, "/Users/irbraun/Desktop/droplet/path/networks/results_matched_edges.csv", retain_edge_quantity=T, doc2vec=F)
check_edges(d, "/Users/irbraun/Desktop/droplet/path/networks/results_d2v_edges.csv", retain_edge_quantity=T, doc2vec=T)






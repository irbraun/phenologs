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



compare_edges <- function(targets, predictions, k){
  
  # Determine which edges are retained when this value of k is used.
  kept_predictions <- predictions[1:k]
  kept_targets <- targets[1:k]
  
  # Note that under this special case precision is recall is F1-score.
  tp <- length(intersect(kept_targets, kept_predictions))
  f1 <- tp/k
  return(f1)
}



compare_edges_with_subsampling <- function(targets, predictions, k, subsampled_id_sets){

  # Determine which edges are retained when this value of k is used.
  kept_predictions <- predictions[1:k]
  kept_targets <- targets[1:k]
  
  # Note that under this special case precision is recall is F1-score.
  tp <- length(intersect(kept_targets, kept_predictions))
  f1 <- tp/k
  
  # With subsampling
  min_f1 <- 1
  max_f1 <- 0
  for (subsampled_id_set in subsampled_id_sets){
    kept_predictions <- predictions[predictions %in% subsampled_id_set][1:k]
    kept_targets <- targets[targets %in% subsampled_id_set][1:k]
    tp <- length(intersect(kept_targets, kept_predictions))
    subsampled_f1 <- tp/k
    min_f1 <- min(subsampled_f1, min_f1)
    max_f1 <- max(subsampled_f1, max_f1)
  }
  return(c(f1,min_f1,max_f1))
}








# The file specifying the network edges.
dir <- "/Users/irbraun/Desktop/droplet/path/networks/"
file <- "phenotype_network_modified.csv"

# Read it in and assign unique IDs to each row.
d <- read(dir,file)
d$id <- row.names(d)



# Define sets of nodes which are subsamples of the entire dataset.
# Define which pair id's are contained within each subsampling.
nodes <- unique(c(d$phenotype_1, d$phenotype_2))
sample_size <- floor(length(nodes)*0.9)
node_sample_sets <- list()
relevant_id_sets <- list()
num_iter <- 10
for(i in seq(1, num_iter)){
  node_sample_sets[[i]] <- sample(nodes, sample_size, replace=F)
  relevant_id_sets[[i]] <- d[d$phenotype_1 %in% node_sample_sets[[i]] & d$phenotype_2 %in% node_sample_sets[[i]],]$id
}



# Define the range of k values to use.
k_start <- 1000 
k_step <- 10000
k_max <- 100000
k_values <- seq(k_start, k_max, k_step)





# Define which columns refer to predictions and targets.
d$target <- d$c_edge
d$predicted <- 1/d$enwiki_dbow

# Sort them in descending order.
ordered_target_ids <- d[order(-d$target),]$id
ordered_predicted_ids <- d[order(-d$predicted),]$id

# Populate the lists of F-scores varying k with or without subsampling.
doc2vec <- sapply(k_values, compare_edges_with_subsampling, targets=ordered_target_ids, predictions=ordered_predicted_ids, subsampled_id_sets=relevant_id_sets)
doc2vec <- sapply(k_values, compare_edges, targets=ordered_target_ids, predictions=ordered_predicted_ids)





# Define which columns refer to predictions and targets.
d$target <- d$c_edge
d$predicted <- d$p_edge

# Sort them in descending order.
ordered_target_ids <- d[order(-d$target),]$id
ordered_predicted_ids <- d[order(-d$predicted),]$id

# Populate the lists of F-scores varying k with or without subsampling.
predeq <- sapply(k_values, compare_edges_with_subsampling, targets=ordered_target_ids, predictions=ordered_predicted_ids, subsampled_id_sets=relevant_id_sets)
predeq <- sapply(k_values, compare_edges, targets=ordered_target_ids, predictions=ordered_predicted_ids)






# Populate a dataframe from the results with or without subsampling.
df <- data.frame(k=k_values, d2v_all=doc2vec[1,], d2v_min=doc2vec[2,], d2v_max=doc2vec[3,], peq_all=predeq[1,], peq_min=predeq[2,], peq_max=predeq[3,])
df <- data.frame(k=k_values, d2v_all=doc2vec, peq_all=predeq)






# converting the dataframe to long format to have multiple methods.
df_long <- gather(df, method, value, d2v_all, peq_all, factor_key=T)
df_long$max <- ifelse(df_long$method=="d2v_all", df_long$d2v_max, df_long$peq_max)
df_long$min <- ifelse(df_long$method=="d2v_all", df_long$d2v_min, df_long$peq_min)
df_long <- df_long[,c("k","method","value","min","max")]


# Make the plot out of obtained dataframe.
ggplot(data=df_long, aes(x=k, y=value, linetype=method)) + geom_line() +
  coord_cartesian(xlim=c(100000,0),ylim = c(0,0.6)) +
  theme_bw() +
  theme(plot.title = element_text(lineheight=1.0, face="bold", hjust=0.5), panel.grid.major = element_blank(), panel.grid.minor = element_blank(), panel.border = element_blank(), axis.line = element_line(colour = "black")) +
  ylab("F1") +
  xlab("Number of Edges") +
  scale_x_continuous(expand=c(0,0)) +
  scale_y_continuous(expand=c(0,0)) +
  geom_ribbon(aes(ymin=df_long$d2v_min, ymax=df_long$d2v_max, alpha=0.3)) +
  geom_ribbon(aes(ymin=df_long$peq_min, ymax=df_long$peq_max, alpha=0.3))
  
  















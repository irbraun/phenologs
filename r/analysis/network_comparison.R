library(ggplot2)
library(tidyr)
library(dplyr)
library(data.table)
library(car)



source("/Users/irbraun/NetBeansProjects/term-mapping/r/analysis/utils.R")
source("/Users/irbraun/NetBeansProjects/term-mapping/r/analysis/utils_for_networks.R")



# The file specifying the network edges.
DIR <- "/Users/irbraun/Desktop/droplet/path/networks/"
IN_FILE <- "phenotype_network_modified.csv"
# Other constants.
SAMPLING_RATIO <- 0.9
OUT_PATH <- "/Users/irbraun/Desktop/f1_comparison.csv"



# Read it in and assign unique IDs to each row.
d <- read(DIR,IN_FILE)
d$id <- row.names(d)



# Define sets of nodes which are subsamples of the entire dataset.
# Define which pair id's are contained within each subsampling.
nodes <- unique(c(d$phenotype_1, d$phenotype_2))
sample_size <- floor(length(nodes)*SAMPLING_RATIO)
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
k_max <- 4000000
total_number_of_edges <- nrow(d)
k_max <- min(k_max,total_number_of_edges)
k_values <- seq(k_start, k_max, k_step)








get_df_for_method_with_subsampling <- function(d){
  
  # Sort them in descending order.
  ordered_target_ids <- d[order(-d$target),]$id
  ordered_predicted_ids <- d[order(-d$predicted),]$id
  
  # Populate the lists of F-scores varying k with subsampling.
  method_specific_df <- sapply(k_values, compare_edges_with_subsampling, targets=ordered_target_ids, predictions=ordered_predicted_ids, subsampled_id_sets=relevant_id_sets)
  return(method_specific_df)
  
}


get_df_for_method_without_subsampling <- function(d){
  
  # Sort them in descending order.
  ordered_target_ids <- d[order(-d$target),]$id
  ordered_predicted_ids <- d[order(-d$predicted),]$id
  
  # Populate the lists of F-scores varying k without subsampling.
  method_specific_df <- sapply(k_values, compare_edges, targets=ordered_target_ids, predictions=ordered_predicted_ids)
  return(method_specific_df)
}









# ---- doc2vec ----

# Redefine which columns refer to predictions and targets.
d$target <- d$c_edge
d$predicted <- range01(1/d$enwiki_dbow)
# Generate a dataframe of results for this method.
doc2vec <- get_df_for_method_with_subsampling(d)



# ---- predicted EQs ----


# Redefine which columns refer to predictions and targets.
d$target <- d$c_edge
d$predicted <- 1/d$enwiki_dbow
# Generate a dataframe of results for this method
predeq <- get_df_for_method_with_subsampling(d)






# Populate a dataframe from the results with subsampling.
df <- data.frame(k=k_values, d2v_all=doc2vec[1,], d2v_min=doc2vec[2,], d2v_max=doc2vec[3,], peq_all=predeq[1,], peq_min=predeq[2,], peq_max=predeq[3,])
# This is how it would be done if sub-sampling was not done.
#df <- data.frame(k=k_values, d2v_all=doc2vec, peq_all=predeq)



# Save this dataframe as a csv file, Read in back in to make plots locally.
write.csv(df, file=OUT_PATH, row.names=F)
df <- read("",OUT_PATH)





# Converting the dataframe to long format to have multiple methods on the same plot.
df_long <- gather(df, method, value, d2v_all, peq_all, factor_key=T)
df_long$max <- ifelse(df_long$method=="d2v_all", df_long$d2v_max, df_long$peq_max)
df_long$min <- ifelse(df_long$method=="d2v_all", df_long$d2v_min, df_long$peq_min)
df_long <- df_long[,c("k","method","value","min","max")]


# Make the plot out of obtained dataframe.
ggplot(data=df_long, aes(x=k, y=value, linetype=method)) + geom_line() +
  scale_linetype_manual("Method", values=c("dashed","solid"), breaks=c("d2v_all","peq_all"), labels=c("NLP","Ontologies")) +
  coord_cartesian(xlim=c(100000,0),ylim = c(0,0.6)) +
  theme_bw() +
  theme(plot.title = element_text(lineheight=1.0, face="bold", hjust=0.5), panel.grid.major = element_blank(), panel.grid.minor = element_blank(), panel.border = element_blank(), axis.line = element_line(colour = "black")) +
  ylab("F1") +
  xlab("Number of Edges") +
  scale_x_continuous(expand=c(0,0)) +
  scale_y_continuous(expand=c(0,0)) +
  # Adding ribbons to show how robust the results are to changes in input phenotypes in this dataset.
  geom_ribbon(aes(ymin=df_long$min, ymax=df_long$max, fill=df_long$method, alpha=0.3), show.legend=F) +
  scale_fill_manual(breaks = c("d2v_all", "peq_all"), values = c("grey49","grey7"))














# Ontology-based similarity value distribution.
d$method <- pmax(d$p_edge,0.00)
ggplot(d, aes(x=method)) + geom_density(colour="black", fill="blue", alpha=0.3) +
  theme_bw() +
  xlab("Ontology-based Similarity") + 
  ylab("Quantity") +
  theme(plot.title = element_text(lineheight=1.0, face="bold", hjust=0.5), panel.grid.major = element_blank(), panel.grid.minor = element_blank(), panel.border = element_blank(), axis.line = element_line(colour = "black"))


# Document embedding-based similarity value distribution.
d$method <- pmax(range01(1/d$enwiki_dbow),0.00)
ggplot(d, aes(x=method)) + geom_density(colour="black", fill="blue", alpha=0.3) +
  theme_bw() +
  xlab("NLP-based Similarity") + 
  ylab("Quantity") +
  theme(plot.title = element_text(lineheight=1.0, face="bold", hjust=0.5), panel.grid.major = element_blank(), panel.grid.minor = element_blank(), panel.border = element_blank(), axis.line = element_line(colour = "black"))



# What are the interesting points on those plots?
# The value of interest should be lowest score that doesn't seem to be part of the giant clump of meaningless similarity values.
# 
# doc2vec is ~0.10?
# ontology-based is ~0.25?
#
# What are the number of pairs (k in the above code) at which those values are being used?
# Find out what that value of k is in each case, (should be somewhat similar between methods).
# Then mark that value of k on the x-axis in the F1 score plot, and everything to the left of that point should be meaningful.
# This is a method of figuring out which similarity scores are likely to be biologically meaningful when we don't have data to answer this.
                    














                    
                    
  
  

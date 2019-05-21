library(ggplot2)
library(tidyr)
library(dplyr)
library(data.table)
library(car)
library(parallel)


source("/Users/irbraun/NetBeansProjects/term-mapping/r/analysis/utils.R")
source("/Users/irbraun/NetBeansProjects/term-mapping/r/analysis/utils_for_networks.R")



# The file specifying the network edges.
DIR <- "/Users/irbraun/Desktop/droplet/path/networks/"
IN_FILE <- "phenotype_network_modified_NEW.csv"
# Other constants.
SAMPLING_RATIO <- 0.9
OUT_PATH <- "/Users/irbraun/Desktop/droplet/path/r/output/network_comparison.csv"
NUM_ITER <- 10



# Read it in and assign unique IDs to each row.
d <- read(DIR,IN_FILE)
d$id <- row.names(d)


# Get the number of cores available for parallelization.
numCores <- detectCores()
cat(paste(numCores,"cores available"))



# Define sets of nodes which are subsamples of the entire dataset.
# Define which pair id's are contained within each subsampling.
nodes <- unique(c(d$phenotype_1, d$phenotype_2))
sample_size <- floor(length(nodes)*SAMPLING_RATIO)
node_sample_sets <- list()
relevant_id_sets <- list()
num_iter <- NUM_ITER
for(i in seq(1, num_iter)){
  node_sample_sets[[i]] <- sample(nodes, sample_size, replace=F)
  relevant_id_sets[[i]] <- d[d$phenotype_1 %in% node_sample_sets[[i]] & d$phenotype_2 %in% node_sample_sets[[i]],]$id
}



# Define the range of k values to use.
k_start <- 1000 
k_step <- 100000
k_max <- 1001000
total_number_of_edges <- nrow(d)
k_max <- min(k_max,total_number_of_edges)
k_values <- seq(k_start, k_max, k_step)
cat(paste("testing",length(k_values),"values of k from",k_start,"to",k_max))












get_df_for_method <- function(pred_col_name, target_col_name, d){
  # Assign different methods to be target and predicted networks.
  d$target <- d[,target_col_name]
  d$predicted <- d[,pred_col_name]
  # Sort them in descending order.
  ordered_target_ids <- d[order(-d$target),]$id
  ordered_predicted_ids <- d[order(-d$predicted),]$id
  # Populate the lists of F-scores varying k with subsampling.
  method_specific_df <- sapply(k_values, compare_edges_with_subsampling, targets=ordered_target_ids, predictions=ordered_predicted_ids, subsampled_id_sets=relevant_id_sets, sampling_ratio=SAMPLING_RATIO)
  return(method_specific_df)
}




# trying to do them all at once.
TARGET_COL_NAME <- "cur_m1_edge"
PRED_COL_NAMES <- c("cur_m1_edge", "cur_m2_edge", "pre_m1_edge", "pre_m2_edge")

method_dfs <- mclapply(PRED_COL_NAMES, get_df_for_method, target_col_name=TARGET_COL_NAME, d=d, mc.cores=numCores)

# This part is still hardcoded, has to be changed here if using additional metrics.
# Do this a better way.
df <- data.frame(k=k_values,
                 cur1_all=method_dfs[[1]][1,],
                 cur1_min=method_dfs[[1]][2,],
                 cur1_max=method_dfs[[1]][3,],
                 cur2_all=method_dfs[[2]][1,],
                 cur2_min=method_dfs[[2]][2,],
                 cur2_max=method_dfs[[2]][3,],
                 pre1_all=method_dfs[[3]][1,],
                 pre1_min=method_dfs[[3]][2,],
                 pre1_max=method_dfs[[3]][3,],
                 pre2_all=method_dfs[[4]][1,],
                 pre2_min=method_dfs[[4]][2,],
                 pre2_max=method_dfs[[4]][3,]
)




# Save this dataframe as a csv file, Read in back in to make plots locally.
write.csv(df, file=OUT_PATH, row.names=F)
df <- read("",OUT_PATH)



# Converting the dataframe to long format to have multiple methods on the same plot.
df_long <- gather(df, method, value, cur1_all, cur2_all, pre1_all, pre2_all, factor_key=TRUE)

# Do this a better way.
df_long[df_long$method=="cur1_all","min"] <- df_long[df_long$method=="cur1_all",]$cur1_min
df_long[df_long$method=="cur1_all","max"] <- df_long[df_long$method=="cur1_all",]$cur1_max
df_long[df_long$method=="cur2_all","min"] <- df_long[df_long$method=="cur2_all",]$cur2_min
df_long[df_long$method=="cur2_all","max"] <- df_long[df_long$method=="cur2_all",]$cur2_max
df_long[df_long$method=="pre1_all","min"] <- df_long[df_long$method=="pre1_all",]$pre1_min
df_long[df_long$method=="pre1_all","max"] <- df_long[df_long$method=="pre1_all",]$pre1_max
df_long[df_long$method=="pre2_all","min"] <- df_long[df_long$method=="pre2_all",]$pre2_min
df_long[df_long$method=="pre2_all","max"] <- df_long[df_long$method=="pre2_all",]$pre2_max
df_long <- df_long[,c("k","method","value","min","max")]





# Make the plot out of obtained dataframe.
line_types <- c("dashed", "solid", "dotted", "dotdash")
method_names <- c("cur1_all","cur2_all","pre1_all","pre2_all")
labels <- c("Curated 1", "Curated 2", "Predicted 1", "Predicted 2")
ribbon_colors = c("grey10","grey20","grey30","grey40")


max_num_phenologs <- 2000000
max_y = 1.0

ggplot(data=df_long, aes(x=k, y=value, linetype=method)) + geom_line() +
  scale_linetype_manual("Legend", values=line_types, breaks=method_names, labels=labels) +
  coord_cartesian(xlim=c(max_num_phenologs,0), ylim = c(0.0,max_y)) +
  theme_bw() +
  theme(plot.title = element_text(lineheight=1.0, face="bold", hjust=0.5), panel.grid.major = element_blank(), panel.grid.minor = element_blank(), panel.border = element_blank(), axis.line = element_line(colour = "black")) +
  ylab("F1-Score") +
  xlab("Number of Phenologs") +
  scale_x_continuous(expand=c(0,0)) +
  scale_y_continuous(expand=c(0,0)) +
  # Adding ribbons to show how robust the results are to changes in input phenotypes in this dataset.
  geom_ribbon(aes(ymin=df_long$min, ymax=df_long$max, fill=df_long$method, alpha=0.1), show.legend=F) +
  scale_fill_manual(breaks=method_names, values=ribbon_colors)














# Ontology-based similarity value distribution.
d$method <- pmax(d$pre_m1_edge, 0.00)
ggplot(d, aes(x=method)) + geom_density(colour="black", fill="blue", alpha=0.3) +
  theme_bw() +
  xlab("Ontology-based Similarity") + 
  ylab("Quantity") +
  theme(plot.title = element_text(lineheight=1.0, face="bold", hjust=0.5), panel.grid.major = element_blank(), panel.grid.minor = element_blank(), panel.border = element_blank(), axis.line = element_line(colour = "black"))


# Document embedding-based similarity value distribution.
d$method <- pmax(d$jaccard, 0.00)
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
                    





# Older versions of these methods.
# get_df_for_method_with_subsampling <- function(d){
#   # Sort them in descending order.
#   ordered_target_ids <- d[order(-d$target),]$id
#   ordered_predicted_ids <- d[order(-d$predicted),]$id
#   # Populate the lists of F-scores varying k with subsampling.
#   method_specific_df <- sapply(k_values, compare_edges_with_subsampling, targets=ordered_target_ids, predictions=ordered_predicted_ids, subsampled_id_sets=relevant_id_sets, sampling_ratio=SAMPLING_RATIO)
#   return(method_specific_df)
# }
# 
# 
# get_df_for_method_without_subsampling <- function(d){
#   # Sort them in descending order.
#   ordered_target_ids <- d[order(-d$target),]$id
#   ordered_predicted_ids <- d[order(-d$predicted),]$id
#   
#   # Populate the lists of F-scores varying k without subsampling.
#   method_specific_df <- sapply(k_values, compare_edges, targets=ordered_target_ids, predictions=ordered_predicted_ids)
#   return(method_specific_df)
# }






                    
                    
  
  







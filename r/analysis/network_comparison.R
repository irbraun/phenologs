library(ggplot2)
library(tidyr)
library(dplyr)
library(data.table)
library(car)
library(parallel)


source("r/lib/utils.R")
source("r/lib/utils_for_networks.R")



# The fils specifying the network edges and output path.
DIR <- "networks/"
IN_FILE_1 <- "phenotype_text_phenotype_network.csv"
IN_FILE_2 <- "phene_text_phenotype_network.csv"
OUT_PATH_1 <- "r/output/phenotype_text_network_comparison.csv"
OUT_PATH_2 <- "r/output/phene_text_network_comparison.csv"

# Other constants for subsampling process.
SAMPLING_RATIO <- 0.8
NUM_ITER <- 10


# Read in the command line argument to choose correct network file.
args = commandArgs(trailingOnly=TRUE)
if (length(args)!=1) {stop("script takes one argument", call.=FALSE)}
dtype = args[1]
if(dtype=="--phenotypes") {
  IN_FILE <- IN_FILE_1
  OUT_PATH <- OUT_PATH_1
}else if(dtype=="--phenes"){
  IN_FILE <- IN_FILE_2
  OUT_PATH <- OUT_PATH_2
}else{
  stop("argument not understood", call.=FALSE)
}





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
k_step <- 1000
k_max <- 600000
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



TARGET_COL_NAME <- "predefined"
PRED_COL_NAMES <- c("pre_m1_edge", "pre_m2_edge", "enwiki_dbow", "cosine", "jaccard")

method_dfs <- mclapply(PRED_COL_NAMES, get_df_for_method, target_col_name=TARGET_COL_NAME, d=d, mc.cores=numCores)

df <- data.frame(k=k_values,
                 pre1_all=method_dfs[[1]][1,],
                 pre1_min=method_dfs[[1]][2,],
                 pre1_max=method_dfs[[1]][3,],
                 pre2_all=method_dfs[[2]][1,],
                 pre2_min=method_dfs[[2]][2,],
                 pre2_max=method_dfs[[2]][3,],
                 enw_all=method_dfs[[3]][1,],
                 enw_min=method_dfs[[3]][2,],
                 enw_max=method_dfs[[3]][3,],
                 cos_all=method_dfs[[4]][1,],
                 cos_min=method_dfs[[4]][2,],
                 cos_max=method_dfs[[4]][3,],
                 jac_all=method_dfs[[5]][1,],
                 jac_min=method_dfs[[5]][2,],
                 jac_max=method_dfs[[5]][3,]
)




# Save this dataframe as a csv file, Read in back in to make plots locally.
write.csv(df, file=OUT_PATH, row.names=F)




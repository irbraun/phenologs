library(ggplot2)
library(tidyr)
library(dplyr)
library(data.table)
library(car)
library(hashmap)
library(parallel)

source("/Users/irbraun/NetBeansProjects/term-mapping/r/analysis/utils.R")
source("/Users/irbraun/NetBeansProjects/term-mapping/r/analysis/utils_for_subsets.R")



# Network files.
NETWORKS_DIR <- "/Users/irbraun/Desktop/droplet/path/networks/"
PHENOTYPE_EDGES_FILE <- "phenotype_network_modified_NEW.csv"
# Function categorization files.
SUBSETS_DIR <- "/Users/irbraun/Desktop/"
SUBSETS_FILENAME <- "out.csv"
# Define properties of the output files.
OUTPUT_DIR <- "/Users/irbraun/Desktop/temp/"
OUT_FILE_COLUMNS <- c("group","class","subset","combined","num_in","num_out","mean_within","mean_between","p_value","greater","significant")

# The column names in the network file for each predictive method.
PRED_COLUMN_NAMES <- c("cur_m1_edge", "cur_m2_edge", "pre_m1_edge", "pre_m2_edge", "jaccard", "cosine")


# Get the number of cores available for parallelization.
numCores <- detectCores()
cat(paste(numCores,"cores available"))








# Read in the phenotype and phene network files output from the pipeline.
phenotype_network <- read(NETWORKS_DIR,PHENOTYPE_EDGES_FILE)

# Read in the categorization file for functional subsets of Arabidopsis genes.
subsets_df <- read(SUBSETS_DIR, SUBSETS_FILENAME)
subset_name_list <- unique(subsets_df$subset)
class_name_list <- unique(subsets_df$class)


# Define mappings between subsets and groups and classes.
mappings_df <- subsets_df %>% distinct(subset, .keep_all=TRUE)
subset2group <- hashmap(mappings_df$subset, mappings_df$group)
subset2class <- hashmap(mappings_df$subset, mappings_df$class)




# Figuring out logical thresholds for what fraction of possible edges are important.
# What's the fraction of possible edges in the network that connect two genes in the same class?
class_sizes <- unname(table(subsets_df$class))
class_edge_quantities <- sapply(class_sizes, function(x){(x*x)-x})
within_class_edge_quantities <- sum(class_edge_quantities)
x <- sum(class_sizes)
total_edge_quantity <- (x*x)-x 
fraction_within_class <- within_class_edge_quantities/total_edge_quantity
fraction_within_class

# What's the fraction of possible edges in the network that connect two genes in the same subset?
subset_sizes <- unname(table(subsets_df$subset))
subset_edge_quantities <- sapply(subset_sizes, function(x){(x*x)-x})
within_subset_edge_quantities <- sum(subset_edge_quantities)
x <- sum(subset_sizes)
total_edge_quantity <- (x*x)-x 
fraction_within_subset <- within_subset_edge_quantities/total_edge_quantity
fraction_within_subset











renamer <- function(df, cols_to_update, postfix){
  # Update suffixes in the output tables so the merged table makes sense.
  for(oldname in cols_to_update){
    names(df)[names(df) == oldname] <- paste(oldname,postfix,sep=".")
  }
  return(df)
}



summarize_method <- function(pred_col_name, df, output_dir){
  # Specify which predictive method to use.
  df$value_to_use <- df[,pred_col_name]
  # Generate the empty table.
  table <- get_empty_table(OUT_FILE_COLUMNS)
  # Iterate through all the subsets.
  for (subset in subset_name_list){
    results <- mean_similarity_within_and_between(df, subsets_df, c(subset))
    table[nrow(table)+1,] <- c(subset2group[[subset]], subset2class[[subset]], subset, 0, results)
  }
  # Iterate through all the classes.
  for (class in class_name_list){
    subset_names <- unique(subsets_df[subsets_df$class==class,]$subset)
    results <- mean_similarity_within_and_between(df, subsets_df, subset_names)
    table[nrow(table)+1,] <- c(subset2group[[subset_names[[1]]]], class, "combined", 1, results)
  }
  # Write a method specific file, fix naming in columns and return.
  write.csv(table, file=paste(output_dir,"summary_of_",pred_col_name,".csv",sep=""),row.names=F)
  cols_to_update <- c("num_in","num_out","mean_within","mean_between","p_value","greater","significant")
  table <- renamer(table, cols_to_update, pred_col_name)
  return(table)
}







# Trying to do them all at once.
method_dfs <- mclapply(PRED_COLUMN_NAMES, summarize_method, df=phenotype_network, output_dir=OUTPUT_DIR, mc.cores=numCores)




# Include the full names of each group and class.
names_df <-read("/Users/irbraun/NetBeansProjects/term-mapping/data/original_datasets/","subset_names_cleaned.csv")
group_names_map <- hashmap(unique(names_df$Group.Symbol), unique(names_df$Group.Name))
class_names_map <- hashmap(unique(names_df$Class.Symbol), unique(names_df$Class.Name))
f_group <- function(x){return(group_names_map[[x]])}
f_class <- function(x){return(class_names_map[[x]])}


# Generate a table that summarizes the overall results.
summary <- Reduce(function(x,y) merge(x = x, y = y, by=c("group","class","combined","subset")), method_dfs)
summary$group <- f_group(summary$group)
summary$class <- f_class(summary$class)
write.csv(summary, file=paste(OUTPUT_DIR,"summary_of_all_methods.csv",sep=""), row.names=F)













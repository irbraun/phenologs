library(ggplot2)
library(tidyr)
library(dplyr)
library(data.table)
library(car)
library(parallel)
library(hashmap, lib.loc="r/lib/")

source("r/lib/utils.R")
source("r/lib/utils_for_subsets.R")



# Network files.
NETWORKS_DIR <- "networks/"
IN_FILE_1 <- "phenotype_text_phenotype_network.csv"
IN_FILE_2 <- "phene_text_phenotype_network.csv"
# Function categorization files.
SUBSETS_DIR <- "data/r_data/"
SUBSETS_FILENAME <- "phenotype_classification_list.csv"
CATEGORY_HIERARCHY_FILENAME <- "subset_names_cleaned.csv"
# Define properties of the output files.
OUTPUT_DIR <- "r/output/"
OUT_FILE_COLUMNS <- c("group","class","subset","combined","num_in","num_out","mean_within","mean_between","p_value","greater","significant","diff","subset_number")

# The column names in the network file for each predictive method.
PRED_COLUMN_NAMES <- c("predefined", "pre_m1_edge", "pre_m2_edge", "enwiki_dbow", "jaccard", "cosine")


# Get the number of cores available for parallelization.
numCores <- detectCores()
cat(paste(numCores,"cores available"))








# Read in the command line argument to choose correct network file.
args = commandArgs(trailingOnly=TRUE)
if (length(args)!=1) {stop("script takes one argument", call.=FALSE)}
dtype = args[1]
if(dtype=="--phenotypes") {
  PHENOTYPE_EDGES_FILE <- IN_FILE_1
  TEXT_TYPE = "phenotype_text"
}else if(dtype=="--phenes"){
  PHENOTYPE_EDGES_FILE <- IN_FILE_2
  TEXT_TYPE = "phene_text"
}else{
  stop("argument not understood", call.=FALSE)
}









# Read in the file that specifies what the hierarchy of the categories looks like.
# Generate maps to move between levels and naming schemes for making the tables.
names_df <-read(SUBSETS_DIR,CATEGORY_HIERARCHY_FILENAME)
group_names_map <- hashmap(names_df$Group.Symbol, names_df$Group.Name)
class_names_map <- hashmap(names_df$Class.Symbol, names_df$Class.Name)
f_group <- function(x){return(group_names_map[[x]])}
f_class <- function(x){return(class_names_map[[x]])}
subset_name_list <- unique(names_df$Subset.Symbol)
class_name_list <- unique(names_df$Class.Symbol)
subset2group <- hashmap(names_df$Subset.Symbol, names_df$Group.Symbol)
subset2class <- hashmap(names_df$Subset.Symbol, names_df$Class.Symbol)
subset2subset_number <- hashmap(names_df$Subset.Symbol, names_df$Subset.Number)


# Read in the categorization file for functional subsets of Arabidopsis genes.
subsets_df <- read(SUBSETS_DIR, SUBSETS_FILENAME)
subsets_df$class <- subset2class[[subsets_df$subset]]
subsets_df$group <- subset2group[[subsets_df$subset]]




# Read in the phenotype and phene network files output from the pipeline.
phenotype_network <- read(NETWORKS_DIR,PHENOTYPE_EDGES_FILE)







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
    diff <- (as.numeric(results[3])-as.numeric(results[4]))/sd(df$value_to_use)
    subset_number <- subset2subset_number[[subset]]
    table[nrow(table)+1,] <- c(subset2group[[subset]], subset2class[[subset]], subset, 0, results, diff, subset_number)
  }
  # Iterate through all the classes.
  for (class in class_name_list){
    subset_names <- unique(subsets_df[subsets_df$class==class,]$subset)
    results <- mean_similarity_within_and_between(df, subsets_df, subset_names)
    diff <- (as.numeric(results[3])-as.numeric(results[4]))/sd(df$value_to_use)
    subset_number <- 0
    table[nrow(table)+1,] <- c(subset2group[[subset_names[[1]]]], class, "all", 1, results, diff, subset_number)
  }
  # Write a method specific file, fix naming in columns and return.
  #write.csv(table, file=paste(output_dir,"summary_of_",pred_col_name,".csv",sep=""),row.names=F)
  cols_to_update <- c("num_in","num_out","mean_within","mean_between","p_value","greater","significant","diff","subset_number")
  table <- renamer(table, cols_to_update, pred_col_name)
  return(table)
}




summarize_method_for_figure <- function(pred_col_name, df, output_dir){
  # Specify which predictive method to use.
  df$value_to_use <- df[,pred_col_name]
  # Generate the empty table.
  table <- get_empty_table(OUT_FILE_COLUMNS)
  # Iterate through all the subsets.
  for (subset in subset_name_list){
    results <- mean_similarity_within_and_between(df, subsets_df, c(subset))
    diff <- (as.numeric(results[3])-as.numeric(results[4]))/sd(df$value_to_use)
    subset_number <- subset2subset_number[[subset]]
    table[nrow(table)+1,] <- c(subset2group[[subset]], subset2class[[subset]], subset, 0, results, diff, subset_number)
  }
  # Iterate through all the classes.
  for (class in class_name_list){
    subset_names <- unique(subsets_df[subsets_df$class==class,]$subset)
    results <- mean_similarity_within_and_between(df, subsets_df, subset_names)
    diff <- (as.numeric(results[3])-as.numeric(results[4]))/sd(df$value_to_use)
    subset_number <- 0
    table[nrow(table)+1,] <- c(subset2group[[subset_names[[1]]]], class, "all", 1, results, diff, subset_number)
  }
  # Write a method specific file, add method column and return.
  #write.csv(table, file=paste(output_dir,"summary_of_",pred_col_name,".csv",sep=""),row.names=F)
  table$method <- pred_col_name
  return(table)
}






# Treating the not-found edges in the predicted network using EQ statements as no similarity
phenotype_network[is.na(phenotype_network)==TRUE] <- 0.00
phenotype_network$pre_m1_edge <- pmax(phenotype_network$pre_m1_edge,0.00)
phenotype_network$pre_m2_edge <- pmax(phenotype_network$pre_m2_edge,0.00)










# Determine the within and between information for all the methods.
method_dfs <- mclapply(PRED_COLUMN_NAMES, summarize_method, df=phenotype_network, output_dir=OUTPUT_DIR, mc.cores=numCores)

# Generate a table that summarizes the overall results.
summary <- Reduce(function(x,y) merge(x = x, y = y, by=c("group","class","combined","subset")), method_dfs)
summary$group <- f_group(summary$group)
summary$class <- f_class(summary$class)
write.csv(summary, file=paste(OUTPUT_DIR,"summary_of_all_methods_",TEXT_TYPE,".csv",sep=""), row.names=F)











# Same thing but using the version that makes generating the figure easy.
method_dfs <- mclapply(PRED_COLUMN_NAMES, summarize_method_for_figure, df=phenotype_network, output_dir=OUTPUT_DIR, mc.cores=numCores)
method_df <- do.call("rbind", method_dfs)
method_df$diff <- as.numeric(method_df$diff)
method_df$subset_number <- as.numeric(method_df$subset_number)
method_df <- method_df[method_df$subset!="all",]
for (method_name in PRED_COLUMN_NAMES){
  avg_diff <- mean(method_df[method_df$method==method_name,]$diff)
  method_df[nrow(method_df) + 1,] = list("","","Avg.",0,0,0,0,0,0,"","",avg_diff,max(method_df$subset_number)+1,method_name)
}




# # Specifications for each plot.
# max_y <- 8
# min_y <- -2
# step_y <- 2
# y_label <- "Similarity(within) - Similarity(between)"
# x_label <- "Functional Subsets"
# 
# # Convert how methods are specified in the table to names that work in the figure.
# method_names <- list(
#   "predefined"="Curated",
#   "pre_m1_edge"="EQs S1", 
#   "pre_m2_edge"="EQs S2", 
#   "enwiki_dbow"="Doc2Vec", 
#   "jaccard"="SOW", 
#   "cosine"="BOW"
# )
# method_labeller <- function(value){return(method_names[value])}
# method_labeller <- as_labeller(method_labeller)
# 
# 
# # Fix the order of the included facets.
# method_df$method_factor <- factor(method_df$method, levels=c("predefined","pre_m1_edge","pre_m2_edge","enwiki_dbow","cosine","jaccard"))
# 
# # Make the barplot version of the figure.
# library(grid)
# ggplot(data=method_df, aes(x=reorder(subset,subset_number), y=diff)) + geom_bar(stat="identity") +
#   theme_bw() +
#   scale_y_continuous(breaks=seq(min_y,max_y,step_y), expand=c(0,0), limits=c(min_y,max_y)) +
#   theme(plot.title = element_text(lineheight=1.0, face="bold", hjust=0.5), panel.grid.major = element_blank(), panel.grid.minor = element_blank(), panel.border = element_blank(), axis.line = element_line(colour = "black")) +
#   #ylab(y_label) +
#   #xlab(x_label) +
#   theme(axis.title.x = element_blank(), axis.title.y = element_blank()) +
#   theme(axis.text.x = element_text(angle = 90, hjust = 1, vjust = .5)) +
#   facet_grid(method_factor  ~ ., labeller=method_labeller) + 
#   theme(panel.spacing = unit(0.8, "lines"))



# Save the image of the plot.
#filename <- paste("r/output/subsets_approach_1_",TEXT_TYPE,".png", sep = "")
#ggsave(filename, plot=last_plot(), device="png", path=NULL, scale=1, width=28, height=14, units=c("cm"), dpi=300, limitsize=TRUE)






# Make the heatmap version of the figure.
df_method_names <- PRED_COLUMN_NAMES
figure_method_names <- c("Curated","Pred EQs S1","Pred EQs S2","Doc2Vec","Set-of-words","Bag-of-words")
high_color <- "black"
low_color <- "white"
max_value <- 6
min_value <- 0
legend_label <- "Consistency Index"

# Correcting the ordering of the methods on the y-axis.
method_df$method_number <-0 
for(i in seq(1,6,1)){method_df[method_df$method==df_method_names[i],]$method_number <- i}

ggplot(data=method_df, aes(x=reorder(subset,subset_number), y=reorder(method,-method_number))) + geom_tile(aes(fill=diff), colour="black") + 
  scale_fill_gradient(low=low_color, high=high_color, name=legend_label, limits=c(min_value,max_value)) +
  theme_bw() +
  theme(plot.title = element_text(lineheight=1.0, face="bold", hjust=0.5), panel.grid.major = element_blank(), panel.grid.minor = element_blank(), panel.border = element_blank(), axis.line = element_blank()) +
  theme(axis.text.x = element_text(angle = 90, hjust = 1, vjust = .5), axis.title.x = element_blank()) +
  ylab("Method (Phenes)") +
  scale_y_discrete(breaks=df_method_names, labels=figure_method_names) +
  theme(legend.position="top") +
  theme(legend.text = element_text(lineheight=1 ,hjust=0.5, size=8), legend.title =element_text(lineheight=1, hjust=0.5, size=10))
  
# Save the image of the plot.
#filename <- paste("/Users/irbraun/Desktop/subsets_approach_1_",TEXT_TYPE,".png", sep = "")
#ggsave(filename, plot=last_plot(), device="png", path=NULL, scale=1, width=28, height=7, units=c("cm"), dpi=300, limitsize=TRUE)


















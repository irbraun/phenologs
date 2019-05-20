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
PHENOTYPE_EDGES_FILE <- "phenotype_text_phenotype_network.csv"
# Function categorization files.
SUBSETS_DIR <- "/Users/irbraun/Desktop/"
SUBSETS_FILENAME <- "out.csv"
# Define properties of the output files.
OUTPUT_DIR <- "/Users/irbraun/Desktop/temp/"
OUT_FILE_COLUMNS <- c("group","class","subset","combined","num_in","num_out","mean_within","mean_between","p_value","greater","significant","diff")

# The column names in the network file for each predictive method.
PRED_COLUMN_NAMES <- c("predefined", "cur_m1_edge", "cur_m2_edge", "pre_m1_edge", "pre_m2_edge", "enwiki_dbow", "jaccard", "cosine")


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
    diff <- (as.numeric(results[3])-as.numeric(results[4]))/sd(df$value_to_use)
    table[nrow(table)+1,] <- c(subset2group[[subset]], subset2class[[subset]], subset, 0, results, diff)
  }
  # Iterate through all the classes.
  for (class in class_name_list){
    subset_names <- unique(subsets_df[subsets_df$class==class,]$subset)
    results <- mean_similarity_within_and_between(df, subsets_df, subset_names)
    diff <- (as.numeric(results[3])-as.numeric(results[4]))/sd(df$value_to_use)
    table[nrow(table)+1,] <- c(subset2group[[subset_names[[1]]]], class, "all", 1, results, diff)
  }
  # Write a method specific file, fix naming in columns and return.
  write.csv(table, file=paste(output_dir,"summary_of_",pred_col_name,".csv",sep=""),row.names=F)
  cols_to_update <- c("num_in","num_out","mean_within","mean_between","p_value","greater","significant","diff")
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
    table[nrow(table)+1,] <- c(subset2group[[subset]], subset2class[[subset]], subset, 0, results, diff)
  }
  # Iterate through all the classes.
  for (class in class_name_list){
    subset_names <- unique(subsets_df[subsets_df$class==class,]$subset)
    results <- mean_similarity_within_and_between(df, subsets_df, subset_names)
    diff <- (as.numeric(results[3])-as.numeric(results[4]))/sd(df$value_to_use)
    table[nrow(table)+1,] <- c(subset2group[[subset_names[[1]]]], class, "all", 1, results, diff)
  }
  # Write a method specific file, add method column and return.
  write.csv(table, file=paste(output_dir,"summary_of_",pred_col_name,".csv",sep=""),row.names=F)
  table$method <- pred_col_name
  return(table)
}









# Determine the within and between information for all the methods.
method_dfs <- mclapply(PRED_COLUMN_NAMES, summarize_method, df=phenotype_network, output_dir=OUTPUT_DIR, mc.cores=numCores)

# Include the full names of each group and class.
names_df <-read("/Users/irbraun/NetBeansProjects/term-mapping/data/original_datasets/cleaned/","subset_names_cleaned.csv")
group_names_map <- hashmap(unique(names_df$Group.Symbol), unique(names_df$Group.Name))
class_names_map <- hashmap(unique(names_df$Class.Symbol), unique(names_df$Class.Name))
f_group <- function(x){return(group_names_map[[x]])}
f_class <- function(x){return(class_names_map[[x]])}

# Generate a table that summarizes the overall results.
summary <- Reduce(function(x,y) merge(x = x, y = y, by=c("group","class","combined","subset")), method_dfs)
summary$group <- f_group(summary$group)
summary$class <- f_class(summary$class)
write.csv(summary, file=paste(OUTPUT_DIR,"summary_of_all_methods.csv",sep=""), row.names=F)








# Same thing but using the version that makes generating the figure easy.
method_dfs <- mclapply(PRED_COLUMN_NAMES, summarize_method_for_figure, df=phenotype_network, output_dir=OUTPUT_DIR, mc.cores=numCores)
method_df <- do.call("rbind", method_dfs)
method_df$diff <- as.numeric(method_df$diff)
method_df <- method_df[method_df$subset!="all",]

max_y <- 8
min_y <- -2
step_y <- 2

y_label <- "Sim(within) - Sim(between)"
x_label <- "Functional Subsets"

library(grid)
ggplot(data=method_df, aes(x=subset, y=diff)) + geom_bar(stat="identity") +
  theme_bw() +
  scale_y_continuous(breaks=seq(min_y,max_y,step_y), expand=c(0,0), limits=c(min_y,max_y)) +
  theme(plot.title = element_text(lineheight=1.0, face="bold", hjust=0.5), panel.grid.major = element_blank(), panel.grid.minor = element_blank(), panel.border = element_blank(), axis.line = element_line(colour = "black")) +
  ylab(y_label) +
  xlab(x_label) +
  theme(axis.text.x = element_text(angle = 90, hjust = 1, vjust = .5)) +
  facet_grid(method  ~ .) + 
  theme(panel.spacing = unit(0.8, "lines"))

















# prepare_one_plot <- function(method_df, x_label, y_label){
#   max_y <- 5
#   min_y <- -1
#   step_y <- 1
#   diff_col_index <- grepl('diff', colnames(method_df))
#   method_df$diff <- as.numeric(method_df[,diff_col_index])
#   method_df <- method_df[method_df$subset!="all",]
#   
#   plt <- ggplot(data=method_df, aes(x=subset, y=diff)) + geom_bar(stat="identity") +
#     theme_bw() +
#     scale_y_continuous(breaks=seq(min_y,max_y,step_y), expand=c(0,0), limits=c(min_y,max_y)) +
# 
#     theme(plot.title = element_text(lineheight=1.0, face="bold", hjust=0.5), panel.grid.major = element_blank(), panel.grid.minor = element_blank(), panel.border = element_blank(), axis.line = element_line(colour = "black")) +
#     ylab(y_label)
#   
#   # Only want to include the x-axis label on some of the plots.
#   if(x_label==""){
#     plt <- plt + theme(axis.title.x=element_blank()) 
#     plt <- plt + theme(axis.text.x=element_blank())
#   }else{
#     plt <- plt + xlab(x_label)
#     plt <- plt + theme(axis.text.x = element_text(angle = 90, hjust = 1, vjust = .5))
#   }
#   return(plt)
# }
# 
# 
# p1 <- prepare_one_plot(method_dfs[[1]], "soetuh", "predefined")
# p2 <- prepare_one_plot(method_dfs[[2]], "tasdkj", "cur_m1")
# p3 <- prepare_one_plot(method_dfs[[3]], "", "cur_m2")
# p4 <- prepare_one_plot(method_dfs[[4]], "", "pre_m1")
# p5 <- prepare_one_plot(method_dfs[[5]], "", "pre_m2")
# p6 <- prepare_one_plot(method_dfs[[6]], "", "doc2vec")
# p7 <- prepare_one_plot(method_dfs[[7]], "", "word set")
# p8 <- prepare_one_plot(method_dfs[[8]], "categories", "word bag")
# 
# 
# 
# 
# 
# p + facet_grid(rows = vars(drv))
# 
# 
# 
# library(gridExtra)
# grid.arrange(p1, p2, p3, p4, p5, p6, p7, p8, nrow = 8)
# 
# 
# 
# library(grid)
# grid.newpage()
# grid.draw(rbind(ggplotGrob(p1), ggplotGrob(p2), size = "last"))
# 
# 
# 
# 
# 
# a <- method_dfs[[5]]
# a <- a[a$subset!="all",]
# a$diff.enwiki_dbow <- as.numeric(a$diff.enwiki_dbow)
# 
# p1 <- ggplot(data=a, aes(x=subset, y=diff.enwiki_dbow)) + geom_bar(stat="identity") +
#   theme_bw() +
#   scale_y_continuous(breaks=seq(-1,5,1.0), expand=c(0,0), limits=c(-1,5)) +
#   theme(axis.text.x = element_text(angle = 90, hjust = 1, vjust = .5)) +
#   theme(plot.title = element_text(lineheight=1.0, face="bold", hjust=0.5), panel.grid.major = element_blank(), panel.grid.minor = element_blank(), panel.border = element_blank(), axis.line = element_line(colour = "black")) +
#   theme(axis.title.x=element_blank()) + 
#   ylab("doc2vec")
# 
# 
# a <- method_dfs[[3]]
# a <- a[a$subset!="all",]
# a$diff.pre_m1_edge <- as.numeric(a$diff.pre_m1_edge)
# 
# p2 <- ggplot(data=a, aes(x=subset, y=diff.pre_m1_edge)) + geom_bar(stat="identity") +
#   theme_bw() +
#   scale_y_continuous(breaks=seq(-1,5,1.0), expand=c(0,0), limits=c(-1,5)) +
#   theme(axis.text.x = element_text(angle = 90, hjust = 1, vjust = .5)) +
#   theme(plot.title = element_text(lineheight=1.0, face="bold", hjust=0.5), panel.grid.major = element_blank(), panel.grid.minor = element_blank(), panel.border = element_blank(), axis.line = element_line(colour = "black")) +
#   ylab("Predicted EQs") +
#   xlab("Functional Subsets")
# 
# 
# library(gridExtra)
# grid.arrange(p1, p2, nrow = 2)





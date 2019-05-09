library(ggplot2)
library(tidyr)
library(dplyr)
library(data.table)
library(car)


source("/Users/irbraun/NetBeansProjects/term-mapping/r/analysis/utils.R")
source("/Users/irbraun/NetBeansProjects/term-mapping/r/analysis/utils_for_networks.R")




# The file specifying the network edges and the output file.
DIR <- "/Users/irbraun/Desktop/droplet/path/networks/old2/"
FILENAME <- "phenotype_network_modified_NEW.csv"
OUTPUT_PATH <- "/Users/irbraun/Desktop/network_table.csv"



get_score_for_method <- function(table,thresholds,levels,d,method_name){
  
  # Completes a row in the output table by finding scores for this method.
  # args
  # table: the growing table to append to.
  # thresholds: a vector of threshold values (fractions of edges to retain).
  # levels: a vector of strings explaining the significance of the threshold values, same length.
  # d: the dataframe with edge values for every method.
  # method_name: a string specifying which method was used to make these predictions.
  
  # Sort them in descending order.
  ordered_target_ids <- d[order(-d$target),]$id
  ordered_predicted_ids <- d[order(-d$predicted),]$id
  # Find the F1 score at each threshold.
  for (i in range(1,length(thresholds))){
    t <- thresholds[i]
    level <- levels[i]
    n <- nrow(d)
    k <- ceiling(t*n)
    f1 <- compare_edges(ordered_target_ids, ordered_predicted_ids, k)
    table[nrow(table)+1,] <- c(method_name, round(t,3), level, k, round(f1,3))
  }
  return(table)
}





# Read it in and assign unique IDs to each row.
d <- read(DIR,FILENAME)
d$id <- row.names(d)



# Create an output table.
table <- get_empty_table(c("method","threshold","level","num_phenologs","f1"))

# Define thresholds for what fraction of edges to retain. Calculated in subsets_approach1.R
fraction_within_class <- 0.2688608
fraction_within_subset <- 0.03944202
thresholds <- c(fraction_within_class,fraction_within_subset)
levels <- c("subset","class")


# Define what the target values are.
d$target <- d$cur_m2_edge



# Test each of the predictive methods.
d$predicted <- d$pre_m1_edge
table <- get_score_for_method(table,thresholds,levels,d,"predeq1")

d$predicted <- d$pre_m2_edge
table <- get_score_for_method(table,thresholds,levels,d,"predeq2")

d$predicted <- d$jaccard
table <- get_score_for_method(table,thresholds,levels,d,"jacm")

d$predicted <- d$cosine
table <- get_score_for_method(table,thresholds,levels,d,"cosm")



# Write the output table to a file.
write.csv(table, file=OUTPUT_PATH, row.names=F)







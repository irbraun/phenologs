library(ggplot2)
library(tidyr)
library(dplyr)
library(data.table)
library(car)
library(bootstrap)
library(DAAG)
library(kSamples)
library(parallel)



source("/Users/irbraun/NetBeansProjects/term-mapping/r/analysis/utils.R")




# Network files.
NETWORKS_DIR <- "/Users/irbraun/Desktop/droplet/path/networks/"
PHENOTYPE_TEXT_EDGES_FILE <- "phenotype_text_phenotype_network.csv"
PHENE_TEXT_EDGES_FILE <- "phene_text_phenotype_network.csv"
# Define properties of the output files.
OUTPUT_DIR <- "/Users/irbraun/Desktop/pathway_membership_files/"
OUT_FILE_COLUMNS <- c("method","rank","sim","rank","sim","rank","sim","rank","sim")

# The column names in the network file for each predictive method.
PRED_COLUMN_NAMES <- c("predefined", "pre_m1_edge", "pre_m2_edge", "enwiki_dbow", "jaccard", "cosine")

# Get the number of cores available for parallelization.
numCores <- detectCores()
cat(paste(numCores,"cores available"))



read <- function(dir,filename){
  d <- read.csv(file=paste(dir,filename,sep=""), header=T, sep=",")
  return(d)
}






# IDs for the phenotypes and corresponding genes in the pathway.
query_ids_list <- list(
  c("GRMZM2G422750", 1262),
  c("GRMZM2G005066", 1262),
  c("GRMZM5G822829", 1262),
  c("GRMZM2G172795", 1262),
  c("GRMZM2G422750", 1262),
  c("GRMZM2G026930", 1262),
  c("GRMZM2G345717", 1262),
  c("GRMZM2G165390", 1262),
  c("GRMZM2G016241", 1262),
  c("GRMZM2G005066", 1262),
  c("GRMZM2G084799", 1262),
  c("GRMZM2G701063", 1262),
  c("GRMZM5G822829", 1262),
  c("GRMZM2G172795", 1262)
)







get_k_best_for_one_method <- function(column_to_use, df, query_id){
  df$value_used <- df[,column_to_use]
  relevant_ids <- query_id
  relevant_slice <- df[df$phenotype_1 %in% relevant_ids | df$phenotype_2 %in% relevant_ids,]
  k = nrow(relevant_slice)
  top_k_rows <- relevant_slice <- relevant_slice[order(-relevant_slice$value_used),][1:k,]
  top_k_rows$top_k_id <- ifelse(top_k_rows$phenotype_1==query_id, top_k_rows$phenotype_2, top_k_rows$phenotype_1)
  return(top_k_rows$top_k_id)
}




# Read in the phenotype network files output from the pipeline using the phenotype descriptions.
phenotype_network <- read(NETWORKS_DIR,PHENOTYPE_TEXT_EDGES_FILE)
for(query_id in query_ids_list){
  # Unpack the query ID information.
  gene_name <- query_id[1]
  query_id_values <- as.numeric(query_id[2:length(query_id)])
  # Generate the ranked list of similar genes.
  table <- mclapply(PRED_COLUMN_NAMES, get_k_best_for_one_method, df=phenotype_network, query_id=query_id_values, mc.cores=numCores)
  table <- as.data.frame(matrix(unlist(table), nrow=length(unlist(table[1]))))
  names(table) <- PRED_COLUMN_NAMES
  write.csv(table, file=paste(OUTPUT_DIR,"phenotype_text_",gene_name,".csv",sep=""), row.names=FALSE)
}



# Read in the phenotype network files output from the pipeline using the phene descriptions.
phenotype_network <- read(NETWORKS_DIR,PHENE_TEXT_EDGES_FILE)
for(query_id in query_ids_list){
  # Unpack the query ID information.
  gene_name <- query_id[1]
  query_id_values <- as.numeric(query_id[2:length(query_id)])
  # Generate the ranked list of similar genes.
  table <- mclapply(PRED_COLUMN_NAMES, get_k_best_for_one_method, df=phenotype_network, query_id=query_id_values, mc.cores=numCores)
  table <- as.data.frame(matrix(unlist(table), nrow=length(unlist(table[1]))))
  names(table) <- PRED_COLUMN_NAMES
  write.csv(table, file=paste(OUTPUT_DIR,"phene_text_",gene_name,".csv",sep=""), row.names=FALSE)
}





# Making the figure.
df <- read("/Users/irbraun/Desktop/", "big_table.csv")
df$gene <- as.character(df$gene)

# Average the values (bin quantities) for each method and text description type.
for (method in PRED_COLUMN_NAMES){
  df[nrow(df)+1,] <- c("average", "Phenotype Descriptions", method, mean(df[(df$dtype=="Phenotype Descriptions") & (df$method==method),]$bin_10), 8.88, 8.88)
  df$bin_10 <- as.numeric(df$bin_10)
  df$bin_100 <- as.numeric(df$bin_100)
  df$bin_inf <- as.numeric(df$bin_inf)
  df[nrow(df)+1,] <- c("average", "Phene Descriptions", method, mean(df[(df$dtype=="Phene Descriptions") & (df$method==method),]$bin_10), 8.88, 8.88)
  df$bin_10 <- as.numeric(df$bin_10)
  df$bin_100 <- as.numeric(df$bin_100)
  df$bin_inf <- as.numeric(df$bin_inf)
}
df <- df[df$gene=="average",]

# Gathering the table data into the long format.
df_long <- gather(df, bin, quantity, bin_10:bin_inf, factor_key=TRUE)





ggplot(data=df_long, aes(x=bin,y=quantity) ) + geom_bar(stat="identity") +
  facet_wrap(~method)





library(ggplot2)
ggplot(data = data, mapping = aes(x = as.factor(zipcode), y = avgLSAmount)) +
  geom_bar(stat = "identity") +
  labs(x = "zipcode")













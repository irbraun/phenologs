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
query_list <- list(
  c("GRMZM2G422750", 1262),                                   # c2
  c("GRMZM2G026930", 1582,1583,1585,1979,2466,2709,171,213),  # a1
  c("GRMZM2G345717", 174,214),                                # a2
  c("GRMZM2G165390", 228,2329),                               # bz1
  c("GRMZM2G016241", 229),                                    # bz2
  c("GRMZM2G005066", 1261,170),                               # c1
  c("GRMZM2G084799", 2467),                                   # p1
  c("GRMZM2G701063", 169,1336),                               # pl1
  c("GRMZM5G822829", 1728,2599,175,215,220,1263),             # r1
  c("GRMZM2G172795", 173,1335,1584)                           # b1
)

# Expanded set.
query_list <- list(
  c("AT5G07990" ,2792),				#  pr1 
  c("AT1G10370" ,1848),				#  bz2 
  c("GRMZM2G172795" ,1584,1335,173),				#  transcription factors 
  c("GRMZM2G084799" ,2467),				#  transcription factors 
  c("AT4G39640" ,449),				#  bz2 
  c("GRMZM5G822829" ,1728,2599,215,220,175,1263),				#  transcription factors 
  c("AT5G13930" ,2793),				#  c2 
  c("GRMZM2G026930" ,1585,2466,2709,213,1979,171,1582,1583),				#  a1 *
  c("AT3G51240" ,2788),				#  fht1 
  c("AT5G17220" ,1730),				#  bz2 
  c("GRMZM2G422750" ,1262),				#  c2 *
  c("GRMZM2G165390" ,228,2326),				#  bz1 *
  c("AT5G24530" ,1912),				#  fht1 
  c("GRMZM2G016241" ,229),				#  bz2 *
  c("AT5G44070" ,1450),				#  bz2 
  c("GRMZM2G701063" ,1336,169),				#  transcription factors 
  c("GRMZM5G881887" ,1890,1891),				#  a1 
  c("AT5G42800" ,2795),				#  a1 
  c("GRMZM2G086773" ,1889),				#  a1 
  c("AT3G55120" ,2789),				#  chi1 
  c("GRMZM2G005066" ,170,1261),				#  transcription factors 
  c("GRMZM2G345717" ,214,174)				#  a2 *
)




get_k_best_for_one_method <- function(column_to_use, df, query_ids){
  df$value_used <- df[,column_to_use]
  relevant_ids <- query_ids
  relevant_slice <- df[df$phenotype_1 %in% relevant_ids | df$phenotype_2 %in% relevant_ids,]
  top_k_rows <- relevant_slice[order(-relevant_slice$value_used),]
  # We have the ordered edges where either p1 or p2 is a query.
  # Now we need to throw out edges where they're both the query.
  top_k_rows <- top_k_rows[!( (top_k_rows$phenotype_1 %in% query_ids) & (top_k_rows$phenotype_2 %in% query_ids) ),]
  # now take just the id values of the found phenotypes.
  top_k_rows$top_k_id <- ifelse(top_k_rows$phenotype_1 %in% query_ids, top_k_rows$phenotype_2, top_k_rows$phenotype_1)
  return(top_k_rows$top_k_id)
}




# Read in the phenotype network files output from the pipeline using the phenotype descriptions.
phenotype_network <- read(NETWORKS_DIR,PHENOTYPE_TEXT_EDGES_FILE)
for(query in query_list){
  # Unpack the query ID information.
  gene_name <- query[1]
  query_id_values <- as.numeric(query[2:length(query)])
  # Generate the ranked list of similar genes.
  table <- mclapply(PRED_COLUMN_NAMES, get_k_best_for_one_method, df=phenotype_network, query_ids=query_id_values, mc.cores=numCores)
  table <- as.data.frame(matrix(unlist(table), nrow=length(unlist(table[1]))))
  names(table) <- PRED_COLUMN_NAMES
  write.csv(table, file=paste(OUTPUT_DIR,"phenotype_text_",gene_name,".csv",sep=""), row.names=FALSE)
}



# Read in the phenotype network files output from the pipeline using the phene descriptions.
phenotype_network <- read(NETWORKS_DIR,PHENE_TEXT_EDGES_FILE)
for(query in query_list){
  # Unpack the query ID information.
  gene_name <- query[1]
  query_id_values <- as.numeric(query[2:length(query)])
  # Generate the ranked list of similar genes.
  table <- mclapply(PRED_COLUMN_NAMES, get_k_best_for_one_method, df=phenotype_network, query_ids=query_id_values, mc.cores=numCores)
  table <- as.data.frame(matrix(unlist(table), nrow=length(unlist(table[1]))))
  names(table) <- PRED_COLUMN_NAMES
  write.csv(table, file=paste(OUTPUT_DIR,"phene_text_",gene_name,".csv",sep=""), row.names=FALSE)
}









# Making the figure.
df <- read("/Users/irbraun/Desktop/", "big_table.csv")
df$gene <- as.character(df$gene)

# Average the values (bin quantities) for each method and text description type and get standard deviation.
for (method in PRED_COLUMN_NAMES){
  
  sub_df <- df[(df$dtype=="Phenotype Descriptions") & (df$method==method),]
  df[nrow(df)+1,] <- c("average", "Phenotype Descriptions", method,  mean(sub_df$bin_10), mean(sub_df$bin_100), mean(sub_df$bin_inf))
  df[nrow(df)+1,] <- c("stdev", "Phenotype Descriptions", method, sd(sub_df$bin_10), sd(sub_df$bin_100), sd(sub_df$bin_inf))
  df$bin_10 <- as.numeric(df$bin_10)
  df$bin_100 <- as.numeric(df$bin_100)
  df$bin_inf <- as.numeric(df$bin_inf)
  
  sub_df <- df[(df$dtype=="Phene Descriptions") & (df$method==method),]
  df[nrow(df)+1,] <- c("average", "Phene Descriptions", method,  mean(sub_df$bin_10), mean(sub_df$bin_100), mean(sub_df$bin_inf))
  df[nrow(df)+1,] <- c("stdev", "Phene Descriptions", method, sd(sub_df$bin_10), sd(sub_df$bin_100), sd(sub_df$bin_inf))
  df$bin_10 <- as.numeric(df$bin_10)
  df$bin_100 <- as.numeric(df$bin_100)
  df$bin_inf <- as.numeric(df$bin_inf)
}

# Averages.
df_avg <- df[df$gene=="average",]
df_long_avg <- gather(df_avg, bin, quantity, bin_10:bin_inf, factor_key=TRUE)
names(df_long_avg)[5] <- "avg"

# Standard deviations.
df_sd <- df[df$gene=="stdev",]
df_long_sd <- gather(df_sd, bin, quantity, bin_10:bin_inf, factor_key=TRUE)
names(df_long_sd)[5] <- "sd"

# Combined.
df_long <- merge(df_long_avg, df_long_sd, by=c("dtype","method","bin"))





# Convert how methods are specified in the table to names that work in the figure and order as well.
df_long$method_factor <- factor(df_long$method, levels=c("predefined", "pre_m1_edge", "pre_m2_edge", "jaccard", "cosine", "enwiki_dbow"))
method_names <- list(
  "predefined"="Curated EQs",
  "pre_m1_edge"="Pred EQs S1",
  "pre_m2_edge"="Pred EQs S2",
  "enwiki_dbow"="Doc2Vec",
  "jaccard"="Set-of-words",
  "cosine"="Bag-of-words",
  "Phenotype Descriptions"="Phenotype Descriptions",
  "Phene Descriptions"="Phene Descriptions"
)
method_labeller <- function(value){return(method_names[value])}
method_labeller <- as_labeller(method_labeller)


# Make the plot.
ggplot(data=df_long, aes(x=bin,y=avg) ) + geom_bar(stat="identity") + geom_errorbar(aes(ymin=avg-sd, ymax=avg+sd), width=.2) +
  facet_grid(dtype~method_factor, labeller=method_labeller) +
  theme_bw() +
  scale_x_discrete(breaks=c("bin_10","bin_100","bin_inf"), labels=c("1-10","11-100","100+")) +
  scale_y_continuous(breaks=seq(0,14,1)) +
  theme(plot.title = element_text(lineheight=1.0, face="bold", hjust=0.5), 
        legend.direction = "vertical", 
        legend.position = "right", 
        panel.grid.minor = element_line(color="lightgray"), 
        panel.grid.major=element_blank(), 
        axis.line=element_blank()) +
  xlab("Rank Value") + ylab("Rank Quantity")

# Save the image of the plot.
filename <- "/Users/irbraun/Desktop/figure6_pathway_example.png"
ggsave(filename, plot=last_plot(), device="png", path=NULL, scale=1, width=24, height=12, units=c("cm"), dpi=300, limitsize=TRUE)





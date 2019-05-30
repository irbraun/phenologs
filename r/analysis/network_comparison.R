library(ggplot2)
library(tidyr)
library(dplyr)
library(data.table)
library(car)
library(parallel)


source("/work/dillpicl/irbraun/term-mapping/path/r/utils.R")
source("/work/dillpicl/irbraun/term-mapping/path/r/utils_for_networks.R")



# The file specifying the network edges.
DIR <- "/work/dillpicl/irbraun/term-mapping/path/networks/"
IN_FILE <- "phenotype_text_phenotype_network.csv"
# Other constants.
SAMPLING_RATIO <- 0.8
OUT_PATH <- "/work/dillpicl/irbraun/term-mapping/path/r/output/network_comparison.csv"
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




# trying to do them all at once.
TARGET_COL_NAME <- "predefined"
PRED_COL_NAMES <- c("predefined", "cur_m1_edge", "cur_m2_edge", "pre_m1_edge", "pre_m2_edge", "enwiki_dbow", "cosine", "jaccard")

method_dfs <- mclapply(PRED_COL_NAMES, get_df_for_method, target_col_name=TARGET_COL_NAME, d=d, mc.cores=numCores)

# This part is still hardcoded, has to be changed here if using additional metrics.
# Do this a better way.
df <- data.frame(k=k_values,
                 ppn_all=method_dfs[[1]][1,],
                 ppn_min=method_dfs[[1]][2,],
                 ppn_max=method_dfs[[1]][3,],
                 cur1_all=method_dfs[[2]][1,],
                 cur1_min=method_dfs[[2]][2,],
                 cur1_max=method_dfs[[2]][3,],
                 cur2_all=method_dfs[[3]][1,],
                 cur2_min=method_dfs[[3]][2,],
                 cur2_max=method_dfs[[3]][3,],
                 pre1_all=method_dfs[[4]][1,],
                 pre1_min=method_dfs[[4]][2,],
                 pre1_max=method_dfs[[4]][3,],
                 pre2_all=method_dfs[[5]][1,],
                 pre2_min=method_dfs[[5]][2,],
                 pre2_max=method_dfs[[5]][3,],
                 enw_all=method_dfs[[6]][1,],
                 enw_min=method_dfs[[6]][2,],
                 enw_max=method_dfs[[6]][3,],
                 cos_all=method_dfs[[7]][1,],
                 cos_min=method_dfs[[7]][2,],
                 cos_max=method_dfs[[7]][3,],
                 jac_all=method_dfs[[8]][1,],
                 jac_min=method_dfs[[8]][2,],
                 jac_max=method_dfs[[8]][3,]
)




# Save this dataframe as a csv file, Read in back in to make plots locally.
write.csv(df, file=OUT_PATH, row.names=F)
df <- read("",OUT_PATH)



# Converting the dataframe to long format to have multiple methods on the same plot.
df_long <- gather(df, method, value, cur1_all, cur2_all, pre1_all, pre2_all, ppn_all, enw_all, cos_all, jac_all, factor_key=TRUE)

# Do this a better way.
df_long[df_long$method=="cur1_all","min"] <- df_long[df_long$method=="cur1_all",]$cur1_min
df_long[df_long$method=="cur1_all","max"] <- df_long[df_long$method=="cur1_all",]$cur1_max
df_long[df_long$method=="cur2_all","min"] <- df_long[df_long$method=="cur2_all",]$cur2_min
df_long[df_long$method=="cur2_all","max"] <- df_long[df_long$method=="cur2_all",]$cur2_max
df_long[df_long$method=="pre1_all","min"] <- df_long[df_long$method=="pre1_all",]$pre1_min
df_long[df_long$method=="pre1_all","max"] <- df_long[df_long$method=="pre1_all",]$pre1_max
df_long[df_long$method=="pre2_all","min"] <- df_long[df_long$method=="pre2_all",]$pre2_min
df_long[df_long$method=="pre2_all","max"] <- df_long[df_long$method=="pre2_all",]$pre2_max
df_long[df_long$method=="ppn_all","min"] <- df_long[df_long$method=="ppn_all",]$ppn_min
df_long[df_long$method=="ppn_all","max"] <- df_long[df_long$method=="ppn_all",]$ppn_max
df_long[df_long$method=="enw_all","min"] <- df_long[df_long$method=="enw_all",]$enw_min
df_long[df_long$method=="enw_all","max"] <- df_long[df_long$method=="enw_all",]$enw_max
df_long[df_long$method=="cos_all","min"] <- df_long[df_long$method=="cos_all",]$cos_min
df_long[df_long$method=="cos_all","max"] <- df_long[df_long$method=="cos_all",]$cos_max
df_long[df_long$method=="jac_all","min"] <- df_long[df_long$method=="jac_all",]$jac_min
df_long[df_long$method=="jac_all","max"] <- df_long[df_long$method=="jac_all",]$jac_max

df_long <- df_long[,c("k","method","value","min","max")]



# Drop methods to not include in the figure.
do_not_include <- c("cur1_all","cur2_all","ppn_all")
df_long <- df_long[!(df_long$method %in% do_not_include),]



# Creating the faceted plot shown in the figure.
# Read in df_long for each method.
# Add 'facet' column to each with the value either "Phenotype Descriptions" or "Phene Descriptions".
# rbind them to each other to a new df_long.
# Create the faceted plot below.
# Otherwise comment out the facet_wrap line.




# Make the plot out of obtained dataframe, color version.
# Number of S(P1,P2) = 0 rows in the dataframe is 583971, limit x-axis to that.
library("viridis")      
library("grid")
color_codes <- viridis(n=5)
color_codes[5] <- "#DBE318"

method_names <- c("pre1_all","pre2_all","enw_all","cos_all","jac_all")
labels <- c("EQs S1", "EQs S2", "Doc2Vec", "Bag of Words", "Set of Words")
ribbon_colors = rep("grey70",5)
max_num_phenologs <- 583971




# Leaving in the curated stuff instead.
# do_not_include <- c("ppn_all")
# df_long <- df_long[!(df_long$method %in% do_not_include),]
# color_codes <- viridis(n=7)
# method_names <- c("pre1_all","pre2_all","enw_all","cos_all","jac_all","cur1_all","cur2_all")
# labels <- c("EQs S1", "EQs S2", "Doc2Vec", "Bag of Words", "Set of Words", "Curated 1", "Curated 2")
# ribbon_colors = rep("grey70",7)




# Adjusting the x-axis
units <- 1000
x_step <- 100000
x_max <- 583971
x_max <- x_max/units
x_step <- x_step/units
df_long$k <- df_long$k/units
options(scipen=10000)


ggplot(data=df_long, aes(x=k, y=value, color=method)) + geom_line(size=0.5,alpha=0.9) +
  facet_wrap(~facet, ncol=2) +
  scale_color_manual("Methods", values=color_codes, breaks=method_names, labels=labels) +
  coord_cartesian(xlim=c(x_max,0),ylim = c(0.0,1.0)) +
  theme_bw() +
  #theme(plot.title = element_text(lineheight=1.0, face="bold", hjust=0.5), panel.grid.major = element_blank(), panel.grid.minor = element_blank(), panel.border = element_blank(), axis.line = element_line(colour = "black")) +
  theme(plot.title = element_text(lineheight=1.0, face="bold", hjust=0.5), panel.grid.minor = element_blank(), axis.line = element_line(colour = "black")) +
  ylab("F1-Score") +
  xlab("Number of Phenologs (1000's)") +
  scale_x_continuous(expand=c(0,0), breaks=seq(0,max_num_phenologs,x_step)) +
  scale_y_continuous(expand=c(0,0), breaks=seq(0,1,0.2)) +
  # Adding ribbons to show how robust the results are to changes in input phenotypes in this dataset.
  geom_ribbon(aes(ymin=df_long$min, ymax=df_long$max, fill=df_long$method, alpha=0.5), show.legend=F) +
  scale_fill_manual(breaks=method_names, values=ribbon_colors)

# Save the image of the plot.
filename <- "/Users/irbraun/Desktop/network_comparison.png"
ggsave(filename, plot=last_plot(), device="png", path=NULL, scale=1, width=20, height=14, units=c("cm"), dpi=300, limitsize=FALSE)















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








                    
                    
  
  

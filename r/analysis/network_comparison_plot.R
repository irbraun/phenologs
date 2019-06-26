library(ggplot2)
library(tidyr)
library(dplyr)
library(data.table)
library(car)
library(hashmap)
library(parallel)

source("/Users/irbraun/NetBeansProjects/term-mapping/r/analysis/utils.R")
source("/Users/irbraun/NetBeansProjects/term-mapping/r/analysis/utils_for_subsets.R")


# Paths to the network comparison results tables.
OUT_PATH_1 <- "/Users/irbraun/Desktop/droplet/path/r/output/phenotype_text_network_comparison.csv"
OUT_PATH_2 <- "/Users/irbraun/Desktop/droplet/path/r/output/phene_text_network_comparison.csv"


get_df_long <- function(OUT_PATH){
  # Save this dataframe as a csv file, Read in back in to make plots locally.
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
  return(df_long)
}





# Read in the network comparison tables individually then combine them.
df_long_phenotypes <- get_df_long(OUT_PATH_1)
df_long_phenotypes$facet <- "Phenotype Descriptions"
df_long_phenes <- get_df_long(OUT_PATH_2)
df_long_phenes$facet <- "Phene Descriptions"
df_long <- rbind(df_long_phenotypes, df_long_phenes)



# Drop methods to not include in the figure.
do_not_include <- c("cur1_all","cur2_all","ppn_all")
df_long <- df_long[!(df_long$method %in% do_not_include),]




# Make the plot out of obtained dataframe, color version.
# Number of S(P1,P2) = 0 rows in the dataframe is 583971, limit x-axis to that.
library("grid")
library("wesanderson")
library("RColorBrewer")
color_codes <- c("#000000", "#E69F00","#D55E00", "#009E73", "#F0E442")



method_names <- c("pre1_all","pre2_all","enw_all","cos_all","jac_all")
labels <- c("Pred EQ S1", "Pred EQ S2", "Doc2Vec", "Bag-of-words", "Set-of-words")
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













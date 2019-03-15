library(tidyr)
library(plyr)
library(dplyr)
library(ggplot2)
library(lattice)

# -------- Figures for random forest classifiers --------


read_dir_for_pr <- function(path){
  # Stack the dataframes from each split ontop of each other.
  filenames = list.files(path=path, full.names=T)
  data <- do.call(rbind, lapply(filenames, read.csv, header=T, stringsAsFactors=F, sep=","))

  # Weight the hf1_p and hf1_fp values before aggregating so they can be calculated.
  data$hf1 <- data$hf1 * (data$fp+data$tp)
  data$hf1_fp <- data$hf1_fp * (data$fp)
  
  # Aggregate based on the threshold value of the row, and sum the columns. 
  data <- ddply(data, .(threshold), function(x) colSums(x[,-1], na.rm=TRUE))
  
  # Recalculate the metrics, since the avg hf1 values were weighted by the population size, should be correct.
  data$hf1 <- data$hf1 / (data$fp+data$tp)
  data$hf1_fp <- data$hf1_fp / (data$fp)
  data$precision = data$tp/(data$tp+data$fp)
  data$recall = data$tp/(data$tp+data$fn)
  data$f1 = 2*(data$recall*data$precision)/(data$recall+data$precision)  
  return(data)
}




read_dir_for_avgh <- function(path){
  num_splits <- 4
  filenames <- list.files(path=path, full.names=T)
  data <- do.call(rbind, lapply(filenames, read.csv, header=T, stringsAsFactors=F, sep=","))
  data <- ddply(data, .(rank), function(x) colSums(x[,-1], na.rm=TRUE))
  data$hF1 <- data$hF1/num_splits
  data$hJac <- data$hJac/num_splits
  return(data)
}





# Reading in from directories containing multiple files, requiring recalculating the pooled values.
pr <- read_dir_for_pr("/Users/irbraun/Desktop/temp/")
avgs <- read_dir_for_avgh("/Users/irbraun/Desktop/temp10/")


# --- or ---


# Reading in single files
dir <- "/Users/irbraun/Desktop/droplet/alpha2/mapping/set2_ppn_spc_po/"
pr <- read.csv(file=paste(dir,"pr_fold1.csv",sep=""), header=T, sep=",")
avgh <- read.csv(file=paste(dir,"avgh_fold1.csv",sep=""), header=T, sep=",")



# # Precision Recall curve (ranking based)
# fig1.long <- gather(fig1,kind,value,precision,hprecision,hrecall,factor_key=TRUE)
# ggplot(fig1.long, aes(x=recall, y=value, linetype=kind)) + geom_line() +
#   theme_bw() + coord_cartesian(xlim=c(1,0),ylim=c(0,1)) +
#   xlab("Recall") + ylab("Precision") +
#   theme(plot.title = element_text(lineheight=1.0, face="bold", hjust=0.5), legend.direction = "vertical", legend.position = "right", panel.grid.major = 
#           element_blank(), panel.grid.minor = element_blank(), panel.border = element_blank(), axis.line = element_line(colour = "black")) +
#   scale_linetype_manual(values=c("solid", "dotted", "dashed"), name="Metric", breaks=c("precision","hprecision","hrecall"), labels=c("Precision","H_Precision","H_Recall")) +
#   scale_x_continuous(expand=c(0,.000)) +
#   scale_y_continuous(expand=c(0,.000))



# What's the maximum F1 score and the rows that hold those values.
pr$f1 <- (2*pr$precision*pr$recall)/(pr$precision+pr$recall)
f1max <- max(pr$f1, na.rm=TRUE)
maxf1rows <- pr[pr$f1==f1max,]




# Precision Recall curve (omega 1 threshold based)
pr$random <- 0.1475 # (the average hf1 you would expect from guessing randomly in PATO)
#pr$random <- 0.2073 # (the avearge hf1 you would expect from guessing randomly in PO)
pr.long <- gather(pr,kind,value,precision,random,hf1_fp,factor_key=TRUE)
ggplot(pr.long, aes(x=recall, y=value, linetype=kind)) + geom_line() +
  theme_bw() + coord_cartesian(xlim=c(1,0),ylim=c(0,1)) +
  xlab("recall") + ylab("metric") +
  theme(plot.title = element_text(lineheight=1.0, face="bold", hjust=0.5), legend.direction = "vertical", legend.position = "right", panel.grid.major = 
          element_blank(), panel.grid.minor = element_blank(), panel.border = element_blank(), axis.line = element_line(colour = "black")) +
  scale_linetype_manual(values=c("solid", "dotted", "dashed"), name="metrics", breaks=c("precision","hf1_fp","random"), labels=c("precision","hier. F1 (false positives)","hier. F1 (random)")) +
  scale_x_continuous(expand=c(0,.000)) +
  scale_y_continuous(expand=c(0,.000))



# Average Hierarchical F1 or Jaccard Similarity value by rank
#avgh$hF1 <- (2*avgh$hprecision*avgh$hrecall)/(avgh$hprecision+avgh$hrecall)
ggplot(avgh, aes(x=rank, y=hF1)) + geom_col() + coord_cartesian(xlim=c(50.5,0),ylim=c(0,1)) +
  theme_bw() +
  xlab("Ranking") + ylab("Avg. Hierarchical F1") +
  theme(plot.title = element_text(lineheight=1.0, face="bold", hjust=0.5), legend.direction = "vertical", legend.position = "right", panel.grid.major = 
          element_blank(), panel.grid.minor = element_blank(), panel.border = element_blank(), axis.line = element_line(colour = "black")) +
  scale_x_continuous(expand=c(0,.000)) +
  scale_y_continuous(expand=c(0,.000))









library(caret)
library(randomForest)
library(dplyr)
library(tidyr)
library(splitstackshape, lib.loc="/work/dillpicl/irbraun/R/")
library(mice, lib.loc="/work/dillpicl/irbraun/R/")


# Arguments that should be provided when learn_otherprobs.R is run.
# 1. ontology
# 2. output_root
# 3. num_testing_files
# 4. testing_data
# 5. split_postfix
# 6. classprobs_filename_1
# 7. classprobs_filename_2
# 8. joint_identifier


# Testing out arguments locally.
#args <- c("pato", "/Users/irbraun/Desktop/wednesday_temp/", "1", "/Users/irbraun/Desktop/droplet/alpha2/sets/pato/data/features.1.csv", "postfix", "/Users/irbraun/Desktop/wednesday_temp/nc_example.csv","noble")




# Function for reading files.
read_dir <- function(path){
  filenames=list.files(path=path, full.names=T)
  data <- do.call(rbind, lapply(filenames, read.csv, header=T, stringsAsFactors=F, sep=","))
  data$response <- as.factor(data$response)
  data[data==-1] <- NA
  return(data)
}

# Function for reading files.
read_list <- function(filenames){
  data <- do.call(rbind, lapply(filenames, read.csv, header=T, stringsAsFactors=F, sep=","))
  data$response <- as.factor(data$response)
  data[data==-1] <- NA
  return(data)
}




# Read in the arguments.
args <- commandArgs(trailingOnly=T)

# Parse the arguments.
ontology <- args[1]
output_root <- args[2]
num_testing_files <- as.numeric(args[3])
testing_filenames <- args[4:(3+num_testing_files)]
split_postfix <- args[(4+num_testing_files)]
classprobs_filename_1 <- args[(5+num_testing_files)]
classprobs_filename_2 <- args[(6+num_testing_files)]
joint_id <- args[(7+num_testing_files)]


# Read in the data.
results <- read_list(testing_filenames)
classprobs_1 <- read.csv(file=classprobs_filename_1, sep=",", stringsAsFactors=FALSE)
classprobs_2 <- read.csv(file=classprobs_filename_2, sep=",", stringsAsFactors=FALSE)
names(classprobs_1)[names(classprobs_1) == "prob"] <- "prob1"
names(classprobs_2)[names(classprobs_2) == "prob"] <- "prob2"
classprobs_joint <- full_join(classprobs_1, classprobs_2, by=c("term","chunk"))
classprobs_joint$prob <- pmax(classprobs_joint$prob1,classprobs_joint$prob2, na.rm=TRUE)
# Do any renaming that is necessary. For example if 'term' and 'chunk' are not the column names currently.
# (insert here)


# Left join, and handle missing data.
results <- left_join(results, classprobs_joint, by=c("term","chunk"))
results$prob[is.na(results$prob)] <- 0.00


# Exactly the same as in the other script.





# Precision Recall table based off P(omega1) threshold.
threshold <- seq(0, 1, 0.01)
cols <- c("precision","recall","hf1","hf1_fp","tp","fp","fn")
table_prt <- data.frame(threshold)
table_prt[,cols] <- 0
for (row in 1:nrow(table_prt)){
  thresh <- table_prt[row,"threshold"]
  results$match <- ifelse(results$prob >= thresh, 1, 0)
  results$match <- as.factor(results$match)
  tp = sum(results$match == 1 & results$response==1)
  fp = sum(results$match == 1 & results$response==0) 
  fn = sum(results$match == 0 & results$response==1)
  precision = tp/(tp+fp)
  recall = tp/(tp+fn)
  f1 = 2*(recall*precision)/(recall+precision)
  
  # Note that these are average hierarchical values at this point for all instances predicted w1.
  hf1 <- sum(results[results$match==1,]$hF1) / (tp+fp) # (average hierarchical F1 value for all terms predicted to be positive)
  hf1_fp <- sum(results[results$match==1 & results$response==0,]$hF1) / (fp) # (average hierchical F1 value for just the false positive terms)
  
  table_prt[row,"precision"] <- precision
  table_prt[row,"recall"] <- recall
  table_prt[row,"hf1"] <- hf1
  table_prt[row,"hf1_fp"] <- hf1_fp
  table_prt[row,"tp"] <- tp
  table_prt[row,"fp"] <- fp
  table_prt[row,"fn"] <- fn
}



# Output the evaluation table files.
filenames.pr <- paste(output_root, "pr_", split_postfix, "_", joint_id, ".csv", sep="")
write.csv(table_prt, file=filenames.pr, row.names=F)


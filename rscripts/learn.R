library(caret)
library(randomForest)
library(dplyr)
library(tidyr)
library(splitstackshape, lib.loc="/work/dillpicl/irbraun/R/")
library(mice, lib.loc="/work/dillpicl/irbraun/R/")


# Arguments that should be provided when learn.R is run.
# 1. ontology
# 2. k (number of top terms to retain per chunk)
# 3. num_features
# 4. output_root
# 5. num_training_files
# 6. training_data
# 7. num_testing_files
# 8. testing_data
# 9. split_postfix

# Hardcoded Parameters
RATIO <- 2
CV_NUM <- 2
MTRYS <- seq(4,4,1)





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


# Custom sampling methods for cross-validation.
undersampler <- list(name = "undersampling method with provided class ratio",
                     func = function(x,y) {
                       dat <- if (is.data.frame(x)) x else as.data.frame(x)
                       dat$.y <- y
                       ratio = RATIO
                       num_pos <- nrow(dat[dat$.y=="one",])
                       dat <- as.data.frame(stratified(dat, ".y", size=c("zero"=num_pos*ratio,"one"=num_pos)))
                       list(x = dat[, !grepl(".y", colnames(dat), fixed = TRUE)], 
                            y = dat$.y)
                     },
                     first = FALSE)


# Example of another custom sampling method.
smotest <- list(name = "SMOTE with more neighbors!",
                func = function (x, y) {
                  library(DMwR)
                  dat <- if (is.data.frame(x)) x else as.data.frame(x)
                  dat$.y <- y
                  dat <- SMOTE(.y ~ ., data = dat, k = 10)
                  list(x = dat[, !grepl(".y", colnames(dat), fixed = TRUE)], 
                       y = dat$.y)
                },
                first = FALSE)


# Create random forest object (with ntrees parameter) that can be used with the caret package.
customRF <- list(type = "Classification", library = "randomForest", loop = NULL)
customRF$parameters <- data.frame(parameter = c("mtry", "ntree"), class = rep("numeric", 2), label = c("mtry", "ntree"))
customRF$grid <- function(x, y, len = NULL, search = "grid") {}
customRF$fit <- function(x, y, wts, param, lev, last, weights, classProbs, ...) {
  randomForest(x, y, mtry = param$mtry, ntree=param$ntree, ...)
}
customRF$predict <- function(modelFit, newdata, preProc = NULL, submodels = NULL)
  predict(modelFit, newdata, type="prob")
customRF$prob <- function(modelFit, newdata, preProc = NULL, submodels = NULL)
  predict(modelFit, newdata, type = "prob", importance=T)
customRF$sort <- function(x) x[order(x[,1]),]
customRF$levels <- function(x) x$classes
customRF




# Read in the arguments.
args <- commandArgs(trailingOnly=T)



# 1. ontology
# 2. k
# 3. num_features
# 4. output_root
# 5. num_training_files
# 6. training_data (list with 1 or more files)
# 7. num_testing_files
# 8. testing_data (list with 1 or more files)
# 9. split_postffix




# Testing out arguments locally.
#args <- c("pato", "5", "16", "/Users/irbraun/Desktop/wednesday_temp/", "1", "/Users/irbraun/Desktop/droplet/alpha2/sets/pato/data/features.0.csv", "1", "/Users/irbraun/Desktop/droplet/alpha2/sets/pato/data/features.1.csv", "splitID1")





# Parse the arguments.
ontology <- args[1]
k <- as.numeric(args[2])
num_features <- as.numeric(args[3])
output_root <- args[4]
num_training_files <- as.numeric(args[5])
training_filenames <- args[6:(5+num_training_files)]
num_testing_files <- as.numeric(args[(6+num_training_files)])
testing_filenames <- args[(7+num_training_files):(6+num_training_files+num_testing_files)]
split_postfix <- args[(7+num_training_files+num_testing_files)]



# Read in the data.
training_data <- read_list(training_filenames)
testing_data <- read_list(testing_filenames)




# Send the output to a text file.
sink(paste(output_root, "results_", split_postfix, ".txt", sep=""))






# -------- Classification for Training Data--------



# Imputation
# mice method (unconditional mean)
temp <- mice(training_data, m=1, method="mean")
training_data <- mice::complete(temp,1)
# mean method to clean up.
training_data.means <- sapply(training_data, function(x){if(is.numeric(x)){mean(x, na.rm=T)}})
training_data <- training_data %>% replace_na(training_data.means)





# start with mtry=(4,5,6) for |features|=44 and cv number = 10 on the cluster.




# Find and save the best model.
x <- training_data[,3:as.numeric(num_features+2)]
y <- training_data$response
levels(y)[levels(y)=="1"] <- "one"
levels(y)[levels(y)=="0"] <- "zero"
grid <- expand.grid(mtry=MTRYS)   
ctrl <- trainControl(method="cv", number=CV_NUM, returnResamp="all", savePredictions="final", classProbs = T, sampling=undersampler)
fit <- train(x, y, method="rf", metric="Kappa", tuneGrid=grid, trControl=ctrl, importance=T, type="prob", localImp=T)
fit
saveRDS(fit, file=paste(output_root, "fit_", split_postfix, ".RData", sep=""))


# Look at variable importance in terms of the overall forest, not individual instances.
vars <- data.frame(importance(fit$finalModel))
vars$Variable <- row.names(vars)
vars <- vars[order(vars$one,decreasing=T),]
names(vars)[names(vars) == "one"] <- "Positive"
names(vars)[names(vars) == "zero"] <- "Negative"
write.csv(vars, file=paste(output_root, "variables_", split_postfix, ".csv", sep=""), row.names=F)


# Look at predictions made on the holdout-fold data during cv with selected model. Add ranks.
pred <- fit$pred %>% arrange(rowIndex)
pred <- pred$one
pred.df <- data.frame(pred)
results <- cbind(training_data,pred.df)
results <- transform(results, rank=ave(pred, chunk, FUN=function(x) rank(-x, ties.method="random")))
names(results)[names(results) == "pred"] <- "prob"


# Create dataframe for the predictions on the training data for the k best terms per text instance.
output_columns <- c("chunk", "term", "prob")
results_kbest <- results[(results$rank <= k),]
results_kbest <- subset(results_kbest, select=output_columns)

# Output the class probability files.
filenames.classprobs.kbest = paste(output_root, "classprobs_training_", split_postfix, ".csv", sep="")
write.csv(results_kbest, file=filenames.classprobs.kbest, row.names=F)








# -------- Predictions for Testing Data--------



# Imputation
# mice method (uncondition mean)
temp <- mice(testing_data, m=1, method="mean")
testing_data <- mice::complete(temp,1)
# mean method
testing_data.means <- sapply(testing_data, function(x){if(is.numeric(x)){mean(x, na.rm=T)}})
testing_data <- testing_data %>% replace_na(testing_data.means)


# Predict classes on the testing data, and add ranks with respect to text chunk.
x <- testing_data[,3:as.numeric(num_features+2)]
y <- testing_data$response
pred <- predict(fit, x, type="prob")
pred.df <- data.frame(pred)
results <- cbind(testing_data,pred.df)
results <- transform(results, rank = ave(one, chunk, FUN=function(x) rank(-x, ties.method="random")))
names(results)[names(results) == "one"] <- "prob"



# Predicted class probabilities.
output_columns <- c("chunk", "term", "prob")
results_kbest <- results[(results$rank <= k),]
results_kbest <- subset(results_kbest, select=output_columns)



# Average value of hierarchical metrics at each rank.
avgh.df <- aggregate(results[,c("hF1","hJac")], list(results$rank), mean)
colnames(avgh.df)[colnames(avgh.df) == 'Group.1'] <- 'rank'








# Notes about adding in the other class probabilty files in order to get the different precision recall values.
# # TODO add in a part that takes into account a class probability file from other types of concept mappers.
# # Those would be read in as other results_kbest tables essentially, because they just have the three columns.
# results_kbest_nc <- csv
# results_kbest_em <- csv
# results$match_2 <- 0
# 
# # Matching the elements from one column in one df with the elements in another.
# df1 <- data.frame(x = c("a", "c", "g"))
# df2 <- data.frame(y = letters[1:7])
# match(df1$x, df2$y)
# # [1] 1 3 7
# which(df2$y %in% df1$x)
# # [1] 1 3 7
#  ^ might be useful for other stuff but won't work here because >1 variable to match on.
# 
# do a left_join from dplyr
# to make this work make the column names for chunk and term ID exactly the same
# then make the prob column in the smaller dataframe have a unique name. then
# left_join(long_dataframe_name, short_dataframe_name, by= c("Age", "Gender"))
#
# To test this out, run like normal but just skip the prediction and manually add a predictions columns 
# (simulate the cbind step) to add just some predictions that ar all 1 or something like that.

# Read in the other files.
#other_classprobs2 <- read.csv(file="/path/here/nc.csv", sep=",", stringsAsFactors=FALSE)
#other_classprobs1 <- read.csv(file="/Users/irbraun/Desktop/wednesday_temp/nc_example.csv", sep=",", stringsAsFactors=FALSE) 

# What are the names of the termID and chunkID columns in the results dataframe?
# not needed if they both use 'term' and 'chunk' as the names.
#names(other_classprobs1)[names(other_classprobs1) == 'oldname'] <- 'newname'
#names(other_classprobs1)[names(other_classprobs1) == 'oldname'] <- 'newname'
#names(other_classprobs2)[names(other_classprobs2) == 'oldname'] <- 'newname'
#names(other_classprobs2)[names(other_classprobs2) == 'oldname'] <- 'newname'

# Make the names for the additional probability columns unique so they don't get combined in the join.
#names(other_classprobs1)[names(other_classprobs1) == 'prob'] <- 'prob_from_nc'
#names(other_classprobs2)[names(other_classprobs2) == 'prob'] <- 'prob_from_exact'

# Do the left joins.
#results <- left_join(results, other_classprobs1, by=c("term","chunk"))
#results <- left_join(results, other_classprobs2, by=c("term","chunk"))

# What is the value of prob_from_nc and prob_from_exact for rows that were only in the bigger dataframe?
# they need to be 0.00, so change them to that if they're NA or something like that.
#results$prob_from_nc[is.na(results$prob_from_nc)] <- 0.00
  
  

# Create the combined prob columns that do a union of two different methods where applicable.
# then we have a set of prob columns that can each have their own precision recall table using the code below.











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



# Precision Recall table based off of ranking within each chunk.
# numTerms <- 100
# threshold <- seq(0, numTerms, 1)
# cols <- c("precision","recall","hf1","hf1_fp","tp","fp","fn")
# table_prr <- data.frame(threshold)
# table_prr[,cols] <- 0
# for (row in 1:nrow(table_prr)){
#   thresh <- table_prr[row,"threshold"]
#   results$match <- ifelse(results$rank <= thresh, 1, 0)
#   results$match <- as.factor(results$match)
#   tp = sum(results$match == 1 & results$response==1)
#   fp = sum(results$match == 1 & results$response==0)
#   fn = sum(results$match == 0 & results$response==1)
#   precision = tp/(tp+fp)
#   recall = tp/(tp+fn)
#   f1 = 2*(recall*precision)/(recall+precision)
#   
#   # Find the hierarchical values for the positives and false positives etc.
#   hf1 <- sum(results[results$match==1,]$hF1) / (tp+fp) # (average hierarchical F1 value for all terms predicted to be positive)
#   hf1_fp <- sum(results[results$match==1 & results$response==0,]$hF1) / (fp) # (average hierchical F1 value for just the false positive terms)
#   
#   table_prr[row,"precision"] <- precision
#   table_prr[row,"recall"] <- recall
#   table_prr[row,"hf1"] <- hf1
#   table_prr[row,"hf1_fp"] <- hf1_fp
#   table_prr[row,"tp"] <- tp
#   table_prr[row,"fp"] <- fp
#   table_prr[row,"fn"] <- fn
# }




# Output the class probability files.
filenames.classprobs.kbest = paste(output_root, "classprobs_testing_", split_postfix, ".csv", sep="")
write.csv(results_kbest, file=filenames.classprobs.kbest, row.names=F)

# Output the evaluation table files.
filenames.avgh <- paste(output_root, "avgh_", split_postfix, ".csv", sep="")
filenames.pr <- paste(output_root, "pr_", split_postfix, ".csv", sep="")
#filenames.prr <- paste(output_root, "prr_", split_postfix, ".csv", sep="")
write.csv(avgh.df, file=filenames.avgh, row.names=F)
write.csv(table_prt, file=filenames.pr, row.names=F)
#write.csv(table_prr, file=filenames.prr, row.names=F)

















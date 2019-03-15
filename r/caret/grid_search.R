library(tidyr)
library(dplyr)
library(ggplot2)
library(lattice)



# ARGUMENTS
# 1. [string] full path of the csv containing the predicted EQ statements.
# 2. [string] full path of the csv file where the output should go.

# Read in the path of the CSV file containng the EQ statement predictions and output path.
args <- commandArgs(trailingOnly=T)
predictions_file <- args[1]
output_file <- args[2]
data <- read.csv(predictions_file, header=T, stringsAsFactors=F, sep=",")

# Send output to a text file.
#sink(output_file)

alphas <- seq(0,1,0.1)
qcs <- seq(0,1,0.01)
betas <- seq(0,1,0.01)


# Similarity is based on atomized statements.
# Coverage is based on atomized statements.

results <- data.frame(matrix(ncol = 4, nrow = 0))
cols <- c("alpha","beta","qc","qc_fixedbeta")
colnames(results) <- cols

#paste("alpha,beta,qc")
for (alpha in alphas){
  h_max <- 0.00
  h_max_bf <- 0.00
  qc_best <- 0.00
  qc_best_bf <- 0.00 #the best qc value when beta is fixed at 1
  beta_best <- 1.00

  for (beta in betas){
    for (qc in qcs){
      data$temp_q <- (beta*data$p1) + ((1-beta)*data$p2)
      data$kept <- ifelse(data$temp_q >= qc, 1, 0)
      data$kept <- as.factor(data$kept)
      a <- length(unique(data[which(data$kept==1),"atomized_statement_id"]))
      b <- length(unique(data[,"atomized_statement_id"]))
      coverage <- a/b
      mean_sim <- mean(data[which(data$kept==1),"atomized_statement_sim"])
      if (is.na(mean_sim)){
        mean_sim <- 0
      }
      h <- (alpha*coverage) + ((1-alpha)*mean_sim)
      if (h >= h_max){
        h_max <- h
        qc_best <- qc
        beta_best <- beta
      }
      # Remember the learned threshold value when beta is fixed at 1.
      if ((h >= h_max_bf) & (beta==1.00)){
        h_max_bf <- h
        qc_best_bf <- qc_best
      }
    }
  }
  results[nrow(results)+1,] <- c(alpha, beta_best, qc_best, qc_best_bf)
}
write.csv(results, file=output_file, row.names=F)


# # Allows only one predicted EQ per input A.
# # -Variable 1 is sim(A,A)
# # -Variable 2 is the fraction of input A that have 1 or more predicted EQ's greater or equal to qc.
# h_max <- 0.00
# qc_best <- 0.00
# beta_best <- 1.00
# for (beta in betas){
#   data$temp_q <- (beta*data$p1) + ((1-beta)*data$p2)
#   subdata <- data[,c("atomized_statement_id","temp_q","atomized_statement_sim")] %>% group_by(atomized_statement_id)  %>% filter(temp_q == max(temp_q))
#   for (qc in qcs){
#     data$kept <- ifelse(data$temp_q >= qc, 1, 0)
#     data$kept <- as.factor(data$kept)
#     a <- length(unique(data[which(data$kept==1),"atomized_statement_id"]))
#     b <- length(unique(data[,"atomized_statement_id"]))
#     coverage <- a/b
#     mean_sim <- mean(data[which(data$kept==1),"atomized_statement_sim"])
#     if (is.na(mean_sim)){
#       mean_sim <- 0
#     }
#     h <- (alpha*coverage) + ((1-alpha)*mean_sim)
#     if (h >= h_max){
#       h_max <- h
#       qc_best <- qc
#       beta_best <- beta
#     }
#   }
# }
# paste("Similarity based off A. Coverage based off A. Allows only one predicted EQ per input A.")
# paste("q_cutoff = ", qc_best, sep="")
# paste("beta = ", beta_best, sep="")


# # Similarity is based on atomized statements.
# # Coverage is based on phenotypes.
# h_max <- 0.00
# qc_best <- 0.00
# beta_best <- 1.00
# for (qc in qcs){
#   for (beta in betas){
#     data$temp_q <- (beta*data$p1) + ((1-beta)*data$p2)
#     data$kept <- ifelse(data$temp_q >= qc, 1, 0)
#     data$kept <- as.factor(data$kept)
#     a <- length(unique(data[which(data$kept==1),"phenotype_id"]))
#     b <- length(unique(data[,"phenotype_id"]))
#     coverage <- a/b
#     mean_sim <- mean(data[which(data$kept==1),"atomized_statement_sim"])
#     if (is.na(mean_sim)){
#       mean_sim <- 0
#     }
#     h <- (alpha*coverage) + ((1-alpha)*mean_sim)
#     if (h >= h_max){
#       h_max <- h
#       qc_best <- qc
#       beta_best <- beta
#     }
#   }
# }
# paste("Similarity based off A. Coverage based off P.")
# paste("q_cutoff = ", qc_best, sep="")
# paste("beta = ", beta_best, sep="")



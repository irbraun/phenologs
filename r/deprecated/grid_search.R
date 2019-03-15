library(tidyr)
library(dplyr)
library(ggplot2)
library(lattice)



# ARGUMENTS

# 1. [decimal] in range 0 to 1, alpha, the tradeoff between coverage and mean similarity to curated statements.
# 2. [string] full path of the csv containing the predicted EQ statements.
# 3. [string] full path of the txt file where the output should go.


# Read in the path of the CSV file containng the EQ statement predictions and output path.
args <- commandArgs(trailingOnly=T)
alpha <- as.numeric(args[1]) # alpha is the importance of coverage, 1-alpha is the importance of high mean/median similarity.
predictions_file <- args[2]
output_file <- args[3]
data <- read.csv(args[2], header=T, stringsAsFactors=F, sep=",")


# Send output to a text file.
sink(output_file)
paste("results for alpha =", alpha)


qcs <- seq(0,1,0.01)
betas <- seq(0,1,0.01)


# Similarity is based on atomized statements.
# Coverage is based on atomized statements.
h_max <- 0.00
qc_best <- 0.00
beta_best <- 1.00
for (qc in qcs){
  for (beta in betas){
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
  }
}
paste("Similarity based off A. Coverage based off A.")
paste("q_cutoff = ", qc_best, sep="")
paste("beta = ", beta_best, sep="")




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








# Similarity is based on atomized statements.
# Coverage is based on phenotypes.
h_max <- 0.00
qc_best <- 0.00
beta_best <- 1.00
for (qc in qcs){
  for (beta in betas){
    data$temp_q <- (beta*data$p1) + ((1-beta)*data$p2)
    data$kept <- ifelse(data$temp_q >= qc, 1, 0)
    data$kept <- as.factor(data$kept)
    a <- length(unique(data[which(data$kept==1),"phenotype_id"]))
    b <- length(unique(data[,"phenotype_id"]))
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
  }
}
paste("Similarity based off A. Coverage based off P.")
paste("q_cutoff = ", qc_best, sep="")
paste("beta = ", beta_best, sep="")




















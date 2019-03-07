


# ARGUMENTS

# 1. [string] full path of the csv containing the predicted EQ statements.
# 2. [string] full path of the directory where the output should go (results text file).




# Read in the path of the CSV file containng the EQ statement predictions and output path.
args <- commandArgs(trailingOnly=T)
predictions_file <- args[1]
output_root <- args[2]


data <- read.csv(args[1], header=T, stringsAsFactors=F, sep=",")



# Send output to a text file.
sink(paste(output_root, "results.txt", sep=""))

# alpha is the importance of coverage, 1-alpha is the importance of high mean/median similarity.
alpha = 0.4

qcs <- seq(0,1,0.01)
betas <- seq(0,1,0.01)


cols <- c("h")
table <- data.frame(qcs)
table[,cols] <- 0

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











# Write the selected value of q_c to a results file.
paste("Retaining all predicted EQ statements for each atomized statement.")
paste("q_cutoff = ", qc_best, sep="")
paste("beta = ", beta_best, sep="")








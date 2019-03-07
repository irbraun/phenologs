

# ARGUMENTS

# 1. [string] full path of the csv containing the predicted EQ statements.
# 2. [string] full path of the directory where the output should go (results text file).



# Read in the path of the CSV file containng the EQ statement predictions and output path.
#args <- commandArgs(trailingOnly=T)

args <- c("/Users/irbraun/Desktop/composer/pred.csv", "/Users/irbraun/Desktop/composer/")

predictions_file <- args[1]
output_root <- args[2]


data <- read.csv(args[1], header=T, stringsAsFactors=F, sep=",")



# Send output to a text file.
#sink(paste(output_root, "results.txt", sep=""))

# alpha is the importance of coverage, 1-alpha is the importance of high mean/median similarity.
alpha = 0.35

qcs <- seq(0,1,0.01)
cols <- c("h","a","b","coverage")
table <- data.frame(qcs)
table[,cols] <- 0

h_max <- 0.00
qc_best <- 0.00


for (row in 1:nrow(table)){
  
  qc <- table[row,"qcs"]
  data$kept <- ifelse(data$q >= qc, 1, 0)
  data$kept <- as.factor(data$kept)
  
  
  #coverage <- nrow(data[data$kept==1,]) / nrow(data)
  # problem, that isn't coverage, thats fraction of predicted statements that are used.
  # we want fraction of atomized statements that retain >= 1 prediction...
  
  #temp <- data[,c("atomized_statement_id", "q")] %>% group_by(q) 
  
  
  a <- length(unique(data[which(data$kept==1),"atomized_statement_id"]))
  b <- length(unique(data[,"atomized_statement_id"]))
  coverage <- a/b
  table[row,"a"] <- a
  table[row,"b"] <- b
  table[row,"coverage"] <- coverage
  
  
  mean_sim <- mean(data[which(data$kept==1),"atomized_statement_sim"])
  if (is.na(mean_sim)){
    mean_sim <- 0
  }
  h <- (alpha*coverage) + ((1-alpha)*mean_sim)
  
  if (h >= h_max){
    h_max <- h
    qc_best <- qc
  }
  
  table[row,"h"] <- h
}
  
# Write the selected value of q_c to a results file.
paste("q_cutoff = ", qc_best, sep="")

# Write the table of all the values to a file.
filenames.qctable <- paste(output_root, "qc_values.csv", sep="")
write.csv(table, file=filenames.qctable, row.names=F)

scatter.smooth(table$qcs, table$h)
alpha
qc_best
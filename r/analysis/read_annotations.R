


# Function for creating a string for input into a table.
table_row <- function(dir,filename,description){
  
  # Read in the data.
  d <- read.csv(file=paste(dir,filename,sep=""), header=T, sep=",")
  
  # Numerical data that is missing was marked as na in the file.
  d[d=="na"] <- NA
  
  # Non-hierarchical metrics.
  tp <- nrow(d[d$category %in% c("TP"),])
  fp <- nrow(d[d$category %in% c("FP"),])
  fn <- nrow(d[d$category %in% c("FN"),])
  p <- tp/(tp+fp)
  r <- tp/(tp+fn)
  f1 <- (2*p*r)/(p+r)
  
  # Graph-based metrics.
  sum <- sum(d[d$category %in% c("TP","FN"),]$similarity)
  avg <- sum/(tp+fn)
  pr <- avg
  sum <- sum(d[d$category %in% c("TP","FP"),]$similarity)
  avg <- sum/(tp+fp)
  pp <- avg
  pf1 <- (2*pp*pr)/(pr+pp)
  n <- tp+fn
  
  # Adding one for partial precision of just the 'false' positives.
  sum <- sum(d[d$category %in% c("FP"),]$similarity)
  avg <- sum/(fp)
  pp_fp <- avg
  
  # Keeping track of how many predicted and target terms there were for averaging.
  num_predicted <- tp+fp
  num_target <- tp+fn
  
  line = c(num_target, num_predicted, pp, pr, pf1, description)
  return(line)
}










# Read in the command line arguments.
# 1 (string) Filename with complete path to send the the summary file of these annotations to.
# 2 (string) Description of what these evaluation files are for.
# 3 (string) Directory that contains all the files passed in.
# 4-k (strings) Complete the paths to each of the files to be read.
args <- commandArgs(trailingOnly=T)
output_file <- args[1]
desc <- args[2]
dir <- args[3]
filenames <- args[4:length(args)]




# Create a summary file for each input annotation file.
cols <- c("num_target", "num_predicted", "pp", "pr", "pf1", "description")
table <- data.frame(matrix(ncol=length(cols), nrow=0))
colnames(table) <- cols

for (filename in filenames){
  row_values <- table_row(dir, filename, desc)
  table[nrow(table)+1,] <- row_values
}

table$num_target <- as.numeric(table$num_target)
table$num_predicted <- as.numeric(table$num_predicted)
table$pp <- as.numeric(table$pp)
table$pr <- as.numeric(table$pr)
table$pf1 <- as.numeric(table$pf1)

# Aggregated values for this described method.
num_target <- sum(table$num_target)
num_predicted <- sum(table$num_predicted)
pp <- (sum(table$pp * table$num_predicted) / num_predicted)
pr <- (sum(table$pr * table$num_target)/ num_target)
pf1 <- (2*pp*pr)/(pp+pr)
description <- "aggregated version of the above for all ontologies"
table[nrow(table)+1,] <- c(num_target, num_predicted, pp, pr, pf1, description)
write.csv(table, output_file, row.names=FALSE)












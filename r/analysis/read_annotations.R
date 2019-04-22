


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

  line = paste(n,pp,pr,pf1,description,sep=",")
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
sink(output_file)
for (filename in filenames){
  cat(paste(table_row(dir,filename,desc),"\n",sep=""))
}
closeAllConnections()
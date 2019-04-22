


source("/Users/irbraun/NetBeansProjects/term-mapping/r/analysis/utils.R")



# Creates a new file with normalized scores in the prob 
# dir: a directory
# filename: a csv file in that directory with a 'prob' column.
adjust_range_in_file <- function(dir,filename){
  df <- read(dir,filename)
  df$prob <- range01(df$prob)
  df$prob <- round(df$prob, 3)
  path <- paste(dir,filename,sep="")
  path <- substring(path,1,nchar(path)-4)
  path <- paste(path,".normalized.csv",sep="")
  write.csv(df, file=path, row.names=F)
  l <<- c(l,sum(is.na(df$prob)))
}



# Read in the set of files passed in as arguments and normalize the probabilities column.
# The files that are passed in should be the x_classprobs.csv files.
# Arguments
# 1 (string) directory
# 2 (string) filename
# ...
# k (string) filename
args <- commandArgs(trailingOnly=T)
dir <- args[1]
for (i in 2:length(args)){
  filename <- args[i]
  adjust_range_in_file(dir,filename)
}
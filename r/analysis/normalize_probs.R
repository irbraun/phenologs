
# Normalization function for numerical values.
range01 <- function(x, ...){(x - min(x, ...)) / (max(x, ...) - min(x, ...))}


read <- function(dir,filename){
  d <- read.csv(file=paste(dir,filename,sep=""), header=T, sep=",")
  return(d)
}

normalize_file <- function(dir,filename){
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
# n (string) filename

l <- c()
args <- commandArgs(trailingOnly=T)
dir <- args[1]
for (i in 2:length(args)){
  filename <- args[i]
  normalize_file(dir,filename)
}
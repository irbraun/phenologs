


source("r/lib/utils.R")






# Creates a new file with adjusted range scores in the prob 
# dir: a directory
# filename: a csv file in that directory with a 'prob' column or 'score' column.
adjust_range_in_file <- function(dir,filename){
  df <- read(dir,filename)
  # The classprob files.
  if("prob" %in% colnames(df)){
    df$prob <- range01(df$prob)
    df$prob <- round(df$prob, 3)
    # Dissallowing blank strings in the node column to be replaced by NA.
    # Unclear on why this is necesary, script wasn't automatically replacing the blank strings in the other files.
    df[is.na(df)] <- ""
  }
  # The eval files.
  else if("score" %in% colnames(df)){
    df[df$score!="none",]$score <-round(range01(as.numeric(df[df$score!="none",]$score)),3)
  }
  # Something wrong with the table.
  else{
    cat("not able to adjust range in file")
    quit()
  }
  path <- paste(dir,filename,sep="")
  # Only do the commented out section if don't want to adjust file in place.
  #path <- substring(path,1,nchar(path)-4)
  #path <- paste(path,".0to1.csv",sep="")
  write.csv(df, file=path, row.names=F)
}










# Read in the set of files passed in as arguments and adjust range in the probabilities column.
# The files that are passed in should be the [info]_classprobs.csv or [info]_eval.csv files.
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



source("/Users/irbraun/NetBeansProjects/term-mapping/r/analysis/utils.R")






# Creates a new file with adjusted range scores in the prob 
# dir: a directory
# filename: a csv file in that directory with a 'prob' column or 'score' column.
adjust_range_in_file <- function(dir,filename){
  df <- read(dir,filename)
  
  if("prob" %in% colnames(df))
  {
    df$prob <- range01(df$prob)
    df$prob <- round(df$prob, 3)
    path <- paste(dir,filename,sep="")
    # Commented out to alter the file in place.
    #path <- substring(path,1,nchar(path)-4)
    #path <- paste(path,".0to1.csv",sep="")
    write.csv(df, file=path, row.names=F)
  }
  else if("score" %in% colnames(df)){
    df$score <- range01(df$score)
    df$score <- round(df$score, 3)
    path <- paste(dir,filename,sep="")
    # Commmented out to alter the file in place.
    #path <- substring(path,1,nchar(path)-4)
    #path <- paste(path,".0to1.csv",sep="")
    write.csv(df, file=path, row.names=F)
  }
  else{
    cat("not able to adjust range in file")
    quit()
  }
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


# Normalization function for numerical scores.
range01 <- function(x, ...){(x - min(x, ...)) / (max(x, ...) - min(x, ...))}


# Function for creating a string for input into a table.
read <- function(dir,filename){
  # Read in the data.
  d <- read.csv(file=paste(dir,filename,sep=""), header=T, sep=",")
  return(d)
}


# Function for creating a string for input into a table.
table_row <- function(dir,filename,description){
  # Read in the data.
  d <- read.csv(file=paste(dir,filename,sep=""), header=T, sep=",")
  
  # Numerical data that is missing was marked as na in the file.
  d[d=="na"] <- NA
  
  # Check this stuff, only applies when a score was return from the concept mapping method.
  #d$score <- as.numeric(as.character(d$score))
  #d$score <- range01(d$score, na.rm=T)
  #d[d$category%in%c("FN"),"score"] <- NA
  #threshold = 0.5
  #d <- d[(d$score >= threshold | is.na(d$score)),]
  
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




args <- commandArgs(trailingOnly=T)
# 1 File to send the output of reading the annotations to.
# 2 Description of what these evaluation files are for.
# 3 Directory that contains all the files passed in.
# 4-k Complete the paths to each of the files to be read.
output_file <- args[1]
desc <- args[2]
dir <- args[3]
filenames <- args[4:length(args)]







sink(output_file)
#cat(paste("n,pp,pr,pf1,description\n"))
for (filename in filenames){
  cat(paste(table_row(dir,filename,desc),"\n",sep=""))
}
  
closeAllConnections()

# Normalization function for numerical scores.
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



l <- c()


# Files that need to be normalized from first testing set.
dir <- "/Users/irbraun/Desktop/droplet/alpha2/nlp/nb/set1/"
f1 <- "outputs_test_ppn_pato/name.fold.classprobs.csv"
f2 <- "outputs_test_ppn_po/name.fold.classprobs.csv"
f3 <- "outputs_test_ppn_go/name.fold.classprobs.csv"
f4 <- "outputs_test_ppn_chebi/name.fold.classprobs.csv"
normalize_file(dir,f1)
normalize_file(dir,f2)
normalize_file(dir,f3)
normalize_file(dir,f4)
dir <- "/Users/irbraun/Desktop/droplet/alpha2/nlp/me/set1/"
f1 <- "outputs_test_ppn_pato/name.fold.classprobs.csv"
f2 <- "outputs_test_ppn_po/name.fold.classprobs.csv"
f3 <- "outputs_test_ppn_go/name.fold.classprobs.csv"   ##
f4 <- "outputs_test_ppn_chebi/name.fold.classprobs.csv" ###
normalize_file(dir,f1)
normalize_file(dir,f2)
normalize_file(dir,f3)
normalize_file(dir,f4)

# Files that need to be normalized from second testing set.
dir <- "/Users/irbraun/Desktop/droplet/alpha2/nlp/nb/set2/"
f1 <- "outputs_test_ppn_pato/name.fold.classprobs.csv"
f2 <- "outputs_test_ppn_po/name.fold.classprobs.csv"
f3 <- "outputs_test_ppn_go/name.fold.classprobs.csv"
f4 <- "outputs_test_ppn_chebi/name.fold.classprobs.csv"
normalize_file(dir,f1)
normalize_file(dir,f2)
normalize_file(dir,f3)
normalize_file(dir,f4)
dir <- "/Users/irbraun/Desktop/droplet/alpha2/nlp/me/set2/"
f1 <- "outputs_test_ppn_pato/name.fold.classprobs.csv"
f2 <- "outputs_test_ppn_po/name.fold.classprobs.csv"
f3 <- "outputs_test_ppn_go/name.fold.classprobs.csv"  ###
f4 <- "outputs_test_ppn_chebi/name.fold.classprobs.csv"
normalize_file(dir,f1)
normalize_file(dir,f2)
normalize_file(dir,f3)
normalize_file(dir,f4)






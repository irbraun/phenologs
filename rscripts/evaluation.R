

# Normalization function for numerical scores.
range01 <- function(x, ...){(x - min(x, ...)) / (max(x, ...) - min(x, ...))}


# Function for creating a string for input into a table.
read <- function(dir,filename){
  # Read in the data.
  d <- read.csv(file=paste(dir,filename,sep=""), header=T, sep=",")
  return(d)
}


# Function for creating a string for input into a table.
table_row <- function(dir,filename){
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
  
  
  

  line = paste(n,pp,pr,pf1,sep=",")
  return(line)
}



# Function for finding an appropriate threshold for this method.
find_threshold <- function(dir,filename,ratio){
  # Read in the data.
  d <- read.csv(file=paste(dir,filename,sep=""), header=T, sep=",")
  
  # Numerical data that is missing was marked as na in the file.
  d[d=="na"] <- NA
  d[d$category%in%c("FN"),"score"] <- NA
  d$score <- as.numeric(as.character(d$score))
  
  # Total number of chunks in the testing set.
  num_chunks <- length(unique(d$chunk))

  # What's the expected number of terms in this ontology?
  expected <- num_chunks*ratio
  
  # What threshold on the predicted term's scores would leave that many?
  d_pred <- d[d$category %in% c("FP","TP"),]
  sorted_pred_scores <- sort(d_pred$score, decreasing=T, na.last=NA)
  threshold <- sorted_pred_scores[ceiling(expected)]
  return(threshold)
}




# Get a csv with metrics for a particular semantic annotation method.
sink("/Users/irbraun/Desktop/metrics.csv")


# testing the word embedding stuff for the nb model.
cat(paste("\nn,pp,pr,pf1\n"))
dir <- "/Users/irbraun/NetBeansProjects/term-mapping/path/annotators/noble/"
f1 <- "output_pato/group2_eval.csv"
f2 <- "output_po/group2_eval.csv"
cat(paste(table_row(dir,f1),"\n",sep=""))
cat(paste(table_row(dir,f2),"\n",sep=""))

closeAllConnections()









 

# The species testing set.

cat(paste("n,pp,pr,pf1\n"))
dir <- "/Users/irbraun/Desktop/droplet/alpha2/nlp/na/"
f1 <- "outputs_species_ppn_pato/name.fold.eval.csv"
f2 <- "outputs_species_ppn_po/name.fold.eval.csv"
f3 <- "outputs_species_ppn_go/name.fold.eval.csv"
f4 <- "outputs_species_ppn_chebi/name.fold.eval.csv"
cat(paste(table_row(dir,f1),"\n",sep=""))
cat(paste(table_row(dir,f2),"\n",sep=""))
cat(paste(table_row(dir,f3),"\n",sep=""))
cat(paste(table_row(dir,f4),"\n",sep=""))
dir <- "/Users/irbraun/Desktop/droplet/alpha2/nlp/nc/"
f1 <- "outputs_species_ppn_pato/name.fold.eval.csv"
f2 <- "outputs_species_ppn_po/name.fold.eval.csv"
f3 <- "outputs_species_ppn_go/name.fold.eval.csv"
cat(paste(table_row(dir,f1),"\n",sep=""))
cat(paste(table_row(dir,f2),"\n",sep=""))
cat(paste(table_row(dir,f3),"\n",sep=""))
dir <- "/Users/irbraun/Desktop/droplet/alpha2/nlp/nb/set1/"
f1 <- "outputs_test_ppn_pato/name.fold.eval.csv"
f2 <- "outputs_test_ppn_po/name.fold.eval.csv"
f3 <- "outputs_test_ppn_go/name.fold.eval.csv"
f4 <- "outputs_test_ppn_chebi/name.fold.eval.csv"
cat(paste(table_row(dir,f1),"\n",sep=""))
cat(paste(table_row(dir,f2),"\n",sep=""))
cat(paste(table_row(dir,f3),"\n",sep=""))
cat(paste(table_row(dir,f4),"\n",sep=""))
dir <- "/Users/irbraun/Desktop/droplet/alpha2/nlp/me/set1/"
f1 <- "outputs_test_ppn_pato/name.fold.eval.csv"
f2 <- "outputs_test_ppn_po/name.fold.eval.csv"
f3 <- "outputs_test_ppn_go/name.fold.eval.csv"
f4 <- "outputs_test_ppn_chebi/name.fold.eval.csv"
cat(paste(table_row(dir,f1),"\n",sep=""))
cat(paste(table_row(dir,f2),"\n",sep=""))
cat(paste(table_row(dir,f3),"\n",sep=""))
cat(paste(table_row(dir,f4),"\n",sep=""))
dir <- "/Users/irbraun/Desktop/droplet/alpha2/nlp/aggregate/set1/"
f1 <- "outputs_species_ppn_pato/name.fold.eval.csv"
f2 <- "outputs_species_ppn_po/name.fold.eval.csv"
f3 <- "outputs_species_ppn_go/name.fold.eval.csv"
f4 <- "outputs_species_ppn_chebi/name.fold.eval.csv"
cat(paste(table_row(dir,f1),"\n",sep=""))
cat(paste(table_row(dir,f2),"\n",sep=""))
cat(paste(table_row(dir,f3),"\n",sep=""))
cat(paste(table_row(dir,f4),"\n",sep=""))




# The random testing set.

dir <- "/Users/irbraun/Desktop/droplet/alpha2/nlp/na/"
f1 <- "outputs_random_ppn_pato/name.fold.eval.csv"
f2 <- "outputs_random_ppn_po/name.fold.eval.csv"
f3 <- "outputs_random_ppn_go/name.fold.eval.csv"
f4 <- "outputs_random_ppn_chebi/name.fold.eval.csv"
cat(paste(table_row(dir,f1),"\n",sep=""))
cat(paste(table_row(dir,f2),"\n",sep=""))
cat(paste(table_row(dir,f3),"\n",sep=""))
cat(paste(table_row(dir,f4),"\n",sep=""))
dir <- "/Users/irbraun/Desktop/droplet/alpha2/nlp/nc/"
f1 <- "outputs_random_ppn_pato/name.fold.eval.csv"
f2 <- "outputs_random_ppn_po/name.fold.eval.csv"
f3 <- "outputs_random_ppn_go/name.fold.eval.csv"
cat(paste(table_row(dir,f1),"\n",sep=""))
cat(paste(table_row(dir,f2),"\n",sep=""))
cat(paste(table_row(dir,f3),"\n",sep=""))
dir <- "/Users/irbraun/Desktop/droplet/alpha2/nlp/nb/set2/"
f1 <- "outputs_test_ppn_pato/name.fold.eval.csv"
f2 <- "outputs_test_ppn_po/name.fold.eval.csv"
f3 <- "outputs_test_ppn_go/name.fold.eval.csv"
f4 <- "outputs_test_ppn_chebi/name.fold.eval.csv"
cat(paste(table_row(dir,f1),"\n",sep=""))
cat(paste(table_row(dir,f2),"\n",sep=""))
cat(paste(table_row(dir,f3),"\n",sep=""))
cat(paste(table_row(dir,f4),"\n",sep=""))
dir <- "/Users/irbraun/Desktop/droplet/alpha2/nlp/me/set2/"
f1 <- "outputs_test_ppn_pato/name.fold.eval.csv"
f2 <- "outputs_test_ppn_po/name.fold.eval.csv"
f3 <- "outputs_test_ppn_go/name.fold.eval.csv"
f4 <- "outputs_test_ppn_chebi/name.fold.eval.csv"
cat(paste(table_row(dir,f1),"\n",sep=""))
cat(paste(table_row(dir,f2),"\n",sep=""))
cat(paste(table_row(dir,f3),"\n",sep=""))
cat(paste(table_row(dir,f4),"\n",sep=""))
dir <- "/Users/irbraun/Desktop/droplet/alpha2/nlp/aggregate/set2/"
f1 <- "outputs_random_ppn_pato/name.fold.eval.csv"
f2 <- "outputs_random_ppn_po/name.fold.eval.csv"
f3 <- "outputs_random_ppn_go/name.fold.eval.csv"
f4 <- "outputs_random_ppn_chebi/name.fold.eval.csv"
cat(paste(table_row(dir,f1),"\n",sep=""))
cat(paste(table_row(dir,f2),"\n",sep=""))
cat(paste(table_row(dir,f3),"\n",sep=""))
cat(paste(table_row(dir,f4),"\n",sep=""))

closeAllConnections()










# These thresholds are found using ratios which are calculated from the training data.
# Have to run the test in the jar which finds these for each split and then copy over.
# For the species split.
find_threshold(dir,f1,1.11)
find_threshold(dir,f2,1.37)
find_threshold(dir,f3,0.24)
find_threshold(dir,f4,0.07)
# For the random split.
find_threshold(dir,f1,1.27)
find_threshold(dir,f2,1.03)
find_threshold(dir,f3,0.22)
find_threshold(dir,f4,0.12)





























################## using everything above this #################################






t <- find_threshold(dir,f1)# Read in the data.
d <- read.csv(file=paste(dir,"chebi_test.fold1.eval.csv",sep=""), header=T, sep=",")

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


d$score <- as.numeric(as.character(d$score))
d[d$category%in%c("FN"),"score"] <- NA

a <- sort(d$score,decreasing=T)











#rank similarity between sets of recall values





# Thresholding using the scores that were returned with each prediction.
results <- data.frame(matrix(ncol = 4, nrow = 0))
cols <- c("threshold","pp","pr","pf1")
colnames(results) <- cols

# Make sure that false negatives rows are represented as missing data.
d[d$category%in%c("FN"),"score"] <- NA
d$score <- as.numeric(as.character(d$score))

min_score <- min(d$score, na.rm=T)
max_score <- max(d$score, na.rm=T)
num_steps <- 100
step_size <- (max_score-min_score)/num_steps
thresholds <- seq(min_score,max_score,step_size)

for (t in thresholds){
  #apply the threshold
  dt <- d
  dt[(dt$score<t) & (dt$category %in% c("TP")),]$category <- "FN" # (update the tp to fn)
  dt[(dt$score<t) & (dt$category %in% c("TP")),]$similarity <- "FN" # (update the tp to fn)
  dt <- dt[(dt$score >= t | dt$category %in% c("FN")),] # (keep all the fn even if they have a score of 1 in the df)
  
  #calculate the new metrics
  tp <- nrow(dt[dt$category %in% c("TP"),])
  fp <- nrow(dt[dt$category %in% c("FP"),])
  fn <- nrow(dt[dt$category %in% c("FN"),])
  p <- tp/(tp+fp)
  r <- tp/(tp+fn)
  f1 <- (2*p*r)/(p+r)
  sum <- sum(dt[dt$category %in% c("TP","FN"),]$similarity)
  avg <- sum/(tp+fn)
  pr <- avg
  sum <- sum(dt[dt$category %in% c("TP","FP"),]$similarity)
  avg <- sum/(tp+fp)
  pp <- avg
  pf1 <- (2*pp*pr)/(pr+pp)
  #write to a new output row
  results[nrow(results)+1,] <- c(t, fn+tp, pr, pf1)
}

# problem here is that as thresholding continues some TP actually become FN 
# sanity check is that obviously n=TP+FN should never change.



# problem
# when t is applied a bunch of columns have to change.
# for FN, sim is defined as the similarity to the closest prediction, 0 if none predicted.
# for TP, sim is defined as 1
# for FP, sim is defined as similarity to the closest TP or FN (curated term).

# when applying the thresholds,
# FN will always stay as FN, but the similarity has to be updated to reflect similarity to closest TP or FP  term, or 0 if there are none of those now.
# TP can become a FN, in which case similarity has to do the above.
# FP may be deleted, or may be kept. similarity never has to be updated.


# how to make this happen?
# 1. remove FP that don't satisfy the threshold.
# 2. change the category of TP to be FN if they don't satisfy the threshold.
# 3. for each chunk: if num of TP+FP=0, then similarity of all FN <- 0.
# 3. for each chunk, if num of TP+FP!=0, then similarity of each FN <- 


































# Optimizing stuff.
results <- data.frame(matrix(ncol = 5, nrow = 0))
cols <- c("threshold","f1","precision","recall","avghf1")
colnames(results) <- cols



thresholds <- seq(0.7,1,0.005)
for (t in thresholds){
  sub <- d[d$score>=t,]
  tp <- nrow(sub[sub$category %in% c("TP"),])
  fp <- nrow(sub[sub$category %in% c("FP"),])
  fn <- nrow(sub[sub$category %in% c("FN"),])
  p <- tp/(tp+fp)
  r <- tp/(tp+fn)
  f1 <- (2*p*r)/(p+r)
  
  pos <- sub[sub$category %in% c("TP","FP"),]
  avghf1 <- mean(pos$similarity)
  
  results[nrow(results)+1,] <- c(t, f1, p, r, avghf1)
}





# look at 
plot(x=results$threshold, y=results$avghf1)
plot(x=results$threshold, y=results$f1)
plot(x=results$threshold, y=results$precision)
#write.csv(results, file=output_file, row.names=F)


















# for the combined stuff (the composer stuff)
# for the actual output 
d <- read.csv(file="/Users/irbraun/Desktop/droplet/alpha2/output/test.csv",header=T,sep=",")
hist(d$phenotype_sim)
hist(d$phene_sim)

d$terms <- range01(d$terms,na.rm=T)

d <- d %>% group_by(phene_id)  %>% filter(terms == max(terms)) %>% filter(1:n() == 1)
d <- d[d$terms>0.8,]

hist(d$phene_sim)


















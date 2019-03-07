library(tidyr)
library(plyr)
library(dplyr)
library(ggplot2)
library(lattice)


# Function for creating a string for input into a table.
read <- function(dir,filename){
  # Read in the data.
  d <- read.csv(file=paste(dir,filename,sep=""), header=T, sep=",")
  return(d)
}

get_correlation <- function(df1, df2){
  # Combining the output files for those different methods.
  joint <- full_join(df1, df2, by=c("chunk","term","text","label"))
  # For the correlation table.
  sum(is.na(joint$similarity.x))
  sum(is.na(joint$similarity.y))
  joint <- joint[is.na(joint$similarity.y)==F,]
  joint <- joint[is.na(joint$similarity.x)==F,]
  rho <- cor(joint$similarity.x,joint$similarity.y,method="spearman")
  return(rho)
}



############### set1





# Get the similarity scores for the target terms for one particular method.
dir <- "/Users/irbraun/Desktop/droplet/alpha2/nlp/nb/set1/"
f1 <- "outputs_test_ppn_pato/name.fold.eval.csv"
f2 <- "outputs_test_ppn_po/name.fold.eval.csv"
f3 <- "outputs_test_ppn_go/name.fold.eval.csv"
f4 <- "outputs_test_ppn_chebi/name.fold.eval.csv"
d1 <- read(dir,f1)
d2 <- read(dir,f2)
d3 <- read(dir,f3)
d4 <- read(dir,f4)
d <- rbind(d1,d2,d3,d4)
d <- d[d$category %in% c("TP","FN"),]
nb <- d

dir <- "/Users/irbraun/Desktop/droplet/alpha2/nlp/me/set1/"
f1 <- "outputs_test_ppn_pato/name.fold.eval.csv"
f2 <- "outputs_test_ppn_po/name.fold.eval.csv"
f3 <- "outputs_test_ppn_go/name.fold.eval.csv"
f4 <- "outputs_test_ppn_chebi/name.fold.eval.csv"
d1 <- read(dir,f1)
d2 <- read(dir,f2)
d3 <- read(dir,f3)
d4 <- read(dir,f4)
d <- rbind(d1,d2,d3,d4)
d <- d[d$category %in% c("TP","FN"),]
me <- d

dir <- "/Users/irbraun/Desktop/droplet/alpha2/nlp/na/"
f1 <- "outputs_species_ppn_pato/name.fold.eval.csv"
f2 <- "outputs_species_ppn_po/name.fold.eval.csv"
f3 <- "outputs_species_ppn_go/name.fold.eval.csv"
f4 <- "outputs_species_ppn_chebi/name.fold.eval.csv"
d1 <- read(dir,f1)
d2 <- read(dir,f2)
d3 <- read(dir,f3)
d4 <- read(dir,f4)
d <- rbind(d1,d2,d3,d4)
d <- d[d$category %in% c("TP","FN"),]
na <- d

dir <- "/Users/irbraun/Desktop/droplet/alpha2/nlp/nc/"
f1 <- "outputs_species_ppn_pato/name.fold.eval.csv"
f2 <- "outputs_species_ppn_po/name.fold.eval.csv"
f3 <- "outputs_species_ppn_go/name.fold.eval.csv"
#f4 <- "outputs_species_ppn_chebi/chebi_species.fold.eval.csv"
d1 <- read(dir,f1)
d2 <- read(dir,f2)
d3 <- read(dir,f3)
#d4 <- read(dir,f4)
d <- rbind(d1,d2,d3)
d <- d[d$category %in% c("TP","FN"),]
nc <- d




get_correlation(na,nc)
get_correlation(na,nb)
get_correlation(na,me)
get_correlation(nc,nb)
get_correlation(nc,me)
get_correlation(nb,me)












################ set2





# Get the similarity scores for the target terms for one particular method.
dir <- "/Users/irbraun/Desktop/droplet/alpha2/nlp/nb/set2/"
f1 <- "outputs_test_ppn_pato/name.fold.eval.csv"
f2 <- "outputs_test_ppn_po/name.fold.eval.csv"
f3 <- "outputs_test_ppn_go/name.fold.eval.csv"
f4 <- "outputs_test_ppn_chebi/name.fold.eval.csv"
d1 <- read(dir,f1)
d2 <- read(dir,f2)
d3 <- read(dir,f3)
d4 <- read(dir,f4)
d <- rbind(d1,d2,d3,d4)
d <- d[d$category %in% c("TP","FN"),]
nb <- d

dir <- "/Users/irbraun/Desktop/droplet/alpha2/nlp/me/set2/"
f1 <- "outputs_test_ppn_pato/name.fold.eval.csv"
f2 <- "outputs_test_ppn_po/name.fold.eval.csv"
f3 <- "outputs_test_ppn_go/name.fold.eval.csv"
f4 <- "outputs_test_ppn_chebi/name.fold.eval.csv"
d1 <- read(dir,f1)
d2 <- read(dir,f2)
d3 <- read(dir,f3)
d4 <- read(dir,f4)
d <- rbind(d1,d2,d3,d4)
d <- d[d$category %in% c("TP","FN"),]
me <- d

dir <- "/Users/irbraun/Desktop/droplet/alpha2/nlp/na/"
f1 <- "outputs_random_ppn_pato/name.fold.eval.csv"
f2 <- "outputs_random_ppn_po/name.fold.eval.csv"
f3 <- "outputs_random_ppn_go/name.fold.eval.csv"
f4 <- "outputs_random_ppn_chebi/name.fold.eval.csv"
d1 <- read(dir,f1)
d2 <- read(dir,f2)
d3 <- read(dir,f3)
d4 <- read(dir,f4)
d <- rbind(d1,d2,d3,d4)
d <- d[d$category %in% c("TP","FN"),]
na <- d

dir <- "/Users/irbraun/Desktop/droplet/alpha2/nlp/nc/"
f1 <- "outputs_random_ppn_pato/name.fold.eval.csv"
f2 <- "outputs_random_ppn_po/name.fold.eval.csv"
f3 <- "outputs_random_ppn_go/name.fold.eval.csv"
#f4 <- "outputs_species_ppn_chebi/chebi_species.fold.eval.csv"
d1 <- read(dir,f1)
d2 <- read(dir,f2)
d3 <- read(dir,f3)
#d4 <- read(dir,f4)
d <- rbind(d1,d2,d3)
d <- d[d$category %in% c("TP","FN"),]
nc <- d


get_correlation(na,nc)
get_correlation(na,nb)
get_correlation(na,me)
get_correlation(nc,nb)
get_correlation(nc,me)
get_correlation(nb,me)











# Find the terms that were highly recalled by ml but lowly recalled by string-based methods.


preprocess_ml <- function(){
  dir <- "/Users/irbraun/Desktop/droplet/alpha2/nlp/nb/set1/"
  f1 <- "outputs_test_ppn_pato/name.fold.eval.csv"
  f2 <- "outputs_test_ppn_po/name.fold.eval.csv"
  f3 <- "outputs_test_ppn_go/name.fold.eval.csv"
  f4 <- "outputs_test_ppn_chebi/name.fold.eval.csv"
  d1 <- read(dir,f1)
  d2 <- read(dir,f2)
  d3 <- read(dir,f3)
  d4 <- read(dir,f4)
  d <- rbind(d1,d2,d3,d4)
  d <- d[d$category %in% c("TP","FN"),]
  e <- d
  dir <- "/Users/irbraun/Desktop/droplet/alpha2/nlp/me/set1/"
  f1 <- "outputs_test_ppn_pato/name.fold.eval.csv"
  f2 <- "outputs_test_ppn_po/name.fold.eval.csv"
  f3 <- "outputs_test_ppn_go/name.fold.eval.csv"
  f4 <- "outputs_test_ppn_chebi/name.fold.eval.csv"
  d1 <- read(dir,f1)
  d2 <- read(dir,f2)
  d3 <- read(dir,f3)
  d4 <- read(dir,f4)
  d <- rbind(d1,d2,d3,d4)
  d <- d[d$category %in% c("TP","FN"),]
  
  # Combining the output files for those different methods.
  joint <- full_join(e, d, by=c("chunk","term","text","label"))
  
  # Remove missing stuff.
  sum(is.na(joint$similarity.x))
  sum(is.na(joint$similarity.y))
  joint <- joint[is.na(joint$similarity.y)==F,]
  joint <- joint[is.na(joint$similarity.x)==F,]

  return(joint)
}

preprocess_st <- function(){
  
  dir <- "/Users/irbraun/Desktop/droplet/alpha2/nlp/na/"
  f1 <- "outputs_species_ppn_pato/name.fold.eval.csv"
  f2 <- "outputs_species_ppn_po/name.fold.eval.csv"
  f3 <- "outputs_species_ppn_go/name.fold.eval.csv"
  f4 <- "outputs_species_ppn_chebi/name.fold.eval.csv"
  d1 <- read(dir,f1)
  d2 <- read(dir,f2)
  d3 <- read(dir,f3)
  d4 <- read(dir,f4)
  d <- rbind(d1,d2,d3,d4)
  d <- d[d$category %in% c("TP","FN"),]
  e <- d
  dir <- "/Users/irbraun/Desktop/droplet/alpha2/nlp/nc/"
  f1 <- "outputs_species_ppn_pato/name.fold.eval.csv"
  f2 <- "outputs_species_ppn_po/name.fold.eval.csv"
  f3 <- "outputs_species_ppn_go/name.fold.eval.csv"
  #f4 <- "outputs_species_ppn_chebi/chebi_species.fold.eval.csv"
  d1 <- read(dir,f1)
  d2 <- read(dir,f2)
  d3 <- read(dir,f3)
  #d4 <- read(dir,f4)
  d <- rbind(d1,d2,d3)
  d <- d[d$category %in% c("TP","FN"),]
  
  # Combining the output files for those different methods.
  joint <- full_join(e, d, by=c("chunk","term","text","label"))
  
  
  # Remove missing stuff.
  sum(is.na(joint$similarity.x))
  sum(is.na(joint$similarity.y))
  joint <- joint[is.na(joint$similarity.y)==F,]
  joint <- joint[is.na(joint$similarity.x)==F,]

  return(joint)
  
}


joint <- preprocess_ml()
joint$similarity <- joint$similarity.x+joint$similarity.y
joint$similarity <- joint$similarity / 2
joint$similarity <- pmax(joint$similarity.x,joint$similarity.y)
joint <- joint[,c(1:4,15)]
ml <- joint

joint <- preprocess_st()
joint$similarity <- joint$similarity.x+joint$similarity.y
joint$similarity <- joint$similarity / 2
joint$similarity <- pmax(joint$similarity.x,joint$similarity.y)
joint <- joint[,c(1:4,15)]
st <- joint

# Combining the output files for those different methods.
joint <- full_join(ml, st, by=c("chunk","term","text","label"))


# Organize target terms by which methods picked them up.
high <- 0.75
low <- 0.25
both_good <- joint[joint$similarity.x>=high & joint$similarity.y>=high,]
mlm_good <- joint[joint$similarity.x>=high & joint$similarity.y<low,]
str_good <- joint[joint$similarity.x<low & joint$similarity.y>=high,]
both_bad <- joint[joint$similarity.x<low & joint$similarity.y<low,]

# problem is that this still has one entry per each occurence of a target term.
# want each term to only be placed in one bin, so it's average for the similarity
# values from each method should be obtained for all occurences of the term.
# Alternatively, check which terms are only present in one of those categories for example.
# i.e. term X is only recalled well by this one method all the time, or normally.

g <- aggregate(data.frame(count = mlm_good$label), list(value = mlm_good$label), length)
h <- aggregate(data.frame(count = str_good$label), list(value = str_good$label), length)
b <- aggregate(data.frame(count = both_good$label), list(value = both_good$label), length)

# ^looks at how often terms appear in the sets, i.e. the most frequently occuring terms in the 
# str_good set are things like, ChEBI and PO terms (fruit, seed, endosperm, yellow, green).
# This gets at which terms go where, does it get at the actual associations that are being made between terms and words?
# for maximum entropy: that comes from the learned weights of each of the fetaure (lambda value)
# for the naive bayes: that comes from the frequencies during training.

# so for each of those terms, what are the words that are getting high frequency scores?
# eg T="whole plant" w={?(p=?), ?(p=?), ...} (are those words even interesting though, or are those terms just coming from the strong priors)



m <- aggregate(data.frame(count = e$label), list(value = e$label), length)













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

# Naive Bayes files
dir <- "/Users/irbraun/Desktop/droplet/path/annotators/naive/"
df <- rbind(read(dir,"output_pato/merged_phene_eval.csv"), read(dir,"output_po/merged_phene_eval.csv"), read(dir,"output_go/merged_phene_eval.csv"), read(dir,"output_chebi/merged_phene_eval.csv"))
df <- df[df$category %in% c("TP","FN"),]
nb <- df


# NCBO Annotator files
dir <- "/Users/irbraun/Desktop/droplet/path/annotators/ncbo/"
df <- rbind(read(dir,"output_pato/group1_phene_eval.csv"), read(dir,"output_po/group1_phene_eval.csv"), read(dir,"output_go/group1_phene_eval.csv"))
df <- df[df$category %in% c("TP","FN"),]
na <- df


# NOBLE Coder files
dir <- "/Users/irbraun/Desktop/droplet/path/annotators/noble/"
df <- rbind(read(dir,"output_pato/group1_phene_eval.csv"), read(dir,"output_po/group1_phene_eval.csv"), read(dir,"output_go/group1_phene_eval.csv"))
df <- df[df$category %in% c("TP","FN"),]
nc <- df


# In between similar methods
get_correlation(na,nc)
# Between those and the word frequency method.
get_correlation(na,nb)
get_correlation(nc,nb)






# Combining string-based methods
joint <- full_join(nc, na, by=c("chunk","term","text","label"))
# Remove missing stuff.
sum(is.na(joint$similarity.x))
sum(is.na(joint$similarity.y))
joint <- joint[is.na(joint$similarity.y)==F,]
joint <- joint[is.na(joint$similarity.x)==F,]
# Find the max similarity to target term obtained.
joint$similarity <- pmax(joint$similarity.x,joint$similarity.y)
joint <- joint[,c(1:4,15)]
st <- joint




# Combine the word frequency methods
joint <- nb
joint <- joint[,c(1:4,8)]
ml <- joint


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













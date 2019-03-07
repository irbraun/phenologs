library(tidyr)
library(plyr)
library(dplyr)
library(ggplot2)
library(lattice)



prep <- function(path, method_name){
  d <- read.csv(path, header=T, sep=",")
  # Look at only rows containing curated terms.
  d <- d[d$category %in% c("TP","FN"),]
  # Rename the similarity column to reflect this method name.
  names(d)[names(d) == "similarity"] <- method_name
  d$chunk <- as.numeric(d$chunk)
  return(d)
}

f1 <- "/Users/irbraun/NetBeansProjects/term-mapping/dmom/NobleCoder/outputs_test_ppn_pato/eval.csv"
f2 <- "/Users/irbraun/Desktop/droplet/alpha2/nlp/nb/outputs_test_ppn_pato/eval.csv"
f3 <- "/Users/irbraun/Desktop/droplet/alpha2/nlp/fm/outputs_test_ppn_pato/eval.csv"
f4 <- "/Users/irbraun/Desktop/droplet/alpha2/rf/set3_ppn_spc_pato/test_eval.csv"


num_methods = 4

fm_name="d"
nc_name="c"
nb_name="a"
rf_name="b"

d1 <- prep(f1,nc_name)
d2 <- prep(f2,nb_name)
d3 <- prep(f3,fm_name)
d4 <- prep(f4,rf_name)


# Can only do one join at a time?
joint <- full_join(d1,d2,by=c("chunk","term"))
joint <- full_join(joint,d3,by=c("chunk","term"))
joint <- full_join(joint,d4,by=c("chunk","term"))
joint$index <- paste(joint$chunk,joint$term,sep=".")

m <- joint %>% select(fm_name, nc_name, nb_name, rf_name, index)
m <- m[with(m, order(-m$d)),] # "d" is referrring to value of variable fm_name.
 
m.t <- data.frame(t(m[-(num_methods+1)]))
colnames(m.t) <- m[,(num_methods+1)]
m.t <- cbind(rownames(m.t),m.t)
names(m.t)[names(m.t)=="rownames(m.t)"] <- "method"




hm_long <- gather(m.t,x_index,Value,2:ncol(m.t),factor_key=TRUE)

# The rows are organized alphabetically by the names in breaks.
ggplot(hm_long, aes(x_index, method)) + geom_raster(aes(fill = Value)) + scale_fill_gradient(low="white",high="darkblue", name="Similarity") +
  labs(x="Curator-Annotated Quality Terms",y="Annotation Method") +
  scale_x_discrete(expand=c(0, 0)) +
  #scale_y_discrete(expand=c(0, 0), breaks=c("d","c","a","b"), labels=c("Fuzzy Matching", "NOBLE-Coder", "Naive Bayes", "Random Forests")) +
  scale_y_discrete(expand=c(0, 0), breaks=c("d","c","a","b"), labels=c("FM", "NC", "NB", "RF")) +
  theme(axis.text.y = element_text(hjust=0)) +
  theme(axis.text.x = element_blank()) +
  theme(axis.ticks.x = element_blank()) +
  theme(axis.ticks.y = element_blank()) +
  theme(panel.background = element_rect(fill = "#000000"))



# numerical approach instead
cor(m$a, m$b, method = c("spearman"))










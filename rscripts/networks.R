library(ggplot2)
library(tidyr)
library(dplyr)
library(data.table)
library(car)
library(bootstrap)
library(DAAG)
library(kSamples)




read <- function(dir,filename){
  d <- read.csv(file=paste(dir,filename,sep=""), header=T, sep=",")
  return(d)
}




# Make a file for reporting on conservation of edges.
dir <- "/Users/irbraun/Desktop/droplet/alpha2/output/set1/"
file <- "phenotype_network.csv"

dir <- "/Users/irbraun/Desktop/droplet/phenotype-search/networks/"
file <- "phenotype_network_modified_apnews.csv"

d <- read(dir,file)
d$id <- row.names(d)



############## using the predicted EQ edges.
edges <- data.frame(matrix(ncol = 8, nrow = 0))
cols <- c("threshold","n_cur","n_pre","overlap","jaccard","precision","recall","f1")
colnames(edges) <- cols

thresholds <- seq(1,0.5,-0.05)
for (t in thresholds){
  sub_cur <- d[d$c_edge >= t,]$id
  
  
  # What happens to performance if using any cutoff that assumes same number of edges?
  num_to_retain = length(sub_cur)
  sub_pre <- d[order(-d$p_edge),][1:num_to_retain,]$id
  # It goes up a little bit but not by an insane amount.
  
  
  #sub_pre <- d[d$p_edge >= t,]$id
  
  i <- length(intersect(sub_cur,sub_pre))
  u <- length(union(sub_cur,sub_pre))
  jac <- i/u
  num_cur <- length(sub_cur)
  num_pred <- length(sub_pre)
  
  tp <- i
  fp <- num_pred-i
  fn <- num_cur-i
  precision <- tp/(tp+fp)
  recall <- tp/(tp+fn)
  f1 <- (2*precision*recall)/(precision+recall)
  
  jac <- round(jac,3)
  precision <- round(precision,3)
  recall <- round(recall,3)
  f1 <- round(f1,3)
  
  edges[nrow(edges)+1,] <- c(t, num_cur, num_pred, i, jac, precision, recall, f1)
}
write.csv(edges, file="/Users/irbraun/Desktop/edges_regular.csv", row.names=F)





############# using the doc2vec edges.
edges <- data.frame(matrix(ncol = 8, nrow = 0))
cols <- c("threshold","n_cur","n_pre","overlap","jaccard","precision","recall","f1")
colnames(edges) <- cols

thresholds <- seq(1,0.5,-0.05)
for (t in thresholds){
  sub_cur <- d[d$c_edge >= t,]$id
  

  # the metrics are no longer comparable (can't use the same threshold)
  # instead we want to just take as many edges as are used by the curated network at this t and see how we do.
  num_to_retain = length(sub_cur)
  sub_pre <- d[order(d$d2v),][1:num_to_retain,]$id
  
  
  #byDayTime[order(byDayTime$count),][1:10,]
  
  
  
  
  #sub_pre <- d[d$p_edge >= t,]$id
  i <- length(intersect(sub_cur,sub_pre))
  u <- length(union(sub_cur,sub_pre))
  jac <- i/u
  num_cur <- length(sub_cur)
  num_pred <- length(sub_pre)
  
  tp <- i
  fp <- num_pred-i
  fn <- num_cur-i
  precision <- tp/(tp+fp)
  recall <- tp/(tp+fn)
  f1 <- (2*precision*recall)/(precision+recall)
  
  jac <- round(jac,3)
  precision <- round(precision,3)
  recall <- round(recall,3)
  f1 <- round(f1,3)
  
  edges[nrow(edges)+1,] <- c(t, num_cur, num_pred, i, jac, precision, recall, f1)
}
write.csv(edges, file="/Users/irbraun/Desktop/edges_d2v.csv", row.names=F)



























# Edge values from the predicted and curated networks.
d <- read.csv(file="/Users/irbraun/Desktop/droplet/alpha2/output/mixed_network.csv", header=T, sep=",")
type <- "phene"


# Seeing how similar the distributions of pairwise similarities are.
dl <- gather(d,Network,Value,p_edge:c_edge,factor_key = T)
ggplot(dl, aes(x=Value,color=Network)) + geom_density(size=1) +
  coord_cartesian(xlim=c(1,0),ylim = c(0,10)) +
  theme_bw() +
  theme(plot.title = element_text(lineheight=1.0, face="bold", hjust=0.5), panel.grid.major = element_blank(), panel.grid.minor = element_blank(), panel.border = element_blank(), axis.line = element_line(colour = "black")) +
  ylab("density") +
  xlab("network edge value") +
  scale_x_continuous(expand=c(0,0)) +
  scale_y_continuous(expand=c(0,0))
  # + scale_colour_manual()


# Statistical test with null that the two distributions are the same.






# What fraction of the most important edges are preserved?
# d[1:10,]
# d$id <- row.names(d)
# top_c <- d[d$c_edge >= 0.75,]$id
# top_p <- d[d$p_edge >= 0.75,]$id
# i <- length(intersect(top_c,top_p))
# u <- length(union(top_c,top_p))





# notes for the example
#c2 = phene 2544, phenotype 1262:
#c1 = phene 2543, phenotype 1261:     1.0
#r1 = phene 2545, phenotype 2599:     1.0
#b1 = phene 3304, phenotype 1584:     0.551





















# For the mixed network
one <- d[d$group %in% c("one"),]
neither <- d[d$group %in% c("neither"),]
both <- d[d$group %in% c("both"),]

# Regular version for the mixed network.
ggplot(data=d, aes(x=m_edge, y=c_edge, color=group)) + geom_point(shape=20, alpha=0.05) +
  theme_bw() + coord_cartesian(xlim=c(1.01,0),ylim=c(0,1.01)) +
  ggtitle(paste("edge values in",type,"network")) + 
  theme(plot.title = element_text(lineheight=1.0, face="bold", hjust=0.5), panel.grid.major = element_blank(), panel.grid.minor = element_blank(), panel.border = element_blank(), axis.line = element_line(colour = "black")) +
  ylab("human annotation") +
  xlab("machine annotation") + 
  scale_x_continuous(expand=c(0,.000)) +
  scale_y_continuous(expand=c(0,.000))

cor(one$c_edge, one$m_edge, method = c("spearman"))
cor(both$c_edge, both$m_edge, method = c("spearman"))
cor(neither$c_edge, neither$m_edge, method = c("spearman"))














# Regular version.
ggplot(data=d, aes(x=p_edge, y=c_edge)) + geom_point(shape=20, alpha=0.1) +
  theme_bw() + coord_cartesian(xlim=c(1.01,0),ylim=c(0,1.01)) +
  ggtitle(paste("edge values in",type,"network")) + 
  theme(plot.title = element_text(lineheight=1.0, face="bold", hjust=0.5), panel.grid.major = element_blank(), panel.grid.minor = element_blank(), panel.border = element_blank(), axis.line = element_line(colour = "black")) +
  ylab("human annotation") +
  xlab("machine annotation") + 
  scale_x_continuous(expand=c(0,.000)) +
  scale_y_continuous(expand=c(0,.000))

# Jittered version.
ggplot(data=d, aes(x=V1, y=V2)) + geom_jitter(shape=20, alpha=0.1, width=0.01, height=0.01) +
  theme_bw() + coord_cartesian(xlim=c(1.01,0),ylim=c(0,1.01)) +
  ggtitle(paste("edge values in",type,"network")) + 
  theme(plot.title = element_text(lineheight=1.0, face="bold", hjust=0.5), panel.grid.major = element_blank(), panel.grid.minor = element_blank(), panel.border = element_blank(), axis.line = element_line(colour = "black")) +
  ylab("human annotation") +
  xlab("machine annotation") + 
  scale_x_continuous(expand=c(0,.000)) +
  scale_y_continuous(expand=c(0,.000))


cor(d$c_edge, d$p_edge, method = c("spearman"))

# Other things that can be looked at, might be easier in python
# this is a completely unthresholded network.
# are the most important (highest value) edges conserved between the human and automated networks?
# are the central nodes conserved between the human and automated networks (again after thresholding)
# what about a measure of overall network similarity between the (network alignment value)
# (would these be recognizable as the same networks?)


# What about just looking at the most important edges (phenologs)?
cor(d$V1, d$V2, method = c("spearman"))
d <- d[d$V2>=0.5,]


#Kolmogorov–Smirnov test
ks.test(d$V1,d$V2)
x <- rnorm(200)
y <- rnorm(200)
ks.test(x,y)


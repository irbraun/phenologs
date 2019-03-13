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


check_edges <- function(d, output_path, retain_edge_quantity=F, doc2vec=F){
  
  edges <- data.frame(matrix(ncol = 8, nrow = 0))
  cols <- c("threshold","n_cur","n_pre","overlap","jaccard","precision","recall","f1")
  colnames(edges) <- cols
  
  thresholds <- seq(1,0.5,-0.05)
  for (t in thresholds){
    sub_cur <- d[d$c_edge >= t,]$id
  
    # What happens if using any cutoff that assumes same number of edges?
    if(retain_edge_quantity==T){
      num_to_retain = length(sub_cur)
      if(doc2vec==T) sub_pre <- d[order(d$d2v),][1:num_to_retain,]$id
      else sub_pre <- d[order(-d$p_edge),][1:num_to_retain,]$id
    } 
    # What happens if we use that same threshold regradless of how many edges that retains?
    else{
      if(doc2vec==T) sub_pre <- d[d$d2v <= t,]$id
      else sub_pre <- d[d$p_edge >= t,]$id
    }
    
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
  write.csv(edges, file=output_path, row.names=F)
  
}





# The file specifying the network edges.
dir <- "/Users/irbraun/Desktop/droplet/path/networks/"
file <- "phenotype_network_modified.csv"

# Read it in and assign unique IDs to each row.
d <- read(dir,file)
d$id <- row.names(d)

# Which doc2vec model should be used?
colnames(d)[colnames(d)=="enwiki_dbow"] <- "d2v"



# Use different predicted sets of edges and check performance.
check_edges(d, "/Users/irbraun/Desktop/droplet/path/networks/results_regular_edges.csv", retain_edge_quantity=F, doc2vec=F)
check_edges(d, "/Users/irbraun/Desktop/droplet/path/networks/results_matched_edges.csv", retain_edge_quantity=T, doc2vec=F)
check_edges(d, "/Users/irbraun/Desktop/droplet/path/networks/results_d2v_edges.csv", retain_edge_quantity=T, doc2vec=T)






# ___________ using everything above this for now _____________________







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


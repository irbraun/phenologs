#nodes <- read.csv("/Users/irbraun/Downloads/netscix2016/Dataset1-Media-Example-NODES.csv", header=T, as.is=T)
#links <- read.csv("/Users/irbraun/Downloads/netscix2016/Dataset1-Media-Example-EDGES.csv", header=T, as.is=T)

# Format for the edges csv file is from,to,weight,attrib1,attrib2,...
# Format for the nodes csv file is id,attrib1,attrib2,... 
# The entries in the id column of the nodes file should be what is present in the from and to columns 
#  in the edges file.
# Other than that the attributes can be anything.
# It's easier to modify the nodes and links dataframes before converting to a igraph object, if more
#  attributes or functions of the existing attributes need to be added.

set.seed(11)
library(RColorBrewer)


read <- function(dir,filename){
  d <- read.csv(file=paste(dir,filename,sep=""), header=T, sep=",")
  return(d)
}

dir <- "/Users/irbraun/Desktop/droplet/path/networks/"
edges_file <- "phenotype_network.csv"
nodes_file <- "phenotype_nodes.csv"
links <- read(dir,edges_file)
nodes <- read(dir,nodes_file)

get_type <- function(t,p,c) { 
  if(p>=t & c>=t) z <- "both"
  else if(p<t & c>=t) z <- "curated"
  else if(p>=t & c<t) z <- "predicted"
  else z <- "neither"
  return(z)
}

# Threshold and format the file specifying the edges for igraph.
threshold <- 0.9
links$type <- mapply(get_type, threshold, links$p_edge, links$c_edge)
links <- links[links$type!="neither",]
head(links)
names(links)[names(links) == "phenotype_1"] <- "from"
names(links)[names(links) == "phenotype_2"] <- "to"
head(links)

# Only keep nodes that are present in the network.
used_edges <- c(links$from,links$to)
nodes <- nodes[nodes$id %in% used_edges,]


# Add attributes for each edge specifying the network it belongs to.
edge_dict <- function(key){
  values <- c(1,2,3)
  names(values) <- c("both","curated","predicted")
  return(values[key])
}
node_dict <- function(key){
  values <- c(1,2,3,4,5,6)
  names(values) <- c("zea_mays","glycine_max","solanum_lycopersicum","oryza_sativa","medicago_truncatula","arabidopsis_thaliana")
  return(values[key])
}

links$type_idx <- edge_dict(links$type)
nodes$spc_idx <- node_dict(nodes$species) 

# Workaround to get a dataframe of node ID's without a separate file.
# a <- links
# b <- links
# a <- a[,-1]
# b <- b[,-2]
# names(a)[1] <- "id"
# names(b)[1] <- "id"
# c <- rbind(a,b)
# c <- subset(c, !duplicated(c[,1])) 
# nodes <- c

library(igraph)
net <- graph_from_data_frame(d=links, vertices=nodes, directed=T)


edge_colors <- c("black","red","green")

edge_colors <- c(adjustcolor("black",alpha.f=.5), adjustcolor("red",alpha.f=.5), adjustcolor("green",alpha.f=.5))


l <- brewer.pal(10,"Spectral")
node_colors <- c(l[4:6],l[1:3])
#node_colors <- c("black","black","black","black","black","black")
species_colors <- c("?")


V(net)$size <- 5
V(net)$frame.color <- "white"
V(net)$label <- "" 
V(net)$color = node_colors[V(net)$spc_idx]
E(net)$arrow.mode <- 0
E(net)$color = edge_colors[E(net)$type_idx]

E(net)



l <- layout_with_fr(net)

plot(net, layout=l)


plot(net)
legend(x=-1.1, y=-0.6, c("Both","Curated", "Predicted"), pch=21, col="#777777", pt.bg=edge_colors, pt.cex=1.4, cex=.9, bty="n", ncol=1)
legend(x=-3.1, y=-0.6, c("zea_mays","glycine_max","solanum_lycopersicum","oryza_sativa","medicago_truncatula","arabidopsis_thaliana"), pch=21, col="#777777", pt.bg=node_colors, pt.cex=1.4, cex=.9, bty="n", ncol=1)



plot(NULL ,xaxt='n',yaxt='n',bty='n',ylab='',xlab='', xlim=0:1, ylim=0:1)
legend("topleft", c("zea_mays","glycine_max","solanum_lycopersicum","oryza_sativa","medicago_truncatula","arabidopsis_thaliana"), pch=21, col="#777777", pt.bg=node_colors, pt.cex=1.4, cex=.9, bty="n", ncol=1)
legend("topleft", c("Both","Curated", "Predicted"), pch=21, col="#777777", pt.bg=edge_colors, pt.cex=1.4, cex=.9, bty="n", ncol=1)












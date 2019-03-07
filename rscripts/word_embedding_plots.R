library(tidyr)
library(plyr)
library(dplyr)
library(ggplot2)
library(lattice)


df <- read.csv(file="/Users/irbraun/Desktop/nc_thresh0dec7_enwiki_precise_phenes.csv", header=T, sep=",")

df.long <- gather(df, kind, value, precision, recall, f1)

# Produce a plot of each relevant metric as it changes with tolerance for word-embeddings similarity.
ggplot(df.long, aes(x=threshold, y=value, linetype=kind)) + geom_line() +
  theme_bw() + coord_cartesian(xlim=c(1,0),ylim=c(0,1)) +
  xlab("Threshold") + ylab("Metric") +
  theme(plot.title = element_text(lineheight=1.0, face="bold", hjust=0.5), legend.direction = "vertical", legend.position = "right", panel.grid.major = 
          element_blank(), panel.grid.minor = element_blank(), panel.border = element_blank(), axis.line = element_line(colour = "black")) +
  scale_linetype_manual(values=c("solid", "dotted", "dashed"), name="Metrics", breaks=c("precision","recall","f1"), labels=c("Precision","Recall","F1")) +
  scale_x_continuous(expand=c(0,.000)) +
  scale_y_continuous(expand=c(0,.000))
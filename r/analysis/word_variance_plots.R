library(ggplot2)
library(tidyr)
library(dplyr)
library(data.table)
library(car)
library(hashmap)
library(parallel)

source("/Users/irbraun/NetBeansProjects/term-mapping/r/analysis/utils.R")
source("/Users/irbraun/NetBeansProjects/term-mapping/r/analysis/utils_for_subsets.R")


# Annotation results file with threshold column.
DIR <- "/Users/irbraun/NetBeansProjects/term-mapping/tables/"
INFILE <- "tableS1_word_variance.csv"



# Read in the file.
df <- read(DIR, INFILE)




df_long <- gather(df, Metric, Value, PP, PR, PF1)

# Produce a plot of each relevant metric as it changes with tolerance for word-embeddings similarity.
ggplot(df_long, aes(x=Threshold, y=Value, linetype=Metric)) + geom_line() +
  theme_bw() + coord_cartesian(xlim=c(1,0.5),ylim=c(0,1)) +
  xlab("Threshold") + ylab("Metric") +
  #theme(plot.title = element_text(lineheight=1.0, face="bold", hjust=0.5), legend.direction = "vertical", legend.position = "right", panel.grid.major = element_blank(), panel.grid.minor = element_blank(), panel.border = element_blank(), axis.line = element_line(colour = "black")) +
  theme(plot.title = element_text(lineheight=1.0, face="bold", hjust=0.5), legend.direction = "vertical", legend.position = "right", panel.grid.minor = element_blank(), panel.border = element_blank(), axis.line = element_line(colour = "black")) +
  scale_linetype_manual(values=c("solid", "dotted", "dashed"), name="Metrics", breaks=c("PF1","PP","PR"), labels=c("PF1","PP","PR")) +
  scale_x_reverse(expand=c(0,.000)) +
  scale_y_continuous(expand=c(0,.000)) +
  facet_grid(Text ~ Method) +
  theme(panel.spacing = unit(1.0, "lines"))

# Save the image of the plot.
filename <- "/Users/irbraun/Desktop/word_variance.png"
ggsave(filename, plot=last_plot(), device="png", path=NULL, scale=1, width=20, height=10, units=c("cm"), dpi=300, limitsize=TRUE)


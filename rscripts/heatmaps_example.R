library(ggplot2)
library(tidyr)
library(dplyr)





# Maize Heatmap



d <- read.csv(file="/Users/irbraun/Desktop/off-targets/pipeline/mummer/maize_mummer_chr_values.csv", header=TRUE, sep=",")
d_long <- gather(d, Chromosome, Value, Chr1:Chr10, factor_key=TRUE)

ggplot(d_long, aes(Chromosome, Alignment)) + geom_tile(aes(fill = Value), colour = "white") + scale_fill_gradient(low="white",high="green", name="Fraction\nChr. Length") +
  #theme_grey(base_size=20) + labs(x="", y="") + 
  scale_x_discrete(expand=c(0, 0)) +
  scale_y_discrete(expand=c(0, 0)) +
  theme(axis.text.y = element_text(hjust=0))


# Rice Heatmap

d <- read.csv(file="/Users/irbraun/Desktop/off-targets/pipeline/mummer/rice_mummer_chr_values.csv", header=TRUE, sep=",")
d_long <- gather(d, Chromosome, Value, Chr1:Chr12, factor_key=TRUE)

ggplot(d_long, aes(Chromosome, Alignment)) + geom_tile(aes(fill = Value), colour = "white") + scale_fill_gradient(low="white", high="blue", name="Fraction\nChr. Length") +
  #theme_grey(base_size=20) + labs(x="", y="") + 
  scale_x_discrete(expand=c(0, 0)) +
  scale_y_discrete(expand=c(0, 0)) +
  theme(axis.text.y = element_text(hjust=0))
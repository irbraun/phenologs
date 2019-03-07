library(tidyr)
library(dplyr)
library(ggplot2)
library(lattice)

outputroot <- "/Users/irbraun/Desktop/droplet/composer/testing/predictions/"


data.file <- paste(outputroot,"test_preds_A.csv",sep="")
data <- read.csv(file=data.file, header=TRUE, sep=",", stringsAsFactors=FALSE)





#    ...  %>% filter(1:n() == 1)
# dplyr filter method for getting just one member of the filter results, when it doens't matter which.
# This is what would have to be done if we want exactly one best prediction per atomized statement, becuase 
# there will be cases where some predictions will tie for the maximum score, this handles that.





# Histogram for atomized statements as input.
# Any number of predictions above the threshold are kept.
# Only the top prediction below the threshold for each non-covered input are shown 

# Parameters
qc <- 0.67
y_height <- 1000

# Read in all the columns of data that are used, and add a column specifying if the threshold is met.
temp <- data[,c("atomized_statement_id", "phenotype_id", "q", "atomized_statement_sim", "phenotype_sim")]
plot_data <- data.frame(temp)
plot_data$atomized_statement_sim <- as.numeric(plot_data$atomized_statement_sim)
plot_data$phenotype_sim <- as.numeric(plot_data$phenotype_sim)
plot_data$group = ifelse(plot_data$q < qc, "0", "1")

# Throw out all but the highest scoring EQ statements (per input) of the group that did not meet the threshold (minimum needed per input to be in coverage).
# This step should actually not be necessary because that's what the composer outputs, if q_c was specified during that step.
topNotKept <- plot_data[which(plot_data$group==0),] %>% group_by(atomized_statement_id) %>% filter(q==max(q))
topNotKept <- data.frame(topNotKept)

# Modify the plot data to only include the covered input text and the top scoring data points of the input text that wasn't covered.
# Again, this shouldn't be necessary if this step is done by composer.java as well.
plot_data <- plot_data[which(plot_data$group==1),]
plot_data <- rbind(plot_data, topNotKept)

# Show the plot.
ggplot(plot_data, aes(x=atomized_statement_sim, fill=group)) + geom_histogram(boundary=0,binwidth=0.1,colour="black") +
  theme_bw() + 
  coord_cartesian(xlim=c(1,0),ylim=c(0,y_height)) +
  xlab("Similarity (A1,A2)") + 
  ylab("Frequency") +
  theme(plot.title = element_text(lineheight=1.0, face="bold", hjust=0.5), panel.grid.major =
          element_blank(), panel.grid.minor = element_blank(), panel.border = element_blank(), axis.line = element_line(colour = "black")) +
  scale_fill_manual("Quality", breaks=c("0","1"),values=c("white","dimgrey"), labels = c("q < threshold", "q > threshold"))

# Statistics
num_atoms <- length(unique(plot_data[,"atomized_statement_id"]))
num_covered_atoms <- length(unique(plot_data[which(plot_data$group==1),"atomized_statement_id"]))
num_predictions_for_atoms <- length(plot_data[which(plot_data$group==1),"atomized_statement_id"])
num_phenotypes <-length(unique(plot_data[,"phenotype_id"]))
num_covered_phenotypes <- length(unique(plot_data[which(plot_data$group==1),"phenotype_id"]))
num_predictions_for_phens <- length(plot_data[which(plot_data$group==1),"phenotype_id"])
paste(num_predictions_for_atoms,"EQs were assigned to", num_covered_atoms, "atomized statements out of", num_atoms, "atomized statements in total.")
paste(num_predictions_for_phens,"EQs were assigned to", num_covered_phenotypes, "phenotypes out of", num_phenotypes, "phenotypes in total.")


# With respect to phenotype similarity instead.
temp <- plot_data[which(plot_data$group==1),] %>% group_by(phenotype_id) %>% filter(phenotype_sim == max(phenotype_sim))  %>% filter(1:n() == 1)
plot_data <- data.frame(temp)
y_height <- 250
ggplot(plot_data, aes(x=phenotype_sim)) + geom_histogram(boundary=0,binwidth=0.1,colour="black") +
  theme_bw() + 
  coord_cartesian(xlim=c(1,0),ylim=c(0,y_height)) +
  xlab("Similarity (P1,P2)") + 
  ylab("Frequency") +
  theme(plot.title = element_text(lineheight=1.0, face="bold", hjust=0.5), panel.grid.major =
          element_blank(), panel.grid.minor = element_blank(), panel.border = element_blank(), axis.line = element_line(colour = "black"))


















# Histogram for atomized statements as inputs.
# Only the top predicted EQ statement taken for each input atomized statement.
# So only the top are shown for both the covered and not covered inputs.

qc <- 0.85
y_height <- 1000

temp <- data[,c("atomized_statement_id", "phenotype_id", "q", "atomized_statement_sim", "phenotype_sim")] %>% group_by(atomized_statement_id)  %>% filter(q == max(q)) %>% filter(1:n() == 1)
plot_data <- data.frame(temp)
plot_data$atomized_statement_sim <- as.numeric(plot_data$atomized_statement_sim)
plot_data$phenotype_sim <- as.numeric(plot_data$phenotype_sim)
plot_data$group = ifelse(plot_data$q < qc, "0", "1")

topNotKept <- plot_data[which(plot_data$group==0),] %>% group_by(atomized_statement_id) %>% filter(q==max(q)) 
topNotKept <- data.frame(topNotKept)
plot_data <- plot_data[which(plot_data$group==1),]
plot_data <- rbind(plot_data, topNotKept)

ggplot(plot_data, aes(x=atomized_statement_sim, fill=group)) + geom_histogram(boundary=0,binwidth=0.1,colour="black") +
  theme_bw() + 
  coord_cartesian(xlim=c(1,0),ylim=c(0,y_height)) +
  xlab("Jaccard Similarity (between predicted and curated EQ statements)") + 
  ylab("Frequency") +
  theme(plot.title = element_text(lineheight=1.0, face="bold", hjust=0.5), panel.grid.major =
          element_blank(), panel.grid.minor = element_blank(), panel.border = element_blank(), axis.line = element_line(colour = "black")) +
  scale_fill_manual("Quality", breaks=c("0","1"),values=c("white","dimgrey"), labels = c("q < threshold", "q > threshold"))

num_atoms <- length(unique(plot_data[,"atomized_statement_id"]))
num_covered_atoms <- length(unique(plot_data[which(plot_data$group==1),"atomized_statement_id"]))
num_predictions <- length(plot_data[which(plot_data$group==1),"atomized_statement_id"])
paste(num_predictions,"were assigned to", num_covered_atoms, "atomized statements out of", num_atoms, "atomized stmts in total.")

# Statistics
num_atoms <- length(unique(plot_data[,"atomized_statement_id"]))
num_covered_atoms <- length(unique(plot_data[which(plot_data$group==1),"atomized_statement_id"]))
num_predictions_for_atoms <- length(plot_data[which(plot_data$group==1),"atomized_statement_id"])
num_phenotypes <-length(unique(plot_data[,"phenotype_id"]))
num_covered_phenotypes <- length(unique(plot_data[which(plot_data$group==1),"phenotype_id"]))
num_predictions_for_phens <- length(plot_data[which(plot_data$group==1),"phenotype_id"])
paste(num_predictions_for_atoms,"EQs were assigned to", num_covered_atoms, "atomized statements out of", num_atoms, "atomized statements in total.")
paste(num_predictions_for_phens,"EQs were assigned to", num_covered_phenotypes, "phenotypes out of", num_phenotypes, "phenotypes in total.")

# With respect to phenotype similarity instead.
temp <- plot_data[which(plot_data$group==1),] %>% group_by(phenotype_id) %>% filter(phenotype_sim == max(phenotype_sim))  %>% filter(1:n() == 1)
plot_data <- data.frame(temp)
y_height <- 250
ggplot(plot_data, aes(x=phenotype_sim)) + geom_histogram(boundary=0,binwidth=0.1,colour="black") +
  theme_bw() + 
  coord_cartesian(xlim=c(1,0),ylim=c(0,y_height)) +
  xlab("Similarity (P1,P2)") + 
  ylab("Frequency") +
  theme(plot.title = element_text(lineheight=1.0, face="bold", hjust=0.5), panel.grid.major =
          element_blank(), panel.grid.minor = element_blank(), panel.border = element_blank(), axis.line = element_line(colour = "black"))




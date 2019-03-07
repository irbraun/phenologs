library(tidyr)
library(dplyr)
library(ggplot2)
library(lattice)



# read in the csv file with the predicted EQ statements.
data_file <- "/Users/irbraun/Desktop/droplet/composer/testing/predictions/test_preds_C2.csv"
data <- read.csv(file=data_file, header=TRUE, sep=",", stringsAsFactors=FALSE)

# read in the file of learned parameters.
parameter_file <- "/Users/irbraun/Desktop/droplet/composer/training/gridsearches/gs_results_C2.csv"
parameters <- read.csv(file=parameter_file, header=TRUE, sep=",")


# Just a note.
#    ...  %>% filter(1:n() == 1)
# dplyr filter method for getting just one member of the filter results, when it doens't matter which.
# This is what would have to be done if we want exactly one best prediction per atomized statement, becuase 
# there will be cases where some predictions will tie for the maximum score, this handles that.






############# What is the value of the gain function at each value of alpha?

# setup the dataframe of results from this section.
results <- data.frame(matrix(ncol = 8, nrow = 0))
cols <- c("alpha","learned_beta","fixed_beta","change","learned_rho_kept","fixed_rho_kept","learned_rho_all","fixed_rho_all")

colnames(results) <- cols

# pick a distribution to used for the structural probabilities.
data$p2 <- data$p2_4


# loop through the values of alpha included in the parameters file.
datasave <- data
for (row in 1:nrow(parameters)){
  
  # get the parameters learned at this value of alpha.
  alpha <- parameters[row,"alpha"]
  beta <- parameters[row,"beta"]
  qc <- parameters[row,"qc"]
  qc_fixedbeta <- parameters[row,"qc_fixedbeta"]

  
  # LEARNED BETA
  # retain only the highest scoring prediction for each text statement.
  data <- datasave %>% group_by(atomized_statement_id)  %>% filter(q == max(q)) %>% filter(1:n() == 1)
  
  # calculate the quality scores for each prediction.
  data$qq <- -1
  data[which(data$p2 != -1),]$qq <- (beta*data[which(data$p2 != -1),]$p1) + ((1-beta)*data[which(data$p2 != -1),]$p2)
  data[which(data$qq == -1),]$qq <- data[which(data$qq == -1),]$p1
  data$q <- data$qq
  
  # calculate the value of the gain function.
  # coverage
  data$kept <- ifelse(data$q >= qc, 1, 0)
  data$kept <- as.factor(data$kept)
  num_with_kept <- length(unique(data[which(data$kept==1),"atomized_statement_id"]))
  num_total <- length(unique(data[,"atomized_statement_id"]))
  coverage <- num_with_kept/num_total
  # mean similarity
  sims <- as.data.frame(data[which(data$kept==1),c("atomized_statement_sim","q")])
  mean_sim <- mean(sims$atomized_statement_sim)
  if (is.na(mean_sim)){
    mean_sim <- 0
  }
  # combined gain function score
  h <- (alpha*coverage) + ((1-alpha)*mean_sim)
  
  # spearman test
  nlp_rho_all <- cor.test(data$q, data$atomized_statement_sim, method="spearman")$estimate
  nlp_rho_kept <- cor.test(sims$q, sims$atomized_statement_sim, method="spearman")$estimate
  
  
  
  # FIXED BETA
  # reset the data to calculate the gain function if the value of beta is fixed at 1.
  # regain only the highest scoring prediction for each text statement.
  data <- datasave %>% group_by(atomized_statement_id)  %>% filter(p1 == max(p1)) %>% filter(1:n() == 1)
  
  # calculate the value of the gain function.
  # coverage
  data$kept <- ifelse(data$p1 >= qc_fixedbeta, 1, 0)
  data$kept <- as.factor(data$kept)
  num_with_kept <- length(unique(data[which(data$kept==1),"atomized_statement_id"]))
  num_total <- length(unique(data[,"atomized_statement_id"]))
  coverage <- num_with_kept/num_total
  # mean similarity
  sims <- as.data.frame(data[which(data$kept==1),c("atomized_statement_sim","p1")])
  mean_sim <- mean(sims$atomized_statement_sim)
  if (is.na(mean_sim)){
    mean_sim <- 0
  }
  h2 <- (alpha*coverage) + ((1-alpha)*mean_sim)
  
  # spearman test
  non_rho_all <- cor.test(data$p1, data$atomized_statement_sim, method="spearman")$estimate
  non_rho_kept <- cor.test(sims$p1, sims$atomized_statement_sim, method="spearman")$estimate
  
  # add the results for this alpha to the dataframe.
  results[nrow(results)+1,] <- c(alpha, h, h2, h-h2, nlp_rho_kept, non_rho_kept, nlp_rho_all, non_rho_all)
}






# make the plot for comparing the gain function between fixed and learned beta values at different alphas.
results.long <- gather(results,kind,value,learned_beta,fixed_beta,factor_key=TRUE)
ggplot(results.long, aes(x=alpha, y=value, linetype=kind)) + geom_line() +
  theme_bw() + coord_cartesian(xlim=c(0.34,0.56),ylim=c(0.5,1.0)) +
  xlab("alpha") + ylab("gain( )") +
  theme(plot.title = element_text(lineheight=1.0, face="bold", hjust=0.5), legend.direction = "vertical", legend.position = "right", panel.grid.major = 
          element_blank(), panel.grid.minor = element_blank(), panel.border = element_blank(), axis.line = element_line(colour = "black")) +
  scale_linetype_manual(values=c("solid","dashed"), name="parameter", breaks=c("learned_beta","fixed_beta"), labels=c("learned","fixed")) +
  scale_x_continuous(expand=c(0,.000)) +
  scale_y_continuous(expand=c(0,.000))






# Used to test different distributions for differnt features of the paths.
# beta <- 0.87
# y_height <- 1000
# p2_col <- 24
# spearman <- function(col_num, beta, data){
#   
#   # move these probabilitites to a new column and scale.
#   data$p2 <- data[,col_num]
#   oldRange <- max(data$p2)-min(data$p2)
#   newRange <- 1
#   data$p2 <- ((data$p2-min(data$p2)) * newRange/oldRange)
#   
#   # recalculate the quality values based the probabilities and parameter.
#   data$qq <- -1
#   data[which(data$p2 != -1),]$qq <- (beta*data[which(data$p2 != -1),]$p1) + ((1-beta)*data[which(data$p2 != -1),]$p2)
#   data[which(data$qq == -1),]$qq <- data[which(data$qq == -1),]$p1
#   data$q <- data$qq
#   
#   # test the 
#   cor.test(data$p1, data$atomized_statement_sim, method="spearman")
#   cor.test(data$q, data$atomized_statement_sim, method="spearman")
#   nlp_rho <- cor.test(data$q, data$atomized_statement_sim, method="spearman")$estimate
#   non_rho <- cor.test(data$p1, data$atomized_statement_sim, method="spearman")$estimate
#   rhos <- c(nlp_rho, non_rho)
#   return(rhos)
# }
# # need to test 11 through 25.
# results <- data.frame(matrix(ncol = 3, nrow = 0))
# cols <- c("nlp","non","column")
# colnames(results) <- cols
# betas <- seq(0.80,0.99,0.01)
# for (i in 11:25){
#   for (beta in betas){
#     a <- spearman(i, 0.9, data)
#     results[nrow(results)+1,] <- c(a[1], a[2], i)
#   }
# }






# Histogram for atomized statements as inputs.
# Only the top predicted EQ statement taken for each input atomized statement.
# So only the top are shown for both the covered and not covered inputs.



# Recalculate the quality scores based on this value of beta.
beta <- 1.00
qc <- 0.70
y_height <- 1000
data$qq <- -1
data[which(data$p2 != -1),]$qq <- (beta*data[which(data$p2 != -1),]$p1) + ((1-beta)*data[which(data$p2 != -1),]$p2)
data[which(data$qq == -1),]$qq <- data[which(data$qq == -1),]$p1
data$q <- data$qq


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
  xlab("Jaccard similarity (b/t predicted and curated EQs)") + 
  ylab("frequency") +
  theme(plot.title = element_text(lineheight=1.0, face="bold", hjust=0.5), panel.grid.major =
          element_blank(), panel.grid.minor = element_blank(), panel.border = element_blank(), axis.line = element_line(colour = "black")) +
  scale_fill_manual("predicted score", breaks=c("0","1"),values=c("white","dimgrey"), labels = c("q < 0.7", "q > 0.7"))
  #scale_fill_manual("Quality", breaks=c("0","1"),values=c("white","dimgrey"), labels = c("q < threshold", "q > threshold"))

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






# USE THIS ONE

# Scaling the structural probabilites.
oldRange <- max(data$p2)-min(data$p2)
newRange <- 1
data$p2 <- ((data$p2-min(data$p2)) * newRange/oldRange)


# oldRange = oldMax - oldMin
# newRange = newMax - newMin
# newValue = ((oldValue - oldMin) * newRange / oldRange) + newMin



# Recalculate the quality scores based on this value of beta.
beta <- 0.99
qc <- 0.7
y_height <- 1000
data$qq <- -1
data[which(data$p2 != -1),]$qq <- (beta*data[which(data$p2 != -1),]$p1) + ((1-beta)*data[which(data$p2 != -1),]$p2)
data[which(data$qq == -1),]$qq <- data[which(data$qq == -1),]$p1
data$q <- data$qq
cor.test(data$p1, data$atomized_statement_sim, method="spearman")
cor.test(data$q, data$atomized_statement_sim, method="spearman")



temp <- data[,c("atomized_statement_id", "phenotype_id", "q", "atomized_statement_sim", "phenotype_sim")] %>% group_by(atomized_statement_id)  %>% filter(q == max(q)) %>% filter(1:n() == 1)
plot_data <- data.frame(temp)
plot_data$atomized_statement_sim <- as.numeric(plot_data$atomized_statement_sim)
plot_data$phenotype_sim <- as.numeric(plot_data$phenotype_sim)
plot_data$group = ifelse(plot_data$q < qc, "0", "1")




topNotKept <- plot_data[which(plot_data$group==0),] %>% group_by(atomized_statement_id) %>% filter(q==max(q)) 
topNotKept <- data.frame(topNotKept)
plot_data <- plot_data[which(plot_data$group==1),]
plot_data <- rbind(plot_data, topNotKept)

#ggplot(plot_data, aes(x=atomized_statement_sim, fill=cut(q,6))) + geom_histogram(boundary=0,binwidth=0.1,colour="black") +
ggplot(plot_data, aes(x=atomized_statement_sim, fill=cut(q,7))) + geom_histogram(boundary=0,binwidth=0.1) +
  theme_bw() + 
  coord_cartesian(xlim=c(1,0),ylim=c(0,y_height)) +
  xlab("Jaccard Similarity (between predicted and curated EQ statements)") + 
  ylab("Frequency") +
  theme(plot.title = element_text(lineheight=1.0, face="bold", hjust=0.5), panel.grid.major =
          element_blank(), panel.grid.minor = element_blank(), panel.border = element_blank(), axis.line = element_line(colour = "black")) +
  scale_fill_grey(start=0.9,end=0)

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





























# Histogram for atomized statements as inputs.
# Only the top predicted EQ statement taken for each input atomized statement.
# So only the top are shown for both the covered and not covered inputs.



# Recalculate the quality scores based on this value of beta.
beta <- 0.9
qc <- 0.75
y_height <- 1000
data$qq <- -1
data[which(data$p2 != -1),]$qq <- (beta*data[which(data$p2 != -1),]$p1) + ((1-beta)*data[which(data$p2 != -1),]$p2)
data[which(data$qq == -1),]$qq <- data[which(data$qq == -1),]$p1
data$q <- data$qq


temp <- data[,c("atomized_statement_id", "phenotype_id", "q", "atomized_statement_sim", "phenotype_sim")] %>% group_by(atomized_statement_id)  %>% filter(q == max(q)) %>% filter(1:n() == 1)
plot_data <- data.frame(temp)
plot_data$atomized_statement_sim <- as.numeric(plot_data$atomized_statement_sim)
plot_data$phenotype_sim <- as.numeric(plot_data$phenotype_sim)
plot_data$group = ifelse(plot_data$q < qc, "0", "1")




topNotKept <- plot_data[which(plot_data$group==0),] %>% group_by(atomized_statement_id) %>% filter(q==max(q)) 
topNotKept <- data.frame(topNotKept)
plot_data <- plot_data[which(plot_data$group==1),]
plot_data <- rbind(plot_data, topNotKept)

ggplot(plot_data, aes(x=atomized_statement_sim, fill=cut(q,10))) + geom_histogram(boundary=0,binwidth=0.1) +
  scale_fill_grey(start=1,end=0)


# ggplot(d, aes(x, fill = cut(x, 100))) +
#   geom_histogram(show.legend = FALSE) +
#   scale_fill_discrete(h = c(240, 10))
#   
  
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







# Read in a csv file with headers as a dataframe.
read <- function(dir,filename){
  d <- read.csv(file=paste(dir,filename,sep=""), header=T, sep=",", stringsAsFactors=FALSE)
  return(d)
}



# Get a blank dataframe with 0 rows and columns that match the input list.
get_empty_table <- function(cols){
  table <- data.frame(matrix(ncol=length(cols), nrow=0))
  colnames(table) <- cols
  return(table)
}



# Return 1 if value meets or exceeds threshold, 0 else.
get_binary_decision <- function(value,threshold){
  output <- ifelse(value>=threshold,1,0)
  return(output)
}



# Return F-score given binary decision matrix for predictions and target values.
get_f_score <- function(pred_matrix, target_matrix){
  tp <- length(which(pred_matrix==1 & target_matrix==1))
  fp <- length(which(pred_matrix==1 & target_matrix==0))
  fn <- length(which(pred_matrix==0 & target_matrix==1))
  tn <- length(which(pred_matrix==0 & target_matrix==0))
  prec <- ifelse(tp+fp==0, 0.000, tp/(tp+fp))
  rec <- ifelse(tp+tn==0, 0.000, tp/(tp+fn))
  f1 <- (2*prec*rec)/(prec+rec)
  return(ifelse(prec+rec == 0, 0.000, f1))
}
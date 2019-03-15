



# # Precision Recall table based off of ranking within each each chunk.
# numTerms <- ontologySize
# threshold <- seq(0, numTerms, 1)
# cols <- c("precision","recall","hprecision","hrecall")
# table_prr <- data.frame(threshold)
# table_prr[,cols] <- 0
# for (row in 1:nrow(table_prr)){
#   thresh <- table_prr[row,"threshold"]
#   results$match <- ifelse(results$rank <= thresh, 1, 0)
#   results$match <- as.factor(results$match)
#   tp = sum(results$match == 1 & results$response==1)
#   fp = sum(results$match == 1 & results$response==0)
#   fn = sum(results$match == 0 & results$response==1)
#   precision = tp/(tp+fp)
#   recall = tp/(tp+fn)
# 
#   hprec <- sum(results[results$match==1,]$hprecision) / (tp+fp)
#   hrec <- sum(results[results$match==1,]$hrecall) / (tp+fp)
# 
#   table_prr[row,"hprecision"] <- hprec
#   table_prr[row,"hrecall"] <- hrec
#   table_prr[row,"precision"] <- precision
#   table_prr[row,"recall"] <- recall
# }

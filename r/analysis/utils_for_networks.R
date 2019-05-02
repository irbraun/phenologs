






# Returns F-score for comparing sets of edges with threshold k.
# -- parameters --
# targets: sorted vector of target edge IDs.
# subset_membership: sorted vector of predicted edge IDs.
# k: the number of top edges from both vectors to retain.
# -- returns --
# The F-score of comparing the two vectors.
compare_edges <- function(targets, predictions, k){
  
  # Determine which edges are retained when this value of k is used.
  kept_predictions <- predictions[1:k]
  kept_targets <- targets[1:k]
  
  # Note that under this special case precision is recall is F1-score.
  tp <- length(intersect(kept_targets, kept_predictions))
  f1 <- tp/k
  return(f1)
}






# Returns F-score for comparing sets of edges with threshold k.
# -- parameters --
# targets: sorted vector of target edge IDs.
# subset_membership: sorted vector of predicted edge IDs.
# k: the number of top edges from both vectors to retain.
# subsampled_id_sets: collections of edge IDs to retain in each subsampling iteration.
# sampling_ratio: the fraction of available phenotypes that were retained.
# -- returns --
# The F-score of comparing the two vectors and plus and minus the std dev from subsampling.
compare_edges_with_subsampling <- function(targets, predictions, k, subsampled_id_sets, sampling_ratio){
  
  # Determine which edges are retained when this value of k is used.
  kept_predictions <- predictions[1:k]
  kept_targets <- targets[1:k]
  
  # Note that under this special case precision is recall is F1-score.
  tp <- length(intersect(kept_targets, kept_predictions))
  f1 <- tp/k
  subsampled_k <- floor(k*sampling_ratio)
  
  f1_distribution <- sapply(subsampled_id_sets, compare_one_subsample, targets=targets, predictions=predictions, k=subsampled_k)
  stdev = sd(f1_distribution)
  return(c(f1,f1-stdev,f1+stdev))
}




compare_one_subsample <- function(subsampled_id_set, targets, predictions, k){
  kept_predictions <- predictions[predictions %in% subsampled_id_set][1:k]
  kept_targets <- targets[targets %in% subsampled_id_set][1:k]
  tp <- length(intersect(kept_targets, kept_predictions))
  subsampled_f1 <- tp/k
  return(subsampled_f1)
}
















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
# -- returns --
# The F-score of comparing the two vectors and the min and max scores during subsampling.
compare_edges_with_subsampling <- function(targets, predictions, k, subsampled_id_sets){
  
  # Determine which edges are retained when this value of k is used.
  kept_predictions <- predictions[1:k]
  kept_targets <- targets[1:k]
  
  # Note that under this special case precision is recall is F1-score.
  tp <- length(intersect(kept_targets, kept_predictions))
  f1 <- tp/k
  
  # With subsampling
  min_f1 <- 1
  max_f1 <- 0
  for (subsampled_id_set in subsampled_id_sets){
    subsampled_k <- floor(k*SAMPLING_RATIO)
    kept_predictions <- predictions[predictions %in% subsampled_id_set][1:subsampled_k]
    kept_targets <- targets[targets %in% subsampled_id_set][1:subsampled_k]
    tp <- length(intersect(kept_targets, kept_predictions))
    subsampled_f1 <- tp/subsampled_k
    min_f1 <- min(subsampled_f1, min_f1)
    max_f1 <- max(subsampled_f1, max_f1)
  }
  return(c(f1,min_f1,max_f1))
}
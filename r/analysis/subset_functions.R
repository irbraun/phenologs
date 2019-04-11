



# Return 1 if subset is in this membership group, 0 else.
membership_func <- function(subset,subset_membership){
  return (ifelse(subset %in% subset_membership, 1, 0))
}



# Return data relevant to similarities within and between a specific subset in a network.
mean_similarity_within_and_between <- function(network_df, subsets_df, subset_name){
  
  # Identify the chunk IDs which are within or outside this subset category.
  # Note that 'within' means the gene was mapped in this subset, 'without' means the gene was never mapped to this subset.
  # Genes can belong to more than one subset, which is why the removal of the intersection is included here.
  inside_df <- subsets_df[subsets_df$subset %in% c(subset_name),]
  outside_df <- subsets_df[!(subsets_df$subset %in% c(subset_name)),]
  inside_chunk_ids <- inside_df$chunk
  outside_chunk_ids <- outside_df$chunk
  intersecting_chunk_ids<- intersect(inside_chunk_ids,outside_chunk_ids)
  outside_chunk_ids <- setdiff(outside_chunk_ids, intersecting_chunk_ids)
  
  # Calculating average similarity within the subset.
  relevant_slice <- network_df[network_df$phenotype_1 %in% inside_chunk_ids & network_df$phenotype_2 %in% inside_chunk_ids,]
  dist_within <- relevant_slice$value_to_use
  average_within_similarity <- mean(relevant_slice$value_to_use)
  
  # Calculating average similarity between this and other subsets.
  relevant_slice <- network_df[(network_df$phenotype_1 %in% inside_chunk_ids & network_df$phenotype_2 %in% outside_chunk_ids) | (network_df$phenotype_1 %in% outside_chunk_ids & network_df$phenotype_2 %in% inside_chunk_ids),]
  dist_between <- relevant_slice$value_to_use
  average_between_similarity <- mean(relevant_slice$value_to_use)
  
  # What were the sample sizes of chunks considered within and outside this subset?
  n_in <- length(inside_chunk_ids)
  n_out <- length(outside_chunk_ids)
  
  # Kolmogorov–Smirnov test, TODO figure out whether this is appropriate or how else these distributions should be treated/compared. 
  p_value <- ks.test(dist_within,dist_between)$p.value
  
  # Values to make the output easier to understand in a table row.
  greater_similarity_grouping <- ifelse(average_within_similarity > average_between_similarity, "within", "between")
  significance_threshold <- 0.05
  significant <- ifelse(p_value <= significance_threshold, "yes", "no")
  
  # Things to return.
  result <- c(n_in, n_out, round(average_within_similarity,3), round(average_between_similarity,3), round(p_value,3), greater_similarity_grouping, significant)
  return(result)
}




# In
# subset-->p_number mapping df
# pairwise similarity df
# phenotype that's dropped out
# subset (name of subset/cluster)
# Out
# Sim(this phenotype, that cluster)
get_similarity_to_cluster <- function(network_df, subsets_df, subset, phenotype_id){
  
  # Get the list of phenotype IDs that are in this subset/cluster.
  phenotype_ids_in_subset <- subsets_df[subsets_df$subset %in% c(subset),]$chunk
  
  # Get the network edges connecting this phenotype's node to any node in the subset/cluster.
  # This is too slow.
  #slice <- network_df[(network_df$phenotype_1 == phenotype_id & network_df$phenotype_2 %in% phenotype_ids_in_subset) | (network_df$phenotype_2 == phenotype_id & network_df$phenotype_1 %in% phenotype_ids_in_subset),]
  # This seems to be much faster, separating the subsetting steps into separate calls.
  phenotype_ids_in_subset <- phenotype_ids_in_subset[!phenotype_ids_in_subset %in% c(phenotype_id)]
  slice_part1 <- network_df[network_df$phenotype_1 == phenotype_id,]
  slice_part2 <- network_df[network_df$phenotype_2 == phenotype_id,]
  slice <- rbind(slice_part1,slice_part2)
  slice <- slice[slice$phenotype_1 %in% phenotype_ids_in_subset | slice$phenotype_2 %in% phenotype_ids_in_subset,]
  
  # Measure similarity between the phenotype and the cluster. Could do mean to all nodes or maximum to any node?
  similarity <- mean(slice$value_to_use)
  return(similarity)
}







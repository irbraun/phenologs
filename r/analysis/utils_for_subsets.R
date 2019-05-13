




membership_func <- function(subset,subset_membership){
  # Return 1 if subset is in this membership group, 0 else.
  # -- parameters --
  # subset: the name of a particular subset
  # subset_membership: a list of subset names
  # -- returns --
  # whether or not the subset is in the list of subsets.
  return (ifelse(subset %in% subset_membership, 1, 0))
}







mean_similarity_within_and_between <- function(network_df, subsets_df, subset_names){
  # Find the average similarity between a phenotype and all phenotypes in a certain functional subset.
  # -- parameters --
  # network_df: the dataframe defining all edges between all nodes
  # subsets_df: the dataframe defining which phenotype are in which subsets
  # subset_names: a vector with one or more subset names in it (this way classes can be used as well by combining multiple subsets).
  # -- returns --
  # a vector of values that are relevant to assessing whether similarity was greater within or between this functional subset.
  
  # Identify the chunk IDs which are within or outside this subset category.
  # Note that 'within' means the gene was mapped in this subset, 'without' means the gene was never mapped to this subset.
  # Genes can belong to more than one subset, which is why the removal of the intersection is included here.
  inside_df <- subsets_df[subsets_df$subset %in% subset_names,]
  outside_df <- subsets_df[!(subsets_df$subset %in% subset_names),]
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




get_similarity_to_cluster <- function(network_df, subsets_df, subset, phenotype_id){
  # Find the average similarity between a phenotype and all phenotypes in a certain functional subset.
  # -- parameters --
  # network_df: the dataframe defining all edges between all nodes, has to have a value_to_use column.
  # subsets_df: the dataframe defining which phenotype are in which groups, classes, and subsets.
  # subset: the name of a particular subset.
  # phenotype_id: the integer for a particular phenotype
  # -- returns --
  # the mean similarity value between this phenotype and all other phenotypes in the subset/cluster.
  
  # Get the list of phenotype IDs that are in this subset/cluster.
  phenotype_ids_in_subset <- subsets_df[subsets_df$subset %in% c(subset),]$chunk
  
  # Get the network edges connecting this phenotype's node to any node in the subset/cluster.
  # Note that separating the subsetting into two calls then rowbinding is faster than using an or statement.
  phenotype_ids_in_subset <- phenotype_ids_in_subset[!phenotype_ids_in_subset %in% c(phenotype_id)]
  slice_part1 <- network_df[network_df$phenotype_1 == phenotype_id,]
  slice_part2 <- network_df[network_df$phenotype_2 == phenotype_id,]
  slice <- rbind(slice_part1,slice_part2)
  slice <- slice[slice$phenotype_1 %in% phenotype_ids_in_subset | slice$phenotype_2 %in% phenotype_ids_in_subset,]
  
  # Measure similarity between the phenotype and the cluster. Could do mean to all nodes or maximum to any node?
  similarity <- mean(slice$value_to_use)
  return(similarity)
}












get_similarity_to_class <- function(network_df, subsets_df, class, phenotype_id){
  # Find the average similarity between a phenotype and all phenotypes in a certain functional subset.
  # -- parameters --
  # network_df: the dataframe defining all edges between all nodes, has to have a value_to_use column.
  # subsets_df: the dataframe defining which phenotype are in which groups, classes, and subsets.
  # class: the name of a particular class.
  # phenotype_id: the integer for a particular phenotype
  # -- returns --
  # the mean similarity value between this phenotype and all other phenotypes in the subset/cluster.
  
  # Get the list of phenotype IDs that are in this subset/cluster.
  phenotype_ids_in_class <- subsets_df[subsets_df$class %in% c(class),]$chunk
  
  # Get the network edges connecting this phenotype's node to any node in the subset/cluster.
  # Note that separating the subsetting into two calls then rowbinding is faster than using an or statement.
  phenotype_ids_in_class <- phenotype_ids_in_class[!phenotype_ids_in_class %in% c(phenotype_id)]
  slice_part1 <- network_df[network_df$phenotype_1 == phenotype_id,]
  slice_part2 <- network_df[network_df$phenotype_2 == phenotype_id,]
  slice <- rbind(slice_part1,slice_part2)
  slice <- slice[slice$phenotype_1 %in% phenotype_ids_in_class | slice$phenotype_2 %in% phenotype_ids_in_class,]
  
  # Measure similarity between the phenotype and the cluster. Could do mean to all nodes or maximum to any node?
  similarity <- mean(slice$value_to_use)
  return(similarity)
}





















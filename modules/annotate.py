import os
import sys





def annotate_with_noblecoder(configs_path, dtype, fuzzy, group_name, level="precise-match", default_prob=1.000):
	"""
    Function to run the NOBLE Coder annotation tool over the input text descriptions and process the output.
    Args:
		dtype: {phene, phenotype, or split_phenotype}, the data type of the input text.
		fuzzy: boolean dictating whether or not a fuzzy matching score should be found for the partial matcher.
		group_name: any identifying string.
		level: {"precise-match","partial-match"}, parameter used by this annotation method.
		default_prob: the score value included in the output when fuzzy matching is not done.
    Returns:
		nothing
    """
	NC_JARFILE = r"NobleCoder-1.0.jar"
	NC_DEFAULT_OUTPUT = r"RESULTS.tsv"
	noblecoder_dir = r'./annotators/noble/'
	ontologies = ["pato","po","go"]
	split_chunks_path = r'./data/split_chunks/'
	for onto in ontologies:
		noble_output_path = os.path.join(noblecoder_dir, r'output_'+onto)
		if not os.path.exists(noble_output_path):
			os.makedirs(noble_output_path)
		
		# Run NOBLE Coder using the provided jar file.
		nc_jar_path = os.path.join(noblecoder_dir, NC_JARFILE)
		nc_reports_path = os.path.join(noble_output_path,"reports","*")
		os.system("java -jar "+nc_jar_path+" -terminology "+onto+" -input "+split_chunks_path+" -output "+noble_output_path+" -search '"+level+"'"+" -score.concepts")
		os.system("rm "+nc_reports_path)

		# Run scripts to interpret the output in the context of the text data.
		raw_output_filename = os.path.join(noble_output_path, NC_DEFAULT_OUTPUT)
		processed_output_filename = os.path.join(noble_output_path, 'results.csv')
		nc_parse_script_path = os.path.join(noblecoder_dir, r'nc_parse.py')
		os.system("python "+nc_parse_script_path+" "+raw_output_filename+" "+processed_output_filename+" "+str(default_prob))

	# This does have some hard-coded assumptions in it, fix these.
	call = "java -jar term-mapping.jar -n2 "+" -d "+dtype+" -name "+group_name
	if fuzzy==True:
		call = call+" -fuzzy"
	call = call+" "+configs_path
	os.system(call)








def annotate_with_ncbo_annotator(configs_path, dtype, fuzzy, group_name):
	"""
    Function to run the NCBO Annotator tool over the input text descriptions and process the output.
    Args:
		dtype: {phene, phenotype, or split_phenotype}, the data type of the input text.
		fuzzy: boolean dictating whether or not a fuzzy matching score should be found for the partial matcher.
		group_name: any identifying string.
    Returns:
		nothing
    """
	ncboannot_dir = r"./annotators/ncbo"
	ontologies = ["pato","po","go","chebi"]
	for onto in ontologies:
		ncboannot_output_path = os.path.join(ncboannot_dir, r'output_'+onto)
		if not os.path.exists(ncboannot_output_path):
			os.makedirs(ncboannot_output_path)
	# This is where ncbo_annot.py would be called if using it within the automated pipeline.
	# As is, the files for the results of running NCBO annotator on each data are already saved because it's slow.
	# This does have some hard-coded assumption in it, fix these.
	call = "java -jar term-mapping.jar -n22 "+" -d "+dtype+" -name "+group_name
	if fuzzy==True:
		call = call+" -fuzzy"
	call = call+" "+configs_path
	os.system(call)







def annotate_with_naivebayes(configs_path, dtype, threshold=1.00):
	"""
    Function to run a naive Bayes classifier to perform annotation using training data.
    Args:
		dtype: {phene, phenotype, or split_phenotype}, the data type of the input text.
		threshold: the similarity threshold at which words are considered the same by the classifier.
    Returns:
		nothing
    """
	naivebayes_dir = r"./annotators/naive"
	ontologies = ["pato", "po", "go", "chebi"]
	for onto in ontologies:
		naivebayes_output_path = os.path.join(naivebayes_dir, r"output_"+onto)
		if not os.path.exists(naivebayes_output_path):
			os.makedirs(naivebayes_output_path)		
	os.system("java -jar term-mapping.jar -n3 -thresh "+str(threshold)+" -d "+dtype+" "+configs_path)
	os.system("bash "+os.path.join(naivebayes_dir,"merge_files.sh")+" "+dtype)








def aggregate_annotations(configs_path, dtype):
	"""
    Function to produce aggregate annotation files by combining the output of individual methods.
    Args:
		dtype: {phene, phenotype, or split_phenotype}, the data type of the input text.
    Returns:
		nothing
    """
	annotations_dir = r"./annotators/"
	aggregated_dir = os.path.join(annotations_dir, "aggregate")
	if not os.path.exists(aggregated_dir):
		os.makedirs(aggregated_dir)
	ontologies = ["pato", "po", "go", "chebi"]
	for onto in ontologies:
		path = os.path.join(aggregated_dir, r"output_"+onto)
		if not os.path.exists(path):
			os.makedirs(path)
	# Which files to merge are specied in the java code.
	os.system("java -jar term-mapping.jar -agg "+annotations_dir+" -d "+dtype+" "+configs_path)














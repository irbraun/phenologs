import os
import sys


# Only topn is taken into account for building the files listing the synonyms, the threshold is not
# used. The threshold is used for building the variants of the descriptions from those files, that
# the string-based methods look at.
def preprocessing(word2vec_model_path, dtype, topn=10, threshold=1.00, dbsetup=1, embeddings=1, split=1):
	
	# Generate a SQLite 3 table containing the information about the phenotype descriptions
	# that are present in a csv file containing all that information that is present in the
	# file which is specified manually in the sql script that populates the table. The format
	# is meant to match how the dataset was provided from the paper and could be adjusted in
	# the sql script as far as specifying column names and what data is included in the table.
	if (dbsetup==1):
		database_filename = "tm.db"
		os.system("sqlite3 "+database_filename+" < populate_table.sql &> populate_table.out")

	# Use word-embeddings to generate a set of synonyms for each word in the data.
	if (embeddings==1):
		# Generate a file containing all words in the text data.
		word_file_path = r"./data/allwords.txt"
		os.system("java -jar term-mapping.jar -w "+word_file_path+" -d "+dtype+" "+configs_path)
		# Generate a file containing each word in data and the n most similar words in the model.
		topn_str = str(topn)
		pair_file_path = r"./data/allpairs.txt"
		os.system("python get_similar_words.py "+word_file_path+" "+word2vec_model_path+" "+topn_str+" "+pair_file_path)

	# For all of the phene descriptions that are present in the data table this generates a
	# separate file for each of them, where the naming scheme is that the filename is the 
	# number of the unique integer that is given to each of the descriptions. This allows 
	# annotations made on each description to be mapped back to the original raw text. Delete
	# the contents of the split chunks directory first to make sure using the right files.
	if (split==1):
		split_chunks_path = r'./data/split_chunks/'
		if os.path.exists(split_chunks_path):
			regex_to_remove = "*.txt"
			os.system("rm "+os.path.join(split_chunks_path,regex_to_remove))
		else:
			os.makedirs(split_chunks_path)
		os.system("java -jar term-mapping.jar -n1 "+split_chunks_path+" -thresh "+str(threshold)+" -d "+dtype+" "+configs_path)




def do_doc_embeddings(training_data_path, split_chunks_path, original_network_path, modified_network_path):
	print "looking at the sentence embeddings"
	os.system("python train_and_use_doc_embeddings.py "+training_data_path+" "+split_chunks_path+" "+original_network_path+" "+modified_network_path)



# Create a directory for the output of the semantic annotation tool NOBLE Coder and then
# run the tool with the defined parameters on all the descriptions that are contained in
# the directory of split sentences. Change the parameters here to test different methods
# of using this annotation tool. The level parameter is a string specifying how the tool
# should be run, the options are 'precise-match' and 'partial-match', among some others.
def annotate_with_noblecoder(dtype, fuzzy, group_name, level="precise-match", default_prob=1.000):
	noblecoder_dir = r'./annotators/noble/'
	ontologies = ["pato","po","go"]
	split_chunks_path = r'./data/split_chunks/'
	for onto in ontologies:
		noble_output_path = os.path.join(noblecoder_dir, r'output_'+onto)
		if not os.path.exists(noble_output_path):
			os.makedirs(noble_output_path)
		
		# Run NOBLE Coder using the provided jar file.
		nc_jar_path = os.path.join(noblecoder_dir, r'NobleCoder-1.0.jar')
		os.system("java -jar "+nc_jar_path+" -terminology "+onto+" -input "+split_chunks_path+" -output "+noble_output_path+" -search '"+level+"'"+" -score.concepts")

		# Run scripts to interpret the output in the context of the text data.
		raw_output_filename = os.path.join(noble_output_path, "RESULTS.tsv")
		processed_output_filename = os.path.join(noble_output_path, 'results.csv')
		nc_parse_script_path = os.path.join(noblecoder_dir, r'nc_parse.py')
		os.system("python "+nc_parse_script_path+" "+raw_output_filename+" "+processed_output_filename+" "+str(default_prob))

	# This does have some hard-coded assumptions in it, fix these.
	call = "java -jar term-mapping.jar -n2 "+" -d "+dtype+" -name "+group_name
	if fuzzy==True:
		call = call+" -fuzzy"
	call = call+" "+configs_path
	os.system(call)
	#os.system("java -jar term-mapping.jar -n2 "+" -d "+dtype+" "+configs_path)




# Repeat for the annotation tool NCBO Annotator. The actual call is generated from within
# a python script that parsed the output returned from the server as well.
def annotate_with_ncbo_annotator(dtype, fuzzy, group_name):
	ncboannot_dir = r"./annotators/ncbo"
	ontologies = ["pato","po","go","chebi"]
	for onto in ontologies:
		ncboannot_output_path = os.path.join(ncboannot_dir, r'output_'+onto)
		if not os.path.exists(ncboannot_output_path):
			os.makedirs(ncboannot_output_path)
	# This is where ncbo_annot.py would be called if using it within the automated pipeline.
	# This does have some hard-coded assumption in it, fix these.
	call = "java -jar term-mapping.jar -n22 "+" -d "+dtype+" -name "+group_name
	if fuzzy==True:
		call = call+" -fuzzy"
	call = call+" "+configs_path
	os.system(call)
	#os.system("java -jar term-mapping.jar -n22 "+" -d "+dtype+" "+configs_path)





# Note that the ontologies used with this method are specified in java function, not here.
def annotate_with_naivebayes(dtype, threshold=1.00):
	naivebayes_dir = r"./annotators/naive"
	ontologies = ["pato", "po", "go", "chebi"]
	for onto in ontologies:
		naivebayes_output_path = os.path.join(naivebayes_dir, r"output_"+onto)
		if not os.path.exists(naivebayes_output_path):
			os.makedirs(naivebayes_output_path)		
	os.system("java -jar term-mapping.jar -n3 -thresh "+str(threshold)+" -d "+dtype+" "+configs_path)
	os.system("bash "+os.path.join(naivebayes_dir,"merge_files.sh")+" "+dtype)



def aggregate_annotations(dtype):
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




# Generate a file of EQ statements using the terms that were identified by the semantic
# annotation tools with the specified parameters for scoring the statements and also for
# thresholding and outputting the results. Changing parameters for testing how performance
# differs with different methods of scoring for the statements should be done here.
def compose(networks_path, dtype):
	if not os.path.exists(networks_path):
		os.makedirs(networks_path)
	os.system("java -jar term-mapping.jar -c "+" -d "+dtype+" "+configs_path)




# Reads the evaluation files generated for the annotations. Note that if using the same
# output file is used it gets reset after every function call to the R script.
def get_metrics(output,desc,directory,files):
	rscripts_path = r"./r/"
	function_call = r"Rscript "+os.path.join(rscripts_path,"read_annotations.R")+" "+output+" "+desc+" "+directory
	for file in files:
		function_call+=(" "+file)
	os.system(function_call)




# Collect a set of documents (sentences from abstracts from pubmed) based on a query
# based on the provided query parameters, some of which are species names which are 
# specified in the other python scripts. This function also trains a new word embedding
# model using the collected documents. This should only be run on the cluster this and
# training the word embedding models that is also done here take a long time.
def collect_documents(queryname, database="pubmed", doc_limit=1000):
	os.system("python ./pubmed/scripts/1_get_abstracts.py "+queryname+" "+database+" "+str(doc_limit))
	os.system("python ./pubmed/scripts/2_split_abstracts.py "+queryname+" "+database+" "+str(doc_limit))
	os.system("python ./pubmed/scripts/3_all_abstract_in_one_testversion.py")
	os.system("python ./pubmed/scripts/4_split_sentences.py")
	os.system("python train_word_embeddings.py")





# Only have to run this once, fetches and preprocesses documents from Pubmed.
# Runtime on Condo was ~3 hrs with a doc_limit of 100,000. 
#collect_documents("search1", database="pubmed", doc_limit=100000)






# Path of the directory that config.properties and config.json, those names are specifed in Java code.
configs_path = r"/work/dillpicl/irbraun/term-mapping/path/config/"


# This should exactly match the enum class strings for now. 
# e.g. "phene", "phenotype", or "split_phenotype".
dtype = sys.argv[1]






# Running NOBLE Coder for semantic annotation with gridsearch for parameters. Very fast.
thresholds = [1.00]
for t in thresholds:

	# Using the domain-specific word embeddings model from pubmed abstracts. Incorrect now, old parameters.
	'''
	description = "'nc; threshold = "+str(t)+"; domain specific embeddings; partial matches; "+str(dtype)+"'"
	output = r"./output/nc_thresh"+str(t).replace(".","d")+"_domain300_partial_"+dtype+".csv"
	preprocessing(topn=20, threshold=t, dbsetup=1, embeddings=1, split=1, word2vec_model_path=r"./gensim/300_size.model", dtype=dtype)
	annotate_with_noblecoder(level="precise-match", default_prob=0.50, dtype=dtype)
	files = ["output_pato/group1_"+dtype+"_eval.csv", "output_po/group1_"+dtype+"_eval.csv", "output_go/group1_"+dtype+"_eval.csv"]
	get_metrics(output, description, r"./annotators/noble/", files)

	# Using the domain-specific word embeddings model from pubmed abstracts. Incorrect now, old parameters.
	description = "'nc; threshold = "+str(t)+"; domain specific embeddings; precise matches; "+str(dtype)+"'"
	output = r"./output/nc_thresh"+str(t).replace(".","d")+"_domain300_precise_"+dtype+".csv"
	preprocessing(topn=20, threshold=t, dbsetup=1, embeddings=1, split=1, word2vec_model_path=r"./gensim/300_size.model", dtype=dtype)
	annotate_with_noblecoder(level="precise-match", default_prob=1.00, dtype=dtype)
	files = ["output_pato/group1_"+dtype+"_eval.csv", "output_po/group1_"+dtype+"_eval.csv", "output_go/group1_"+dtype+"_eval.csv"]
	get_metrics(output, description, r"./annotators/noble/", files)
	'''

	# Using the pre-trained word embeddings from wikipedia.
	description = "'nc; threshold = "+str(t)+"; pre-trained wikipedia embeddings; partial matches;"+str(dtype)+"'"
	output = r"./output/nc_thresh"+str(t).replace(".","d")+"_enwiki_partial_"+dtype+".csv"
	preprocessing(topn=20, threshold=t, dbsetup=1, embeddings=1, split=1, word2vec_model_path=r"./gensim/wiki_sg/word2vec.bin", dtype=dtype)
	annotate_with_noblecoder(level="precise-match", default_prob=0.50, dtype=dtype, fuzzy=True, group_name="group1")
	files = ["output_pato/group1_"+dtype+"_eval.csv", "output_po/group1_"+dtype+"_eval.csv", "output_go/group1_"+dtype+"_eval.csv"]
	get_metrics(output, description, r"./annotators/noble/", files)

	# Using the pre-trained word embeddings from wikipedia.
	description = "'nc; threshold = "+str(t)+"; pre-trained wikipedia embeddings; precise matches;"+str(dtype)+"'"
	output = r"./output/nc_thresh"+str(t).replace(".","d")+"_enwiki_precise_"+dtype+".csv"
	preprocessing(topn=20, threshold=t, dbsetup=1, embeddings=1, split=1, word2vec_model_path=r"./gensim/wiki_sg/word2vec.bin", dtype=dtype)
	annotate_with_noblecoder(level="precise-match", default_prob=1.00, dtype=dtype, fuzzy=False, group_name="group2")
	files = ["output_pato/group2_"+dtype+"_eval.csv", "output_po/group2_"+dtype+"_eval.csv", "output_go/group2_"+dtype+"_eval.csv"]
	get_metrics(output, description, r"./annotators/noble/", files)





# Running NCBO Annotator for semantic annotation with gridsearch for parameters. Very fast.
thresholds = [1.00]
for t in thresholds:

	# Using the pre-trained word-embeddings from wikipedia.
	description = "'na; threshold = "+str(t)+"; pre-trained wikipedia embeddings; "+str(dtype)+"'"
	output = r"./output/na_thresh"+str(t).replace(".","d")+"_enwiki_default_"+dtype+".csv"
	preprocessing(topn=20, threshold=t, dbsetup=1, embeddings=1, split=1, word2vec_model_path=r"./gensim/wiki_sg/word2vec.bin", dtype=dtype)
	annotate_with_ncbo_annotator(dtype=dtype, fuzzy=False, group_name="group1")
	files = ["output_pato/group1_"+dtype+"_eval.csv", "output_po/group1_"+dtype+"_eval.csv", "output_go/group1_"+dtype+"_eval.csv", "output_chebi/group1_"+dtype+"_eval.csv"]
	get_metrics(output, dtype, r"./annotators/ncbo/", files)






# Running naive Bayes for semantic annotation with gridsearch for parameters. Each takes 10 single core minutes.
thresholds = []
for t in thresholds:

	'''
	# Using the domain-specific word embeddings model from pubmed abstracts.
	description = "'nb; threshold = "+str(t)+"; domain specific embeddings; precise matches;"+str(dtype)+"'"
	output = r"./output/nb_thresh"+str(t).replace(".","d")+"_domain300_precise_"+dtype+".csv"
	preprocessing(topn=20, threshold=t, dbsetup=1, embeddings=1, split=1, word2vec_model_path=r"./gensim/300_size.model", dtype=dtype)
	annotate_with_naivebayes(threshold=t, dtype=dtype)
	files = ["output_pato/merged_"+dtype+"_eval.csv", "output_po/merged_"+dtype+"_eval.csv", "output_go/merged_"+dtype+"_eval.csv", "output_chebi/merged_"+dtype+"_eval.csv"]
	get_metrics(output, description, r"./annotators/naive/", files)
	'''

	# Using the pre-trained word embeddings from wikipedia.
	description = "'nb; threshold = "+str(t)+"; pre-trained wikipedia embeddings; precise matches;"+str(dtype)+"'"
	output = r"./output/nb_thresh"+str(t).replace(".","d")+"_enwiki_precise_"+dtype+".csv"
	preprocessing(topn=20, threshold=t, dbsetup=1, embeddings=1, split=1, word2vec_model_path=r"./gensim/wiki_sg/word2vec.bin", dtype=dtype)
	annotate_with_naivebayes(threshold=t, dtype=dtype)
	files = ["output_pato/merged_"+dtype+"_eval.csv", "output_po/merged_"+dtype+"_eval.csv", "output_go/merged_"+dtype+"_eval.csv", "output_chebi/merged_"+dtype+"_eval.csv"]
	get_metrics(output, description, r"./annotators/naive/", files)




'''
# Aggregate a set of the output files from the semantic annotation step for comparison.
aggregate_annotations(dtype=dtype)
files = ["output_pato/group1_eval.csv", "output_po/group1_eval.csv", "output_go/group1_eval.csv", "output_chebi/group1_eval.csv"]
output = r"./output/aggregate"+dtype+".csv"
get_metrics(output, dtype, r"./annotators/aggregate/", files)
print "finished semantic annotation"
'''





# Generate final output.
#compose(networks_path="./networks/", dtype=dtype)
#print "finished generating output file"




# Provide the gathered text data that will be used to train domain-specific doc2vec models.
# Those should have been obtained using collect_documents() with some doc number limit.
# Give it a specific network file which will get modified, a column for doc2vec similarity measure will be added.
# Note, this step reads the text for each phene or phenotype from the ./data/split_chunks/ folder so that has to match what you want to use this for.
# Use preprocessing(split=1) to do that.

# for example, even if the compose step was done using the split phenotypes or the phenes, this has to be specific to the file of nodes.
# If obtaining doc2vec estimations for the graph where nodes are phenotypes, this has to be "python pipeline.py phenotype" and call preprocess().
# If obtaining doc2vec estimations for the graph where nodes are phenes, this has to be "python pipeline.py phene" and call preprocess().
# Have to change it to phenotypes in the config/config.properties file in this case too!

'''
preprocessing(dbsetup=0, embeddings=0, split=1, word2vec_model_path=r"./gensim/wiki_sg/word2vec.bin", dtype="phenotype")
do_doc_embeddings("./pubmed/combined_abstracts.txt", "./data/split_chunks/", "./networks/phenotype_network.csv", "./networks/phenotype_network_modified.csv")
print "finished generating all the edge values for the phenotype network"
preprocessing(dbsetup=0, embeddings=0, split=1, word2vec_model_path=r"./gensim/wiki_sg/word2vec.bin", dtype="phene")
do_doc_embeddings("./pubmed/combined_abstracts.txt", "./data/split_chunks/", "./networks/phene_network.csv", "./networks/phene_network_modified.csv")
print "finished generating all the edge values for the phene network"
'''







# Notes for running to get the data presented in the paper.

# Phene Descriptions
# 1. Change config.properties to use the correct (phene) source annotation files (only matters for NCBO Annotator).
# 2. Change config.json to use the phene annotations files as the sources for EQ statements.
# 3. Run pipeline.py with 'phene' as the dtype.

# Phenotype Descriptions.
# 1. Change config.properties to use the correct (phenotype) source annotation files (only matters for NCBO Annotator).
# 2. Run pipeline.py with 'phenotype' as the dtype but stop after semantic annotation.
# 3. Change config.properties to use the correct (splitphenotypes) source annotation files (only matters for NCBO Annotator).
# 4. Change config.json to use the split phenotype annotations files as the sources for EQ statements.
# 5. Leave the phene annotation files as the one's used to estimate the dG path length probabilities.
# 6. Run pipeline.py with 'split_phenotype' as the dtype and continue through the whole thing.
# 7. There won't be any eval files or output semantic annotation output files for the split annotations run, just the graphs and annot file.










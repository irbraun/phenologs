import os
import sys





# Only topn is taken into account for building the files listing the synonyms, the threshold is not
# used. The threshold is used for building the variants of the descriptions from those files, that
# the string-based methods look at.
def preprocessing(word2vec_model_path, topn=10, threshold=1.00, dbsetup=1, embeddings=1, split=1):
	
	# Generate a SQLite 3 table containing the information about the phenotype descriptions
	# that are present in a csv file containing all that information that is present in the
	# file which is specified manually in the sql script that populates the table. The format
	# is meant to match how the dataset was provided from the paper and could be adjusted in
	# the sql script as far as specifying column names and what data is included in the table.
	if (dbsetup==1):
		database_filename = "tm.db"
		os.system("sqlite3 " + database_filename + " < populate_table.sql &> populate_table.out")

	# Use word-embeddings to generate a set of synonyms for each word in the data.
	if (embeddings==1):
		# Generate a file containing all words in the text data.
		word_file_path = r"./data/allwords.txt"
		os.system("java -jar term-mapping.jar -w "+word_file_path+" "+configs_path)
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
		os.system("java -jar term-mapping.jar -n1 " + split_chunks_path + " -thresh " + str(threshold) + " " + configs_path)




def do_doc_embeddings(training_data_path, split_chunks_path, original_network_path, modified_network_path):
	os.system("python train_and_use_doc_embeddings.py "+training_data_path+" "+split_chunks_path+" "+original_network_path+" "+modified_network_path)




# Create a directory for the output of the semantic annotation tool NOBLE Coder and then
# run the tool with the defined parameters on all the descriptions that are contained in
# the directory of split sentences. Change the parameters here to test different methods
# of using this annotation tool. The level parameter is a string specifying how the tool
# should be run, the options are 'precise-match' and 'partial-match', among some others.
def annotate_with_noblecoder(level="precise-match"):
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
		raw_output_filename = os.path.join(noble_output_path, 'results.tsv')
		processed_output_filename = os.path.join(noble_output_path, 'results.csv')
		nc_parse_script_path = os.path.join(noblecoder_dir, r'nc_parse.py')
		os.system("python " + nc_parse_script_path + " " + raw_output_filename + " " + processed_output_filename)

	# This does have some hard-coded assumptions in it, fix these.
	os.system("java -jar term-mapping.jar -n2 " + configs_path)	





# Repeat for the annotation tool NCBO Annotator. The actual call is generated from within
# a python script that parsed the output returned from the server as well.
def annotate_with_ncbo_annotator():
	ncboannot_dir = r"./annotators/ncbo"
	ontologies = ["pato","po","go","chebi"]
	for onto in ontologies:
		ncboannot_output_path = os.path.join(ncboannot_dir, r'output_'+onto)
		if not os.path.exists(ncboannot_output_path):
			os.makedirs(ncboannot_output_path)

	# This is where ncbo_annot.py would be called if using it for real.		
	# This does have some hard-coded assumption in it, fix these.
	os.system("java -jar term-mapping.jar -n22 " + configs_path)





# Note that the ontologies used with this method are specified in java function, not here.
def annotate_with_naivebayes(threshold=1.00):
	naivebayes_dir = r"./annotators/naive"
	ontologies = ["pato", "po", "go", "chebi"]
	for onto in ontologies:
		naivebayes_output_path = os.path.join(naivebayes_dir, r"output_"+onto)
		if not os.path.exists(naivebayes_output_path):
			os.makedirs(naivebayes_output_path)		
	os.system("java -jar term-mapping.jar -n3 -thresh "+str(threshold)+" "+configs_path)
	os.system("bash " + os.path.join(naivebayes_dir,"merge_files.sh"))


def aggregate_annotations():
	annotations_dir = r"./annotators/"
	aggregated_dir = os.path.join(annotations_dir, "aggregate")
	if not os.path.exists(aggregated_dir):
		os.makedirs(aggregated_dir)
	ontologies = ["pato", "po", "go", "chebi"]
	for onto in ontologies:
		path = os.path.join(aggregated_dir, r"output_"+onto)
		if not os.path.exists(path):
			os.makedirs(path)
	os.system("java -jar term-mapping.jar -agg "+annotations_dir+" "+configs_path)




# Generate a file of EQ statements using the terms that were identified by the semantic
# annotation tools with the specified parameters for scoring the statements and also for
# thresholding and outputting the results. Changing parameters for testing how performance
# differs with different methods of scoring for the statements should be done here.
def compose(networks_path):
	if not os.path.exists(networks_path):
		os.makedirs(networks_path)
	os.system("java -jar term-mapping.jar -c " + configs_path)






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









configs_path = r"/Users/irbraun/NetBeansProjects/term-mapping/path/config/"
dtype = "splitphenotypes"
#collect_documents("search1", database="pubmed", doc_limit=10000)    # ran this one on condo, runtime approx 3 hrs with lim=100,000





preprocessing(topn=5, threshold=1.00, dbsetup=0, embeddings=0, split=1, word2vec_model_path=r"./gensim/wiki_sg/word2vec.bin")




# Running NOBLE Coder for semantic annotation with gridsearch for parameters.
thresholds = []
for t in thresholds:

	# Using the domain-specific word embeddings model from pubmed abstracts.
	description = "'nc; threshold = "+str(t)+"; domain specific embeddings; precise matches; "+str(dtype)+"'"
	output = r"./output/nc_thresh"+str(t).replace(".","d")+"_domain300_precise_"+dtype+".csv"
	preprocessing(topn=20, threshold=t, dbsetup=1, embeddings=1, split=1, word2vec_model_path=r"./gensim/300_size.model")
	annotate_with_noblecoder(level="precise-match")
	files = ["output_pato/group1_eval.csv", "output_po/group1_eval.csv", "output_go/group1_eval.csv"]
	get_metrics(output, description, r"./annotators/noble/", files)

	'''
	# Using the pre-trained word embeddings from wikipedia.
	description = "'nc; threshold = "+str(t)+"; pre-trained wikipedia embeddings; precise matches;"+str(dtype)+"'"
	output = r"./output/nc_thresh"+str(t).replace(".","d")+"_enwiki_precise_"+dtype+".csv"
	preprocessing(topn=20, threshold=t, dbsetup=1, embeddings=1, split=1, word2vec_model_path=r"./gensim/wiki_sg/word2vec.bin")
	annotate_with_noblecoder(level="precise-match")
	files = ["output_pato/group1_eval.csv", "output_po/group1_eval.csv", "output_go/group1_eval.csv"]
	get_metrics(output, description, r"./annotators/noble/", files)

	# Using the domain-specific word embeddings model from pubmed abstracts.
	description = "'nc; threshold = "+str(t)+"; domain specific embeddings; partial matches; "+str(dtype)+"'"
	output = r"./output/nc_thresh"+str(t).replace(".","d")+"_domain300_partial_"+dtype+".csv"
	preprocessing(topn=20, threshold=t, dbsetup=1, embeddings=1, split=1, word2vec_model_path=r"./gensim/300_size.model")
	annotate_with_noblecoder(level="partial-match")
	files = ["output_pato/group1_eval.csv", "output_po/group1_eval.csv", "output_go/group1_eval.csv"]
	get_metrics(output, description, r"./annotators/noble/", files)

	# Using the pre-trained word embeddings from wikipedia.
	description = "'nc; threshold = "+str(t)+"; pre-trained wikipedia embeddings; partial matches;"+str(dtype)+"'"
	output = r"./output/nc_thresh"+str(t).replace(".","d")+"_enwiki_partial_"+dtype+".csv"
	preprocessing(topn=20, threshold=t, dbsetup=1, embeddings=1, split=1, word2vec_model_path=r"./gensim/wiki_sg/word2vec.bin")
	annotate_with_noblecoder(level="partial-match")
	files = ["output_pato/group1_eval.csv", "output_po/group1_eval.csv", "output_go/group1_eval.csv"]
	get_metrics(output, description, r"./annotators/noble/", files)
	'''




# Running NCBO Annotator for semantic annotation with gridsearch for parameters.
thresholds = []
for t in thresholds:

	# Using the pre-trained word-embeddings from wikipedia.
	description = "'na; threshold = "+str(t)+"; pre-trained wikipedia embeddings; "+str(dtype)+"'"
	output = r"./output/na_thresh"+str(t).replace(".","d")+"_enwiki_default_"+dtype+".csv"
	preprocessing(topn=20, threshold=t, dbsetup=1, embeddings=1, split=1, word2vec_model_path=r"./gensim/wiki_sg/word2vec.bin")
	annotate_with_ncbo_annotator()
	files = ["output_pato/group1_eval.csv", "output_po/group1_eval.csv", "output_go/group1_eval.csv", "output_chebi/group1_eval.csv"]
	get_metrics(output, dtype, r"./annotators/ncbo/", files)





# Running naive Bayes for semantic annotation with gridsearch for parameters. Each takes 10 single core minutes.
thresholds = []
for t in thresholds:

	# Using the domain-specific word embeddings model from pubmed abstracts.
	description = "'nb; threshold = "+str(t)+"; domain specific embeddings; precise matches;"+str(dtype)+"'"
	output = r"./output/nb_thresh"+str(t).replace(".","d")+"_domain300_precise_"+dtype+".csv"
	preprocessing(topn=20, threshold=t, dbsetup=1, embeddings=1, split=1, word2vec_model_path=r"./gensim/300_size.model")
	annotate_with_naivebayes(threshold=t)
	files = ["output_pato/merged_eval.csv", "output_po/merged_eval.csv", "output_go/merged_eval.csv", "output_chebi/merged_eval.csv"]
	get_metrics(output, description, r"./annotators/naive/", files)

	'''
	# Using the pre-trained word embeddings from wikipedia.
	description = "'nb; threshold = "+str(t)+"; pre-trained wikipedia embeddings; precise matches;"+str(dtype)+"'"
	output = r"./output/nb_thresh"+str(t).replace(".","d")+"_enwiki_precise_"+dtype+".csv"
	preprocessing(topn=20, threshold=t, dbsetup=1, embeddings=1, split=1, word2vec_model_path=r"./gensim/wiki_sg/word2vec.bin")
	annotate_with_naivebayes(threshold=t)
	files = ["output_pato/merged_eval.csv", "output_po/merged_eval.csv", "output_go/merged_eval.csv"]
	get_metrics(output, description, r"./annotators/naive/", files)
	'''




# Want to run this on a select set of the above experimenets which make sense.
'''
aggregate_annotations()
files = ["output_pato/group1_eval.csv", "output_po/group1_eval.csv", "output_go/group1_eval.csv", "output_chebi/group1_eval.csv"]
output = r"./output/aggregate"+dtype+".csv"
get_metrics(output, dtype, r"./annotators/aggregate/", files)
'''






print "done"























'''
# Generate both the annotations output file and the networks that result from those annotations.
compose("./networks/")



# Add to the network edge files generated from the result using sentence embedding models.
os.system(r"python train_and_use_doc_embeddings.py ./docs100.txt ./data/split_chunks/ ./networks/phenotype_network_small.csv ./networks/phenotype_network_small_modified.csv")

do_doc_embeddings("./pubmed/combined_abstracts.txt", "./data/split_chunks/", "./networks/phene_network.csv", "./networks/phene_network_modified.csv")
'''





''' Notes
The increase in recall is extremely small but the overall performance of the annotation method is actually 
pretty good, worth including in the results. Have to look at how those annotations work for producing EQ
statements that are actually useful. The different cases are where the number of desired EQ statements is 
completely unknown, estimated from something like length or number of delimiters (.;) or something like
that. The increase in recall is negligable when using the partial-matching mode of the annotation tool as
well, so the problem is simply that synonyms are not helpful in the case of this specific data.


Things that are currently hard-coded which need to be fixed.
1. Paths to the word-embeddings results file in NaiveBayes.java and Text.java
2. Paths of the class probability and evaluation files in all the Java classes that generate them.

Process for this script should be:
1. Generate a huge set of output files, look at them and put them into the tables/results section.
2. Using the best looking parameters (ignoring the overfit nb stuff), generate EQ statements and network files for whole data.
3. Run the doc2vec stuff to compare against.
4. Put those network figures, edge tables and descriptions in to the tables/results section.




'''





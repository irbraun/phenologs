import os
import sys
import argparse


sys.path.append("./modules")
import preprocess as ppr
import annotate as ann
import doc_utils as dt
import word_utils as wt









def compose(networks_path, dtype):
	"""
	Generate a file of EQ statements using the terms that were identified by the semantic
	annotation tools with the specified parameters for scoring the statements and also for
	thresholding and outputting the results. Changing parameters for testing how performance
	differs with different methods of scoring for the statements should be done here.
	"""
	if not os.path.exists(networks_path):
		os.makedirs(networks_path)
	os.system("java -jar term-mapping.jar -c "+" -d "+dtype+" "+configs_path)






def get_metrics(output,desc,directory,files):
	"""
    Function to run the R script that reads the evaluation files generated for all annotations. 
    Args:
    	output: full path of the csv file where this summary of metrics should be saved.
		desc: description of the methods and parameters used in these annotations, will be used in output file.
		directory: path to folder where all the annotation files are.
		files: k different strings where they are all the filenames of annotation files in this folder to be read.
    Returns:
		nothing
    """
	rscripts_path = r"./r/"
	function_call = r"Rscript "+os.path.join(rscripts_path,"read_annotations.R")+" "+output+" "+desc+" "+directory
	for file in files:
		function_call+=(" "+file)
	os.system(function_call)





def collect_documents(queryname, docs_directory, database="pubmed", doc_limit=1000):
	"""
	Collect a set of documents (sentences from abstracts from pubmed) based on a query
	based on the provided query parameters, some of which are species names which are 
	specified in the other python scripts. This function also trains a new word embedding
	model using the collected documents. This should only be run on the cluster this and
	training the word embedding models that is also done here take a long time.
	"""
	# Use mainly pre-existing scripts to run the querying and collecting of abstracts.
	os.system("python ./pubmed/scripts/1_get_abstracts.py"+" --name "+queryname+" --db "+database+" --limit "+str(doc_limit)+" --dir "+docs_directory)
	os.system("python ./pubmed/scripts/2_split_abstracts.py"+" --name "+queryname+" --db "+database+" --limit "+str(doc_limit)+" --dir "+docs_directory)
	os.system("python ./pubmed/scripts/3_combine_abstracts.py"+" --dir "+"./pubmed/")
	os.system("python ./pubmed/scripts/4_split_sentences.py"+" --dir "+"./pubmed/")
	# Train word embedding models using the collected documents.
	wt.train_word2vec_models(models_path="./gensim", text_training_file="./pubmed/combined_abstracts.txt")













# Only have to run this once, fetches and preprocesses documents from pubmed.
# Runtime on Condo was ~3 hrs with a doc_limit of 100,000. 

#collect_documents(queryname="search1", docs_directory="./pubmed", database="pubmed", doc_limit=100000)











# Read in the command line arguments, the type of text descriptions and path to required config files.
# Datatype of the text that's being looked at. This should exactly match the enum class strings for now.
# {phene, phenotype, split_phenotype}
# The other option is path of the directory that config.properties and config.json are in, those names are fixed.
parser = argparse.ArgumentParser()
parser.add_argument("-d", dest="datatype", required=True)
parser.add_argument("-c", dest="config_path", required=True)
args = parser.parse_args()
dtype = args.datatype
configs_path = args.config_path




# Running NOBLE Coder for semantic annotation with gridsearch for parameters.
thresholds = [1.0]
for t in thresholds:

	'''
	# Using the domain-specific word embeddings model from pubmed abstracts, partial matching.
	description = "'nc; threshold = "+str(t)+"; domain specific embeddings; partial matches;"+str(dtype)+"'"
	output = r"./output/nc_thresh"+str(t).replace(".","d")+"_domain300_partial_"+dtype+".csv"
	ppr.preprocessing(topn=20, threshold=t, dbsetup=1, embeddings=1, split=1, word2vec_model_path=r"./gensim/300_size.model", configs_path=configs_path, dtype=dtype)
	ann.annotate_with_noblecoder(level="partial-match", default_prob=0.50, dtype=dtype, fuzzy=True, group_name="group1", configs_path=configs_path)
	files = ["output_pato/group1_"+dtype+"_eval.csv", "output_po/group1_"+dtype+"_eval.csv", "output_go/group1_"+dtype+"_eval.csv"]
	get_metrics(output, description, r"./annotators/noble/", files)

	# Using the domain-specific word embeddings model from pubmed abstracts, precise matching.
	description = "'nc; threshold = "+str(t)+"; domain specific embeddings; precise matches;"+str(dtype)+"'"
	output = r"./output/nc_thresh"+str(t).replace(".","d")+"_domain300_precise_"+dtype+".csv"
	ppr.preprocessing(topn=20, threshold=t, dbsetup=1, embeddings=1, split=1, word2vec_model_path=r"./gensim/300_size.model", configs_path=configs_path, dtype=dtype)
	ann.annotate_with_noblecoder(level="precise-match", default_prob=1.00, dtype=dtype, fuzzy=False, group_name="group2", configs_path=configs_path)
	files = ["output_pato/group2_"+dtype+"_eval.csv", "output_po/group2_"+dtype+"_eval.csv", "output_go/group2_"+dtype+"_eval.csv"]
	get_metrics(output, description, r"./annotators/noble/", files)
	'''

	# Using the pre-trained word embeddings from wikipedia, partial matching.
	description = "'nc; threshold = "+str(t)+"; pre-trained wikipedia embeddings; partial matches;"+str(dtype)+"'"
	output = r"./output/nc_thresh"+str(t).replace(".","d")+"_enwiki_partial_"+dtype+".csv"
	ppr.preprocessing(topn=20, threshold=t, dbsetup=1, embeddings=1, split=1, word2vec_model_path=r"./gensim/wiki_sg/word2vec.bin", configs_path=configs_path, dtype=dtype)
	ann.annotate_with_noblecoder(level="partial-match", default_prob=0.50, dtype=dtype, fuzzy=True, group_name="group1", configs_path=configs_path)
	files = ["output_pato/group1_"+dtype+"_eval.csv", "output_po/group1_"+dtype+"_eval.csv", "output_go/group1_"+dtype+"_eval.csv"]
	get_metrics(output, description, r"./annotators/noble/", files)

	# Using the pre-trained word embeddings from wikipedia, precise matching.
	description = "'nc; threshold = "+str(t)+"; pre-trained wikipedia embeddings; precise matches;"+str(dtype)+"'"
	output = r"./output/nc_thresh"+str(t).replace(".","d")+"_enwiki_precise_"+dtype+".csv"
	ppr.preprocessing(topn=20, threshold=t, dbsetup=1, embeddings=1, split=1, word2vec_model_path=r"./gensim/wiki_sg/word2vec.bin", configs_path=configs_path, dtype=dtype)
	ann.annotate_with_noblecoder(level="precise-match", default_prob=1.00, dtype=dtype, fuzzy=False, group_name="group2", configs_path=configs_path)
	files = ["output_pato/group2_"+dtype+"_eval.csv", "output_po/group2_"+dtype+"_eval.csv", "output_go/group2_"+dtype+"_eval.csv"]
	get_metrics(output, description, r"./annotators/noble/", files)





# Running NCBO Annotator for semantic annotation with gridsearch for parameters.
thresholds = [1.0]
for t in thresholds:

	# Using the pre-trained word-embeddings from wikipedia.
	description = "'na; threshold = "+str(t)+"; pre-trained wikipedia embeddings; "+str(dtype)+"'"
	output = r"./output/na_thresh"+str(t).replace(".","d")+"_enwiki_default_"+dtype+".csv"
	ppr.preprocessing(topn=20, threshold=t, dbsetup=1, embeddings=1, split=1, word2vec_model_path=r"./gensim/wiki_sg/word2vec.bin", configs_path=configs_path, dtype=dtype)
	ann.annotate_with_ncbo_annotator(dtype=dtype, fuzzy=False, group_name="group1", configs_path=configs_path)
	files = ["output_pato/group1_"+dtype+"_eval.csv", "output_po/group1_"+dtype+"_eval.csv", "output_go/group1_"+dtype+"_eval.csv", "output_chebi/group1_"+dtype+"_eval.csv"]
	get_metrics(output, description, r"./annotators/ncbo/", files)





# Running naive Bayes for semantic annotation with gridsearch for parameters.
thresholds = [1.0]
for t in thresholds:

	# Using the pre-trained word embeddings from wikipedia.
	description = "'nb; threshold = "+str(t)+"; pre-trained wikipedia embeddings; precise matches;"+str(dtype)+"'"
	output = r"./output/nb_thresh"+str(t).replace(".","d")+"_enwiki_precise_"+dtype+".csv"
	ppr.preprocessing(topn=20, threshold=t, dbsetup=1, embeddings=1, split=1, word2vec_model_path=r"./gensim/wiki_sg/word2vec.bin", configs_path=configs_path, dtype=dtype)
	ann.annotate_with_naivebayes(threshold=t, dtype=dtype, configs_path=configs_path)
	files = ["output_pato/merged_"+dtype+"_eval.csv", "output_po/merged_"+dtype+"_eval.csv", "output_go/merged_"+dtype+"_eval.csv", "output_chebi/merged_"+dtype+"_eval.csv"]
	get_metrics(output, description, r"./annotators/naive/", files)


	# Adjust the ranges of the scores given in the output files to be between 0 and 1 in each file.
	rscripts_path = r"./r/"
	function_call = r"Rscript "+os.path.join(rscripts_path,"adjust_range_in_tables.R")+" "+r"./annotators/naive/"
	files.extend(["output_pato/merged_"+dtype+"_classprobs.csv", "output_po/merged_"+dtype+"_classprobs.csv", "output_go/merged_"+dtype+"_classprobs.csv", "output_chebi/merged_"+dtype+"_classprobs.csv"])
	for file in files:
		function_call+=(" "+file)
	os.system(function_call)




# Aggregate a set of the output files from the semantic annotation step for comparison.
ann.aggregate_annotations(dtype=dtype, configs_path=configs_path)
files = ["output_pato/group1_eval.csv", "output_po/group1_eval.csv", "output_go/group1_eval.csv", "output_chebi/group1_eval.csv"]
output = r"./output/aggregate"+dtype+".csv"
get_metrics(output, dtype, r"./annotators/aggregate/", files)
print "finished semantic annotation"




# Don't use unprocesed phenotype descriptions to build networks.
if dtype=="phenotype":
	sys.exit()




# Generate final output.
compose(networks_path="./networks/", dtype=dtype)
print "finished generating output annotation file"



# The next step uses other NLP methods including doc2vec and different measures of sentence similarity
# to add each possible pairwise similarity to the existing files that describe the networks generated
# either from the phenotype or phene descripton datasets. Note that these are not methods are not using
# the additional syntax for the text chunk files which have been generated with word choice variability
# with the word2vec models, so the embeddings parameter needs to be 0 in the preprocesing calls. The 
# preprocessing calls are also necessary because the correct set of text chunk files in the chunks 
# directory need to be present as they are used here.



# Load or train any of the document embeddings models to be used.
model_wiki = dt.load_model("./gensim/enwiki_dbow/doc2vec.bin")
#model_domain_dmpv = train_model("./pubmed/combined_abstracts.txt", "dmpv.model", dm=0, size=300)
#model_domain_dbow = train_model("./pubmed/combined_abstracts.txt", "dbow.model", dm=1, size=300)



# Update the sets of edges given that the input descriptions were for phenes.
if dtype == "phene":
	# Update the file for the phenotype network.
	ppr.preprocessing(dbsetup=0, embeddings=0, split=1, word2vec_model_path=r"./gensim/wiki_sg/word2vec.bin", dtype="phenotype", configs_path=configs_path, concat="true")
	dt.update_networkfile_d2v([model_wiki], ["enwiki_dbow"], "./data/split_chunks/", "./networks/phene_text_phenotype_network.csv", "./networks/phene_text_phenotype_network.csv")
	dt.update_networkfile_bow("./data/split_chunks/", "./networks/phene_text_phenotype_network.csv", "./networks/phene_text_phenotype_network.csv")
	print "done generating additional similarity values for the phenotype network"

	# Update the file for the phene network.
	ppr.preprocessing(dbsetup=0, embeddings=0, split=1, word2vec_model_path=r"./gensim/wiki_sg/word2vec.bin", dtype="phene", configs_path=configs_path, concat="false")
	dt.update_networkfile_d2v([model_wiki], ["enwiki_dbow"], "./data/split_chunks/", "./networks/phene_text_phene_network.csv", "./networks/phene_text_phene_network.csv")
	dt.update_networkfile_bow("./data/split_chunks/", "./networks/phene_text_phene_network.csv", "./networks/phene_text_phene_network.csv")
	print "done generating additional similarity values for the phene network"



# Update the sets of edges given that the input descriptions were for phenotypes.
if dtype == "split_phenotype":
	# Update the file for the phenotype network.
	ppr.preprocessing(dbsetup=0, embeddings=0, split=1, word2vec_model_path=r"./gensim/wiki_sg/word2vec.bin", dtype="phenotype", configs_path=configs_path, concat="false")
	dt.update_networkfile_d2v([model_wiki], ["enwiki_dbow"], "./data/split_chunks/", "./networks/phenotype_text_phenotype_network.csv", "./networks/phenotype_text_phenotype_network.csv")
	dt.update_networkfile_bow("./data/split_chunks/", "./networks/phenotype_text_phenotype_network.csv", "./networks/phenotype_text_phenotype_network.csv")
	print "done generating additional similarity values for the phenotype network"



print "done"






# Notes for running to get the data presented in the paper.

# Phene Descriptions
# 1. Change config.properties to use the correct (phene) source annotation files (only matters for NCBO Annotator).
# 2. Change config.json to use the phene annotations files as the sources for EQ statements.
# 3. Run pipeline.py with 'phene' as the dtype.
# 4. Save the resulting network files somewhere.


# Phenotype Descriptions.
# 1. Change config.properties to use the correct (phenotype) source annotation files (only matters for NCBO Annotator).
# 2. Run pipeline.py with 'phenotype' as the dtype but stop after semantic annotation.
# 3. Change config.properties to use the correct (splitphenotypes) source annotation files (only matters for NCBO Annotator).
# 4. Change config.json to use the split phenotype annotations files as the sources for EQ statements.
# 5. Leave the phene annotation files as the one's used to estimate the dG path length probabilities.
# 6. Run pipeline.py with 'split_phenotype' as the dtype and continue through the whole thing.
# 7. There won't be any eval files or output semantic annotation output files for the split annotations run, just the graphs and annot file.
# 8. Errors will be thrown for calls to generate the output annotation tables and aggregate the annotations, should fix this.
# 9. Save the resulting network files somewhere.


# The important files that are created.
# 1. Summaries of the semantic annotation steps on each datatype are in the /output folder.
# 2. Overall EQ annotation files are in the /output folder.
# 3. Files specifying similarity edges for eawch datatype are in the /network file.












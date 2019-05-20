import os
import sys
from word_utils import make_synonym_file


# Paths that are hardcoded for these processes.
# SQLite3 database: "./tm.db"
# Text file containing complete data vocabulary: "./data/allwords.txt"
# Text file containing all pairs of words from word2vec model satisfying threshold: "./data/allpairs.txt"
# Directory containing all the text files corresponding to the input data: "./data/split_chunks"
# The naming scheme for the text files within that directory is also defined here.





def preprocessing(word2vec_model_path, configs_path, dtype, topn=10, threshold=1.00, dbsetup=1, embeddings=1, split=1, concat="false"):
	"""
    Function to setup the necessary objects for running text descriptions through the pipeline.
    Args:
    	word2vec_model_path: path for reading in a pre-trained complete word2vec model.
        configs_path: columns that have desired y values in them.
		dtype: {phene, phenotype, or split_phenotype}, the data type of the input text.
		topn: the number of matches (similar words) that are found for each word in the vocabulary using the word2vec model.
		threshold: threshold for word2vec-determined similarility when generating variants of input text descriptions.
		dbsetup: set to 1 to call external script for populating the database from the csv file.
		embeddings: set to 1 to use the word2vec models to help generate variants of the input text descriptions.
		split: set to 1 to generate the individual text files for each text description and its variants.
    Returns:
		nothing
    """
	
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
		word_file_path = r"./data/vocabulary/allwords.txt"
		os.system("java -jar term-mapping.jar -w "+word_file_path+" -d "+dtype+" "+configs_path)
		# Generate a file containing each word in data and the n most similar words in the model.
		pair_file_path = r"./data/vocabulary/allpairs.txt"
		make_synonym_file(word_file_path, word2vec_model_path, topn, pair_file_path)



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
		os.system("java -jar term-mapping.jar -n1 "+split_chunks_path+" -thresh "+str(threshold)+" -d "+dtype+" "+" -concat "+concat+" "+configs_path)
#!/usr/bin/env python
# -*- coding: utf-8 -*-


# Code partially credited to Xing et al. (2018)
# "A gene–phenotype relationship extraction pipeline from the biomedical literature using a representation learning approach"
# Link: https://www.ncbi.nlm.nih.gov/pmc/articles/PMC6022650/



from gensim.models import word2vec
from scipy import spatial
import re
import os
import sys
import gensim
import logging
import numpy
import glob
from collections import Counter
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.metrics.pairwise import cosine_similarity
import pandas as pd


LabeledSentence = gensim.models.doc2vec.LabeledSentence
reload(sys)
sys.setdefaultencoding('utf8')




def load_model(path_to_model):
	model = gensim.models.Doc2Vec.load(path_to_model)
	return model




def train_model(training_sentences_file, model_filename, dm=1, size=300):
	"""
	Function to train a neural network using doc2vec for the purpose of generating document embeddings.
	Args:
		training_setences_file: full path to the text file of sentences to be used for training new models.
		model_filename: string to be used as the model filename, should include any extension desired.
		dm: 0 or 1 to specify the class of network to be built, see options below.
		size: the size of the vectors to be generated for each doc, corresponds to size of a hidden layer.
	Returns:
		model
	"""

	# Collect sentences to be used in training the model.
	labeled_data = []
	with open(training_sentences_file) as f:
		sentence_set = f.read().split('\n')
	i = 0
	for line in sentence_set:
		if line != '':
			labeled_data.append(gensim.models.doc2vec.TaggedDocument(words=gensim.utils.simple_preprocess(line), tags=['SENT'+str(i), 1]))
			i = i + 1

	# Using dm=1 refers to the DPMV model.
	if dm == 1: #dpmv
		model = gensim.models.Doc2Vec(vector_size=size, window=10, min_count=5, workers=5, alpha=0.025, min_alpha=0.025)
	# Using dm=0 refers to the DBOW model.
	elif dm == 0: #dbow
		model = gensim.models.Doc2Vec(vector_size=size, window=10, min_count=5, dm=0, workers=5, alpha=0.025, min_alpha=0.025, dbow_words=1)
	else:
		sys.exit()

	# Training the neural network.
	model.build_vocab(labeled_data)
	for epoch in range(10):
		print "training epoch "+str(epoch) 
		model.train(labeled_data, total_words=model.corpus_count, epochs=model.epochs)
		model.alpha -= 0.002            # (original note: decrease the learning rate)
		model.min_alpha = model.alpha   # (original note: fix the learning rate, no decay)
	model.save(os.join(r"./gensim/",model_filename))
	return model








# https://towardsdatascience.com/overview-of-text-similarity-metrics-3397c4601f50
def get_cosine_sim_value(str1, str2):
	strs = [str1,str2]
	vectors = [t for t in get_vectors(*strs)]
	similarity_matrix = cosine_similarity(vectors)
	return similarity_matrix[0][1]

def get_cosine_sim_matrix(*strs):
	vectors = [t for t in get_vectors(*strs)]
	similarity_matrix = cosine_similarity(vectors)
	return similarity_matrix

def get_vectors(*strs):
	text = [t for t in strs]
	vectorizer = CountVectorizer(text)
	vectorizer.fit(text)
	return vectorizer.transform(text).toarray()







def update_networkfile_bow(chunks_path, network_filename, revised_network_filename):

	# Setting up intitial structures.
	jac_dict = dict()	# Mapping from chunks to vectors used to calculate Jaccard similarity
	cos_dict = dict()	# Mapping from chunks to position in the list which is position in cosine similarity matrix.
	cos_list = []		# List of strings which are in the order they are used to build the cosine similarity matrix.

	# Collect representations for all phenotype or phene descriptions in the dataset. 
	for filepath in glob.iglob(os.path.join(chunks_path,r"*.txt")):
		file = open(filepath)
		sentence = file.read()
		chunk_id = int(filepath.split("/")[-1].split(".")[0])
		jac_dict[chunk_id] = set(sentence.lower().split())
		
		# Remember where to find each pairwise similarity in the simialrity matrix to be built.
		index = len(cos_list)
		cos_list.append(sentence.lower())
		cos_dict[chunk_id] = index

	# Build cosine similarity matrix and unpack strings in the list so they're read as multiple arguments.
	cos_sim_matrix = get_cosine_sim_matrix(*cos_list)


	# Have to update this header if changing what new distance measurements are added to the file.
	df = pd.read_csv(network_filename, sep=",", dtype=str)
	revised_network= open(revised_network_filename,"w")
	new_header = ",".join(df.columns)+",jaccard,cosine"
	revised_network.write(new_header+"\n")

	# Iterate through all the rows in the file, get new distances and rewrite.
	ID1_COL = 1
	ID2_COL = 2
	for row in df.itertuples():
		p1 = int(row[ID1_COL])
		p2 = int(row[ID2_COL])
		distances = []

		# Add the jaccard similarity between the presence/absence vectors.
		v1 = jac_dict[p1]
		v2 = jac_dict[p2]
		intersection = v1.intersection(v2)
		dist = float(len(intersection)) / (len(v1)+len(v2)-len(intersection))
		dist = round(dist,3)
		distances.append(dist)

		# Add the cosine similarity between the bag of words count vectors.
		dist = cos_sim_matrix[cos_dict[p1]][cos_dict[p2]]
		dist = round(dist,3)
		distances.append(dist)

		# Ignore the index whene converting to list.
		new_row = list(row)[1:]
		new_row.extend(distances)
		new_row = [str(x) for x in new_row]
		revised_network.write(",".join(new_row)+"\n")







def update_networkfile_d2v(model_list, model_name_list, chunks_path, network_filename, revised_network_filename):

	# Some initial structures.
	num_models = len(model_list)					# Number of doc2vec models passed in.
	dicts = [dict() for x in range(num_models)]		# List of dictionaries for mapping chunks to their position in matrix.
	vectors = [[] for x in range(num_models)]		# List of list of vectors which are the rows and columns of the matrix.
	matrices = [[] for x in range(num_models)]		# List of cosine similarity matrices.


	# Iterate through all of the descriptions in the input directory.
	for filepath in glob.iglob(os.path.join(chunks_path,r"*.txt")):
		file = open(filepath)
		sentence = file.read()
		chunk_id = int(filepath.split("/")[-1].split(".")[0])
		for i in range(num_models):
			model = model_list[i]
			inferred_vector = model.infer_vector(sentence.lower().split())

			# Remember this vector and where to find it in the similarity matrix.
			index = len(vectors[i])
			vectors[i].append(inferred_vector)
			dicts[i][chunk_id] = index

	# Build the cosine similarity matrices.
	for i in range(num_models):
		matrices[i] = cosine_similarity(vectors[i])


	# Have to update this header if changing what new distance measurements are added to the file.
	df = pd.read_csv(network_filename, sep=",", dtype=str)
	revised_network= open(revised_network_filename,"w")
	new_header = ",".join(df.columns)+","+",".join(model_name_list) # remove this new_header = ",".join(df.columns)+",enwiki_dbow"
	revised_network.write(new_header+"\n")


	# Iterate through all the rows in the file, get new distances and rewrite.
	ID1_COL = 1
	ID2_COL = 2
	for row in df.itertuples():
		p1 = int(row[ID1_COL])
		p2 = int(row[ID2_COL])
		distances = []
		for i in range(num_models):
			dist = matrices[i][dicts[i][p1]][dicts[i][p2]]
			dist = round(dist,3)
			distances.append(dist)
		# Ignore the index whene converting to list.
		new_row = list(row)[1:]
		new_row.extend(distances)
		new_row = [str(x) for x in new_row]
		revised_network.write(",".join(new_row)+"\n")



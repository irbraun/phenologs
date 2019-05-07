#!/usr/bin/env python
# -*- coding: utf-8 -*-


# Code partially credited to Xing et al. (2018)
# "A gene–phenotype relationship extraction pipeline from the biomedical literature using a representation learning approach"
# Link: https://www.ncbi.nlm.nih.gov/pmc/articles/PMC6022650/



from gensim.models import word2vec
from scipy import spatial
import pandas as pd
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


LabeledSentence = gensim.models.doc2vec.LabeledSentence
reload(sys)
sys.setdefaultencoding('utf8')








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
def get_cosine_sim(*strs):
    vectors = [t for t in get_vectors(*strs)]
    similarity_matrix = cosine_similarity(vectors)
    return similarity_matrix[0][1]
def get_vectors(*strs):
    text = [t for t in strs]
    vectorizer = CountVectorizer(text)
    vectorizer.fit(text)
    return vectorizer.transform(text).toarray()









def update_networkfile_bow(training_setences_file, chunks_path, network_filename, revised_network_filename):
    """
    Function to find similarity between text chunks based on bag and set of words metrics, then update network edge file.
    Args:
        training_setences_file: full path to the text file of sentences to be used for training new models.
        chunks_path: full path to the directory that contains the text descriptions split into single files.
        network_filename: full path to the csv file which defines network edges that we want to add to.
        revisted_network_filename: the file that will be identical to the passed in one but with new edge columns.
    Returns:
        nothing
    """

    # TODO if the cosine similarity calculations are too slow on a large volume of descriptons
    # the cosine similarity function from sklearn already produces a pairwise matrix for an 
    # input of k different vectors so that could be modified to do the whole thing at once
    # instead, would just not follow the convention of how the rest of the similarities are done.

	# Dictionaries to hold the representation of each text used in calculating similarity.
	jac_dict = dict()
	cos_dict = dict()

	# Collect representations for all phenotype or phene descriptions in the dataset. 
    for filepath in glob.iglob(os.path.join(chunks_path,r"*.txt")):
        file = open(filepath)
        sentence = file.read()
        chunk_id = int(filepath.split("/")[-1].split(".")[0])
        jac_dict[chunk_id] = set(sentence.lower().split())
        cos_dict[chunk_id] = sentence.lower()

	# Have to update this header if changing what new distance measurements are added to the file.
    df = pd.read_csv(network_filename, sep=",", dtype=str)
    revised_network= open(revised_network_filename,"w")
    new_header = ",".join(df.columns)+",bow_jaccard,bow_cosine"
    revised_network.write(new_header+"\n")

    # Iterate through all the rows in the file, get new distances and rewrite.
    ID1_COL = 1
    ID2_COL = 2
    for row in df.itertuples():
        p1 = int(row[ID1_COL])
        p2 = int(row[ID2_COL])
        distances = []

        # Add the jaccard similarity between the presence/absence vectors.
        v1 = dicts[i][p1]
        v2 = dicts[i][p2]
        intersection = v1.intersection(v2)
        dist = float(len(intersection)) / (len(v1)+len(v2)-len(intersection))
        distances.append(dist)

        # Add the cosine similarity between the bag of words count vectors.
        dist = get_cosine_sim(cos_dict[p1], cos_dict[p2])
        distances.appen(dist)

        # Ignore the index whene converting to list.
        new_row = list(row)[1:]
        new_row.extend(distances)
        new_row = [str(x) for x in new_row]
        revised_network.write(",".join(new_row)+"\n")









def update_networkfile_doc(training_sentences_file, chunks_path, network_filename, revised_network_filename):
    """
    Function to find similarity between text chunks based on doc embeddings, then update a network edge file.
    Args:
        training_setences_file: full path to the text file of sentences to be used for training new models.
        chunks_path: full path to the directory that contains the text descriptions split into single files.
        network_filename: full path to the csv file which defines network edges that we want to add to.
        revisted_network_filename: the file that will be identical to the passed in one but with new edge columns.
    Returns:
        nothing
    """


    # Train models based on domain specific data or use a pre-trained model (English Wikipedia).
    # The path for the the pre-trained model is hardcoded and specified here.

    # Skipping these for now to save time and only use the best (Wikipedia) model.
    #model_domain_dmpv = train_model(training_sentences_file, "dmpv.model", dm=0, size=300)
    #model_domain_dbow = train_model(training_sentences_file, "dbow.model", dm=1, size=300)
    model_enwiki = gensim.models.Doc2Vec.load("./gensim/enwiki_dbow/doc2vec.bin")

    # (adding this in for now.)
    model_domain_dbow = model_enwiki
    model_domain_dmpv = model_enwiki


    # List of the model and corresponding list of dictionaries to store their inferred vectors in.
    model_list = [model_domain_dmpv, model_domain_dbow, model_enwiki] 
    num_models = len(model_list)
    dicts = [dict() for x in range(num_models)]

    # Infer vector embeddings for all models for all phenotype or phene descriptons in the dataset.
    for filepath in glob.iglob(os.path.join(chunks_path,r"*.txt")):
        file = open(filepath)
        sentence = file.read()
        chunk_id = int(filepath.split("/")[-1].split(".")[0])
        for i in range(num_models):
            model = model_list[i]
            inferred_vector = model.infer_vector(sentence.lower().split())
            dicts[i][chunk_id] = inferred_vector


    # TODO If processing is slow, stack the inferred vectors found above with some mapping
    # between the chunk ID for each of the vectors and the row that the vectors are being
    # placed in to keep track of them, that generates an input matrix. Then use the pair-wise
    # similarity/distance functions from sklearn to generate another matrix of values that
    # can be indexed to get the values for the new column of the network file built below.
    # Note, The way its currently done is fast enough for the <10k text descriptions.

    # Accepts an existing file specifying network two different network edges values for a list
    # of pairs of nodes. Preserve the names of the nodes and the edges that were already found
    # and adds new columns representing distances found using the sentence embeddings models for
    # either phenotype or phene descriptions whatever was used as the input datatype.

    # Have to update this header if changing what new distance measurements are added to the file.
    df = pd.read_csv(network_filename, sep=",", dtype=str)
    revised_network= open(revised_network_filename,"w")
    new_header = ",".join(df.columns)+",pubmed_dpmv,pubmed_dbow,enwiki_dbow"
    revised_network.write(new_header+"\n")


    # Define which columns hold the IDs of the phenotypes or phenes to compare.
    # Removed this, always just use the first two columns, that's how the network files are specified.

    # Iterate through all the rows in the file, get new distances and rewrite.
    ID1_COL = 1
    ID2_COL = 2
    for row in df.itertuples():
        p1 = int(row[ID1_COL])
        p2 = int(row[ID2_COL])
        distances = []
        for i in range(num_models):
            v1 = dicts[i][p1]
            v2 = dicts[i][p2]
            dist = spatial.distance.cosine(v1,v2).round(3)
            distances.append(dist)
        # Ignore the index whene converting to list.
        new_row = list(row)[1:]
        new_row.extend(distances)
        new_row = [str(x) for x in new_row]
        revised_network.write(",".join(new_row)+"\n")




#!/usr/bin/env python
# -*- coding: utf-8 -*-

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


LabeledSentence = gensim.models.doc2vec.LabeledSentence
reload(sys)
sys.setdefaultencoding('utf8')


def train_model(training_sentences_file, model_filename, dm=1, size=300):

    print "training doc2vec model"

    # Collect sentences to be used in training the model.
    labeled_data = []
    with open(training_sentences_file) as f:
        sentence_set = f.read().split('\n')
    i = 0
    for line in sentence_set:
        if line != '':
            labeled_data.append(gensim.models.doc2vec.TaggedDocument(words=gensim.utils.simple_preprocess(line), tags=['SENT'+str(i), 1]))
            i = i + 1

    if dm == 1: #dpmv
        model = gensim.models.Doc2Vec(vector_size=size, window=10, min_count=5, workers=5, alpha=0.025, min_alpha=0.025)
    elif dm == 0: #dbow
        model = gensim.models.Doc2Vec(vector_size=size, window=10, min_count=5, dm=0, workers=5, alpha=0.025, min_alpha=0.025, dbow_words=1)
    else: # any other option
        sys.exit()

    model.build_vocab(labeled_data)
    for epoch in range(10):
        print "training epoch "+str(epoch) 
        model.train(labeled_data, total_words=model.corpus_count, epochs=model.epochs)
        model.alpha -= 0.002  # decrease the learning rate
        model.min_alpha = model.alpha  # fix the learning rate, no decay
    model.save(os.join(r"./gensim/",model_filename))
    return model



# Read in the necessary arguments.
if len(sys.argv)!=5:
    sys.exit()
args = sys.argv
training_sentences_file = args[1]
chunks_path = args[2]
network_filename = args[3]
revised_network_filename = args[4]

# Train models based on domain specific data or use a pre-trained model (English Wikipedia).
# The path for the the pre-trained model is hardcoded and specified here.

# (skipping these for now.)
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

# Note: this is fast enough for the ~7000 phene descriptions.

# Accepts an existing file specifying network two different network edges values for a list
# of pairs of nodes. Preserve the names of the nodes and the edges that were already found
# and adds new columns representing distances found using the sentence embeddings models for
# either phenotype or phene descriptions whatever was used as the input datatype.
df = pd.read_csv(network_filename, sep=",", dtype=str)
revised_network= open(revised_network_filename,"w")
new_header = ",".join(df.columns)+",pubmed_dpmv,pubmed_dbow,enwiki_dbow"
revised_network.write(new_header+"\n")





# big problem! 
# those column indices are only correct for the phenotype network files.
# in the phene network files their are 4 columns of IDs and then the next 5 should be the values from the old files and distances from this modification.



# Notes about how this works.
# The phenotype/phene ID numbers which are used to index into the dict of vectors above
# always use either the phene descriptions when the phene network file is being modified
# and the phenotype descriptions when the phenotype network file is being modified.
# So in order to make this work with both 

# The file refers to a phenotype network.
if len(df.columns) == 4:
    for row in df.itertuples():
        p1 = int(row[1])
        p2 = int(row[2])
        edge_value_1 = row[3]
        edge_value_2 = row[4]
        distances = []
        for i in range(num_models):
            v1 = dicts[i][p1]
            v2 = dicts[i][p2]
            dist = spatial.distance.cosine(v1,v2).round(3)
            distances.append(dist)
        items = [str(p1),str(p2),edge_value_1,edge_value_2,str(distances[0]),str(distances[1]),str(distances[2])]
        revised_network.write(",".join(items)+"\n")

    revised_network.close()

# The file refers to a phene network.
elif len(df.columns) == 6:
    for row in df.itertuples():
        p1 = int(row[1])
        p2 = int(row[2])
        ph1 = int(row[3])
        ph2 = int(row[4])
        edge_value_1 = row[5]
        edge_value_2 = row[6]
        distances = []
        for i in range(num_models):
            v1 = dicts[i][p1]
            v2 = dicts[i][p2]
            dist = spatial.distance.cosine(v1,v2).round(3)
            distances.append(dist)
        items = [str(p1),str(p2),str(p3),str(p4),edge_value_1,edge_value_2,str(distances[0]),str(distances[1]),str(distances[2])]
        revised_network.write(",".join(items)+"\n")
        
    revised_network.close()




































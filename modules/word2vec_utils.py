#!/usr/bin/env python
# -*- coding: utf-8 -*-


# Code partially credited to Xing et al. (2018)
# "A gene–phenotype relationship extraction pipeline from the biomedical literature using a representation learning approach"
# Link: https://www.ncbi.nlm.nih.gov/pmc/articles/PMC6022650/
from gensim.models import word2vec
import logging, os, sys







def train_word2vec_models(models_path, text_training_file):
    """
    Train a set of word2vec models using a single text file representing combined abstracts of interest.
    Args:
        nothing
    Returns:
        nothing
    """

    # Specify where the directory with all the models is.
    PATH = models_path
    if not os.path.exists(PATH):
       os.makedirs(PATH)

    # Specify where the file with the combined abstracts text is.
    sentences = word2vec.Text8Corpus(text_training_file) 

    # Specify which dimension sizes should be tested when building all the models.
    for i in range(300,1000,200):
        model = word2vec.Word2Vec(sentences, size=i, sg=1, hs=1, sample=1e-3, window=10, alpha=0.025, workers=5) 
        model.save(os.path.join(PATH, str(i) + r"_size.model"))







def make_synonym_file(word_filename, model_filename, topn, output_filename):
    """
    Generates the set of pairs of words that could occur between words present in the input data
    which is represented in the input text file. Note that the output is another text file where
    each line has a pair of words or tokens separated by a colon (:) and both orderings of the 
    two words are not represented, so that needs to be checked for during use. Also word pairings
    that satisfy the threshold due to identify (same word) are not included so that needs to be 
    checked for during use of the list of word pairs.

    Args:
        word_filename: text file representing the vocabulary of interest with one word per
        model_filename: full path to the file containing the word2vec model to be used.
        topn: the maximum number of synonyms (or similar words) to be found for each word in the vocabulary.
        output_filename: where to put the resulting text file that has the lists of similar words.

    Returns:
        nothing
    """


    # Read in all the words in a text file that contains the full vocabulary with one word per line.
    word_file = open(word_filename, 'r')
    word_list = word_file.read().split('\n')
    word_file.close()

    # Version that makes sense for string-based methods like NOBLE Coder.
    model = word2vec.Word2Vec.load(model_filename)
    result = open(output_filename, "w")
    for word_in_data in word_list:
        try:
            matches = model.most_similar(word_in_data,topn=topn)
            for match in matches:
                word_in_model = match[0]
                similarity = match[1]
                # Ensure consisteny with csv format.
                word_in_data.replace(",","")
                word_in_model.replace(",","")
                result.write(word_in_data+","+word_in_model+","+str(similarity)+"\n")
        except KeyError:
            continue
        except UnicodeEncodeError:
            continue
    result.close()


    # version that makes sense for naive bayes, only looking at a values between words in the data.
    '''
    pairs = []
    model = word2vec.Word2Vec.load(model_filename)
    result = open(output_filename, "w")
    for i in range(0,len(word_list)-1000):
        w1 = word_list[i]
        #print w1
        for j in range(i,len(word_list)):
            w2 = word_list[j]
            try:
                s = model.similarity(w1,w2)
                if s >= threshold and w1!=w2:
                    pair_str = w1+":"+w2
                    pairs.append(pair_str)
            except KeyError:
                continue
            except UnicodeEncodeError:
                continue

    for pair in pairs:
        result.write(pair)
        result.write("\n")
    result.close()
    '''














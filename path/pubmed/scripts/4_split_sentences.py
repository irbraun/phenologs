# coding:utf-8
"""
@author: hh
label the positive and negtive sentence and train doc2vec model
"""

"""
What does this one need?
the weakly supervised training data in the form of three text files
each has one sentence per line, the sentences are either ones that 
contain a phenotype description, don't contain a phenotype descripiton,
or haven't looked at it.
"""



"""
Thinking about the logic of how the search is done on the word level.
Noun phrases containing words which appear in high frequency in the seed set are 
concatenated into single words. But then we're using the seed set itself to try
and identify the top matches after generating word embeddings for the entire set
of abstracts. So then why are there embeddings for the seed set words at all, are 
those only learned when they are not part of a larger noun phrase? Seems weird.

What are the alternatives to doing it this way?

A. skip the abstract modification step (adding underscores) entirely, and then 
just learn vectors for the unmodified words in all the abstracst, then search using
the seed set for the most similar words. Then could follow this by THEN doing the 
parsing, and taking the noun phrases that contain that expanded set of words to
generated the large set of PHE tokens.

Might depend on exactly how the final set of phenotype descriptions are used.
Have to work with that and see if this winds up making more sense.

"""



import re
import os
import sys
import gensim
import logging
import numpy
import glob
import string
import urllib
import nltk

reload(sys)
sys.setdefaultencoding('utf8')


PATH = r'./pubmed/'
TRAIT_FILE = 'split_sentences.txt'

if not os.path.exists(PATH):
    os.makedirs(PATH)

split_sentences_file = open(os.path.join(PATH, TRAIT_FILE), 'w+')

abstract_files = glob.glob(r'./pubmed/abstracts_split/*.txt') # using split now instead of modified, would change to modified when using the underscores.
for filename in abstract_files:
    abstract_file = open(filename, 'r')
    raw_text_block = abstract_file.read()
    sentence_list = nltk.sent_tokenize(raw_text_block)
    for sentence in sentence_list:
    	split_sentences_file.write(sentence + '\n\n')    # have to add the period that was used as a delimiter back in.

    abstract_file.close()

split_sentences_file.close()

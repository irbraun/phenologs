#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
Train word2vec model
"""

from gensim.models import word2vec
import logging,os

PATH = r'./gensim/'
if not os.path.exists(PATH):
   os.makedirs(PATH)


logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.INFO)
sentences = word2vec.Text8Corpus(r"./pubmed/combined_abstracts.txt") 

for i in range(300,1000,200):
    model = word2vec.Word2Vec(sentences, size=i, sg=1, hs=1, sample=1e-3, window=10, alpha=0.025, workers=5) 
    model.save(os.path.join(PATH, str(i) + r"_size.model"))

if __name__ == "__main__":
    pass

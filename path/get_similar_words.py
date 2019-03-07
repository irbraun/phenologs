#!/usr/bin/env python
# -*- coding: utf-8 -*-

from gensim.models import word2vec
import logging, os, sys


'''
Generates the set of pairs of words that could occur between words present in the input data
which is represented in the input text file. Note that the output is another text file where
each line has a pair of words or tokens separated by a colon (:) and both orderings of the 
two words are not represented, so that needs to be checked for during use. Also word pairings
that satisfy the threshold due to identify (same word) are not included so that needs to be 
checked for during use of the list of word pairs.

python embed.py train_words.txt /Users/irbraun/Desktop/droplet/phenotype-search/search1_word_embeddings/300_size.model 0.6 ./pairs.txt

'''
if len(sys.argv)!=5:
    sys.exit()
args = sys.argv
word_filename = args[1]
model_filename = args[2]
topn = int(args[3])
output_filename = args[4]

logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.INFO)

word_file = open(word_filename, 'r')
word_list = word_file.read().split('\n')
word_file.close()


# version that makes sense for naive bayes, only looking at a values between words in the data.

# note threshold is not defined anymore, would have to change that.

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







# Version that makes sense for string-based methods like noble coder.
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
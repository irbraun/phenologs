# coding:utf-8
'''
@author: hh
Clean the abstracts and make them in one file
'''


import io



import string
import urllib
import sys, os, glob, re
import nltk
nltk.download('punkt')   # had to add this.


import codecs
from nltk.corpus import stopwords
from nltk.stem import WordNetLemmatizer
reload(sys)
sys.setdefaultencoding('utf8')



# using this on abstracts_split instead of abstracts_modified for now, because ignoring the parsing and underscore thing.
abstract_files = glob.glob(r'./pubmed/abstracts_split/*.txt')



"""
This should take all the abstracts that have been modified and put them into
a single text file in preparation for input into the word2vec algorithm.
"""




def token_file(file):
    try:
        token_file = nltk.word_tokenize(file)
    except UnicodeDecodeError:
        return []
    return token_file


#filename = "All_articleNP_v1.txt"
filename = "./pubmed/combined_abstracts.txt"
encoding = "utf-8"


article = open(filename, 'w')
list_sign = [' (', ') ', '; ', ', ', '. ', '! ', '?',':']
for filename in abstract_files:
    token_list = []
    abstract_file = open(filename, 'r')
    lines = abstract_file.read()
    abstract_file.close()
    lines_change = lines 

    # Adding this?
    #line_change=lines_change.decode('utf-8','ignore').encode("utf-8")

    for i in list_sign:
        lines_change = lines_change.replace(i, ' ')
    lines_change = lines_change.replace('(', '') 
    lines_change = lines_change.replace(')', '')
    tokens = token_file(lines_change)  
    if len(tokens)>=1:
        for token in tokens:
            tocken_change = token
            tocken_change = re.sub('^[\d.]+$', 'NBR', tocken_change)  # Relace number to 'NBR '
          
            try:
                if tocken_change == 'NBR':
                    token_list.append(tocken_change)
                else:
                    token_list.append(tocken_change.lower())
            except UnicodeDecodeError:
                continue
    #article.write(' '.join(token_list) + '\n')
    # Had to change this from the above to not get the encoding errors
    # when training the word2vec model.
    s = ' '.join(token_list) + '\n'
    s = s.decode('utf-8','ignore').encode("utf-8")
    article.write(s)
    print "processing down..........", filename
article.close()
        
















    
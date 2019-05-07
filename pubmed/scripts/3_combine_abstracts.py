import io
import string
import urllib
import sys, os, glob, re
import nltk
nltk.download('punkt')

import codecs
from nltk.corpus import stopwords
from nltk.stem import WordNetLemmatizer
reload(sys)
sys.setdefaultencoding('utf8')



# Read in the arguments to identify where all the literature query information is.
parser = argparse.ArgumentParser()
parser.add_argument("--dir", dest="directory", required=True)
args = parser.parse_args()
pubmed_directory = args.directory



# Collect all of the text files which are in the split abstracts directory.
split_abstracts_dir = os.path.join(pubmed_directory,"abstracts_split")
glob_string = os.path.join(split_abstracts_dir,r"*.txt")
abstract_files = glob.glob(glob_string)





# Following code credited to Xing et al. (2018)
# "A gene–phenotype relationship extraction pipeline from the biomedical literature using a representation learning approach"
# Link: https://www.ncbi.nlm.nih.gov/pmc/articles/PMC6022650/

# This should take all the abstracts that have been split and put them into
# a single text file in preparation for input into the word2vec algorithm.


def token_file(file):
    try:
        token_file = nltk.word_tokenize(file)
    except UnicodeDecodeError:
        return []
    return token_file


filename = os.path.join(pubmed_directory,"combined_abstracts.txt")
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
    # Had to change this from the above to not get the encoding errors when training the word2vec model.
    s = ' '.join(token_list) + '\n'
    s = s.decode('utf-8','ignore').encode("utf-8")
    article.write(s)
    print "processing down..........", filename
article.close()
        



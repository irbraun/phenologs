import re
import sys
import os
import string
import urllib
from utils import get_lit_query_options




reload(sys)
sys.setdefaultencoding('utf8')




# Read in the arguments to construct this literature query.
parser = argparse.ArgumentParser()
parser.add_argument("--name", dest="query_name", required=True)
parser.add_argument("--db", dest="database", required=True)
parser.add_argument("--limit", dest="query_limit", required=True)
parser.add_argument("--dir", dest="directory", required=True)
args = parser.parse_args()
search_name = args.query_name
db = args.database
max_article_number = args.query_limit
pubmed_directory = args.directory





# Following code credited to Xing et al. (2018)
# "A gene–phenotype relationship extraction pipeline from the biomedical literature using a representation learning approach"
# Link: https://www.ncbi.nlm.nih.gov/pmc/articles/PMC6022650/


file_dir = os.path.join(pubmed_directory, search_name + '_' + db + '_lim' + str(max_article_number))
filename = os.path.join(file_dir, 'article.txt')

f = open(filename, 'r+')
raw_txt = f.read()
f.close()

id_list = re.findall(r'\n\d+\.[\w\W]+?PMID: \d+', raw_txt, re.IGNORECASE)


split_abstracts_dir = os.path.join(pubmed_directory,"abstracts_split")
if not os.path.exists(split_abstracts_dir):
    os.makedirs(split_abstracts_dir)

for id in id_list:
    pmid_match = re.search(r'PMID: (\d+)', id)
    if pmid_match:
        pmid = pmid_match.group(1)
    else:
        print 'Cannot find pmid.'
        continue

    temp_list = id.split('\n\n')
    print 'Finding abstract of', pmid
    for temp in temp_list:
        if len(temp) > 200 and (not temp.startswith('Author information:')) and temp.find('©') == -1:
            print '  save PMID:', str(pmid)
            filename = os.path.join(split_abstracts_dir, str(pmid)+'.txt')
            f = open(filename, 'w+')
            temp_save = temp.replace(' \n', ' ')
            temp_save = temp_save.replace('\n', ' ')
            f.write(temp_save+' ')
            f.close()

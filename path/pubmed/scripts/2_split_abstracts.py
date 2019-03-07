import re
import sys
import os
import string
import urllib
from utils import get_lit_query_options

reload(sys)
sys.setdefaultencoding('utf8')

search_name, db, max_article_number = get_lit_query_options(sys.argv)

file_dir = os.path.join(r'./pubmed/', search_name + '_' + db + '_lim' + str(max_article_number))
filename = os.path.join(file_dir, 'article.txt')

f = open(filename, 'r+')

raw_txt = f.read()
f.close()

id_list = re.findall(r'\n\d+\.[\w\W]+?PMID: \d+', raw_txt, re.IGNORECASE)

file_dir = './pubmed/abstracts_split'
if not os.path.exists(file_dir):
    os.makedirs(file_dir)

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
            filename = os.path.join(file_dir, str(pmid)+'.txt')
            f = open(filename, 'w+')
            temp_save = temp.replace(' \n', ' ')
            temp_save = temp_save.replace('\n', ' ')
            f.write(temp_save+' ')
            f.close()

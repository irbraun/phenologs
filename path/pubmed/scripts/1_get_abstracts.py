import re
import sys
import os
import string
import urllib
from utils import get_lit_query_options

reload(sys)
sys.setdefaultencoding('utf8')

keywords = []
keywords.append('arabidopsis')
keywords.append('maize')
keywords.append('solanum lycopersicum')
keywords.append('oryza sativa')
keywords.append('medicago truncatula')
keywords.append('glycine max')

query = " OR ".join(keywords)
search_name, db, max_article_number = get_lit_query_options(sys.argv)

def reporthook(blocks_read, block_size, total_size):
    if not blocks_read:
        print 'Connection opened'
        return
    if total_size < 0:
        sys.stdout.write('Read %d blocks (%d bytes)' % (blocks_read, blocks_read * block_size))
        sys.stdout.write('\r')
    else:
        amount_read = blocks_read * block_size
        sys.stdout.write('Read %d blocks,  %d/%d, %.0f%%' % (blocks_read, amount_read, total_size, amount_read*100.0/(total_size)))
        sys.stdout.write('\r')
    return


if __name__ == '__main__' :
    print 'query:', query
    print 'database:', db
    print 'the max number of downloaded articles:', max_article_number
    file_dir = os.path.join(r'./pubmed/', search_name + '_' + db + '_lim' + str(max_article_number))
    if not os.path.exists(file_dir):
        os.makedirs(file_dir)
    id_filename = os.path.join(file_dir, 'id.xml')
    query_filename = os.path.join(file_dir, 'query_string.txt')

    #
    if max_article_number == 0:
        print 'retrieving all matching articles'
        id_url = r'http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=' + db + '&term=' + query + r'&retmax=' + str(max_article_number) + '&usehistory=y'
        ufile = urllib.urlopen(id_url)
        html = ufile.read()
        count_match = re.search(r'<Count>(.*?)</Count>', html, re.IGNORECASE)
        if count_match:
            max_article_number = count_match.group(1)
        else:
            print 'cannot find the total number of the article'
    print 'max article number is', str(max_article_number)

    # Download an xml file of the id list.
    id_url = r'http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=' + db + r'&term=' + query + r'&retmax=' + str(max_article_number) + r'&usehistory=y'
    urllib.urlretrieve(id_url, id_filename)
    f = open(id_filename, 'r+')
    id_xml = f.read()
    f.close()

    # Save a text file describing the exact query that was used.
    f = open(query_filename, 'w+')
    f.write(query)
    f.close()


    # Download abstracts.
    if db == 'pubmed':
        # find the QueryKey and WebEnv
        QueryKey_match = re.search(r'<QueryKey>(.*?)</QueryKey>', id_xml, re.IGNORECASE)
        WebEnv_match = re.search(r'<WebEnv>(.*?)</WebEnv>', id_xml, re.IGNORECASE)
        if QueryKey_match and WebEnv_match:
            QueryKey = QueryKey_match.group(1)
            WebEnv = WebEnv_match.group(1)
        else:
            print 'Cannot find QueryKey or WebEnv.'
        result = ''
        if int(max_article_number) > 10000:
            retmax = '10000'
        else:
            retmax = str(max_article_number)
        # download the abstract
        for retstart in range(0, int(max_article_number), 10000):
            print 'Downloading', str(retstart), 'to', str(retstart+10000), 'article....'

            article_url = r'http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=' + db + r'&query_key=' + QueryKey + r'&WebEnv=' + WebEnv + r'&retmax=' + retmax + r'&rettype=fasta&retmode=text&retstart=' + str(retstart)
            article_filename = os.path.join(file_dir, 'article_' + str(retstart) + '.txt')
            urllib.urlretrieve(article_url, article_filename, reporthook)
            f = open(article_filename, 'r+')
            result += f.read()
            f.close()
        # combine the abstract
        print '\nCombining...'
        result_filename = os.path.join(file_dir, 'article.txt')
        f = open(result_filename, 'w+')
        f.write(result)
        f.close()

    # Download whole articles.
    elif db == 'pmc':
        #read downloaded id number
        downloaded_id_filename = os.path.join(file_dir, 'downloaded_id.txt')
        if os.path.exists(downloaded_id_filename):
            f = open(downloaded_id_filename, 'r+')
            downloaded_list = f.read().split('\n')
            f.close()
        else:
            downloaded_list = []
        # find the id number
        id_list = re.findall('<Id>(.*?)</Id>', id_xml, re.IGNORECASE)
        for i in range(len(id_list)):
            # download the article by each id
            article_url = r'https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pmc&id=' + id_list[i]
            article_filename = os.path.join(file_dir, id_list[i] + '.xml')
            if id_list[i] not in downloaded_list:
                urllib.urlretrieve(article_url, article_filename, reporthook)
                downloaded_list.append(id_list[i])
            print str(i+1)+'/'+str(len(id_list)) + '    ' + id_list[i], 'finished downloading......'
            #save the downloaded id
            f = open(downloaded_id_filename, 'w+')
            f.write('\n'.join(downloaded_list))
            f.close()
    else:
        print 'Database not supported.'


    print 'Done.'

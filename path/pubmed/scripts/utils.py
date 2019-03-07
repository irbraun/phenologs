import sys







"""
The name of the search is arbitrary just for naming the directory the results are in.
The value of database should be 'pubmed' for abstracts or 'pmc' for full articles.
The value of the query limit should be the maximum number of articles or abstracts
to be downloaded. Use a value of '0' to specify that all the matching articles or 
abstracts should be downloaded (slow).
"""
def get_lit_query_options(args):
	if len(args)<4:
		print "provide search name, database, and query limit."
		sys.exit()
	search_name = args[1]
	db = args[2]
	max_article_number = args[3]
	return search_name, db, max_article_number
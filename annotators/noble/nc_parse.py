from collections import defaultdict
import pandas as pd
import sys

# arguments
if (len(sys.argv)!=4):
	print "wrong number of arguments were provided to nc_parse.py"
	sys.exit()

input_filename = sys.argv[1] 	# tsv file which is the output from noble coder.
output_filename = sys.argv[2] 	# csv file which will be in the input to the jar.
default_prob = sys.argv[3] 		# the default score value to use for matches found with these settings.

df = pd.read_csv(input_filename, usecols=["Document", "Matched Term", "Code"], sep="\t")

outfile = open(output_filename,"w")
outfile.write("chunk,term,prob,nodes\n")

# keep track of which terms are already added to the annotations for which chunks.
used = defaultdict(set)

for row in df.itertuples():

	chunkDocument = row[1]
	chunkID = int(chunkDocument.split('.')[0].split('_')[0])
	nodes = row[2].replace(' ','|')
	termID = row[3]
	probability = default_prob
	# dont add duplicate terms for one chunk to the processed output file.
	if termID not in used[chunkID]:
		outfile.write(str(chunkID)+","+termID+","+str(probability)+","+nodes+"\n")
		used[chunkID].add(termID) # remember that this term has been annotated to this chunk.

outfile.close()


import urllib2
import json
import os
import sys
import os.path
from pprint import pprint
from time import sleep

REST_URL = "http://data.bioontology.org"
API_KEY = "73bfd890-f082-4f20-82ec-0d8cd56c3cd4"



def get_json(url):
	opener = urllib2.build_opener()
	opener.addheaders = [('Authorization', 'apikey token=' + API_KEY)]
	return json.loads(opener.open(url).read())



def print_annotations(annotations, get_class=True):
	for result in annotations:
		class_details = get_json(result["annotatedClass"]["links"]["self"]) if get_class else result["annotatedClass"]
		print "Class details"
		print "\tid: " + class_details["@id"]
		print "\tprefLabel: " + class_details["prefLabel"]
		print "\tontology: " + class_details["links"]["ontology"]

		print "Annotation details"
		for annotation in result["annotations"]:
			print "\tfrom: " + str(annotation["from"])
			print "\tto: " + str(annotation["to"])
			print "\tmatch type: " + annotation["matchType"]

		if result["hierarchy"]:
			print "\n\tHierarchy annotations"
			for annotation in result["hierarchy"]:
				class_details = get_json(annotation["annotatedClass"]["links"]["self"])
				pref_label = class_details["prefLabel"] or "no label"
				print "\t\tClass details"
				print "\t\t\tid: " + class_details["@id"]
				print "\t\t\tprefLabel: " + class_details["prefLabel"]
				print "\t\t\tontology: " + class_details["links"]["ontology"]
				print "\t\t\tdistance from originally annotated class: " + str(annotation["distance"])

		print "\n\n"



def write_to_csv(outfiles, ontologies, chunkID, annotations, sentence, get_class=True):
	for result in annotations:
		class_details = get_json(result["annotatedClass"]["links"]["self"]) if get_class else result["annotatedClass"]
		termID = class_details["@id"]

		# Drop the obo url from the term ID for consistent naming.
		termID = termID.split('/')[-1]

		# Make sure it the number includes all seven digits for consistency.
		correct_num_digits = 7
		number = termID.split('_')[1]
		name = termID.split('_')[0]
		num_digits = len(number)
		for i in range(0,correct_num_digits-num_digits,1):
			number = "0"+number
		termID = name+"_"+number

		probability = 1.00
		matching_words = []

		# Get the tokens from the original text that caused this match.
		for annotation in result["annotations"]:
			matching_words.append(sentence[int(annotation["from"]-1):int(annotation["to"])].replace(" ","|"))
		nodes = "|".join(matching_words)

		# Write the line to the correct file and only if this ontology applies.
		for o in ontologies:
			if o.lower() in termID.lower():
				outfiles[o].write(str(chunkID)+","+termID+","+str(probability)+","+nodes+"\n")
				break







# Usage example: 'python ncbo_annot.py phenotypes_results x y'
# where x and y specify the range of chunk IDs to be sent to the rest API for annotaiton.
# where the output annotation files will be named like phenotype_results.csv.

# Where should the parent directory for the output files be?
base_output_directory="/Users/irbraun/NetBeansProjects/term-mapping/path/annotators/ncbo/"

# Where are the text files that have the phenotypes or phenes in them?
directory = "/Users/irbraun/NetBeansProjects/term-mapping/path/data/split_chunks/"



# Read in arguments and create output csv files.
base_name = sys.argv[1]
ontologies = ["pato","po","go","chebi"]
outfiles = {}
for o in ontologies:
	filename_part1 = "output_"+o
	filename_part2 = base_name+".csv"
	filename = os.path.join(filename_part1,filename_part2)

	# Only write the header in cases where the file doesn't exist already.
	if os.path.exists(base_output_directory+filename):
		outfile = open(base_output_directory+filename,"a")
	else:
		outfile = open(base_output_directory+filename,"a")
		outfile.write("chunk,term,prob,nodes\n")
	outfiles[o] = outfile



# These are specified as inclusive.
lower_id = int(sys.argv[2])
upper_id = int(sys.argv[3])
chunk_range = range(lower_id, upper_id+1)


filename_list = []
for i in chunk_range:
	filename_list.append(str(i)+".txt")

wait_after = 10
ctr = 0
files_present_in_dir = os.listdir(directory)

for filename in filename_list:
	if os.path.exists(directory+filename):

		# Get the chunk ID from the filename.
		chunkID = int(filename.split('.')[0]) # get the chunk ID from the filename
		if chunkID in chunk_range:

			# Open and read the text file.
			path = directory+filename
			with open(path, 'r') as file:
				text_to_annotate=file.read().replace('\n', '')
				annotations = get_json(REST_URL + "/annotator?ontologies=PATO,GO,PO,CHEBI&text=" + urllib2.quote(text_to_annotate))
				write_to_csv(outfiles, ontologies, chunkID, annotations, text_to_annotate)
				ctr = ctr+1


	if ctr%wait_after == 0:
		print ctr
		sleep(1.0)

for k,v in outfiles.items():
	v.close()






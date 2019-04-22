import sys



# Generates slurm submit scripts for learn.R



# Parameters specified on the command line.
base_dir = sys.argv[1] # (with trailing /)
r_script_name = base_dir+sys.argv[2]
ontology = sys.argv[3]
k = sys.argv[4]
num_features = sys.argv[5]
output_root = base_dir+sys.argv[6]
data_path = base_dir+sys.argv[7]
submit_filename = sys.argv[8] #(without extension)
num_training_files = sys.argv[9]
trainpart_lower = sys.argv[10]
trainpart_upper = sys.argv[11]
num_testing_files = sys.argv[12]
testpart_lower = sys.argv[13]
testpart_upper = sys.argv[14]
split_postfix = sys.argv[15]



# Hardcoded parameters that could be changed if necessary.
data_name = "features"

module_names = ["r/3.4.3-py2-kaltwmm",
	"r-caret/6.0-73-py2-r3.4-x4wqfe7",
	"r-dplyr/0.7.3-py2-r3.4-p2gyosx",
	"r-tidyr/0.5.1-py2-r3.4-fv6p25a",
	"r-randomforest/4.6-12-py2-r3.4-2biqljo",
	"r-e1071/1.6-7-py2-r3.4-ihpogov"]

walltime = "72:00:00"
jobname = submit_filename
email = "irbraun@iastate.edu"
output_filename = "%s_output.txt"%submit_filename





# Location to dump any created submit scripts is hardcoded.
dump_directory = "/Users/irbraun/NetBeansProjects/term-mapping/slurm/submits_dump/"

# Specify the slurm parameters.
f=open(dump_directory+submit_filename+".sub","w+")
f.writelines(["#!/bin/bash\n", 
	"#Submit this script with: sbatch thefilename\n", 
	"#SBATCH -t %s   # walltime\n"%walltime,
	"#SBATCH -N 1   # number of nodes in this job\n",
	"#SBATCH -n 16   # total number of processor cores in this job\n",
	"#SBATCH -J \"%s\"   # job name\n"%jobname,
	"#SBATCH --mail-user=irbraun@iastate.edu   # email address\n",
	"#SBATCH --mail-type=BEGIN\n",
	"#SBATCH --mail-type=END\n",
	"#SBATCH --mail-type=FAIL\n",
	"#SBATCH --output=%s.out\n"%(jobname)])

# Loading required modules.
f.write("\n")
f.write("\n")
for module in module_names:
	f.write("module load %s\n"%module)

# Writing the rest of the stuff
f.write("\n")
f.write("\n")
f.write("Rscript %s %s %s %s %s %s " % (r_script_name, ontology, k, num_features, output_root, num_training_files))
for part in range(int(trainpart_lower),int(trainpart_upper)+1):
	f.write("%s%s.%d.csv "%(data_path,data_name,part))
f.write("%s " % num_testing_files)
for part in range(int(testpart_lower),int(testpart_upper)+1):
	f.write("%s%s.%d.csv "%(data_path,data_name,part))
f.write(split_postfix)
f.close()
import sys

# Generates slurm submit scripts for learn_otherprobs_joint.R


# Parameters specified on the command line.
base_dir = sys.argv[1] # (with trailing /)
data_path = base_dir+sys.argv[2]
output_root = base_dir+sys.argv[3]
r_script_name = base_dir+sys.argv[4]
submit_filename = sys.argv[5] #(with no extenision)
classprobs_filename_1 = base_dir+sys.argv[6]
classprobs_filename_2 = base_dir+sys.argv[7]
joint_classprobs_id = sys.argv[8]
ontology = sys.argv[9]
num_testing_files = sys.argv[10]
testpart_lower = sys.argv[11]
testpart_upper = sys.argv[12] #inclusive
split_postfix = sys.argv[13]


# Hardcoded parameters
data_name = "features"

module_names = ["r/3.4.3-py2-kaltwmm",
	"r-caret/6.0-73-py2-r3.4-x4wqfe7",
	"r-dplyr/0.7.3-py2-r3.4-p2gyosx",
	"r-tidyr/0.5.1-py2-r3.4-fv6p25a",
	"r-randomforest/4.6-12-py2-r3.4-2biqljo",
	"r-e1071/1.6-7-py2-r3.4-ihpogov"]

walltime = "4:00:00"
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

# Load the required modules.
f.write("\n")
f.write("\n")
for module in module_names:
	f.write("module load %s\n"%module)

# Writing the rest of the stuff.
f.write("\n")
f.write("\n")
f.write("Rscript %s %s %s %s " % (r_script_name, ontology, output_root, num_testing_files))
for part in range(int(testpart_lower),int(testpart_upper)+1):
	f.write("%s%s.%d.csv "%(data_path,data_name,part))
f.write("%s %s %s %s"%(split_postfix, classprobs_filename_1, classprobs_filename_2, joint_classprobs_id))
f.close()
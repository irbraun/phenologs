#!/bin/bash
cd annotators/naive/output_pato
sed 1d testfold1_$1_eval.csv > temp1.csv
sed 1d testfold2_$1_eval.csv > temp2.csv
sed 1d testfold3_$1_eval.csv > temp3.csv
cat testfold4_$1_eval.csv temp1.csv temp2.csv temp3.csv > merged_$1_eval.csv
sed 1d testfold1_$1_classprobs.csv > temp4.csv
sed 1d testfold2_$1_classprobs.csv > temp5.csv
sed 1d testfold3_$1_classprobs.csv > temp6.csv
cat testfold4_$1_classprobs.csv temp4.csv temp5.csv temp6.csv > merged_$1_classprobs.csv

cd ../output_po
sed 1d testfold1_$1_eval.csv > temp1.csv
sed 1d testfold2_$1_eval.csv > temp2.csv
sed 1d testfold3_$1_eval.csv > temp3.csv
cat testfold4_$1_eval.csv temp1.csv temp2.csv temp3.csv > merged_$1_eval.csv
sed 1d testfold1_$1_classprobs.csv > temp4.csv
sed 1d testfold2_$1_classprobs.csv > temp5.csv
sed 1d testfold3_$1_classprobs.csv > temp6.csv
cat testfold4_$1_classprobs.csv temp4.csv temp5.csv temp6.csv > merged_$1_classprobs.csv

cd ../output_go
sed 1d testfold1_$1_eval.csv > temp1.csv
sed 1d testfold2_$1_eval.csv > temp2.csv
sed 1d testfold3_$1_eval.csv > temp3.csv
cat testfold4_$1_eval.csv temp1.csv temp2.csv temp3.csv > merged_$1_eval.csv
sed 1d testfold1_$1_classprobs.csv > temp4.csv
sed 1d testfold2_$1_classprobs.csv > temp5.csv
sed 1d testfold3_$1_classprobs.csv > temp6.csv
cat testfold4_$1_classprobs.csv temp4.csv temp5.csv temp6.csv > merged_$1_classprobs.csv

cd ../output_chebi
sed 1d testfold1_$1_eval.csv > temp1.csv
sed 1d testfold2_$1_eval.csv > temp2.csv
sed 1d testfold3_$1_eval.csv > temp3.csv
cat testfold4_$1_eval.csv temp1.csv temp2.csv temp3.csv > merged_$1_eval.csv
sed 1d testfold1_$1_classprobs.csv > temp4.csv
sed 1d testfold2_$1_classprobs.csv > temp5.csv
sed 1d testfold3_$1_classprobs.csv > temp6.csv
cat testfold4_$1_classprobs.csv temp4.csv temp5.csv temp6.csv > merged_$1_classprobs.csv

cd ../../..

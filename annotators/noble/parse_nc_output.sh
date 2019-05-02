#!/bin/bash
python nc_parse.py outputs_all_ppn_pato/RESULTS.tsv outputs_all_ppn_pato/original_classprobs_pato.csv
python nc_parse.py outputs_all_ppn_po/RESULTS.tsv outputs_all_ppn_po/original_classprobs_po.csv
python nc_parse.py outputs_all_ppn_go/RESULTS.tsv outputs_all_ppn_go/original_classprobs_go.csv
#python nc_parse.py outputs_test_ppn_chebi/RESULTS.tsv outputs_test_ppn_chebi/original_classprobs.csv

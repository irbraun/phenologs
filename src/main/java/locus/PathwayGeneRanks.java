
package locus;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import objects.Chunk;
import text.Text;


public class PathwayGeneRanks {
    
    
    
    public static void rankGenes() throws Exception{
        
        
        

        Text text = new Text();
        
     
        
        
        /** Step 1) Find which genes from a large list are in the data, which is input to R script. **/
        lookForGenesInData(text, "/Users/irbraun/Desktop/big_gene_list.txt");
        
        
        // Then copy and paste resulting genes and corresponding phenotype IDs into the R script to get query specific files.
        // Copy and paste the list of genes into a text file (gene_list.txt) so that it can be used to references files made by the R script.
        
        
        /** Step 2) Generate a table of ranked genes from files produced by the R script. **/
        String inputGeneList = "/Users/irbraun/Desktop/gene_list.txt";
        
        // Read the list of gene IDs that are expected to be in this pathway or regulatory network or related.
        BufferedReader reader_gene_list = new BufferedReader(new FileReader(inputGeneList));
        HashSet<String> pathwayGeneIDs = new HashSet<>();
        String line;
        while ((line = reader_gene_list.readLine()) != null){
            pathwayGeneIDs.add(line.trim());
        }
        

        // Set up the output table.
        PrintWriter printer = new PrintWriter("/Users/irbraun/Desktop/big_table.csv");

        String header = "gene, dtype, method, bin_10, bin_100, bin_inf";
        printer.println(header);
        
        
        // Create a list of the files created for each query.
        String phenotypeTextFileTemplate = "/Users/irbraun/Desktop/pathway_membership_files/phenotype_text_%s.csv";
        String pheneTextFileTemplate = "/Users/irbraun/Desktop/pathway_membership_files/phene_text_%s.csv";
        for (String geneID: pathwayGeneIDs){
            // Define where the input network files should be for just this query.
            String phenotypeTextFilename = String.format(phenotypeTextFileTemplate, geneID);
            String pheneTextFilename = String.format(pheneTextFileTemplate, geneID);
            // Run the process for a single query gene on each data type.
            rankWithTextFile(text, phenotypeTextFilename, pathwayGeneIDs, "Phenotype Descriptions", geneID, printer);
            rankWithTextFile(text, pheneTextFilename, pathwayGeneIDs, "Phene Descriptions", geneID, printer);
        
        }
        printer.close();
        
    }
    
    
    private static void rankWithTextFile(Text text, String inputPath, HashSet<String> pathwayGeneIDs, String dtype, String queryGene, PrintWriter printer) throws FileNotFoundException, IOException{
        
        // Define the bins to be used for assessing gene ranks.
        ArrayList<Integer> binMaxValues = new ArrayList<>();
        binMaxValues.add(10);
        binMaxValues.add(100);
        binMaxValues.add(10000);
        
        
        

        // File produced by the R script that contains all the 
        BufferedReader reader = new BufferedReader(new FileReader(inputPath));
        String line;
        String delimiter = ",";
        

        // Read the header of file file produced by the R script and map column positions to gene accession lists.
        HashMap<Integer, ArrayList<String>> methodToGeneListMap = new HashMap<>();
        String methodNamesHeader = reader.readLine();
        String[] methodNamesArray = methodNamesHeader.split(delimiter);
        int numMethods = methodNamesArray.length;
        for (int idx=0; idx<numMethods; idx++){
            methodToGeneListMap.put(idx, new ArrayList<>());
        }
        

        
        
        // Populate the map of ordered lists that contain the ranked genes for each method.
        while ((line = reader.readLine()) != null)
        {
            String[] lineValues = line.split(delimiter);
            for (int idx=0; idx<lineValues.length; idx++){
                String geneID = text.getPhenotypeChunkFromID(Integer.valueOf(lineValues[idx])).geneIdentifier;
                if (!methodToGeneListMap.get(idx).contains(geneID)){
                    methodToGeneListMap.get(idx).add(geneID);
                }
            }
        }
        reader.close();
        
        
        
        
        // Check the mean and mode ranks of each gene known to be in the pathway for each method.
        for (int idx=0; idx<numMethods; idx++){
            String methodName = methodNamesArray[idx];
            ArrayList<Integer> foundRanks = new ArrayList<>();
            for (String geneID: pathwayGeneIDs){
                int rank = methodToGeneListMap.get(idx).indexOf(geneID)+1;
                if (rank != 0){
                    foundRanks.add(rank);
                }
            }
            
            
            // Bin the ranks that were found.
            int binMin = 1;
            int[] binCounts = new int[3];
            for (int i=0; i<binCounts.length; i++){
                for (Integer r: foundRanks){
                    if (r>=binMin && r<binMaxValues.get(i)){
                        binCounts[i]++;
                    }
                }
                binMin = binMaxValues.get(i);
            }      
                    
            // Produce a line for the output table.
            Object[] data = {queryGene, dtype, methodName, binCounts[0], binCounts[1], binCounts[2]};
            printer.println(String.format("%s,%s,%s,%s,%s,%s", data));
            
        }
        
    }
    
    
    
    
    
    private static void lookForGenesInData(Text text, String inputPath) throws FileNotFoundException, IOException, SQLException{
        // Read the list of gene IDs that are expected to be in this pathway or regulatory network or related.
        String inputGeneList = inputPath;
        BufferedReader reader_gene_list = new BufferedReader(new FileReader(inputGeneList));
        HashSet<String> pathwayGeneIDs = new HashSet<>();
        HashMap<String,String> idToName = new HashMap<>();
        HashSet<String> originalReferences = new HashSet<>();
        HashSet<String> foundGeneIDs = new HashSet<>();
        String line;
        String currentName = "";
        while ((line = reader_gene_list.readLine()) != null){
        switch (line.substring(0,1)) {
            case "#":
                currentName = line.substring(1, line.length());
                break;
            case "*":
                pathwayGeneIDs.add(line.substring(1,line.length()).trim());
                idToName.put(line.substring(1,line.length()).trim(), currentName);
                originalReferences.add(line.substring(1,line.length()).trim());
                break;
            default:
                pathwayGeneIDs.add(line.trim());
                idToName.put(line.trim(),currentName);
                break;
        }

        }
        System.out.println("List of found genes in querying format: \n\n");
        for (String geneID: pathwayGeneIDs){
            HashSet<Integer> atomIDs = new HashSet<>();
            for (Chunk c: text.getAllAtomChunks()){
                if (c.geneIdentifier.trim().equals(geneID.trim())){
                    atomIDs.add(c.chunkID);
                }
            }
            HashSet<Integer> phenotypeIDs = new HashSet<>();
            for (Integer atomID: atomIDs){
                phenotypeIDs.add(text.getPhenotypeIDfromAtomID(atomID));
            }
            if (!phenotypeIDs.isEmpty()){
                foundGeneIDs.add(geneID);
                StringBuilder sb = new StringBuilder();
                for (Integer s: phenotypeIDs){
                    sb.append(",");
                    sb.append(s.toString());
                }
                String original = "";
                if (originalReferences.contains(geneID)){
                    original = "*";
                }
                System.out.println(String.format("c(\"%s\" %s)\t\t\t\t# %s %s", geneID, sb.toString(), idToName.get(geneID), original));
            }
        }
        
        System.out.println("\n\nList of found genes in txt file format: \n\n");
        for (String geneID: foundGeneIDs){
            System.out.println(geneID);
        }
        
    }
    
    
    
    
    
    
}

/*
 * Ian Braun
 * irbraun@iastate.edu
 * term-mapping 
 */
package composer;

import enums.Ontology;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author irbraun
 */
public class ComposerIO {
    
    
    
    
    
    
    
    
    /**
     * This methods takes a list of class probability files which must have the columns (chunk ID, term ID,
     * probability) and can have an optional fourth column (nodes) depending on the method that was used to
     * generate them. Returns a map from chunk ID to a list of the top k candidate terms that match that 
     * text as identified by whatever methods were used to generate the files. The value of k is specified
     * in this method. The files can contain terms form multiple or just one ontology. So if the best k GO 
     * terms are desired, only pass in GO files. If the best k entity terms in general are required, pass in
     * files from all ontologies from which entities can be drawn.
     * @param filePaths
     * @param k
     * @return
     * @throws FileNotFoundException 
     */
    public static HashMap readClassProbFiles(List<String> filePaths, int k) throws FileNotFoundException{
        
        // The mapping from chunk ID's to lists of candidate terms.
        HashMap<Integer,ArrayList<Term>> chunkToTermsMap = new HashMap<>();
        
        for (String filename: filePaths){
            File classProbFile = new File(filename);
            Scanner scanner = new Scanner(classProbFile);
            scanner.useDelimiter(",");
            // Account for the header in the class probability file.
            scanner.nextLine();
            while(scanner.hasNextLine()){
                String line = scanner.nextLine();
                String[] lineValues = line.split(",");
                
                // Get the values from each data line in the class probabilities file.
                String chunkIDStr = lineValues[0].replace("\"", "").trim(); // (R scripts might add quotes to csv files).
                int chunkID = Integer.valueOf(chunkIDStr);
                String termID = lineValues[1].replace("\"","");
                double probability = Double.valueOf(lineValues[2]);
                String nodes = "";
                if (lineValues.length > 3){
                    nodes = lineValues[3].trim();
                }
                
                // Some of the methods allow terms from separate vocabularies into the output files.
                if (!utils.Util.inferOntology(termID).equals(Ontology.UNSUPPORTED)){
                
                    // Check if this term was already added to the list form another method.
                    // Update the entry for this term if that is the case, but don't add it again.
                    boolean alreadyInList = false;
                    for (Term term: chunkToTermsMap.getOrDefault(chunkID, new ArrayList<>())){
                        if (term.id.equals(termID)){
                            term = updateTermProperties(term, probability, nodes);
                            alreadyInList = true;
                            break;
                        }
                    }    

                    if (!alreadyInList){
                        ArrayList<Term> listOfTerms = chunkToTermsMap.getOrDefault(chunkID, new ArrayList<>());
                        listOfTerms.add(new Term(termID,probability,utils.Util.inferOntology(termID), utils.Util.getNodeSetFromString(nodes)));
                        chunkToTermsMap.put(chunkID, listOfTerms);
                    }
                    
                }       
                    
            }   
                
            // Limit the number of candidate terms from this ontology for each chunk.
            Utils.TermComparatorByProb comparer = new Utils.TermComparatorByProb();
            for (Integer chunkID: chunkToTermsMap.keySet()){
                Collections.sort(chunkToTermsMap.get(chunkID), comparer);
                chunkToTermsMap.put(chunkID, new ArrayList<>(chunkToTermsMap.get(chunkID).subList(0, Math.min(k, chunkToTermsMap.get(chunkID).size()))));           
            }
            scanner.close();
        }    
        return chunkToTermsMap; 
   
    }
        
        
        
        
    
    
    public static Term updateTermProperties(Term term, double prob, String nodes){
        term.probability = Math.max(term.probability, prob);
        term.nodes.addAll(utils.Util.getNodeSetFromString(nodes));
        return term;
    }
    
    
    
    
    
   
    
    
    
    
    
    
    
    /**
     * 
     * TODO still need this?
     * 
     * 
     * Accepts any number of maps associating chunks with lists of candidate terms, and integrates them
     * into a single map. This is used for entities because valid entities can come from a number of 
     * different ontologies but they need to be combined so a single list of valid candidate terms can
     * be found for each text chunk.
     * @param hms
     * @return 
     */
    public HashMap<Integer,ArrayList<Term>> mergeClassProbFiles(List<HashMap<Integer,ArrayList<Term>>> hms){
        HashMap<Integer,ArrayList<Term>> mergedChunkToTermsMap = new HashMap<>();
        HashSet<Integer> chunkIDs = new HashSet<>();
        for (HashMap<Integer,ArrayList<Term>> hm: hms){
            chunkIDs.addAll(new HashSet<>(hm.keySet()));
        }
        for (int chunkID: (HashSet<Integer>) chunkIDs){
            ArrayList<Term> terms = new ArrayList<>();
            for (HashMap<Integer,ArrayList<Term>> hm: hms){
                for (Term t: hm.getOrDefault(chunkID, new ArrayList<>())){
                    terms.add(t);
                }  
            }
            mergedChunkToTermsMap.put(chunkID, terms);
        }
        return mergedChunkToTermsMap;
    }

    
    
    
}

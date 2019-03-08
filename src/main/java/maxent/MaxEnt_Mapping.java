/*
 * Ian Braun
 * irbraun@iastate.edu
 * term-mapping 
 */
package maxent;

import composer.Term;
import config.Config;
import enums.Ontology;
import enums.Role;
import enums.TextDatatype;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import main.Group;
import static main.Main.logger;
import main.Partitions;
import nlp_annot.Utils;
import ontology.Onto;
import structure.Chunk;
import structure.OntologyTerm;
import text.Text;
import static utils.Util.range;

/**
 *
 * @author irbraun
 */
public class MaxEnt_Mapping {
    
    
    
    
    HashMap<Ontology,Double> ratios;
    
    
    
    
    
    
    public void run(int maxIter) throws SQLException, Exception{
        
        // Generate the objects for the tagged data.
        Text text = new Text();
        List<Chunk> chunks = text.getAllAtomChunks();
        Partitions partitions = new Partitions(text);   
        
        
        
        
        // Get ratios.
        HashMap<Ontology,Integer> counts = new HashMap<>();
        for (Ontology o: Ontology.values()){
            counts.put(o, 0);
        }
        // Count the occurences of terms from each ontology in the training set.
        List<Chunk> trainingChunks = partitions.getChunksInPartitionRangeInclusive(5, 31, chunks);
        for (Chunk c: trainingChunks){
            for (String termID: text.getCuratedEQStatementFromAtomID(c.chunkID).getAllTermIDs()){
                int count = counts.get(utils.Util.inferOntology(termID));
                count++;
                counts.put(utils.Util.inferOntology(termID), count);
            }
        }
        // Output the ratios as term per chunk of text for each ontology.
        ratios = new HashMap<>();
        for (Ontology o: counts.keySet()){
            double ratio = (double) counts.get(o) / (double) trainingChunks.size();
            ratios.put(o, ratio);
        }
        
        
        
        
        
        
        // Create and train the ME and then use it on remaining data.
        
        // trying out separating by ontology to reduce number of target classes.
        logger.info("training with max of " + maxIter + " iterations.");
        MaxEnt_Model mec1 = new MaxEnt_Model(maxIter, Ontology.PATO);
        mec1.train(text, partitions.getChunksFromPartitions(range(5,31), chunks));
        runOneFold(mec1, "fold", range(0,31), range(0,4), range(5,31), Ontology.PATO);
        
        /*
        MaxEnt_Model mec2 = new MaxEnt_Model(maxIter, Ontology.PO);
        mec2.train(text, partitions.getChunksFromPartitions(range(5,31), chunks));
        runOneFold(mec2, "fold", range(0,31), range(0,4), range(5,31), Ontology.PO);
        
        MaxEnt_Model mec3 = new MaxEnt_Model(maxIter, Ontology.GO);
        mec3.train(text, partitions.getChunksFromPartitions(range(5,31), chunks));
        runOneFold(mec3, "fold", range(0,31), range(0,4), range(5,31), Ontology.GO);
        
        MaxEnt_Model mec4 = new MaxEnt_Model(maxIter, Ontology.CHEBI);
        mec4.train(text, partitions.getChunksFromPartitions(range(5,31), chunks));
        runOneFold(mec4, "fold", range(0,31), range(0,4), range(5,31), Ontology.CHEBI);
        */
        
        
        
        
        
    }
    
    
    
    private void runOneFold(MaxEnt_Model mec, String fold, List<Integer> allParts, List<Integer> testParts, List<Integer> trainParts, Ontology o) throws FileNotFoundException, SQLException, Exception{
        
        // Pick a base directory based on the testing set.
        String baseDirectory;
        /*
        switch (Config.typePartitions) {
            case "species":
                baseDirectory = String.format("%s%s/", Config.mePath, "set1");
                break;
            case "random":
                baseDirectory = String.format("%s%s/", Config.mePath, "set2");
                break;
            default:
                throw new Exception();
        }
        */
        baseDirectory = String.format("%s%s/", Config.mePath, "set1");
        
       
        // Partition numbers for testing and training.
        List<Integer> allPartitionNumbers = allParts;
        List<Integer> testingPartitionNumbers = testParts;
        List<Integer> trainingPartitionNumbers = trainParts;
        
        Text text = new Text();
        Partitions p = new Partitions(text);  
        
        String allName = "all";
        String trainingName = "train";
        String testingName = "test";
                
        String dataset = "ppn";
        
               
        // Annotated data available in the Plant PhenomeNET.
        if (o.equals(Ontology.PATO)){
            List<Group> patoGroups = new ArrayList<>();
            patoGroups.add(new Group("name", fold, allPartitionNumbers, String.format("%s%s_%s_%s_%s",baseDirectory,"outputs", allName, dataset, "pato/"), p));
            patoGroups.add(new Group("name", fold, testingPartitionNumbers, String.format("%s%s_%s_%s_%s",baseDirectory,"outputs", testingName, dataset, "pato/"), p));
            patoGroups.add(new Group("name", fold, trainingPartitionNumbers, String.format("%s%s_%s_%s_%s",baseDirectory,"outputs", trainingName, dataset, "pato/"), p));
            search(Ontology.PATO, new Text(), patoGroups, mec);
        }
        
        if (o.equals(Ontology.PO)){
            List<Group> poGroups = new ArrayList<>();
            poGroups.add(new Group("name", fold, allPartitionNumbers, String.format("%s%s_%s_%s_%s",baseDirectory,"outputs", allName, dataset, "po/"), p));
            poGroups.add(new Group("name", fold, testingPartitionNumbers, String.format("%s%s_%s_%s_%s",baseDirectory,"outputs", testingName, dataset, "po/"), p));
            poGroups.add(new Group("name", fold, trainingPartitionNumbers, String.format("%s%s_%s_%s_%s",baseDirectory,"outputs", trainingName, dataset, "po/"), p));
            search(Ontology.PO, new Text(), poGroups, mec);
        }
        
        if (o.equals(Ontology.GO)){
            List<Group> goGroups = new ArrayList<>();
            goGroups.add(new Group("name", fold, allPartitionNumbers, String.format("%s%s_%s_%s_%s",baseDirectory,"outputs", allName, dataset, "go/"), p));
            goGroups.add(new Group("name", fold, testingPartitionNumbers, String.format("%s%s_%s_%s_%s",baseDirectory,"outputs", testingName, dataset, "go/"), p));
            goGroups.add(new Group("name", fold, trainingPartitionNumbers, String.format("%s%s_%s_%s_%s",baseDirectory,"outputs", trainingName, dataset, "go/"), p));
            search(Ontology.GO, new Text(), goGroups, mec);
        }
        
        if (o.equals(Ontology.CHEBI)){
            List<Group> chebiGroups = new ArrayList<>();
            chebiGroups.add(new Group("name", fold, allPartitionNumbers, String.format("%s%s_%s_%s_%s",baseDirectory,"outputs", allName, dataset, "chebi/"), p));
            chebiGroups.add(new Group("name", fold, testingPartitionNumbers, String.format("%s%s_%s_%s_%s",baseDirectory,"outputs", testingName, dataset, "chebi/"), p));
            chebiGroups.add(new Group("name", fold, trainingPartitionNumbers, String.format("%s%s_%s_%s_%s",baseDirectory,"outputs", trainingName, dataset, "chebi/"), p));
            search(Ontology.CHEBI, new Text(), chebiGroups, mec);
        }
    }
        
    
    
    
    
    
    
    private void search(Ontology ontology, Text text, List<Group> groups, MaxEnt_Model mec) throws SQLException, Exception{
        
        // Which ontology currently working on.
        logger.info(String.format("working on terms from %s",ontology.toString()));
        
        // Plant PhenomeNET text data.
        List<Chunk> chunks = text.getAllAtomChunks();
        Partitions partsObj = new Partitions(text);
        String ontologyPath = utils.Util.pickOntologyPath(ontology.toString());
        Onto onto = new Onto(ontologyPath);
        List<OntologyTerm> terms = onto.getTermList();
        
        
        
        
        
        
        // Inserting this here to take care of the thresholding issue.
        HashMap<Chunk,List<Result>> searchResultsMap = new HashMap<>();
        List<Result> allMatchesBeforeThresholding = new ArrayList<>();                    // nothing ever gets add 
        for (Chunk chunk: chunks){
            double initialThreshold = Double.NEGATIVE_INFINITY;
            List<Result> matches = findMatchingTerms(chunk, terms, mec, initialThreshold);
            searchResultsMap.put(chunk, matches);
            allMatchesBeforeThresholding.addAll(matches);
        }
        // Sort all the matching terms for all chunks.
        Collections.sort(allMatchesBeforeThresholding, new ResultComparatorByProb());
        int numResultsBefore = allMatchesBeforeThresholding.size();                                         // at this point the size of the list is 0
        // Find the threshold value.
        double ratio = ratios.get(ontology);
        logger.info(String.format("the expected ratio for this ontology from training data is %s", ratio));
        int lowestAcceptedTermIdx = (int) Math.floor(ratio * (double)chunks.size());
        logger.info(String.format("the lowest accepted term is index=%s out of %s", lowestAcceptedTermIdx, numResultsBefore));
        // Check if there are any extra terms that don't exceed the threshold.
        // If not, just leave it alone.
        if (lowestAcceptedTermIdx < allMatchesBeforeThresholding.size()){
            double threshold = allMatchesBeforeThresholding.get(lowestAcceptedTermIdx).prob;
            for (Chunk chunk: chunks){
                List<Result> resultsForThisChunk = searchResultsMap.get(chunk);
                List<Result> keptResultsForThisChunk = new ArrayList<>();
                for (Result r: resultsForThisChunk){
                    if (r.prob >= threshold){
                        keptResultsForThisChunk.add(r);
                    }
                }
                searchResultsMap.put(chunk, keptResultsForThisChunk);
            }
        }
        // Check how many search results were thrown out after thresholding.
        int numResultsAfter = 0;
        for (Chunk c: chunks){
            numResultsAfter += searchResultsMap.get(c).size();
        }
        logger.info(String.format("the number of search results was reduced from %s to %s after thresholding",numResultsBefore,numResultsAfter));
        
        
        
        
        
        
        
        
        
        
        
        
        String evalHeader = "chunk,text,label,term,score,component,category,similarity,nodes";
        String classProbHeader = "chunk,term,prob,nodes";
        
        for (Group g: groups){
            g.classProbsPrinter.println(classProbHeader);
            g.evalPrinter.println(evalHeader);
        }
        
        int ctr = 0;
        for (Chunk chunk: chunks){
           
           
            // What are the term ID's of the terms that can be found to match this chunk with this classifier?
            //List<Result> matches = findMatchingTerms(chunk, terms, mec, Config.annotThresholds.get(ontology));
            List<Result> matches = searchResultsMap.get(chunk); // Just use the map from the first pass above instead.
            
            
            List<String> allTermIDsFoundBySearching = new ArrayList<>();
            for (Result r: matches){
                allTermIDsFoundBySearching.add(r.termID);
            }
            if (allTermIDsFoundBySearching.size() != matches.size()){
                throw new Exception("wrong number of matches returned");
            }
 
            
            // Find all the ontology terms that were curated for this chunk and which components they are.
            ArrayList<String> termIDs = text.getCuratedEQStatementFromAtomID(chunk.chunkID).getAllTermIDs();
            ArrayList<Role> termRoles = text.getCuratedEQStatementFromAtomID(chunk.chunkID).getAllTermRoles();
            
            // Iterate through the curated terms specific to this ontology.
            for (int i=0; i<termIDs.size(); i++){
                String id = termIDs.get(i);
                Role role = termRoles.get(i);

                if (utils.Util.inferOntology(id).equals(ontology)){
                    try{
                        // FN (false negatives)
                        if (!allTermIDsFoundBySearching.contains(id)){
                            String label = onto.getTermFromTermID(id).label;
                            String sim = String.format("%.3f",utils.Util.getMaxSimJac(id, allTermIDsFoundBySearching, onto));
                            Object[] line = {chunk.chunkID, chunk.getRawText().replace(",", ""), label, id, "none", Role.getAbbrev(role), "FN", sim,"none"};
                            int part = partsObj.getPartitionNumber(chunk);
                            Utils.writeToEvalFiles(line, part, groups);
                        }
                        // TP (true positives)
                        else {
                            String label = onto.getTermFromTermID(id).label;
                            HashSet<String> nodes = matches.get(allTermIDsFoundBySearching.indexOf(id)).nodes;
                            double prob = ((double) matches.get(allTermIDsFoundBySearching.indexOf(id)).prob);
                            String probStr = String.format("%.3f",prob);
                            String joinedNodes = nodes.stream().collect(Collectors.joining("|"));
                            Object[] line = {chunk.chunkID, chunk.getRawText().replace(",", ""), label, id, probStr, Role.getAbbrev(role), "TP", "1.000", joinedNodes};
                            int part = partsObj.getPartitionNumber(chunk);
                            Utils.writeToEvalFiles(line, part, groups);
                        }
                    }
                    catch(NullPointerException e){
                        logger.info(String.format("%s was found in the curated annotations but not in the owl file",id));
                    }
                }
            }
            
            // Iterate through the terms that were found using fuzzy matching (might not have been included in the above loop).
            // Don't need to include the try catch for unsupported ontology terms here because these all come directly from the owl file.
            for (String id: allTermIDsFoundBySearching){
                if(!termIDs.contains(id)){
                    // FP (false positives)
                    HashSet<String> nodes = matches.get(allTermIDsFoundBySearching.indexOf(id)).nodes;
                    String joinedNodes = nodes.stream().collect(Collectors.joining("|"));
                    double prob = ((double) matches.get(allTermIDsFoundBySearching.indexOf(id)).prob);
                    String probStr = String.format("%.3f",prob);
                    String sim = String.format("%.3f",utils.Util.populateAttributes(chunk, onto.getTermFromTermID(id), text, onto, ontology).hJac);
                    Object[] line = {chunk.chunkID, chunk.getRawText().replace(",",""), onto.getTermFromTermID(id).label, id, probStr, Role.getAbbrev(Role.UNKNOWN), "FP", sim, joinedNodes};
                    int part = partsObj.getPartitionNumber(chunk);
                    Utils.writeToEvalFiles(line, part, groups);
                }
                // Put these terms in the class probabilitites file, regardless of whether they are TP or FP, just include all mapped terms.
                HashSet<String> nodes = matches.get(allTermIDsFoundBySearching.indexOf(id)).nodes;
                String joinedNodes = nodes.stream().collect(Collectors.joining("|"));
                double prob = ((double) matches.get(allTermIDsFoundBySearching.indexOf(id)).prob);
                String probStr = String.format("%.3f",prob);
                Object[] line = {chunk.chunkID,id,probStr,joinedNodes};
                int part = partsObj.getPartitionNumber(chunk);
                Utils.writeToClassProbFiles(line, part, groups);
            }       
            
            ctr++;
            Utils.updateLog(ctr, 1000);
          
        }

        
        
        for (Group g: groups){
            g.classProbsPrinter.close();
            g.evalPrinter.close();
        }
        
        
        
    }
    
    
    
    
    
    private List<Result> findMatchingTerms(Chunk chunk, List<OntologyTerm> terms, MaxEnt_Model mec, double threshold) throws Exception{
        int k = Config.maxTermsUsedPerOntology;
        List<Result> matches = new ArrayList<>();
        List<Term> matchingTerms = mec.getBestKTerms(terms, chunk, k);
        
        
        // no terms are returned here.... always zero.
        //logger.info(String.format("calling getBestKTerms, sending %s terms for chunk %s ",terms.size(),chunk.chunkID));
        
        for (Term t: matchingTerms){
            Result r = new Result();
            r.prob = t.probability;
            r.termID = t.id;
            if (r.prob >=  threshold){
                matches.add(r);
            }
        }
        return matches;
    } 
        
        
        
        
    
    
    private static class Result{
        public String termID;
        public double prob;
        public HashSet<String> nodes = new HashSet<>();
    }
    
    
    static class ResultComparatorByProb implements Comparator<Result>{
        @Override
        public int compare(Result r1, Result r2){
            if (r1.prob < r2.prob){
                return 1;
            }
            else if (r1.prob > r2.prob){
                return -1;
            }
            return 0;
        }  
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}

/*
 * Ian Braun
 * irbraun@iastate.edu
 * term-mapping 
 */
package nlp_annot;

import composer.Term;
import config.Config;
import enums.Ontology;
import enums.Role;
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
import ontology.Onto;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import structure.Chunk;
import structure.OntologyTerm;
import text.Text;
import uk.ac.ebi.brain.error.ClassExpressionException;
import uk.ac.ebi.brain.error.NewOntologyException;
import static utils.Util.range;

/**
 *
 * @author irbraun
 */


/*
What should happen here when the phenotypes are split using ; and .? 
The training should ignore the split phenotypes and just look at the whole phenotypes.
The testing should then predict using the actual split ones.

I don't think that's true. Doesn't matter which, its just counting how many times words co-occur with terms.
*/




public class NaiveBayes_Mapping {
    
    
    
    private final HashMap<Ontology,Onto> ontoObjects;
    
    public NaiveBayes_Mapping() throws OWLOntologyCreationException, NewOntologyException, ClassExpressionException, Exception{
        ontoObjects = new HashMap<>();
        for (Ontology ontology: Ontology.getAllOntologies()){
            String ontologyPath = utils.Util.pickOntologyPath(ontology.toString());
            Onto onto = new Onto(ontologyPath);
            ontoObjects.put(ontology, onto);
        }
    }
    
   
    private HashMap<Ontology,Double> getLearnedRatios(Text text, List<Chunk> trainingChunks){
        HashMap<Ontology,Integer> counts = new HashMap<>();
        for (Ontology o: Ontology.values()){
            counts.put(o, 0);
        }
        // Count the occurences of terms from each ontology in the training set.
        for (Chunk c: trainingChunks){
            for (String termID: text.getCuratedEQStatementFromAtomID(c.chunkID).getAllTermIDs()){
                int count = counts.get(utils.Util.inferOntology(termID));
                count++;
                counts.put(utils.Util.inferOntology(termID), count);
            }
        }
        // Output the ratios as term per chunk of text for each ontology.
        HashMap<Ontology,Double> ratios = new HashMap<>();
        for (Ontology o: counts.keySet()){
            double ratio = (double) counts.get(o) / (double) trainingChunks.size();
            ratios.put(o, ratio);
        }
        return ratios;
    }
    
    
    
    public void run(double threshold) throws SQLException, Exception{
        
        // Generate the objects for the tagged data.
        Text text = new Text();
        List<Chunk> chunks = text.getAllChunksOfDType(Config.format);
        Partitions partitions = new Partitions(text);        

        // Create and train the naive Bayes classifiers and then use it on remaining data.
        NaiveBayes_Model nb1 = new NaiveBayes_Model();
        nb1.train(text, partitions.getChunksFromPartitions(range(8,31), chunks), threshold);
        HashMap<Ontology,Double> ratios1 = getLearnedRatios(text, partitions.getChunksFromPartitions(range(8,31), chunks));
        runOneFold(nb1, "fold1", range(0,31), range(0,7), range(8,31), ratios1);
        
        NaiveBayes_Model nb2 = new NaiveBayes_Model();
        nb2.train(text, partitions.getChunksFromPartitions(range(0,7,16,31), chunks), threshold);
        HashMap<Ontology,Double> ratios2 = getLearnedRatios(text, partitions.getChunksFromPartitions(range(0,7,16,31), chunks));
        runOneFold(nb2, "fold2", range(0,31), range(8,15), range(0,7,16,31), ratios2);
        
        NaiveBayes_Model nb3 = new NaiveBayes_Model();
        nb3.train(text, partitions.getChunksFromPartitions(range(0,15,24,31), chunks), threshold);
        HashMap<Ontology,Double> ratios3 = getLearnedRatios(text, partitions.getChunksFromPartitions(range(0,15,24,31), chunks));
        runOneFold(nb3, "fold3", range(0,31), range(16,23), range(0,15,24,31), ratios3);
        
        NaiveBayes_Model nb4 = new NaiveBayes_Model();
        nb4.train(text, partitions.getChunksFromPartitions(range(0,23), chunks), threshold);
        HashMap<Ontology,Double> ratios4 = getLearnedRatios(text, partitions.getChunksFromPartitions(range(0,23), chunks));
        runOneFold(nb4, "fold4", range(0,31), range(24,31), range(0,23), ratios4);
        
       
    }
    
    private void runOneFold(NaiveBayes_Model nb, String fold, List<Integer> allParts, List<Integer> testParts, List<Integer> trainParts, HashMap<Ontology,Double> ratios) throws FileNotFoundException, SQLException, Exception{
        

        String baseDirectory = Config.nbPath;
        
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
       
        
        
        // Find the fraction of the testing set that is directly present in the training set.
        List<String> testSetInstances = new ArrayList<>();
        for (Chunk c: p.getChunksFromPartitions(testParts, text.getAllAtomChunks())){
            testSetInstances.add(String.format("%s:%s",c.getRawText(),text.getCuratedEQStatementFromAtomID(c.chunkID).toIDText()));
        }
        HashSet<String> trainingSetInstances = new HashSet<>();
        for (Chunk c: p.getChunksFromPartitions(trainParts, text.getAllAtomChunks())){
            trainingSetInstances.add(String.format("%s:%s",c.getRawText(),text.getCuratedEQStatementFromAtomID(c.chunkID).toIDText()));
        }
        int numOverlappingInstances = 0;
        for (String instance: testSetInstances){
            if (trainingSetInstances.contains(instance)){
                numOverlappingInstances++;
            }
        }
        double fractionOfTestingInTraining = (double) numOverlappingInstances / (double) testSetInstances.size();
        System.out.println(String.format("For fold %s %s of the testing instances are observed in the training data.", fold, fractionOfTestingInTraining));
        // Done doing that.

        
        
        // Annotated data available in the Plant PhenomeNET.
        List<Group> patoGroups = new ArrayList<>();
        String outputPath = String.format("%s/%s",baseDirectory,"output_pato");
        patoGroups.add(new Group("all"+fold, fold, allPartitionNumbers, outputPath, p));
        patoGroups.add(new Group("test"+fold, fold, testingPartitionNumbers, outputPath, p));
        patoGroups.add(new Group("train"+fold, fold, trainingPartitionNumbers, outputPath, p));
        search(Ontology.PATO, new Text(), patoGroups, nb, ratios);
       
        List<Group> poGroups = new ArrayList<>();
        outputPath = String.format("%s/%s",baseDirectory,"output_po");
        poGroups.add(new Group("all"+fold, fold, allPartitionNumbers, outputPath, p));
        poGroups.add(new Group("test"+fold, fold, testingPartitionNumbers, outputPath, p));
        poGroups.add(new Group("train"+fold, fold, trainingPartitionNumbers, outputPath, p));
        search(Ontology.PO, new Text(), poGroups, nb, ratios);
        
        List<Group> goGroups = new ArrayList<>();
        outputPath = String.format("%s/%s",baseDirectory,"output_go");
        goGroups.add(new Group("all"+fold, fold, allPartitionNumbers, outputPath, p));
        goGroups.add(new Group("test"+fold, fold, testingPartitionNumbers, outputPath, p));
        goGroups.add(new Group("train"+fold, fold, trainingPartitionNumbers, outputPath, p));
        search(Ontology.GO, new Text(), goGroups, nb, ratios);
        
        List<Group> chebiGroups = new ArrayList<>();
        outputPath = String.format("%s/%s",baseDirectory,"output_chebi");
        chebiGroups.add(new Group("all"+fold, fold, allPartitionNumbers, outputPath, p));
        chebiGroups.add(new Group("test"+fold, fold, testingPartitionNumbers, outputPath, p));
        chebiGroups.add(new Group("train"+fold, fold, trainingPartitionNumbers, outputPath, p));
        search(Ontology.CHEBI, new Text(), chebiGroups, nb, ratios);
        
    }
    
    
    
    
    
    
  
    
    
    
    
    private void search(Ontology ontology, Text text, List<Group> groups, NaiveBayes_Model nb, HashMap<Ontology,Double> ratios) throws SQLException, Exception{
        
        
       
        // Which ontology currently working on.
        logger.info(String.format("working on terms from %s",ontology.toString()));
        
        // Plant PhenomeNET text data.
        List<Chunk> chunks = text.getAllAtomChunks();
        Partitions partsObj = new Partitions(text);
               
        Onto onto = ontoObjects.get(ontology);
        List<OntologyTerm> terms = onto.getTermList();
        
        
        
        // Inserting this here to take care of the thresholding issue.
        HashMap<Chunk,List<Result>> searchResultsMap = new HashMap<>();
        List<Result> allMatchesBeforeThresholding = new ArrayList<>();
        for (Chunk chunk: chunks){
            double initialThreshold = Double.NEGATIVE_INFINITY;
            List<Result> matches = findMatchingTerms(chunk, terms, nb, initialThreshold);
            searchResultsMap.put(chunk, matches);
            allMatchesBeforeThresholding.addAll(matches);
        }
        // Sort all the matching terms for all chunks.
        Collections.sort(allMatchesBeforeThresholding, new ResultComparatorByProb());
        int numResultsBefore = allMatchesBeforeThresholding.size();
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
            //List<Result> matches = findMatchingTerms(chunk, terms, nb, Config.annotThresholds.get(ontology));
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
    
    
    
    
            
            
            
    private List<Result> findMatchingTerms(Chunk chunk, List<OntologyTerm> terms, NaiveBayes_Model nb, double threshold){
        int k = Config.maxTermsUsedPerOntology;
        List<Result> matches = new ArrayList<>();
        List<Term> matchingTerms = nb.getBestKTerms(terms, chunk, k);
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

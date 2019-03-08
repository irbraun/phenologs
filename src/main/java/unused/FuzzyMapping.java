/*
 * Ian Braun
 * irbraun@iastate.edu
 * term-mapping 
 */
package unused;

import config.Config;
import enums.Ontology;
import enums.Role;
import enums.TextDatatype;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import main.Group;
import static main.Main.logger;
import main.Partitions;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;
import nlp.CoreNLP;
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
public class FuzzyMapping {
    
    
    public void routine() throws SQLException, Exception{
       
        runFold("fold", range(0,31), range(0,4), range(5,31));
        
        // Test using specific terms.
        /*
        System.out.println(FuzzySearch.ratio("arbuscle", "arbuscule"));
        System.out.println(FuzzySearch.ratio("arbuscule", "arbuscle"));
        Text text = new Text();
        Chunk c = text.getAtomChunkFromID(3229);

        File ontologyFile = new File("/Users/irbraun/Desktop/owl/go.owl");
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology o = manager.loadOntologyFromOntologyDocument(ontologyFile);
        Brain brain = new Brain(o);
        
        System.out.println("looking through go");
        for (OWLClass cls: o.getClassesInSignature()){
            
            String fullID = cls.toStringID();
            String termID = fullID.substring(fullID.lastIndexOf("/")+1);
            if (termID.equals("GO_0085041")){
                OntologyTerm term = new OntologyTerm(cls, o, brain); 
                List<OntologyTerm> terms = new ArrayList<>();
                terms.add(term);
                System.out.println("searching for terms now");
                for (Result r: findMatchingTerms(c, terms)){
                    System.out.println(r.termID);
                } 
            }
        }
        System.out.println("done");
        */
    }    

    
      

    private void runFold(String fold, List<Integer> all, List<Integer> test, List<Integer> train) throws FileNotFoundException, SQLException, Exception{

        // Output directory.
        String dirBase = "not used";
        
        
        Text text = new Text();
        Partitions pRandom = new Partitions(text);   
        Partitions pSpecies = new Partitions(text);

        
        // Annotated data available in the Plant PhenomeNET.
        List<Group> patoGroups = new ArrayList<>();
        patoGroups.add(new Group("pato_all", fold, all, String.format("%s%s",dirBase,"outputs_all_ppn_pato/"), pSpecies));
        patoGroups.add(new Group("pato_species", fold, test, String.format("%s%s",dirBase,"outputs_species_ppn_pato/"), pSpecies));
        patoGroups.add(new Group("pato_random", fold, test, String.format("%s%s",dirBase,"outputs_random_ppn_pato/"), pRandom));
        search(Ontology.PATO, text, patoGroups);
        
        List<Group> poGroups = new ArrayList<>();
        poGroups.add(new Group("po_all", fold, all, String.format("%s%s",dirBase,"outputs_all_ppn_po/"), pSpecies));
        poGroups.add(new Group("po_species", fold, test, String.format("%s%s",dirBase,"outputs_species_ppn_po/"), pSpecies));
        poGroups.add(new Group("po_random", fold, test, String.format("%s%s",dirBase,"outputs_random_ppn_po/"), pRandom));
        search(Ontology.PO, text, poGroups);
        
        List<Group> goGroups = new ArrayList<>();
        goGroups.add(new Group("go_all", fold, all, String.format("%s%s",dirBase,"outputs_all_ppn_go/"), pSpecies));
        goGroups.add(new Group("go_species", fold, test, String.format("%s%s",dirBase,"outputs_species_ppn_go/"), pSpecies));
        goGroups.add(new Group("go_random", fold, test, String.format("%s%s",dirBase,"outputs_random_ppn_go/"), pRandom));
        search(Ontology.GO, text, goGroups);
        
        List<Group> chebiGroups = new ArrayList<>();
        chebiGroups.add(new Group("chebi_all", fold, all, String.format("%s%s",dirBase,"outputs_all_ppn_chebi/"), pSpecies));
        chebiGroups.add(new Group("chebi_species", fold, test, String.format("%s%s",dirBase,"outputs_species_ppn_chebi/"), pSpecies));
        chebiGroups.add(new Group("chebi_random", fold, test, String.format("%s%s",dirBase,"outputs_random_ppn_chebi/"), pRandom));
        search(Ontology.CHEBI, text, chebiGroups);
        
    } 
    
    
    
    private void search(Ontology ontology, Text text, List<Group> groups) throws SQLException, Exception{
        
        // Which ontology currently working on.
        logger.info(String.format("working on terms from %s",ontology.toString()));
        
        // Plant PhenomeNET text data.
        List<Chunk> chunks = text.getAllAtomChunks();
        Partitions partsObj = new Partitions(text);
        String ontologyPath = utils.Util.pickOntologyPath(ontology.toString());
        Onto onto = new Onto(ontologyPath);
        List<OntologyTerm> terms = onto.getTermList();
        
        
        String evalHeader = "chunk,text,label,term,score,component,category,similarity,nodes";
        String classProbHeader = "chunk,term,prob,nodes";
        
        for (Group g: groups){
            g.classProbsPrinter.println(classProbHeader);
            g.evalPrinter.println(evalHeader);
        }
        
        int ctr = 0;
        for (Chunk chunk: chunks){
           
           
            // What are the term ID's of the terms that can be found to match this chunk with fuzzy matching?
            List<Result> matches = findMatchingTerms(chunk, terms);
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
                            double prob = ((double) matches.get(allTermIDsFoundBySearching.indexOf(id)).fullRatio) * 0.01;
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
                    double prob = ((double) matches.get(allTermIDsFoundBySearching.indexOf(id)).fullRatio) * 0.01;
                    String probStr = String.format("%.3f",prob);
                    String sim = String.format("%.3f",utils.Util.populateAttributes(chunk, onto.getTermFromTermID(id), text, onto, ontology).hJac);
                    Object[] line = {chunk.chunkID, chunk.getRawText().replace(",",""), onto.getTermFromTermID(id).label, id, probStr, Role.getAbbrev(Role.UNKNOWN), "FP", sim, joinedNodes};
                    int part = partsObj.getPartitionNumber(chunk);
                    Utils.writeToEvalFiles(line, part, groups);
                }
                // Put these terms in the class probabilitites file, regardless of whether they are TP or FP, just include all mapped terms.
                HashSet<String> nodes = matches.get(allTermIDsFoundBySearching.indexOf(id)).nodes;
                String joinedNodes = nodes.stream().collect(Collectors.joining("|"));
                double prob = ((double) matches.get(allTermIDsFoundBySearching.indexOf(id)).fullRatio) * 0.01;
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
    
    
    
    
    
    
  
    
    
    /**
     * Returns a list of Result objects, which contain an ontology term, the matching score found
     * by the fuzzy searching algorithm (between 0 and 100), and a string representing the nodes
     * or tokens in the original text which mapped to the ontology term, can be used to identify
     * which words are associated with this ontology term in the original text or parses of it.
     * Using a default threshold of 90 for the fuzzy search.
     * The other threshold is applied for the similarity between the pattern from the term and just
     * the tokens from the text that matched that pattern. This is so a way to eliminate matches
     * that occur because either the pattern or node is so small (1 or 2 characters) that it occurs
     * without meaning in many other words and patterns. i.e. The pattern must match a substring of
     * the text, and then the words which contain that substring must also be a pretty good match
     * with the pattern. Note that the ratio and partial ratio functions are symmetrical, which 
     * argument is the shorter string is detected automatically.
     * @param chunk
     * @param terms
     * @return 
     */
    private List<Result> findMatchingTerms(Chunk chunk, List<OntologyTerm> terms){
        
        List<Result> matches = new ArrayList<>();
        int partialRatioThreshold = 80;
        int substringRatioThreshold = 70;
        
        
        for (OntologyTerm term: terms){
            String text = chunk.getRawText().toLowerCase();
            ArrayList<String> patterns = new ArrayList<>();
            patterns.add(term.label);
            patterns.addAll(term.exactSynonyms);
            patterns.addAll(term.relatedSynonyms);
            patterns.addAll(term.narrowSynonyms);
            patterns.addAll(term.broadSynonyms);
            patterns = CoreNLP.removeStopWords(patterns);
            patterns = CoreNLP.removeSingleCharWords(patterns);
            
            /*
            System.out.println("the patterns are");
            for (String p: patterns){
                System.out.println(p);
            }
            System.out.println("-");
            System.out.println(text);
            System.out.println("-");
            */
            
            // The maximum score and best result are based on the partial ratio (local alignment score).
            int maxScore = 0;
            Result bestResult = new Result();
            for (String pattern: patterns){
                pattern = pattern.toLowerCase();
                int partialRatio = FuzzySearch.partialRatio(pattern, text);
                // If this pattern against the text surpass the threshold and is the best pattern so far.
                if (partialRatio >= partialRatioThreshold && partialRatio > maxScore){
                    HashSet<String> nodes = new HashSet<>();
                    String[] patternTokens = pattern.split(" ");
                    for (String token: patternTokens){
                        ExtractedResult er = FuzzySearch.extractOne(token, Arrays.asList(text.split(" ")));
                        String matchingItem = Arrays.asList(text.split(" ")).get(er.getIndex());
                        nodes.add(matchingItem.trim());
                    }
                    int ratio = FuzzySearch.tokenSortRatio(pattern, String.join(" ",nodes));
                    Result r = new Result();
                    r.nodes = nodes;
                    r.partialRatio = maxScore;
                    r.fullRatio = ratio;
                    r.termID = term.termID;
                    // Only accept this pattern's match if it also satisfies the other threshold.
                    if (ratio >= substringRatioThreshold){
                        bestResult = r;
                        maxScore = Math.max(maxScore, partialRatio);
                    }
                }
            }
            // Add the result for the best fitting pattern.
            if (maxScore>0){
                matches.add(bestResult);
            }
        }
        return matches;
    }
    
    
    
    
    
    

    
    public static class Result{
        public String termID;
        public HashSet<String> nodes;
        public int fullRatio;
        public int partialRatio;
    }
    
    

    
        
   
    
    
}

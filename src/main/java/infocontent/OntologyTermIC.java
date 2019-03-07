/*
 * Ian Braun
 * irbraun@iastate.edu
 * term-mapping 
 */
package infocontent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import static main.Main.logger;
import structure.OntologyTerm;
import uk.ac.ebi.brain.core.Brain;
import uk.ac.ebi.brain.error.ClassExpressionException;

/**
 *
 * @author irbraun
 */
class OntologyTermIC {
    
    private final HashMap<String, Integer> termDepths;
    private final HashMap<String, Double> infoContent;
    private final Brain brain;
    
    OntologyTermIC(ArrayList<OntologyTerm> ontologyTerms, Brain brainArg) throws ClassExpressionException{     
        infoContent = new HashMap<>();
        termDepths = new HashMap<>();
        brain = brainArg;
        
        // Map each term to a depth, done recursively, couldn't quickly find a good built-in function.
        List<String> subClasses = brain.getSubClassesFromLabel("Thing", true);
        for (String termID: subClasses){
            recurse(termID, 1);
        }
        
        // In all the owl ontologies currently using the root is defined as Thing.
        // Use the depths to calculate the structure-based information content of each term.
        // Depreciated terms among other might not be descendants of the root.
        // Handle this case, although it looks like this is only a problem right now in PATO.
        // Assume a depth of 1 for those terms.
        for (OntologyTerm t: ontologyTerms){
            int depth = termDepths.getOrDefault(t.unnormalizedTermID, 0);
            if (depth == 0){
                depth = 1;
            }
            int numDesc = brain.getSubClasses(t.unnormalizedTermID, false).size();
            int numAll = ontologyTerms.size();
            double ic = (double)depth * (1.00-(Math.log((double)numDesc+1.00) / Math.log((double)numAll)));
            infoContent.put(t.termID, ic);
        }
    }
    
    
    
    private void recurse(String termID, int depth) throws ClassExpressionException{
        for (String nextTermID: brain.getSubClasses(termID, true)){
            termDepths.put(nextTermID, depth);
            recurse(nextTermID, depth+1);
        }
    }
    
   
    double getIC(String termID){
        try{
            return infoContent.get(termID);
        }
        catch(NullPointerException e){
            logger.info(String.format("problem finding ontology ic of %s",termID));
            return 0.000;
        }
    }
    
    double getIC(OntologyTerm term){
        try{
            return infoContent.get(term.termID);
        }
        catch(NullPointerException e){
            logger.info(String.format("problem finding ontology ic of %s",term.termID));
            return 0.000;
        }
    }
    
    
    
    
    
    
    
    
}
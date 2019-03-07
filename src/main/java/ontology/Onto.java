/*
 * Ian Braun
 * irbraun@iastate.edu
 * term-mapping 
 */
package ontology;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import structure.OntologyTerm;
import uk.ac.ebi.brain.core.Brain;
import uk.ac.ebi.brain.error.ClassExpressionException;
import uk.ac.ebi.brain.error.NewOntologyException;
import uk.ac.ebi.brain.error.NonExistingEntityException;

/**
 *
 * @author irbraun
 */
public class Onto{
    
    private ArrayList<OntologyTerm> terms;
    private HashMap<String,Integer> termOverlaps;
    // make this private now
    public HashMap<String,OntologyTerm> termsMap;
    private final OWLOntology ontology;
    private final Brain brain;
    
    
    
    
    public Onto(String owlFilePath) throws OWLOntologyCreationException, NewOntologyException, ClassExpressionException{
        File ontologyFile = new File(owlFilePath);
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        ontology = manager.loadOntologyFromOntologyDocument(ontologyFile);
        brain = new Brain(ontology);
        populateTermList();
    }
    
      
    
    // Only terms that have labels are added to the term list.
    private void populateTermList() throws ClassExpressionException{
        terms = new ArrayList<>();     
        termsMap = new HashMap<>();
        for (OWLClass cls : ontology.getClassesInSignature()){
            try{
                OntologyTerm term = new OntologyTerm(cls, ontology, brain); 
                terms.add(term);
                termsMap.put(term.termID, term);
            }
            catch(NonExistingEntityException e){
            }
        }
    }
    
    
    public OntologyTerm getTermFromTermID(String termID){
        return termsMap.getOrDefault(termID, null);
    }
    
    
    public Brain getBrain(){
        return brain;
    }

    
    
    /*
     * Only should be called when using smaller ontologies, way too expensive for GO.
     * This is not currently used at all. Could provide small time savings because 
     * this method avoids having the calculate any hierarchical similarities between
     * two terms and their paths more than once. These are part of the attributes
     * (values associated with (c,t) instance that are used for evaluation, analysis,
     * or as a target value for regression or classification, but not as a feature. And
     * finding all the attributes is a very very small fraction of the runtime, so the
     * difference is not likely to matter.
    /*
    private void populateTermOverlapMap(){
        for (int i=0; i<terms.size(); i++){
            for (int j=i+1; j<terms.size(); j++){
                OntologyTerm term1 = terms.get(i);
                OntologyTerm term2 = terms.get(j);

                List intersect = new ArrayList<>(term1.allNodes);
                intersect.retainAll(term2.allNodes);
                int overlap = intersect.size();   
                
                Object[] data = {term1.termID, term2.termID};
                String nodePair = String.format("%s:%s",data);
                termOverlaps.put(nodePair, overlap);                     
            }
        }
    }
    */
    

    
    
    public ArrayList<OntologyTerm> getTermList(){
        return terms;
    }
   
    public int getTermListSize(){
        return terms.size();
    }
    
    
    
    /**
     * Only finds the metrics dynamically, does not use the term overlap map.
     * See note above, this might be called more than once for the same pair of terms
     * which is inefficient but it is a tiny fraction of the overall runtime.
     * @param term1 Considered to the predicted term.
     * @param term2 Considered to the curated term.
     * @return 
     */
    public double[] getHierarchicalEvals(OntologyTerm term1, OntologyTerm term2){

        double overlap;
        double combined;
        HashSet intersect = new HashSet<>(term1.allNodes);
        intersect.retainAll(term2.allNodes);
        HashSet union = new HashSet<>(term1.allNodes);
        union.addAll(term2.allNodes);

        overlap = intersect.size();   
        combined = union.size();   

        double hPrec = (double) overlap / (double) term1.allNodes.size();
        double hRec = (double) overlap / (double) term2.allNodes.size();
        double hJac = (double) overlap / (double) combined;
        
        // F1 is undefined when precision and recall are both 0, just return a minimum value for F1 in that case.
        double hF1 = 0.00;
        if (hPrec+hRec != 0){
            hF1 = (double) (2.000*hPrec*hRec) / (double) (hPrec+hRec);
        }
        
        double[] hierEvals = {hPrec, hRec, hF1, hJac};
        return hierEvals;
    }
    
    
    
    
    /**
     * Overloading for string arguments.
     * @param term1ID
     * @param term2ID
     * @return 
     */
    public double[] getHierarachicalEvals(String term1ID, String term2ID){
        OntologyTerm term1 = termsMap.get(term1ID);
        OntologyTerm term2 = termsMap.get(term2ID);
        return getHierarchicalEvals(term1, term2);
    }
    
    
    

    
    
    
}

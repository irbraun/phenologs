
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
import objects.OntologyTerm;
import uk.ac.ebi.brain.core.Brain;
import uk.ac.ebi.brain.error.ClassExpressionException;
import uk.ac.ebi.brain.error.NewOntologyException;
import uk.ac.ebi.brain.error.NonExistingEntityException;


public class Onto{
    
    private ArrayList<OntologyTerm> terms;
    private HashMap<String,Integer> termOverlaps;
    private HashMap<String,OntologyTerm> termsMap;
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

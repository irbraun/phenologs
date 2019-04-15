package structure;

import static composer.Utils.normalizeTermID;
import enums.Aspect;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.ebi.brain.core.Brain;
import uk.ac.ebi.brain.error.ClassExpressionException;
import uk.ac.ebi.brain.error.NonExistingEntityException;


public class OntologyTerm {
    
    
    public String termID;
    public String unnormalizedTermID; 
    // The unnormalizedTermID is necessary to remember because when the Brain is called outside of
    // of this class it needs access to the unnormalized version of the ID so that it exactly matches
    // the OWL file.
    public String label;
    public String description;
    public List<String> exactSynonyms;
    public List<String> narrowSynonyms;
    public List<String> broadSynonyms;
    public List<String> relatedSynonyms;
    public List inheritedNodes;
    public List siblingNodes;
    public List parentNodes;
    public List allNodes;
    
    
    
    public OntologyTerm(OWLClass cls, OWLOntology ontology, Brain brain) throws NonExistingEntityException, ClassExpressionException{
    
        // Get the ID and label for this class.
        String fullID = cls.toStringID();
        this.unnormalizedTermID = fullID.substring(fullID.lastIndexOf("/")+1);
        this.termID = normalizeTermID(unnormalizedTermID);
        this.label = brain.getLabel(unnormalizedTermID).replace(",", "").trim();
        
        List<String> temp = brain.getSuperClasses(unnormalizedTermID, false);
        temp.remove("Thing");
        this.inheritedNodes = temp;
        List<String> temp2 = new ArrayList<>(this.inheritedNodes);
        temp2.add(this.termID);
        this.allNodes = temp2;

        // The extended description of the term, represented by an empty string if not used.
        Set <OWLAnnotationAssertionAxiom> annotationSet = cls.getAnnotationAssertionAxioms(ontology);
        String desc = getDescription(annotationSet);
        this.description = getDescription(annotationSet);
        
        // Get the sibling terms of this term.
        siblingNodes = new ArrayList<>();
        List<String> parents = brain.getSuperClasses(unnormalizedTermID, true);
        if (!parents.contains("Thing")){
            for (String parent: parents){
                List<String> siblings = brain.getSubClasses(parent, true);
                for (String sibling: siblings){
                    siblingNodes.add(sibling);
                }
            }
            siblingNodes.remove(this.termID);
        }
        
        // Get the parent terms of this term.
        parentNodes = new ArrayList<>();
        parentNodes = brain.getSuperClasses(unnormalizedTermID, true);

        
        // Synonyms are represented by emtpy lists if they are not used by this term.
        try{
            this.exactSynonyms = brain.getAnnotations(unnormalizedTermID, "hasExactSynonym");
            for (String s: this.exactSynonyms){
                s = s.replace("(exact)","").trim();
            }
        }
        catch(NonExistingEntityException e){
            this.exactSynonyms = new ArrayList<>();
        }
        
        try{
            this.narrowSynonyms = brain.getAnnotations(unnormalizedTermID, "hasNarrowSynonym");
            for (String s: this.exactSynonyms){
                s = s.replace("(narrow)","").trim();
            }
        }
        catch(NonExistingEntityException e){
            this.narrowSynonyms = new ArrayList<>();
        }
        
        try{
            this.broadSynonyms = brain.getAnnotations(unnormalizedTermID, "hasBroadSynonym");
            for (String s: this.exactSynonyms){
                s = s.replace("(broad)","").trim();
            }
        }
        catch(NonExistingEntityException e){
            this.broadSynonyms = new ArrayList<>();
        }
        
        try{
            this.relatedSynonyms = brain.getAnnotations(unnormalizedTermID, "hasRelatedSynonym");
            for (String s: this.exactSynonyms){
                s = s.replace("(related)","").trim();
            }
        }
        catch(NonExistingEntityException e){
            this.relatedSynonyms = new ArrayList<>();
        }
        
    }
 
    
    
    
    // Returns a list of words that were in the desired aspect of this term.
    public List<String> getAllWords(Aspect aspect){
        
        switch(aspect){
        case LABEL: return tokenized(label);
        case DESCRIPTION: return tokenized(description);
        case EXACT_SYN: return tokenized(exactSynonyms);
        case RELATED_SYN: return tokenized(relatedSynonyms);
        case NARROW_SYN: return tokenized(narrowSynonyms);
        case BROAD_SYN: return tokenized(broadSynonyms);
        }
        return null;
    }
    
    public List<String> getAllSynonymWords(){
        ArrayList<String> synonyms = new ArrayList<>();
        synonyms.addAll(this.getAllWords(Aspect.EXACT_SYN));
        synonyms.addAll(this.getAllWords(Aspect.NARROW_SYN));
        synonyms.addAll(this.getAllWords(Aspect.RELATED_SYN));
        synonyms.addAll(this.getAllWords(Aspect.BROAD_SYN));
        return synonyms;
    }
    
    public List<String> getAllSynonyms(){
        ArrayList<String> synonyms = new ArrayList<>();
        synonyms.addAll(this.exactSynonyms);
        synonyms.addAll(this.narrowSynonyms);
        synonyms.addAll(this.relatedSynonyms);
        synonyms.addAll(this.broadSynonyms);
        return synonyms;
    }
    
    
    // Split the aspect into individual words. Handles empty lists as well.
    private List<String> tokenized(List<String> synonyms){
        List tokens = new ArrayList<>();
        for (String synonym : synonyms){
            String cleaned = synonym.replaceAll("\\s+|_|-|,"," ").trim();
            tokens.addAll(Arrays.asList(cleaned.split("\\s+")));
        }
        return tokens;
    }
 
    // Split the aspect into individual words. Handles emtpy strings as well.
    private List<String> tokenized(String other){
        if (other.equals("")){
            return new ArrayList<>();
        } 
        else{
            String cleaned = other.replaceAll("\\s+|_|-|,"," ").trim();
            List tokens = Arrays.asList(cleaned.split("\\s+"));  
            return tokens;
        }
    }
    
    

    
    /**
     * Ad hoc method that finds the extended description of a term from a given ontology.
     * Rules for each ontology for finding these descriptions from the set of annotations
     * for the class can be specified here on a case by base basis.
     * @param annotationSet
     * @return 
     */
    private String getDescription(Set<OWLAnnotationAssertionAxiom> annotationSet){
        String desc = "";
        switch(utils.Util.inferOntology(this.termID)){  
        case PATO:
            try{
                for (OWLAnnotationAssertionAxiom annotation : annotationSet){
                    String annotationStr = annotation.toString();
                    if (annotationStr.contains("AnnotationAssertion(Annotation(")){
                        desc = annotationStr.substring(annotationStr.lastIndexOf(">")+3, annotationStr.lastIndexOf("\"")-1);
                    }
                }
            }
            catch(Exception e){
            }
        case PO:                
            try{
                for (OWLAnnotationAssertionAxiom annotation : annotationSet){
                    String annotationStr = annotation.toString();
                    if (annotationStr.contains("AnnotationAssertion(Annotation(")){
                        desc = annotationStr.substring(annotationStr.lastIndexOf(">")+3, annotationStr.lastIndexOf("\"")-1);
                    }
                }
            }
            catch(Exception e){
            }
        case GO:
            try{
                for (OWLAnnotationAssertionAxiom annotation : annotationSet){
                    String annotationStr = annotation.toString();
                    if (annotationStr.contains("AnnotationAssertion(Annotation(")){
                        desc = annotationStr.substring(annotationStr.lastIndexOf(">")+3, annotationStr.lastIndexOf("\"")-1);
                    }
                }
            }
            catch(Exception e){
            }
        default:
            try{
                for (OWLAnnotationAssertionAxiom annotation : annotationSet){
                    String annotationStr = annotation.toString();
                    if (annotationStr.contains("AnnotationAssertion(Annotation(")){
                        desc = annotationStr.substring(annotationStr.lastIndexOf(">")+3, annotationStr.lastIndexOf("\"")-1);
                    }
                }
            }
            catch(Exception e){
            }
        }
        return desc;
    }
    
}

/*
 * Ian Braun
 * irbraun@iastate.edu
 * term-mapping 
 */
package composer;

import config.Config;
import enums.Ontology;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import ontology.Onto;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import uk.ac.ebi.brain.core.Brain;
import uk.ac.ebi.brain.error.NewOntologyException;

/**
 *
 * @author irbraun
 */
public class RulesForPATO {
    
    public HashSet<String> relationalQualityIDs;
    public HashSet<String> qualifierIDs;
    
    
    public RulesForPATO(Onto patoOntoObject) throws NewOntologyException, OWLOntologyCreationException, Exception{
           
        
        // Populating the list of relational qualities.
        relationalQualityIDs = new HashSet<>(); 
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology patoOntology = manager.loadOntologyFromOntologyDocument(new File(Config.ontologyPaths.get(Ontology.PATO)));
        Brain patoBrain = new Brain(patoOntology);
        
        // Determine if the given class is relational.
        for (OWLClass cls : patoOntology.getClassesInSignature()){
            String fullID = cls.toStringID();
            String termID = fullID.substring(fullID.lastIndexOf("/")+1);
            Set <OWLAnnotationAssertionAxiom> annotationSet = cls.getAnnotationAssertionAxioms(patoOntology);
            for (OWLAnnotationAssertionAxiom annotation : annotationSet){
                if (annotation.toString().contains("relational_slim")){
                    relationalQualityIDs.add(termID);
                }
            }
        }
        
        // The optional qualifiers that were used by curators for the Plant PhenomeNET data, restricting to this list.
        String[] qualifierIDsArr = { 
            "PATO_0000049", // intensity
            "PATO_0000394", // mild
            "PATO_0000396", // severe
            "PATO_0000460", // abnormal
            "PATO_0000461", // normal
            "PATO_0000462", // absent
            "PATO_0000467"  // present
        };   
        qualifierIDs = new HashSet(Arrays.asList(qualifierIDsArr));
        
       
        
    }
}

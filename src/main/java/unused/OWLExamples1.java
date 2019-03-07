 package unused;


import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import uk.ac.ebi.brain.core.Brain;
import uk.ac.ebi.brain.error.BrainException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author irbraun
 */
public class OWLExamples1 {
    
    public static void main(String[] args) throws BrainException, OWLOntologyCreationException, IOException, ClassNotFoundException, SQLException{
        
        
  
       
        
        // Read in the PATO ontology file.
        File patoFile = new File("/Users/irbraun/Desktop/pato.owl.txt");
        
        // Create the owl objects.
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology;
        ontology = manager.loadOntologyFromOntologyDocument(patoFile);
        
        // Create the brain wrapper.
        Brain brain = new Brain(ontology);
        
        
        
        List <String> subClassesTest = brain.getSuperClasses("PATO_0002393", true);
        for (String a : subClassesTest){
            System.out.println(brain.getLabel(a));
        }
        
        
        
        //String an = brain.getAnnotation("PATO_0002403", "inSubset");
        //System.out.println(an);
        String an2 = brain.getAnnotation("PATO_0002403", "creation_date");
        System.out.println("this " + an2);
        
        
        

        //Loop through all the classes in the ontology and collect information.
        for (OWLClass cls : ontology.getClassesInSignature()){
            
            // Get the short name of the class.
            String fullID = cls.toStringID();
            String id = fullID.substring(fullID.lastIndexOf("/")+1);
            //System.out.println(id);
            
            // Label
            // Note, for some reason mutli-word labels don't work in get functions.
            String label = brain.getLabel(id);
            
            // Synonyms
            List<String> exactSynonyms = brain.getAnnotations(id, "hasExactSynonym");
            List<String> narrowSynonyms = brain.getAnnotations(id, "hasNarrowSynonym");
            List<String> broadSynonyms = brain.getAnnotations(id, "hasBroadSynonym");
            List<String> relatedSynonyms = brain.getAnnotations(id, "hasRelatedSynonym");
            
            
            // Sub and Super Classes, true means direct, so only the immediate next level results.
            // What's a sub and super are already specified, "Restriction" says what the specific type of edge is.
            List <String> subClasses = brain.getSubClasses(id, true);
            List <String> superClasses = brain.getSuperClasses(id, true);
            
            
            // Need to implement those methods. Unimplemented methods.
            Set <OWLAnnotationAssertionAxiom> annotationSet = cls.getAnnotationAssertionAxioms(ontology);
            //String slim = getSlim(annotationSet);
            //String description = getDescription(annotationSet);
            
            
            // This gives you everything. This is how the above will ultimately be implemented.
            // Need to pull #inSubset, and "Annotation" one has the extended description for whatever reason.
            //OWLClass c = brain.getOWLClass("PATO_0002403");
            //for (OWLAnnotationAssertionAxiom aaa : c.getAnnotationAssertionAxioms(ontology)){
            //    System.out.println(aaa);
            //}
            
            
  
        }    

           
      
    }
    
      
    
}

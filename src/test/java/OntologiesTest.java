/*
 * Ian Braun
 * irbraun@iastate.edu
 * term-mapping 
 */

import composer.EQStatement;
import composer.Term;
import config.Config;
import config.Connect;
import enums.Ontology;
import enums.TextDatatype;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nlp.CoreNLP;
import ontology.Onto;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import objects.Chunk;
import objects.OntologyTerm;
import text.Text;
import uk.ac.ebi.brain.error.ClassExpressionException;
import uk.ac.ebi.brain.error.NewOntologyException;

/**
 *
 * @author irbraun
 */
public class OntologiesTest {
    
    public OntologiesTest() throws IOException, SQLException {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws IOException, SQLException, FileNotFoundException, ParseException {
        //Config config = new Config("/Users/irbraun/NetBeansProjects/term-mapping/");
        //Connect conn = new Connect();
        //CoreNLP.setup();
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    
    
   
    
    
    
    
    
    /*
    
    @Test
    public void testOntologyConsistency() throws OWLOntologyCreationException, NewOntologyException, ClassExpressionException, SQLException, IOException{

        setUp();
        Text text = new Text();
        HashMap<Ontology,Onto> ontoObjects = new HashMap<>();
        ontoObjects.put(Ontology.PATO, new Onto(Config.patoOntologyPath));
        ontoObjects.put(Ontology.PO, new Onto(Config.poOntologyPath));        
        ontoObjects.put(Ontology.GO, new Onto(Config.goOntologyPath));
        
        List<Chunk> chunks = text.getAllAtomChunks();
        Set<String> corpusTermIDs = new HashSet<>();
        for (Chunk chunk: chunks){
            corpusTermIDs.addAll(text.getAllTermIDs(chunk.chunkID, TextType.ATOM));
        }
        
        System.out.println(corpusTermIDs.size() + " unique ontology terms are present in the corpus.");
        System.out.println();
        
        for (Map.Entry<Ontology,Onto> entry: ontoObjects.entrySet()){
            Ontology ontologyName = entry.getKey();
            Onto ontologyObj = entry.getValue();
            int terms = 0;
            int obsoleteTerms = 0;
            int usedTerms = 0;
            int usedObsoleteTerms = 0;
            for (OntologyTerm term: ontologyObj.getTermList()){
                terms++;
                if (term.label.contains("obsolete")){
                    obsoleteTerms++;
                    if (corpusTermIDs.contains(term.termID)){
                        usedObsoleteTerms++;
                    }
                }
                if (corpusTermIDs.contains(term.termID)){
                    usedTerms++;
                }
            }
            System.out.println(String.format("%s contains %s terms, of which %s are obsolete.", ontologyName.toString(), terms, obsoleteTerms));
            System.out.println(String.format("The corpus uses %s %s terms, of which %s are obsolete.", usedTerms, ontologyName.toString(), usedObsoleteTerms));
            System.out.println();
        }
    }
    

    */
    
    
    
    
    
    
}

/*
 * Ian Braun
 * irbraun@iastate.edu
 * term-mapping 
 */

import config.Config;
import config.Connect;
import enums.Ontology;
import enums.Role;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import nlp.CoreNLP;
import ontology.Onto;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import structure.Chunk;
import text.Text;
import uk.ac.ebi.brain.error.ClassExpressionException;
import uk.ac.ebi.brain.error.NewOntologyException;

/**
 *
 * @author irbraun
 */
public class ConceptMappingTest {
    
    public ConceptMappingTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
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
    public void testNobleCoder() throws IOException, FileNotFoundException, SQLException, ParseException, NewOntologyException, OWLOntologyCreationException, ClassExpressionException{
       
        String outputFile = "/Users/irbraun/NetBeansProjects/term-mapping/dmom/NobleCoder/evaluation.csv";
        String inputFile = "/Users/irbraun/NetBeansProjects/term-mapping/dmom/NobleCoder/outputs_all_ppn_pato/classprobs.csv";
        
        nobleCoderHelper(Ontology.PATO, inputFile, outputFile);

    }
    
    
    
    
    private void nobleCoderHelper(Ontology ontology, String inputFilename, String outputFilename) throws IOException, FileNotFoundException, ParseException, SQLException, OWLOntologyCreationException, NewOntologyException, ClassExpressionException{
        
        Config config = new Config("/Users/irbraun/NetBeansProjects/term-mapping/");
        Connect conn = new Connect();
        Text text = new Text();
        List<Chunk> chunks = text.getAllAtomChunks();
        
        HashMap<Integer,ArrayList<String>> nobleCoderTermsMap = new HashMap<>();
        
        File output = new File(outputFilename);
        PrintWriter writer = new PrintWriter(output);
        
        File classProbFile = new File(inputFilename);
        Scanner scanner = new Scanner(classProbFile);
        scanner.useDelimiter(",");
        scanner.nextLine();

        while(scanner.hasNextLine()){
            String line = scanner.nextLine();
            String[] lineValues = line.split(",");
            String chunkIDStr = lineValues[0].replace("\"", "");
            int chunkID = Integer.valueOf(chunkIDStr);
            String termID = lineValues[1].replace("\"","");
            double probability = Double.valueOf(lineValues[2]);
            //annotatedTermsMap.getOrDefault(chunkID, new ArrayList<>()).add(termID);
            ArrayList<String> a = nobleCoderTermsMap.getOrDefault(chunkID, new ArrayList<>());
            a.add(termID);
            nobleCoderTermsMap.put(chunkID, a);
        }
        
        Onto onto = new Onto(Config.ontologyPaths.get(ontology));
        
        writer.println("chunk,text,label,term,component,category,similarity");
        
        for (Chunk c: chunks){
            ArrayList<String> termIDs = text.getCuratedEQStatementFromAtomID(c.chunkID).getAllTermIDs();
            ArrayList<Role> termRoles = text.getCuratedEQStatementFromAtomID(c.chunkID).getAllTermRoles();
            for (int i=0; i<termIDs.size(); i++){
                String id = termIDs.get(i);
                Role role = termRoles.get(i);
                if (utils.Util.inferOntology(id).equals(ontology)){
                    if (!nobleCoderTermsMap.getOrDefault(c.chunkID, new ArrayList<>()).contains(id)){
                        // false negatives
                        Object[] line = {c.chunkID, c.getRawText().replace(",", ""), onto.getTermFromTermID(id).label, id, Role.getAbbrev(role), "FN", "0.000"};
                        writer.println(String.format("%s,%s,%s,%s,%s,%s,%s",line));
                    }
                    else {
                        // true positives
                        Object[] line = {c.chunkID, c.getRawText().replace(",", ""), onto.getTermFromTermID(id).label, id, Role.getAbbrev(role), "TP", "1.000"};
                        writer.println(String.format("%s,%s,%s,%s,%s,%s,%s",line));
                    }
                }
            }
            for (String id: nobleCoderTermsMap.getOrDefault(c.chunkID, new ArrayList<>())){
                if(!termIDs.contains(id)){
                    // false positives
                    double simD = utils.Util.populateAttributes(c, onto.getTermFromTermID(id), text, onto).hJac;
                    String sim = String.format("%.3f",simD);
                    Object[] line = {c.chunkID, c.getRawText().replace(",",""), onto.getTermFromTermID(id).label, id, "none", "FP", sim};
                    writer.println(String.format("%s,%s,%s,%s,%s,%s,%s",line));
                }
            }
        }
 
        writer.close();
 
    }
    */
    
    
    
    
    
    
    
    
    
    
    
}

package unused;



import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import uk.ac.ebi.brain.core.Brain;
import uk.ac.ebi.brain.error.NewOntologyException;
import uk.ac.ebi.brain.error.NonExistingEntityException;



/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author irbraun
 */
public class OWLExamples2 {
    
    private Connection conn;
    private final String TABLE;
    private int entryIdx;
    private Multimap mmp;
    private Set set;
    private Map indexBridgeMap;
    private Map indexPatoIDMap;
    
    
    
    /**
     * Constructor that establishes the connection with the database, populates the database with the ontology,
     * and queries it to provide other useful structures.
     * @param username
     * @param password
     * @param database
     * @param table
     * @throws ClassNotFoundException
     * @throws SQLException 
     */
    public OWLExamples2(String username, String password, String database, String table, String ontologyFileName) throws ClassNotFoundException, SQLException, OWLOntologyCreationException, NewOntologyException{
        
        this.TABLE = table;
        
        // Establish the connection with the database.
        String connectionStr = "jdbc:mysql://localhost/" + database + "?useSSL=false";
                
        try{
            this.conn = (Connection) DriverManager.getConnection(connectionStr, username, password);  
        }
        catch(SQLException ex){
            System.out.println("SQLException: " + ex.getMessage());
        }
        
        
        Statement stmt = null;
        
        // Drop the table if it already exists, so that we're not adding to an existing one.
        String attemptDropTable = "DROP TABLE IF EXISTS " + table;
        stmt = conn.createStatement();
        stmt.execute(attemptDropTable);
        
     
        // Make a table for all words in PATO if the table does not already exist, doesn't really need to check now. 
        String attemptCreateTable = "CREATE TABLE IF NOT EXISTS " + table + " (entry_idx int, word VARCHAR(50), pato_id VARCHAR(50), source VARCHAR(50));";
        stmt = conn.createStatement();
        stmt.execute(attemptCreateTable);
        
        indexBridgeMap = new HashMap<>();
        indexPatoIDMap = new HashMap<>();
        
        // Add the ontology to the database.
        addOntology(ontologyFileName);
        
        // Use the contents of the database to provide other useful information from this class.
        makeWordMultiMap();
        makeWordSet();
        
        
        
    }
    
    
    
   
    /**
     * Used by the constructor to make a multimap that associates words with lines in the searchable PATO
     * database. Actually uses the database itself for now.
     * @throws SQLException 
     */
    private void makeWordMultiMap() throws SQLException{
        
        mmp = ArrayListMultimap.create();

        String stmtStr = String.format("SELECT * FROM %s;", TABLE);
        Statement selectStmt = conn.createStatement();
        selectStmt.execute(stmtStr);
        
        ResultSet resultSet = selectStmt.getResultSet();
        while(resultSet.next()){
            String word = resultSet.getString("word");
            int idx = resultSet.getInt("entry_idx");
            mmp.put(word, idx);
        }
    }
    
   
    private void makeWordSet(){
        set = new HashSet<>();
        set.addAll(mmp.keys());
    }
    
    
 
    
    public Set getWordSet(){
        return set;
    }
 
    
    public Collection getIndices(String word){
        return mmp.get(word);
    }
    

    public String indexToBridge(int index){
        return (String) indexBridgeMap.get(index);
    }
    

    public String indexToPatoID(int index){
        return (String) indexPatoIDMap.get(index);
    }
    
    

    
    
    /**
     * Adds the contents of the passed in PATO owl file to the database that was created. Uses the brain
     * OWL API and Elk wrapper to look through the classes of the ontology.
     * @param ontologyFileName
     * @throws OWLOntologyCreationException
     * @throws NewOntologyException
     * @throws SQLException
     * @throws ClassNotFoundException 
     */
    private void addOntology(String ontologyFileName) throws OWLOntologyCreationException, NewOntologyException, SQLException, ClassNotFoundException{
    
        // Read in the PATO ontology and create the brain wrapper.
        File patoFile = new File(ontologyFileName);
        
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology;
        ontology = manager.loadOntologyFromOntologyDocument(patoFile);
        
        Brain brain = new Brain(ontology);
        

        // Iterate over all classes found in PATO.
        for (OWLClass cls : ontology.getClassesInSignature()){
            
            String fullID = cls.toStringID();
            String id = fullID.substring(fullID.lastIndexOf("/")+1);
            
            // The label of the term.
            try {
                String label = brain.getLabel(id);
                breakAndInsert(label, id, "Label");
            } 
            catch (NonExistingEntityException ex) {
                //Logger.getLogger(SearchablePATO.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
          
            // The synonyms, of all types.
            for (String synType : Arrays.asList("Exact", "Narrow", "Broad", "Related")){
            
                try{
                    List<String> synonyms = brain.getAnnotations(id, "has"+synType+"Synonym");
                    for (String synonym : synonyms){
                        breakAndInsert(synonym, id, synType);
                    }
                }
                catch (uk.ac.ebi.brain.error.NonExistingEntityException ex){
                }
            }

            // The extended description of the term.
            Set <OWLAnnotationAssertionAxiom> annotationSet = cls.getAnnotationAssertionAxioms(ontology);
            
            String description = getDescription(annotationSet);
            if (description != null){
                breakAndInsert(description, id, "Description");
            }
            

        }
        

    }
    
    
    /**
     * Adds each individual word from the phrase into the searchable ontology database.
     * @param phrase Some phrase from the ontology.
     * @param patoID
     * @param bridge What type the phrase is (label, synonym, description, etc).
     * @throws SQLException 
     */
    private void breakAndInsert(String phrase, String patoID, String bridge) throws SQLException{
    
        for (String word : phrase.trim().split("\\s+")){
            
            Object[] data = {TABLE, "entry_idx", "word", "pato_id", "source", entryIdx, word, patoID, bridge};    
            String stmtStr = String.format("INSERT INTO %s (%s,%s,%s,%s) VALUES (%d,\"%s\",\"%s\",\"%s\");", data);
                                                                      
            Statement insertStmt = conn.createStatement();
            insertStmt.execute(stmtStr);
            entryIdx++;
            indexBridgeMap.put(entryIdx, bridge);
            indexPatoIDMap.put(entryIdx, patoID);
        }
    }
    
    
    
    /**
     * Ad hoc method that finds the extended description of a class in the ontology.
     * @param annotationSet
     * @return 
     */
    private String getDescription(Set<OWLAnnotationAssertionAxiom> annotationSet){
        
        String description = null;
        for (OWLAnnotationAssertionAxiom annotation : annotationSet){
            
            String annotationStr = annotation.toString();
            
            // This part is especially ad hoc, specific to the ontology.
            if (annotationStr.contains("AnnotationAssertion(Annotation(")){
                description = annotationStr.substring(annotationStr.lastIndexOf(">")+3, annotationStr.lastIndexOf("\"")-1);
            }
        }
        return description;
    }
    
    
 
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}

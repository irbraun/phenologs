/*
 * Ian Braun
 * irbraun@iastate.edu
 * term-mapping 
 */
package unused;

import composer.Term;
import enums.Ontology;
import java.io.File;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import ontology.Onto;
import objects.Chunk;
import objects.OntologyTerm;
import text.Text;

/**
 *
 * @author irbraun
 */
public class LabelMatching2 {
    
    private List<OntologyTerm> terms; 
    private Ontology ontology;    
    
    public void routine() throws Exception{
        //search(Ontology.UBERON, new Text("char"), "/Users/irbraun/Desktop/droplet/alpha2/mapping/label_matching/gsd_uberon_testing.csv");
        //search(Ontology.PATO, new Text("state"), "/Users/irbraun/Desktop/droplet/alpha2/mapping/label_matching/gsd_pato_testing.csv");
    }
    
    
    

    
    private void search(Ontology ontology, Text text, String outputPath) throws SQLException, Exception{
        
        List<Chunk> chunks = text.getAllAtomChunks();
        String ontologyPath = utils.Utils.pickOntologyPath(ontology.toString());
        Onto onto = new Onto(ontologyPath);
        terms = onto.getTermList();        
        
        // Iterate through each chunk in this group of partitions.
        ArrayList<String> lines = new ArrayList<>();
        for (Chunk chunk: chunks){
            List<Term> matchingTerms = findMatchingTerms(chunk);
            for (Term term: matchingTerms){
                Object[] items2 = {chunk.chunkID, term.id, term.probability};
                String line = String.format("%s,%s,%s",items2);       
                lines.add(line);
            }
        }
        
        File outputFile = new File(outputPath);
        PrintWriter writer = new PrintWriter(outputFile);
        writer.println("chunk,term,prob");
        for (String line: lines){
            writer.println(line);
        }
        writer.close();
    }
    
    
    
    /**
     * Could add additional RegEx replacement simplification for the raw text. Not 
     * currently actually using a true method for fuzzy matching yet, just label matching.
     * Could additionally look at the bag of words as well, for both the chunk and the 
     * ontology term and then the probability could come from the fraction of the bag
     * of words for the label that is an exact match. Might be making it too complicated
     * though, don't need all that just to compare to the random forest which is the 
     * intended method for the smaller ontologies.
     * @param chunk
     * @return 
     */
    private List<Term> findMatchingTerms(Chunk chunk){
        List<Term> matches = new ArrayList<>();
        for (OntologyTerm term: terms){
            if(chunk.getRawText().toLowerCase().contains(term.label.toLowerCase())){
                matches.add(new Term(term.termID, 1.00, ontology));
            }
        }
        return matches;
    }
    
    
    
    
    
    
    
    
    
    
    
}

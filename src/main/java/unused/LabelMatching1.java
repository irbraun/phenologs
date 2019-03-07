/*
 * Ian Braun
 * irbraun@iastate.edu
 * term-mapping 
 */
package unused;

import composer.Term;
import config.Config;
import enums.Ontology;
import enums.TextDatatype;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import main.Partitions;
import ontology.Onto;
import structure.Chunk;
import structure.OntologyTerm;
import text.Text;



/**
 * Class for producing class probability files using simple label matching. All of the probabilities
 * are currently simply one if the term is present at all in the resulting list of matching terms.
 * Has to be used in conjunction with at least one of the other methods that provides at least 
 * candidate term for each input text, otherwise there could be a case where no candidate term is
 * provided for a mandatory role in the output and that case isn't accounted for.
 * @author irbraun
 */
public class LabelMatching1 {
    
    
    
    private List<OntologyTerm> terms; 
    private Ontology ontology;    
    
    public void routine() throws Exception{
        
        /*
        for (String ontologyName: Config.fuzzyOntologies){
            for (String format: Config.fuzzyFormats){
                // Hardcoding the split between the outer loops for cross-validation.
                // Notation is that for out cv split "A", partitions 0 through 7 are the testing data, etc.
                HashMap<String,List<String>> linesMap = new HashMap<>();
                linesMap.put("A", search(utils.Util.inferOntology(ontologyName), utils.Util.inferTextType(format), 0, 7));
                linesMap.put("B", search(utils.Util.inferOntology(ontologyName), utils.Util.inferTextType(format), 8, 15));
                linesMap.put("C", search(utils.Util.inferOntology(ontologyName), utils.Util.inferTextType(format), 16, 23));
                linesMap.put("D", search(utils.Util.inferOntology(ontologyName), utils.Util.inferTextType(format), 24, 31));
                writeFiles(utils.Util.inferOntology(ontologyName), utils.Util.inferTextType(format), linesMap);
            }
        }
        */
    }
    
    
    

    
    private ArrayList<String> search(Ontology ontology, TextDatatype format, int lowerPartNum, int upperPartNum) throws SQLException, Exception{
        
        Text text = new Text();
        List<Chunk> chunks = text.getChunksOfKind(format);
        
        // The lower and upper partition numbers are inclusive.
        List<Integer> parts = new ArrayList<>();
        for (int part=lowerPartNum; part<upperPartNum+1; part++){
            parts.add(part);
        }
       
        Partitions partitions = new Partitions(text, "");
        List<Chunk> usedChunks = partitions.getChunksFromPartitions(parts, chunks);
        String ontologyPath = utils.Util.pickOntologyPath(ontology.toString());
        Onto onto = new Onto(ontologyPath);
        
        terms = onto.getTermList();        
        
        int i=0;
        // Iterate through each chunk in this group of partitions.
        ArrayList<String> lines = new ArrayList<>();
        for (Chunk chunk: usedChunks){
            i++;
            List<Term> matchingTerms = findMatchingTerms(chunk);
            for (Term term: matchingTerms){
                Object[] items2 = {chunk.chunkID, term.id, term.probability};
                String line = String.format("%s,%s,%s",items2);       
                lines.add(line);
            }
        }
        return lines;
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
    
    
    
    
    
    
    
    
    
    
    private void writeFiles(Ontology ontology, TextDatatype format, HashMap<String,List<String>> linesMap) throws FileNotFoundException{
        
        for (String postfix: linesMap.keySet()){

            /*
            // Get all the lines that make up the testing data for this particular split.
            Object[] items = {Config.fuzzyOutputPath, ontology.toString().toLowerCase(), format.toString().toLowerCase(), postfix};
            File outputFileTesting = new File(String.format("%s%s_%s_%s_testing.csv", items));
            PrintWriter testWriter = new PrintWriter(outputFileTesting);
            testWriter.println("chunk,term,prob");
            for (String line: linesMap.get(postfix)){
                testWriter.println(line);
            }
            testWriter.close();
            
            
            // Get all the lines that make up the training data for this particular split.
            File outputFileTraining = new File(String.format("%s%s_%s_%s_training.csv", items));
            PrintWriter trainWriter = new PrintWriter(outputFileTraining);
            trainWriter.println("chunk,term,prob");
            ArrayList<String> trainingLines = new ArrayList<>();
            for (String innerPostfix: linesMap.keySet()){
                if (!innerPostfix.equals(postfix)){
                    trainingLines.addAll(linesMap.get(innerPostfix));
                }
            }            
            for (String line: trainingLines){
                trainWriter.println(line);
            }
            trainWriter.close();
            */
        }        
        
    }
    
    
    
    
    
    
    
   
}

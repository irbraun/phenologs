

package infocontent;

import enums.TextDatatype;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import static main.Main.logger;
import objects.Chunk;
import objects.Word;
import text.Text;



class CorpusWordIC {
    
    private HashMap<String, Integer> counts;
    private HashMap<String, Double> infoContent;
    
    public CorpusWordIC(Text text, TextDatatype type) throws SQLException{
        counts = new HashMap<>();
        infoContent = new HashMap<>();
        
        ArrayList <Chunk> chunks = new ArrayList<>();
        switch(type){
            case PHENOTYPE: 
                chunks = text.getAllPhenotypeChunks();
                break;
            case PHENE: 
                chunks = text.getAllAtomChunks();
                break;
        }
        
        // Find counts of all the words in the corpus.
        for (Chunk c: chunks){
            for (Word w: c.getBagOfWords()){
                if (counts.containsKey(w.value)){
                    counts.put(w.value, counts.get(w.value)+1);
                }
                else {
                    counts.put(w.value, 1);
                }
            }
        }
        
        // Get the total number of words in the corpus.
        int sum = 0;
        for (Integer value : counts.values()){
            sum += value;
        }
        
        // Get the information content of all the unique words in the ontology.
        for (String word : counts.keySet()){
            double frequency = (double) counts.get(word) / (double) sum;
            double ic = (double) -1 * (double) Math.log(frequency);
            infoContent.put(word, ic);
        }
        
    }
    
 
    double getIC(String word){
        try{
            return infoContent.get(word);
        }
        catch(NullPointerException e){
            logger.info(String.format("problem finding corpus ic of %s",word));
            return 0.000;
        }
    }
    
    
    
    
    
}

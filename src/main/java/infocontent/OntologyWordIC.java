
package infocontent;

import enums.Aspect;
import structure.OntologyTerm;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import static main.Main.logger;



class OntologyWordIC {
 
    
    private final HashMap<String, Integer> counts;
    private final HashMap<String, Double> infoContent;
    
    OntologyWordIC(ArrayList ontologyTerms){     
        counts = new HashMap<>();
        infoContent = new HashMap<>();
        // Find counts of all the words in the ontology.
        for (OntologyTerm term : (ArrayList<OntologyTerm>) ontologyTerms){
            for (Aspect aspect : Aspect.values()){
                List<String> termWords = term.getAllWords(aspect);
                for (String word : termWords){
                    if (counts.containsKey(word)){
                        counts.put(word, counts.get(word)+1);
                    }
                    else {
                        counts.put(word, 1);
                    }
                }
            }
        }
        // Get the total number of words in the ontology.
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
        try {
            return infoContent.get(word);
        }
        catch (NullPointerException e){
            logger.info(String.format("problem finding ontology ic of %s",word));
            return 0.00;
        }
    }
    
}

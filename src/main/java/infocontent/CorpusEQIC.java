
package infocontent;

import enums.Ontology;
import java.sql.SQLException;
import java.util.HashMap;
import ontology.Onto;
import text.Text;



public class CorpusEQIC {
        
    private final HashMap<String, Integer> counts;
    private final HashMap<String, Double> infoContent;
    
    public CorpusEQIC(Text text, HashMap<Ontology,Onto> ontoObjects) throws SQLException, Exception{
        counts = new HashMap<>();
        infoContent = new HashMap<>();
        

        
        
        /*
        // Update the counts for all EQs in the corpus, based on a standardized representation of EQs defined in their class.
        for (EQStatement explicitEQ: text.getAllCuratedEQStatements()){
            
            HashSet<String> allEQsForThisExplicitEQ = new HashSet<>();
            allEQsForThisExplicitEQ.add(explicitEQ.getStandardizedRepresentation());
            for (EQStatement inheritedEQ: composer.Utils.getInheritedEQs(explicitEQ, ontoObjects)){
                allEQsForThisExplicitEQ.add(inheritedEQ.getStandardizedRepresentation());
            }
        
            // Add all of these EQs to the running counts of EQs.
            for (String eqStr: allEQsForThisExplicitEQ){
                if (counts.containsKey(eqStr)){
                    counts.put(eqStr, counts.get(eqStr)+1);
                }
                else {
                    counts.put(eqStr, 1);
                }
            }
        }

        // Get the total number of EQs in the corpus.
        int sum = 0;
        for (Integer value : counts.values()){
            sum += value;
        }

        // Get the information content of all the unique EQs in the corpus.
        for (String eqStr : counts.keySet()){
            double frequency = (double) counts.get(eqStr) / (double) sum;
            double ic = (double) -1 * (double) Math.log(frequency);
            infoContent.put(eqStr, ic);
        }
        */


    }
    
    double getIC(String eqStr) throws Exception{
        throw new Exception("information content of EQ statement not supported");
    }
}

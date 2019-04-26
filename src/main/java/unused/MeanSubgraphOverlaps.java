/*
 * Ian Braun
 * irbraun@iastate.edu
 * term-mapping 
 */
package unused;

import java.util.List;
import ontology.Onto;
import objects.OntologyTerm;

/**
 *
 * @author irbraun
 */
public class MeanSubgraphOverlaps {
    
    
    
    
    public static void getMeanOverlap() throws Exception{
        Onto onto = new Onto(utils.Utils.pickOntologyPath("po"));
        List<OntologyTerm> terms = onto.getTermList();
        double sumOfAverages = 0.00;
        for (OntologyTerm curatedTerm: terms){
            int n = terms.size();
            double sum = 0.00;
            for (OntologyTerm randomTerm: terms){
                sum += onto.getHierarchicalEvals(randomTerm, curatedTerm)[2];
            }
            double averageF1 = (double) sum / (double) n;
            sumOfAverages += averageF1;
        }
        double randomAvg = (double) sumOfAverages / (double) terms.size();
        System.out.println(randomAvg);    
    }
    
    
    
    
    
    
    
}

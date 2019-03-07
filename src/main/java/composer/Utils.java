
package composer;

import enums.Ontology;
import infocontent.InfoContent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import static main.Main.logger;
import ontology.Onto;





public class Utils {
  
    /**
     * Calculates the weighted Jaccard similarity between a pair of EQ statements. Note that these functions 
     * were written so that they can be passed EQ statements that have unsupported terms in them (terms coming 
     * from ontologies other than those we have objects for). This is because we can't figure out what those 
     * terms inherit, so they are meaningless towards the Jaccard similarity anyway and their impact would 
     * already be very underrepresented.
     * @param eq1
     * @param eq2
     * @param ontoObjects
     * @return 
     */
    public static double getSimilarity(EQStatement eq1, EQStatement eq2, HashMap<Ontology,Onto> ontoObjects){
        
        HashSet<String> termSetP1 = new HashSet<>();
        HashSet<String> termSetP2 = new HashSet<>();
        
        try {
            for (Term term: (ArrayList<Term>)eq1.getSupportedTermChain()){
                termSetP1.addAll(ontoObjects.get(term.ontology).getTermFromTermID(term.id).allNodes);   
            }
            for (Term term: (ArrayList<Term>)eq2.getSupportedTermChain()){
                termSetP2.addAll(ontoObjects.get(term.ontology).getTermFromTermID(term.id).allNodes);
            }
        }
        catch (Exception e){
            System.out.println("problem getting supported terms from the eq statement objects");
            return -1;
        }
        
        HashSet<String> intersection = new HashSet<>(termSetP1);
        intersection.retainAll(termSetP2);
        HashSet<String> union = new HashSet<>(termSetP1);
        union.addAll(termSetP2);
        double sumIntersection = 0.000;
        double sumUnion = 0.000;

        try {
            for (String termID: (HashSet<String>) intersection){
                sumIntersection += InfoContent.getICofTermFromCorpus(termID);
            }
            for (String termID: (HashSet<String>) union){
                sumUnion += InfoContent.getICofTermFromCorpus(termID);
            }
            double similarity = sumIntersection / sumUnion;
            return similarity;
        }
        catch (Exception e){
            logger.info("problem getting the corpus-based information content of a term");
            return -1;
        }
        
    }
     
    

    
    /**
     * See the above method. Overloaded for lists of EQ statements, allows for calculating Jaccard
     * similarity for entire phenotypes instead of at the atomized statement level.
     * @param eqList1
     * @param eqList2
     * @param ontoObjects
     * @return 
     */
    public static double getSimilarity(ArrayList<EQStatement> eqList1, ArrayList<EQStatement> eqList2, HashMap<Ontology,Onto> ontoObjects){
        
        HashSet termSetP1 = new HashSet<>();
        HashSet termSetP2 = new HashSet<>();

        for (EQStatement eq1: eqList1){
            for (Term term: (ArrayList<Term>)eq1.getSupportedTermChain()){
                try{
                    termSetP1.addAll(ontoObjects.get(utils.Util.inferOntology(term.id)).getTermFromTermID(term.id).allNodes);  
                }
                catch(NullPointerException e){
                    logger.info(String.format("problem retrieving ancestor nodes for %s", term.id));
                }
            }
        }
        for (EQStatement eq2: eqList2){
            for (Term term: (ArrayList<Term>)eq2.getSupportedTermChain()){
                try{
                    termSetP2.addAll(ontoObjects.get(utils.Util.inferOntology(term.id)).getTermFromTermID(term.id).allNodes);
                }
                catch(NullPointerException e){
                    logger.info(String.format("problem retrieving ancestor nodes for %s", term.id));
                }
            }
        }
        
        HashSet intersection = new HashSet<>(termSetP1);
        intersection.retainAll(termSetP2);
        HashSet union = new HashSet<>(termSetP1);
        union.addAll(termSetP2);
        double sumIntersection = 0.000;
        double sumUnion = 0.000;
        
        
        try {
            for (String termID: (HashSet<String>) intersection){
                sumIntersection += InfoContent.getICofTermFromCorpus(termID);
            }
            for (String termID: (HashSet<String>) union){
                sumUnion += InfoContent.getICofTermFromCorpus(termID);
            }
            double similarity = sumIntersection / sumUnion;
            return similarity;
        }
        catch (Exception e){
            logger.info("problem getting corpus-based information content of a term");
            return -1;
        }
    }
    
    
    // Makes sure that the term ID uses all 7 digits.
    public static String normalizeTermID(String id){
        int correctNumDigits = 7;
        
        if (id.contains("_")){
            String number = id.split("_")[1];
            String name = id.split("_")[0];
            int numDigits = number.length();
            for (int i=0; i<(correctNumDigits-numDigits); i++){
                number = String.format("%s%s","0",number);
            }
            String normalizedID = String.format("%s_%s", name, number);
            return normalizedID;
        }
        else if (id.contains(":")){
            String number = id.split(":")[1];
            String name = id.split(":")[0];
            int numDigits = number.length();
            for (int i=0; i<(correctNumDigits-numDigits); i++){
                number = String.format("%s%s","0",number);
            }
            String normalizedID = String.format("%s_%s", name, number);
            return normalizedID;
        }
        else {
            logger.info("problem with " + id);   // happening when the IDs aren't actually IDs at all, things that are in the wrong column.
            return id;
        }

    }
    
    
    
    
    
    
    
    static class EQComparatorP implements Comparator<EQStatement>
    {
        @Override
        public int compare(EQStatement eq1, EQStatement eq2){
            if (eq1.termScore < eq2.termScore){
                return 1;
            }
            else if(eq1.termScore > eq2.termScore){
                return -1;
            }
            return 0;
        }
    }
    
    static class EQComparatorQ implements Comparator<EQStatement>
    {
        @Override
        public int compare(EQStatement eq1, EQStatement eq2){
            if (eq1.termScore < eq2.termScore){
                return 1;
            }
            else if(eq1.termScore > eq2.termScore){
                return -1;
            }
            return 0;
        }
    }
    
    
    
    static class TermComparatorByProb implements Comparator<Term>
    {
        @Override
        public int compare(Term term1, Term term2){
            if (term1.probability < term2.probability){
                return 1;
            }
            else if (term1.probability > term2.probability){
                return -1;
            }
            return 0;
        }  
    }
    
    static class TermComparatorByLabelLength implements Comparator<Term>
    {
        @Override
        public int compare(Term term1, Term term2){
            if (InfoContent.getLabelLength(term1.id) < InfoContent.getLabelLength(term2.id)){
                return 1;
            }
            else if (InfoContent.getLabelLength(term1.id) > InfoContent.getLabelLength(term2.id)){
                return -1;
            }
            return 0;
        }  
    }
    
    
    
    static class TermComparatorByIC implements Comparator<Term>
    {
        @Override
        public int compare(Term term1, Term term2){
            if (InfoContent.getICofTermInOntology(utils.Util.inferOntology(term1.id), term1.id) < InfoContent.getICofTermInOntology(utils.Util.inferOntology(term2.id),term2.id)){
                return 1;
            }
            else if (InfoContent.getICofTermInOntology(utils.Util.inferOntology(term1.id), term1.id) > InfoContent.getICofTermInOntology(utils.Util.inferOntology(term2.id),term2.id)){
                return -1;
            }
            return 0;
        }  
    }
    
    
    static class EQComparatorByMetrics implements Comparator<EQStatement>
    {
        /**
         * Uses all three metrics corresponding to EQ statements to try and 
         * order them. Then the list of EQ statements can be appropriately
         * thresholded to reduce the number of low quality outputs.
         * @param eq1
         * @param eq2
         * @return 
         */
        @Override
        public int compare(EQStatement eq1, EQStatement eq2){
            if (eq1.termScore < eq2.termScore){
                return 1;
            }
            else if (eq1.termScore > eq2.termScore){
                return -1;
            }
            // The EQ's are equivalent in terms of average term score.
            else {
                if (eq1.coverage < eq2.coverage){
                    return 1;
                }
                else if (eq1.coverage > eq2.coverage){
                    return -1;
                }
                // The EQ's are also tied in terms of token coverage.
                else {
                    if (Double.valueOf(eq1.dGraphScore) < Double.valueOf(eq2.dGraphScore)){
                        return 1;
                    }
                    else if (Double.valueOf(eq1.dGraphScore) > Double.valueOf(eq2.dGraphScore)){
                        return -1;
                    }
                }
            }
            // The EQ's are equivalent in terms of everything.
            return 0;
        }  
    }
    
    
    
    static class EQComparatorByMetricsMethod2 implements Comparator<EQStatement>
    {
        /**
         * Uses all three metrics corresponding to EQ statements to try and 
         * order them. Then the list of EQ statements can be appropriately
         * thresholded to reduce the number of low quality outputs.
         * @param eq1
         * @param eq2
         * @return 
         */
        @Override
        public int compare(EQStatement eq1, EQStatement eq2){
            if (eq1.coverage < eq2.coverage){
                return 1;
            }
            else if (eq1.coverage > eq2.coverage){
                return -1;
            }
            // The EQ's are equivalent in terms of average term score.
            else {
                if (Double.valueOf(eq1.dGraphScore) < Double.valueOf(eq2.dGraphScore)){
                    return 1;
                }
                else if (Double.valueOf(eq1.dGraphScore) > Double.valueOf(eq2.dGraphScore)){
                    return -1;
                }
                // The EQ's are also tied in terms of token coverage.
                else {
                    if (eq1.termScore < eq2.termScore){
                        return 1;
                    }
                    else if (eq1.termScore > eq2.termScore){
                        return -1;
                    }
                }
            }
            // The EQ's are equivalent in terms of everything.
            return 0;
        }  
    }
    
    
        
    
    
    
    
    
    
    
    
    
}

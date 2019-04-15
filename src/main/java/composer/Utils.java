
package composer;

import enums.EQFormat;
import enums.Ontology;
import infocontent.InfoContent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import static main.Main.logger;
import ontology.Onto;
import org.apache.commons.lang3.ArrayUtils;
import structure.OntologyTerm;





public class Utils {
  
    
    
    
    
    
    public static double getEQSimilarity(EQStatement eq1, EQStatement eq2, HashMap<Ontology,Onto> ontoObjects) throws Exception{
        
        HashSet<String> eqSet1 = new HashSet<>();
        HashSet<String> eqSet2 = new HashSet<>();
        
        try{
            for (EQStatement eq: getInheritedEQs(eq1, ontoObjects)){
                eqSet1.add(eq.toIDText());
            }
            for (EQStatement eq: getInheritedEQs(eq2, ontoObjects)){
                eqSet2.add(eq.toIDText());
            }
        }
        catch(Exception e){
            logger.info("problem getting standardized representations of EQs");
            return -1;
        }
            
        
        HashSet<String> intersection = new HashSet<>(eqSet1);
        intersection.retainAll(eqSet2);
        HashSet<String> union = new HashSet<>(eqSet1);
        union.addAll(eqSet2);
        double sumIntersection = 0.000;
        double sumUnion = 0.000;
        
        try{
            for (String eqStr: (HashSet<String>) intersection){
                sumIntersection += InfoContent.getICofEQInCorpus(eqStr);
            }
            for (String eqStr: (HashSet<String>) union){
                sumUnion += InfoContent.getICofEQInCorpus(eqStr);
            }
            double similarity = sumIntersection / sumUnion;
            return similarity;
        }
        catch(Exception e){
            logger.info("problem getting the corpus-based information content of an EQ statement");
            return -1;
        }
    }
    
    
    
    
    
    public static double getEQSimilarity(ArrayList<EQStatement> eqList1, ArrayList<EQStatement> eqList2, HashMap<Ontology,Onto> ontoObjects){
        
        HashSet<String> eqSet1 = new HashSet<>();
        HashSet<String> eqSet2 = new HashSet<>();
        
        try{
            for (EQStatement eq1: eqList1){
                for (EQStatement eq: getInheritedEQs(eq1, ontoObjects)){
                    eqSet1.add(eq.toIDText());
                }
            }
            for (EQStatement eq2: eqList2){
                for (EQStatement eq: getInheritedEQs(eq2, ontoObjects)){
                    eqSet2.add(eq.toIDText());
                }
            }
        }
        catch(Exception e){
            logger.info("problem getting standardized representations of EQs");
            return -1;
        }
            
        
        HashSet<String> intersection = new HashSet<>(eqSet1);
        intersection.retainAll(eqSet2);
        HashSet<String> union = new HashSet<>(eqSet1);
        union.addAll(eqSet2);
        double sumIntersection = 0.000;
        double sumUnion = 0.000;
        
        try{
            for (String eqStr: (HashSet<String>) intersection){
                sumIntersection += InfoContent.getICofEQInCorpus(eqStr);
            }
            for (String eqStr: (HashSet<String>) union){
                sumUnion += InfoContent.getICofEQInCorpus(eqStr);
            }
            double similarity = sumIntersection / sumUnion;
            return similarity;
        }
        catch(Exception e){
            logger.info("problem getting the corpus-based information content of an EQ statement");
            return -1;
        } 
    }
    
    
    
    
    
    
    
    
    
    
    public static ArrayList<EQStatement> getInheritedEQs(EQStatement eq, HashMap<Ontology,Onto> ontoObjects) throws Exception{
        if (eq.format.equals(EQFormat.NOT_SPECIFIED)){
            return getInheritedCuratedEQs(eq, ontoObjects);
        }
        else {
            return getInheritedPredictedEQs(eq, ontoObjects);
        }
    }
    
    /**
     * Get all the EQ statements that are inherited by the EQ statement passed in. This is a
     * recursive method that's designed to work with the EQ statements that are predicted. The
     * difference that is accounted for is that these used the constructor that takes lists of
     * particular terms and an EQ statement architecture that describes which terms are used
     * for which roles in the statements.
     * @param eq
     * @param ontoObjects
     * @return
     * @throws Exception 
     */
    private static ArrayList<EQStatement> getInheritedPredictedEQs(EQStatement eq, HashMap<Ontology,Onto> ontoObjects) throws Exception{
        
        // Get the list of terms for this EQ and start a list of inherited ones to be returned.
        ArrayList<EQStatement> inheritedEQs = new ArrayList<>();
        EQFormat format = eq.format;
        ArrayList<Term> terms = eq.termChain;
        
        // Iterate through the terms in this EQ.
        for (Term t: terms){
            List<String> parentIDs = ontoObjects.get(utils.Util.inferOntology(t.id)).getTermFromTermID(t.id).parentNodes;
            int position = terms.indexOf(t);
            for (String parentTermID: parentIDs){
                OntologyTerm parentTerm = ontoObjects.get(utils.Util.inferOntology(parentTermID)).getTermFromTermID(parentTermID);
                terms.remove(position);
                Term parentTermCopy = new Term(parentTerm);
                terms.add(position, parentTermCopy);
                EQStatement inheritedEQ = new EQStatement(terms,format);
                inheritedEQs.add(inheritedEQ);
                inheritedEQs.addAll(getInheritedPredictedEQs(inheritedEQ, ontoObjects));
            }
        }
        return inheritedEQs;
    }
    
    
    
    /**
     * Get all the EQ statements that are inherited by the EQ statement passed in. This is a 
     * recursive method that's designed to work with EQ statements that come from the set of 
     * curated EQ statements. The difference that's accounted for is that the constructor which
     * just takes an array of strings is used, where the order is specified already and the 
     * terms don't have probabilities. Unused roles take empty strings, i.e. "". The 
     * @param eq
     * @param ontoObjects
     * @return
     * @throws Exception 
     */
    private static ArrayList<EQStatement> getInheritedCuratedEQs(EQStatement eq, HashMap<Ontology,Onto> ontoObjects) throws Exception{
        
        // Get the term ID strings for this EQ and start a list of inherited ones to be returned.
        ArrayList<EQStatement> inheritedEQs = new ArrayList<>();
        String[] components = eq.componentStrings;

        // Iterate through the term IDs in this EQ.
        for (String termID: components){
            // Check that A) this role in the EQ is used, and B) we haven't reached the root.
            if (!termID.equals("") &&  !termID.trim().equals("Thing")){
                // Get the immediate parent terms.
                List<String> parentIDs = new ArrayList<>();
                try{
                    parentIDs = ontoObjects.get(utils.Util.inferOntology(termID)).getTermFromTermID(termID).parentNodes;
                }
                catch(NullPointerException e){
                    System.out.println("could not get parent terms for " + termID);
                }
                int position = ArrayUtils.indexOf(components, termID);
                // Iterate through the parent terms, replacing the child term with them and recursively calling this method.
                for (String parentTermID: parentIDs){
                    if (!parentTermID.trim().equals("Thing")){
                        components[position] = parentTermID;
                        EQStatement inheritedEQ = new EQStatement(components);
                        inheritedEQs.add(inheritedEQ);
                        inheritedEQs.addAll(getInheritedCuratedEQs(inheritedEQ, ontoObjects));
                    }
                }
            }
        }
        return inheritedEQs;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
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
    public static double getTermSetSimilarity(EQStatement eq1, EQStatement eq2, HashMap<Ontology,Onto> ontoObjects){
        
        
        
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
    public static double getTermSetSimilarity(ArrayList<EQStatement> eqList1, ArrayList<EQStatement> eqList2, HashMap<Ontology,Onto> ontoObjects){
        
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
    
    
    /**
     * Converts an ontology ID to use a specific number of digits.
     * @param id
     * @return 
     */
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
            logger.info("problem with " + id);
            return id;
        }

    }
    
    
    
    
    
    
    
    static class EQComparatorAverageTermScore implements Comparator<EQStatement>
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
    
    static class EQComparatorTotalScore implements Comparator<EQStatement>
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
    
    
    
    static class TermComparatorByScore implements Comparator<Term>
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
    
    
    static class EQComparatorByAllMetrics implements Comparator<EQStatement>
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
    
    
    
    static class EQComparatorByAllMetricsAlternateOrder implements Comparator<EQStatement>
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

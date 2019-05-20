
package composer;

import enums.EQFormat;
import enums.Ontology;
import infocontent.InfoContent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import static main.Main.logger;
import ontology.Onto;
import org.apache.commons.lang3.ArrayUtils;
import objects.OntologyTerm;





public class Utils {
  
    
    
    
    
   
    
    
    /**
     * A method for calculating how many EQ statements are inherited by a given EQ statement.
     * This is calculated by identifying how many combinations of individual inherited terms
     * in the statement can be arranged to generate a EQ statement which is a semantically
     * less informative statement which is true given the provided statement.
     * @param eq
     * @param ontoObjects
     * @return 
     */
    public static int getNumInheritedEQs(EQStatement eq, HashMap<Ontology,Onto> ontoObjects){
        ArrayList<Integer> termDepths = new ArrayList<>();
        for (Term t: eq.termChain){
            termDepths.add(ontoObjects.get(t.ontology).getTermFromTermID(t.id).inheritedNodes.size()+1);
        }
        return utils.Utils.product(termDepths);
    }
    
    
    
    
    
    
    /**
     * A method of calculating similarity that uses commonly inherited EQ statements. 
     * Enforces the fact that the formats of the two EQ statements has to be the same,
     * i.e. that have to be using the same quantity of terms with the same roles such
     * as quality and optional qualifier. The inherited EQ statements are not weighted
     * based on their information content, that process was extremely computationally
     * expensive because the space of possible EQ statements grows exponentially with 
     * additional terms added to the statements (such as complex ones with multi-part
     * entities and qualities and terms with many ancestors such as those in GO and 
     * ChEBI).
     * @param eq1
     * @param eq2
     * @param ontoObjects
     * @return
     * @throws Exception 
     */
    public static double getEQSimilarityNoWeighting(EQStatement eq1, EQStatement eq2, HashMap<Ontology,Onto> ontoObjects) throws Exception{
        
        // Check if these EQ statements have the same components.
        if (!eq1.getFormat().equals(eq2.getFormat())){
            return 0.00;
        }
       
        
        // These two EQ statements have the same componenets. That should always mean they have the same number of terms.
        if (eq1.termChain.size() != eq2.termChain.size()){
            System.out.println("1: "+eq1.toIDText());
            System.out.println("2: "+eq2.toIDText());
            throw new Exception();
        }
        
        // How many EQ statements are inherited by both of these EQs? How many are inherited by only one of them?
        ArrayList<Integer> quantityOfCommonTermsAtEachComponent = new ArrayList<>();
        ArrayList<Integer> quantityOfTermsAtEachComponentInEQ1 = new ArrayList<>();
        ArrayList<Integer> quantityOfTermsAtEachComponentInEQ2 = new ArrayList<>();
        for (int i=0; i<eq1.termChain.size(); i++){
            HashSet<String> s1;
            HashSet<String> s2;
            // Obtaining the inherited set of nodes fails when the terms come from an ontology that's not supported.
            // Some occurences in the original dataset that cause this, using NCBITaxon.
            try{
                s1 = new HashSet<>(ontoObjects.get(eq1.termChain.get(i).ontology).getTermFromTermID(eq1.termChain.get(i).id).allNodes);
            }
            catch(NullPointerException e){
                logger.info(String.format("could not find the set of inherited nodes for %s", eq1.termChain.get(i).id));
                s1 = new HashSet<>();
            }
            try{
                s2 = new HashSet<>(ontoObjects.get(eq2.termChain.get(i).ontology).getTermFromTermID(eq2.termChain.get(i).id).allNodes);
            }
            catch(NullPointerException e){
                logger.info(String.format("could not find the set of inherited nodes for %s", eq2.termChain.get(i).id));
                s2 = new HashSet<>();
            }
            HashSet intersect = new HashSet<>(s1);
            intersect.retainAll(s2);
            quantityOfCommonTermsAtEachComponent.add(intersect.size());
            quantityOfTermsAtEachComponentInEQ1.add(s1.size());
            quantityOfTermsAtEachComponentInEQ2.add(s2.size());
        }
        int numIntersectionEQs = utils.Utils.product(quantityOfCommonTermsAtEachComponent);
        int numEQ1Inherited = utils.Utils.product(quantityOfTermsAtEachComponentInEQ1);
        int numEQ2Inherited = utils.Utils.product(quantityOfTermsAtEachComponentInEQ2);        
        int numEQ1Exclusive = numEQ1Inherited-numIntersectionEQs;
        int numEQ2Exclusive = numEQ2Inherited-numIntersectionEQs;
        int numInUnion = Math.max(1, numIntersectionEQs+numEQ1Exclusive+numEQ2Exclusive);
        return (double)numIntersectionEQs / (double)numInUnion;
    }
    
    public static double getEQSimilarityNoWeighting(ArrayList<EQStatement> eqList1, ArrayList<EQStatement> eqList2, HashMap<Ontology,Onto> ontoObjects) throws Exception{
        
        // Check to make sure that neither of the lists has zero EQs in it.               
        if (eqList1.isEmpty() || eqList2.isEmpty()){
            return -1;
        }
        
        // Placeholder method for comparison while figuring out how to reproduce the exact values.
        ArrayList<EQStatement> shorterEQsList;
        ArrayList<EQStatement> longerEQsList;
        if (eqList1.size() >= eqList2.size()){
            shorterEQsList = new ArrayList<>(eqList1);
            longerEQsList = new ArrayList<>(eqList2);
        }
        else{
            shorterEQsList = new ArrayList<>(eqList2);
            longerEQsList = new ArrayList<>(eqList1);
        }
        ArrayList<Double> maxSimilarities = new ArrayList<>();
        for (EQStatement eq1: shorterEQsList){
            double maxSim = 0.00;
            for (EQStatement eq2: longerEQsList){
                maxSim = Math.max(maxSim, getEQSimilarityNoWeighting(eq1, eq2, ontoObjects));            
            }
            maxSimilarities.add(maxSim);
        }
        return utils.Utils.mean(maxSimilarities);
        
        
        /*
        // Find the number of EQ statements which are uniquely inherited by those in list 1.
        int numEQsIn1ButNot2 = 0;
        for (EQStatement eq1: eqList1){
            // Loop through the terms for this EQ statement.
            ArrayList<Integer> quantityOfUniqueTermsAtEachComponent = new ArrayList<>();
            for (int i=0; i<eq1.termChain.size(); i++){
                HashSet<String> s1 = new HashSet<>(ontoObjects.get(eq1.termChain.get(i).ontology).getTermFromTermID(eq1.termChain.get(i).id).allNodes);
                for (EQStatement eq2: eqList2){
                    if (eq1.format.equals(eq2.format)){
                        HashSet<String> s2 = new HashSet<>(ontoObjects.get(eq2.termChain.get(i).ontology).getTermFromTermID(eq2.termChain.get(i).id).allNodes);
                        s1.removeAll(s2);
                    }
                }    
                quantityOfUniqueTermsAtEachComponent.add(s1.size());
            }
            numEQsIn1ButNot2 += utils.Util.product(quantityOfUniqueTermsAtEachComponent);
        }
        
        // Find the number of EQ statements which are inherited by those in both lists 1 and 2.
        
        // Find the number of EQ statements which are uniquely inherited by those in list 2.
        
        // Idea
        for each EQ in list 1:
            how many inherited EQs are there that aren't covered by anything in list2?
            alg,
            throw out every eq that doesn't match formatting of this eq
            for all the ones that do match.
                remove all terms from that are present in any of the comparison ones.
                your left with some amount of terms in each slot for this eq
                use that to generate the number of possible ones.

            how many inherited EQs are there that aren't covered by anything in list1?
            repeat this process.
        */
        
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    /**
     * Get the weighted Jaccard similarity of two EQ statements. These two EQ statements
     * must exist in order for this function to be called, it does not check. Also does
     * not indicate whether the EQ statements were actually found in the original dataset
     * and able to have a measured frequency. The frequency is just assumed to the baseline
     * frequency defined in the information content classes.
     * @param eq1
     * @param eq2
     * @param ontoObjects
     * @return
     * @throws Exception 
     */
    public static double getEQSimilarity(EQStatement eq1, EQStatement eq2, HashMap<Ontology,Onto> ontoObjects) throws Exception{

        // The sets of EQ statements which comprise representations of those inherited by the ones passed in.
        HashSet<String> eqSet1 = new HashSet<>();
        HashSet<String> eqSet2 = new HashSet<>();
        
        // Populate those sets of representations of inherited EQ statements.
        for (EQStatement eq: getInheritedEQs(eq1, ontoObjects)){
            eqSet1.add(eq.getStandardizedRepresentation());
        }
        for (EQStatement eq: getInheritedEQs(eq2, ontoObjects)){
            eqSet2.add(eq.getStandardizedRepresentation());
        }

        // Define which EQ statements are in the union and interestion of those passed in.
        HashSet<String> intersection = new HashSet<>(eqSet1);
        intersection.retainAll(eqSet2);
        HashSet<String> union = new HashSet<>(eqSet1);
        union.addAll(eqSet2);
        double sumIntersection = 0.000;
        double sumUnion = 0.000;
        
        // Calculate and return the weighted Jaccard similarity between these EQ statements.
        // Note that each statement is weighted as it's information content based on frequency in the original dataset.
        for (String eqStr: (HashSet<String>) intersection){
            sumIntersection += InfoContent.getICofEQInCorpus(eqStr);
        }
        for (String eqStr: (HashSet<String>) union){
            sumUnion += InfoContent.getICofEQInCorpus(eqStr);
        }
        double similarity = sumIntersection / sumUnion;
        return similarity;
    }
    
    
    
    /**
     * Get the weighted Jaccard similarity of two sets of EQ statements. Checks to make
     * sure that the lists of EQ statements are not empty, if one of them is then this 
     * method returns -1, which indicates that the similarity between these two EQ statements
     * could not be calculated. This is important for this method because it is receiving 
     * EQ statements are the result of predictions, where there can be any number of predicted
     * EQ statements for some input ranging from 0 and upwards. Does not indicate whether the 
     * EQ statements were actually found in the original dataset and able to have a measured 
     * frequency. The frequency is just assumed to the baseline frequency defined in the 
     * information content classes.
     * @param eqList1
     * @param eqList2
     * @param ontoObjects
     * @return 
     * @throws java.lang.Exception 
     */
    public static double getEQSimilarity(ArrayList<EQStatement> eqList1, ArrayList<EQStatement> eqList2, HashMap<Ontology,Onto> ontoObjects) throws Exception{
        
        // Check to make sure that neither of the lists has zero EQs in it.
        if (eqList1.isEmpty() || eqList2.isEmpty()){
            return -1;
        }
        
        // The sets of EQ statements which comprise representations of those inherited by the ones passed in.
        HashSet<String> eqSet1 = new HashSet<>();
        HashSet<String> eqSet2 = new HashSet<>();
        
        // Populate those sets of representations of inherited EQ statements.
        for (EQStatement eq1: eqList1){
            for (EQStatement eq: getInheritedEQs(eq1, ontoObjects)){
                eqSet1.add(eq.getStandardizedRepresentation());
            }
        }
        for (EQStatement eq2: eqList2){
            for (EQStatement eq: getInheritedEQs(eq2, ontoObjects)){
                eqSet2.add(eq.getStandardizedRepresentation());
            }
        }

        // Define which EQ statements are in the union and interestion of those passed in.
        HashSet<String> intersection = new HashSet<>(eqSet1);
        intersection.retainAll(eqSet2);
        HashSet<String> union = new HashSet<>(eqSet1);
        union.addAll(eqSet2);
        double sumIntersection = 0.000;
        double sumUnion = 0.000;
        
        
        // Calculate and return the weighted Jaccard similarity between these EQ statements.
        // Note that each statement is weighted as it's information content based on frequency in the original dataset.
        for (String eqStr: (HashSet<String>) intersection){
            sumIntersection += InfoContent.getICofEQInCorpus(eqStr);
        }
        for (String eqStr: (HashSet<String>) union){
            sumUnion += InfoContent.getICofEQInCorpus(eqStr);
        }
        double similarity = sumIntersection / sumUnion;
        return similarity;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    /**
     * Returns a list of standard string representations of EQ statements which are inherited
     * by that passed in. Differentiates between EQ statements that come from the curated
     * dataset and those that have been predicted by other methods and uses the appropriate
     * function.
     * @param eq
     * @param ontoObjects
     * @return
     * @throws Exception 
     */
    public static ArrayList<EQStatement> getInheritedEQs(EQStatement eq, HashMap<Ontology,Onto> ontoObjects) throws Exception{
        if (eq.isFromCuratedDataSet()){
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
        EQFormat format = eq.getFormat();
        ArrayList<Term> terms = eq.termChain;
        
        // Iterate through the terms in this EQ.
        for (Term t: terms){
            List<String> parentIDs = new ArrayList<>();
            try{
                parentIDs = ontoObjects.get(utils.Utils.inferOntology(t.id)).getTermFromTermID(t.id).parentNodes;
            }
            catch(Exception e){
                logger.info(String.format("problem obtaining parents of %s", t.id));
            }
            int position = terms.indexOf(t);
            for (String parentTermID: parentIDs){
                OntologyTerm parentTerm = ontoObjects.get(utils.Utils.inferOntology(parentTermID)).getTermFromTermID(parentTermID);
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
        String[] components = Arrays.copyOf(eq.getComponentStrings(), eq.getComponentStrings().length);

        // Iterate through the term IDs in this EQ.
        for (String termID: components){
            // Check that A) this role in the EQ is used, and B) we haven't reached the root.
            if (!termID.equals("") &&  !termID.trim().equals("Thing")){
                // Get the immediate parent terms.
                List<String> parentIDs = new ArrayList<>();
                try{
                    parentIDs = ontoObjects.get(utils.Utils.inferOntology(termID)).getTermFromTermID(termID).parentNodes;
                }
                catch(NullPointerException e){
                    logger.info(String.format("problem obtaining parents of %s", termID));
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
            logger.info("problem getting supported terms from the eq statement objects");
            return 0.000;
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
            if (sumUnion!=0.000){
                double similarity = sumIntersection / sumUnion;
                return similarity;
            }
            else {
                return 0.000;
            }
        }
        catch (Exception e){
            logger.info("problem getting the corpus-based information content of a term");
            return 0.000;
        }
        
    }
     
    

    
    /**
     * Calculates the weighted Jaccard similarity between a pair of EQ statements. Note that these functions 
     * were written so that they can be passed EQ statements that have unsupported terms in them (terms coming 
     * from ontologies other than those we have objects for). This is because we can't figure out what those 
     * terms inherit, so they are meaningless towards the Jaccard similarity anyway and their impact would 
     * already be very underrepresented. Overloaded for a lists of EQ statements instead of single one.
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
                    termSetP1.addAll(ontoObjects.get(utils.Utils.inferOntology(term.id)).getTermFromTermID(term.id).allNodes);  
                }
                catch(NullPointerException e){
                    logger.info(String.format("problem retrieving ancestor nodes for %s", term.id));
                }
            }
        }
        for (EQStatement eq2: eqList2){
            for (Term term: (ArrayList<Term>)eq2.getSupportedTermChain()){
                try{
                    termSetP2.addAll(ontoObjects.get(utils.Utils.inferOntology(term.id)).getTermFromTermID(term.id).allNodes);
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
            return 0.000;
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
    
    
    
    
    
    
}

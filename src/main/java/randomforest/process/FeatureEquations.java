
package randomforest.process;

import config.Config;
import enums.Ontology;
import infocontent.InfoContent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


class FeatureEquations {
    
    
    /**
     * Finds the weighted sum of all the best word-to-word matches between some bag
     * of words in a text component and some bag of words from an ontology term
     * component. For this method, it doesn't matter whether the text side of the term
     * side bag of words was selected to dictate the total number of word-to-word
     * matches between the bags, that's a design choice that is separate from this 
     * function. This method does specifically weight by the ontology word level IC 
     * however.
     * @param matchTermWords
     * @param matchScores
     * @return 
     */
    public static double eqn1(ArrayList<String> matchTermWords, ArrayList<Double> matchScores){
        Ontology ontology = utils.Utils.inferOntology(Config.ontologyName);
        double[] weights = new double[matchScores.size()];
        int numWordsInMatch = matchScores.size();
        // Find initial weights.
        double wSum = 0.000;
        for (int i=0; i<numWordsInMatch; i++){
            double w = InfoContent.getICofWordInOntology(ontology, matchTermWords.get(i));
            weights[i] = w;
            wSum += w;
        }
        // Normalize weights.
        for (int i=0; i<numWordsInMatch; i++){
            weights[i] = weights[i] / wSum;
        }
        // Use weights to determine match value.
        double value = 0.000;
        for (int i=0; i<numWordsInMatch; i++){
            value += (double) matchScores.get(i) * (double) weights[i];
        }
        return value;
    }
    
    
    
    /**
     * Same as the above, except that weighting of the scores of each word pairing is 
     * done differently. In this case, each value is weighted by the information
     * content of the text-side word in the corpus, instead of the term-side word in
     * the ontology.
     * @param matchTextWords
     * @param matchScores
     * @return 
     */
    public static double eqn2(ArrayList<String> matchTextWords, ArrayList<Double> matchScores){
        double[] weights = new double[matchScores.size()];
        int numWordsInMatch = matchScores.size();
        // Find initial weights.
        double wSum = 0.000;
        for (int i=0; i<numWordsInMatch; i++){
            double w = InfoContent.getICofWordInCorpus(matchTextWords.get(i));
            weights[i] = w;
            wSum += w;
        }
        // Normalize weights.
        for (int i=0; i<numWordsInMatch; i++){
            weights[i] = weights[i] / wSum;
        }
        // Use weights to determine match value.
        double value = 0.000;
        for (int i=0; i<numWordsInMatch; i++){
            value += (double) matchScores.get(i) * (double) weights[i];
        }
        return value;
    }
    
    
    
    
    
    public static double eqn12noWt(ArrayList<Double> matchScores){
        int numWordsInMatch = matchScores.size();
        double value = 0.000;
        for (double score: matchScores){
            value += score;
        }
        value = value / (double) numWordsInMatch;
        return value;
    }
    
    
  

    
    
    /**
     * Just finds the maximum value of this feature for any parent term in order to
     * use this value as a new context-based feature. This is not currently used for
     * any of the context-based metrics.
     * @param pMatchScores
     * @return 
     */
    public static double eqn4(List<Double> pMatchScores){
        return Collections.max(pMatchScores);
    }
    
    
    
    

    
    /**
     * The two lists are the same length and the position indices have to be consistent.
     * This is the method that finds a sum of the parent feature values as weighted by 
     * the structure-based information content of each term.
     * @param parentValues
     * @param parentTermIDs
     * @return 
     */
    public static double eqn5(List<String> parentTermIDs, List<Double> parentValues){
        Ontology ontology = utils.Utils.inferOntology(Config.ontologyName);
        int numParents = parentValues.size();
        // find weights
        List<Double> weights = new ArrayList<>();
        double weightSum = 0.00;
        for (String termID: parentTermIDs){
            double w = InfoContent.getICofTermInOntology(ontology, termID);
            weights.add(w);
            weightSum += w;
        }
        // normalize the weights
        for (double w: weights){
            w = w/weightSum;
        }
        // calculate this new feature
        double value = 0.00;
        for (int i=0; i<parentValues.size(); i++){
            value += (double)parentValues.get(i) * (double)weights.get(i);
        }
        return value;
    }
    
    
    
    
    /**
     * Another context-based feature equation. This one is for finding the average value of a
     * particular non-context feature (or combination of the value of different features) 
     * across all the sibling terms of the ontology term in question for this feature.
     * @param siblingValues
     * @return 
     */
    public static double eqn6(List<String> siblingTermIDs, List<Double> siblingValues){
        double sum = 0.00;
        for (double siblingValue: siblingValues){
            sum += siblingValue;
        }
        double value = sum / (double) siblingValues.size();
        return value;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
 
}

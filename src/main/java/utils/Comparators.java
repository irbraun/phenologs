
package utils;

import composer.EQStatement;
import composer.Term;
import infocontent.InfoContent;
import java.util.Comparator;





public class Comparators {
        
    

    
    
    
    
    
    public static final class EQComparatorAverageTermScore implements Comparator<EQStatement>{
        @Override
        public int compare(EQStatement eq1, EQStatement eq2){
            if (eq1.getAverageTermScore() < eq2.getAverageTermScore()){
                return 1;
            }
            else if(eq1.getAverageTermScore() > eq2.getAverageTermScore()){
                return -1;
            }
            return 0;
        }
    }
    
    
 
    public static final class EQComparatorTotalScore implements Comparator<EQStatement>{
        @Override
        public int compare(EQStatement eq1, EQStatement eq2){
            if (eq1.getAverageTermScore() < eq2.getAverageTermScore()){
                return 1;
            }
            else if(eq1.getAverageTermScore() > eq2.getAverageTermScore()){
                return -1;
            }
            return 0;
        }
    }
    
    
    
    public static final class TermComparatorByScore implements Comparator<Term>{
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
    
    
    
    public static final class TermComparatorByLabelLength implements Comparator<Term>{
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
    
    
    
    public static final class TermComparatorByIC implements Comparator<Term>{
        @Override
        public int compare(Term term1, Term term2){
            if (InfoContent.getICofTermInOntology(utils.Utils.inferOntology(term1.id), term1.id) < InfoContent.getICofTermInOntology(utils.Utils.inferOntology(term2.id),term2.id)){
                return 1;
            }
            else if (InfoContent.getICofTermInOntology(utils.Utils.inferOntology(term1.id), term1.id) > InfoContent.getICofTermInOntology(utils.Utils.inferOntology(term2.id),term2.id)){
                return -1;
            }
            return 0;
        }  
    }
    
    
    
    
    
    public static final class EQComparatorAvgScoreAndCoverage implements Comparator<EQStatement>{
        /**
         * Description
         * @param eq1
         * @param eq2
         * @return 
         */
        @Override
        public int compare(EQStatement eq1, EQStatement eq2){
            if (eq1.getNodeOverlap() < eq2.getNodeOverlap()){
                return 1;
            }
            else if (eq1.getNodeOverlap() > eq2.getNodeOverlap()){
                return -1;
            }
            else {
                if (eq1.getAverageTermScore() < eq2.getAverageTermScore()){
                    return 1;
                }
                else if (eq1.getAverageTermScore() > eq2.getAverageTermScore()){
                    return -1;
                }
            }
            return 0;
        }  
    }
    
    
    public static final class EQComparatorAvgScoreAndCoverageAndDG implements Comparator<EQStatement>{
        /**
         * Description
         * @param eq1
         * @param eq2
         * @return 
         */
        @Override
        public int compare(EQStatement eq1, EQStatement eq2){
            // Test in terms of average term score.
            if (eq1.getAverageTermScore() < eq2.getAverageTermScore()){
                return 1;
            }
            else if (eq1.getAverageTermScore() > eq2.getAverageTermScore()){
                return -1;
            }
            // Break ties.
            else {
                // Test in terms of likelihood of dependency graphs.
                if (Double.valueOf(eq1.getDependencyGraphValues()[3]) < Double.valueOf(eq2.getDependencyGraphValues()[3])){
                    return 1;
                }
                else if (Double.valueOf(eq1.getDependencyGraphValues()[3]) > Double.valueOf(eq2.getDependencyGraphValues()[3])){
                    return -1;
                }
                // Break ties.
                else {
                    // Test in terms of node overlap.
                    if (eq1.getNodeOverlap() < eq2.getNodeOverlap()){
                        return 1;
                    }
                    else if (eq1.getNodeOverlap() > eq2.getNodeOverlap()){
                        return -1;
                    }
                }
            }
            return 0;
        }  
    }
    
    
    
    
    

    
    
    
    
}


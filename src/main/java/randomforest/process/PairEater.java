package randomforest.process;

import config.Config;
import structure.Feature;
import structure.OntologyTerm;
import structure.Word;
import structure.Chunk;
import structure.FeatureVector;
import enums.Aspect;
import enums.Context;
import enums.Metric;
import structure.FeatureArray;
import java.util.ArrayList;
import java.util.List;





public class PairEater {
    
    
    /*
    Important note: this class is responsible for making sure that meaningless features are assigned the numerical
    value of -1. Anything returned from the feature equations class was assumed to have meaning, and even if lists 
    were empty or something like that a value of -1 is not automatically assigned there, it should be checked for here.
    Don't call those equations unless data is available to make the feature meaningful or at least expected.
    */
    
    
    
    
    
    
    // Dummy function to illustrate how you could add arbitrary features.
    public static FeatureVector getDummyVector(Chunk chunk, OntologyTerm term){
        FeatureVector vector = new FeatureVector(chunk.chunkID, term.termID);
        Feature feature = new Feature("dummy",8.88);
        vector.add(feature);
        return vector;
    }
    
    
    
    
    // For features that are not based on context.
    public static FeatureVector getVector(SimilarityFinder finder, Metric metric, Chunk chunk, OntologyTerm term) throws Exception{
        FeatureVector vector = new FeatureVector(chunk.chunkID, term.termID);
        for (Aspect aspect: Config.normalAspects){
            
            // Get the feature values for whatever kind of metric and weighting is specified.
            double score = getScore(finder, metric, aspect, term, chunk);
            vector.add(new Feature(metric, aspect, Config.numEdges, Config.weighting, Context.NONE, score));
        }
        return vector;
    }
    
    
    
    
    
    
    
    
    // Deprecated.
    /**
     * All the scores for these features are given as similarities.
     * Range varies but worst value is always 0.
     * @param finder
     * @param metric
     * @param chunk
     * @param term
     * @return
     * @throws IOException 
     */
    /*
    public static FeatureVector getVector(SimilarityFinder finder, MetricSemantic metric, Chunk chunk, OntologyTerm term) throws IOException{        
        FeatureVector vector = new FeatureVector(chunk.chunkID, term.termID);
        for (TermAspect aspect: UseFeatures.termAspects){
            for (Side edgeSide: UseFeatures.edgeSides){
                    
                // Get the feature values for whatever kind of weighting is specified.
                double[] scores = getSemanticScore(finder, metric, aspect, term, chunk, edgeSide);
                if (Arrays.asList(UseFeatures.weightSides).contains(Side.TERM)){
                    vector.add(new Feature(Metric.valueOf(metric.toString()), aspect, edgeSide, Side.TERM, Context.NO, scores[0]));
                }
                if (Arrays.asList(UseFeatures.weightSides).contains(Side.TEXT)){
                    vector.add(new Feature(Metric.valueOf(metric.toString()), aspect, edgeSide, Side.TEXT, Context.NO, scores[1]));
                }
            }
        }
        return vector;
    }
    */
    
    
    // Deprecated.
    /**
     * Overload
     * @param finder
     * @param metric
     * @param chunk
     * @param term
     * @return
     * @throws IOException 
     */
    /*
    public static FeatureVector getVector(SimilarityFinder finder, MetricSyntactic metric, Chunk chunk, OntologyTerm term) throws IOException{
        FeatureVector vector = new FeatureVector(chunk.chunkID, term.termID);
        for (TermAspect aspect: UseFeatures.termAspects){
            for (Side edgeSide: UseFeatures.edgeSides){

                // Get the feature values for whatever kind of weighting is specified.
                double[] scores = getSyntacticScore(finder, metric, aspect, term, chunk, edgeSide);
                if (Arrays.asList(UseFeatures.weightSides).contains(Side.TERM)){
                    vector.add(new Feature(Metric.valueOf(metric.toString()), aspect, edgeSide, Side.TERM, Context.NO, scores[0]));
                }
                if (Arrays.asList(UseFeatures.weightSides).contains(Side.TEXT)){
                    vector.add(new Feature(Metric.valueOf(metric.toString()), aspect, edgeSide, Side.TEXT, Context.NO, scores[1]));
                }
            }
        }
        return vector;
    }
    */
    
        
    
    
    
    
    
    
    
    /**
     * Handles all cases of finding a feature value using semantic or syntactic methods.
     * 
     * (Semantic methods)
     * Score is returned as -1 only when the ontology has no words associated with this particular aspect.
     * When checking individual best edges between words, if the maximum edge score is still -1 (method
     * doesn't support calculating a score for this match), 0 is added as the edges value instead. This
     * is dependent on the fact that these are all similarity methods for which the worst value (least similar)
     * is 0. This is currently true, but would need to change if new metrics are added.
     * 
     * (Syntactic methods)
     * Distinguishes between distance and similarity methods.
     * Does not check for replacing -1 with a value which makes sense, because none of the current
     * methods are incapable of assigning an actual value in the meaningful domain to any pair of 
     * characters. This is because they are purely string-based methods. This would have to change if 
     * this becomes not true later.
     * For the similarities, the best value is 1 and for distances the best value is 0.
     * 
     * @param finder
     * @param metric
     * @param aspect
     * @param term
     * @param chunk
     * @return
     * @throws Exception 
     */
    private static double getScore(SimilarityFinder finder, Metric metric, Aspect aspect, OntologyTerm term, Chunk chunk) throws Exception{
        
        double score = -1;
        
        // Check to make sure there are words in this aspect of the term.
        List<String> termWords = term.getAllWords(aspect);
        if (!termWords.isEmpty()){
            
            
            switch(Config.numEdges){
                
                
                case TEXT:{
                    // Information currently used for finding the weighted similarity value.
                    ArrayList<String> matchTermWords = new ArrayList<>();
                    ArrayList<Double> matchScores = new ArrayList<>();

                    // Check whether we want to search for the largest or the smallest value (is the metric for similarity or distance).
                    if (!Metric.isDistanceMetric(metric)){    
                        // Similarity case.
                        for (Word word : chunk.getBagOfWords()){
                            // Initialize to actual values.
                            String maxTermWord = termWords.get(0);
                            double maxScore = finder.getSimilarity(metric, word.value, termWords.get(0));
                            for (String termWord : termWords){
                                double nextScore = finder.getSimilarity(metric, word.value, termWord);
                                if (nextScore >= maxScore){
                                    maxScore = nextScore;
                                    maxTermWord = termWord;
                                }
                            }
                            matchTermWords.add(maxTermWord);
                            
                            // Check if this is a semantic similarity metric, if so only allow a minimum score of 0.00.
                            if (Metric.isSemanticMetric(metric)){
                                matchScores.add(Math.max(maxScore, 0.00));
                            }
                            else {
                                matchScores.add(maxScore);
                            }
                            
                        }  
                    }
                    else {
                        // Distance case.
                        for (Word word : chunk.getBagOfWords()){
                            // Initialize to actual values.
                            String minTermWord = termWords.get(0);
                            double minScore = finder.getSimilarity(metric, word.value, termWords.get(0));
                            for (String termWord : termWords){
                                double nextScore = finder.getSimilarity(metric, word.value, termWord);
                                if (nextScore <= minScore){
                                    minScore = nextScore;
                                    minTermWord = termWord;
                                }
                            }
                            matchTermWords.add(minTermWord);
                            matchScores.add(minScore);
                        }    
                    }
                    // Check how the weighting should be done when calculating the feature value.
                    switch (Config.weighting) {
                    case TERM:
                        score = FeatureEquations.eqn1(matchTermWords, matchScores);
                        break;
                    case TEXT:
                        score = FeatureEquations.eqn2(chunk.getBagValues(), matchScores);
                        break;
                    case NONE:
                        score = FeatureEquations.eqn12noWt(matchScores);
                        break;
                    default:
                        throw new Exception();
                    }
                }
                break;
            
            
            
                case TERM:{
                    // Information currently used for finding the weighted similarity value.
                    ArrayList<String> matchTextWords = new ArrayList<>();
                    ArrayList<Double> matchScores = new ArrayList<>();

                    // Check whether we want to search for the largest or the smallest value (is the metric for similarity or distance).
                    if (!Metric.isDistanceMetric(metric)){
                        // Similarity case.
                        for (String termWord : term.getAllWords(aspect)){
                            // Initiate to actual values.
                            String maxTextWord = chunk.getBagOfWords().get(0).value;
                            double maxScore = finder.getSimilarity(metric, termWord, chunk.getBagOfWords().get(0).value);
                            for (Word textWord: chunk.getBagOfWords()){
                                double nextScore = finder.getSimilarity(metric, termWord, textWord.value);
                                if (nextScore >= maxScore){
                                    maxScore = nextScore;
                                    maxTextWord = textWord.value;
                                }
                            }
                            matchTextWords.add(maxTextWord);
                            
                            // Check if this is a semantic similarity metric, if so only allow a minimum score of 0.00.
                            if (Metric.isSemanticMetric(metric)){
                                matchScores.add(Math.max(maxScore, 0.00));
                            }
                            else {
                                matchScores.add(maxScore);
                            }
                            
                        }
                    }
                    else {
                        // Distance case.
                        for (String termWord : term.getAllWords(aspect)){
                            // Initiate to actual values.
                            String minTextWord = chunk.getBagOfWords().get(0).value;
                            double minScore = finder.getSimilarity(metric, termWord, chunk.getBagOfWords().get(0).value);
                            for (Word textWord: chunk.getBagOfWords()){
                                double nextScore = finder.getSimilarity(metric, termWord, textWord.value);
                                if (nextScore <= minScore){
                                    minScore = nextScore;
                                    minTextWord = textWord.value;
                                }
                            }
                            matchTextWords.add(minTextWord);
                            matchScores.add(minScore);
                        }

                    }

                    // Check how the weighting should be done when calculating the feature value.
                    switch(Config.weighting){
                    case TERM:
                        score = FeatureEquations.eqn1(new ArrayList<String>(term.getAllWords(aspect)), matchScores);
                        break;
                    case TEXT:
                        score = FeatureEquations.eqn2(matchTextWords, matchScores);
                        break;
                    case NONE:
                        score = FeatureEquations.eqn12noWt(matchScores);
                        break;
                    default:
                        throw new Exception();
                    }
                } 
                break;
            
            }
        }
        
        return score;
        
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
   
    // Deprecated.
    
    /**
     * Handles all cases of finding a similarity features value using a semantic method.
     * Score is returned as -1 only when the ontology has no words associated with this particular aspect.
     * When checking individual best edges between words, if the maximum edge score is still -1 (method
     * doesn't support calculating a score for this match), 0 is added as the edges value instead. This
     * is dependent on the fact that these are all similarity methods for which the worst value (least similar)
     * is 0. This is currently true, but would need to change if new metrics are added.
     * @param finder
     * @param metric
     * @param aspect
     * @param term
     * @param chunk
     * @param edges
     * @param weights
     * @return
     * @throws IOException 
     */
    /*
    private static double[] getSemanticScore(SimilarityFinder finder, MetricSemantic metric, Aspect aspect, OntologyTerm term, Chunk chunk, Side edges) throws IOException{
        
        double scoreTermWeight = -1;
        double scoreTextWeight = -1;
        // Check to make sure their are words in this aspect of the term.
        List<String> termWords = term.getAllWords(aspect);
        if (termWords.size() > 0){
            
            switch(edges){
                case TEXT:{    
                    // Information currently used for finding the weighted similarity value.
                    ArrayList<String> matchTermWords = new ArrayList<>();
                    ArrayList<Double> matchScores = new ArrayList<>();
                    
                    int idx = 0;
                    for (Word word : chunk.getBag()){

                        // Initiate to actual values.
                        String maxTermWord = termWords.get(0);
                        double maxScore = finder.getSimilarity(metric, word.value, termWords.get(0));

                        for (String termWord : termWords){
                            double nextScore = finder.getSimilarity(metric, word.value, termWord);
                            if (nextScore >= maxScore){
                                maxScore = nextScore;
                                maxTermWord = termWord;
                            }
                        }
                        matchTermWords.add(maxTermWord);
                        matchScores.add(Math.max(maxScore, 0.00));
                        idx++;
                    }    

                    // Check how we want the weighting to be done. Actually just return both.
                    if (Arrays.asList(UseFeatures.weightSides).contains(Side.TERM)){
                        scoreTermWeight = FeatureEquations.eqn1(matchTermWords, matchScores);
                    }
                    if (Arrays.asList(UseFeatures.weightSides).contains(Side.TEXT)){
                        scoreTextWeight = FeatureEquations.eqn2(chunk.getBagValues(), matchScores);
                    }
                }
                break;

                case TERM:{
                    // Information currently used for finding the weighted similarity value.
                    ArrayList<String> matchTextWords = new ArrayList<>();
                    ArrayList<Double> matchScores = new ArrayList<>();
                   
                    int idx = 0;
                    for (String termWord : term.getAllWords(aspect)){

                        // Initiate to actual values.
                        String maxTextWord = chunk.getBag().get(0).value;
                        double maxScore = finder.getSimilarity(metric, termWord, chunk.getBag().get(0).value);

                        for (Word textWord: chunk.getBag()){
                            double nextScore = finder.getSimilarity(metric, termWord, textWord.value);
                            if (nextScore >= maxScore){
                                maxScore = nextScore;
                                maxTextWord = textWord.value;
                            }
                        }
                        matchTextWords.add(maxTextWord);
                        matchScores.add(Math.max(maxScore, 0.00));
                        idx++;
                    }

                    // Check how we want the weighting to be done.
                    if (Arrays.asList(UseFeatures.weightSides).contains(Side.TERM)){
                        scoreTermWeight = FeatureEquations.eqn1(new ArrayList<String>(term.getAllWords(aspect)), matchScores);
                    }
                    if (Arrays.asList(UseFeatures.weightSides).contains(Side.TEXT)){
                        scoreTextWeight = FeatureEquations.eqn2(matchTextWords, matchScores);
                    }
                }
                break;
            }
        }
        double[] scores = {scoreTermWeight, scoreTextWeight};
        return scores;
       
    }
    */
    
    
    
    
    // Deprecated.
    /**
     * Handles all cases of finding a similarity features value using a syntactic method.
     * Distinguishes between distance and similarity methods.
     * Does not check for replacing -1 with a value which makes sense, because none of the current
     * methods are incapable of assigning an actual value in the meaningful domain to any pair of 
     * characters. This is because they are purely string-based methods. This would have to change if 
     * this becomes not true later.
     * For the similarities, the best value is 1 and for distances the best value is 0.
     * @param finder
     * @param metric
     * @param aspect
     * @param term
     * @param chunk
     * @param edges
     * @param weights
     * @return
     * @throws IOException 
     */
    /*
    private static double[] getSyntacticScore(SimilarityFinder finder, MetricSyntactic metric, Aspect aspect, OntologyTerm term, Chunk chunk, Side edges) throws IOException{
        
        
       
        double scoreTermWeight = -1;
        double scoreTextWeight = -1;
        // Check to make sure their are words in this aspect of the term.
        List<String> termWords = term.getAllWords(aspect);
        if (termWords.size() > 0){
            
            
            switch(edges){
                case TEXT:{
                    
                    // Information currently used for finding the weighted similarity value.
                    ArrayList<String> matchTermWords = new ArrayList<>();
                    ArrayList<Double> matchScores = new ArrayList<>();
                    int idx = 0;
                    
                    
                    
                    // Check whether we want to search for the largest or the smallest value.
                    if (MetricSyntactic.getSimilarityMetrics().contains(metric)){
                        // Similarity case.
                        for (Word word : chunk.getBag()){

                            // Initiate to actual values.
                            String maxTermWord = termWords.get(0);
                            double maxScore = finder.getSimilarity(metric, word.value, termWords.get(0));

                            for (String termWord : termWords){
                                double nextScore = finder.getSimilarity(metric, word.value, termWord);
                                if (nextScore >= maxScore){
                                    maxScore = nextScore;
                                    maxTermWord = termWord;
                                }
                            }
                            matchTermWords.add(maxTermWord);
                            matchScores.add(maxScore);
                            idx++;
                        }  
                    }
                    else {
                        // Distance case.
                        for (Word word : chunk.getBag()){

                            // Initiate to actual values.
                            String minTermWord = termWords.get(0);
                            double minScore = finder.getSimilarity(metric, word.value, termWords.get(0));

                            for (String termWord : termWords){

                                double nextScore = finder.getSimilarity(metric, word.value, termWord);
                                if (nextScore <= minScore){
                                    minScore = nextScore;
                                    minTermWord = termWord;
                                }
                            }
                            matchTermWords.add(minTermWord);
                            matchScores.add(minScore);
                            idx++;
                        }    
                    }
                    
                    // Check how we want the weighting to be done.
                    if (Arrays.asList(UseFeatures.weightSides).contains(Side.TERM)){
                        scoreTermWeight = FeatureEquations.eqn1(matchTermWords, matchScores);
                    }
                    if (Arrays.asList(UseFeatures.weightSides).contains(Side.TEXT)){
                        scoreTextWeight = FeatureEquations.eqn2(chunk.getBagValues(), matchScores);
                    }
                }
                break;
                
                
                case TERM:{
                    
                    // Information currently used for finding the weighted similarity value.
                    ArrayList<String> matchTextWords = new ArrayList<>();
                    ArrayList<Double> matchScores = new ArrayList<>();
                    int idx = 0;
                    
                    
                    // Check whether we want to search for the largest or the smallest value.
                    if (MetricSyntactic.getSimilarityMetrics().contains(metric)){
                        
                        // Similarity case.
                        for (String termWord : term.getAllWords(aspect)){

                            // Initiate to actual values.
                            String maxTextWord = chunk.getBag().get(0).value;
                            double maxScore = finder.getSimilarity(metric, termWord, chunk.getBag().get(0).value);

                            for (Word textWord: chunk.getBag()){
                                double nextScore = finder.getSimilarity(metric, termWord, textWord.value);
                                if (nextScore >= maxScore){
                                    maxScore = nextScore;
                                    maxTextWord = textWord.value;
                                }
                            }
                            matchTextWords.add(maxTextWord);
                            matchScores.add(maxScore);
                            idx++;
                        }
  
                    }
                    else {
                        
                        // Distance case.
                        for (String termWord : term.getAllWords(aspect)){

                            // Initiate to actual values.
                            String minTextWord = chunk.getBag().get(0).value;
                            double minScore = finder.getSimilarity(metric, termWord, chunk.getBag().get(0).value);

                            for (Word textWord: chunk.getBag()){
                                double nextScore = finder.getSimilarity(metric, termWord, textWord.value);
                                if (nextScore <= minScore){
                                    minScore = nextScore;
                                    minTextWord = textWord.value;
                                }
                            }
                            matchTextWords.add(minTextWord);
                            matchScores.add(minScore);
                            idx++;
                        }
          
                    }

                    // Check how we want the weighting to be done.
                    if (Arrays.asList(UseFeatures.weightSides).contains(Side.TERM)){
                        scoreTermWeight = FeatureEquations.eqn1(new ArrayList<String>(term.getAllWords(aspect)), matchScores);
                    }
                    if (Arrays.asList(UseFeatures.weightSides).contains(Side.TEXT)){
                        scoreTextWeight = FeatureEquations.eqn2(matchTextWords, matchScores);
                    }
                }   
                break;
                
                
            } 
        }
        double[] scores = {scoreTermWeight, scoreTextWeight};
        return scores;
    }
    */
    
    
    
    /**
     * Produces a vector of features values based off of context given by sibling nodes.
     * @param chunk
     * @param term
     * @param partialMatrix
     * @return 
     */
    public static FeatureVector getSiblingContextVector(Chunk chunk, OntologyTerm term, FeatureArray partialMatrix){
        
        FeatureVector vector = new FeatureVector(chunk.chunkID, term.termID);
        for (Metric metric: Config.contextFunctions){
            for (Aspect aspect: Config.contextAspects){
                
                double score = -1;
                List<String> siblingTermIDs = term.siblingNodes;     
                List<Double> siblingMatchScores = new ArrayList<>();
                List<String> siblingMatchIDs = new ArrayList<>();
                
                for (String siblingTermID: siblingTermIDs){
                    Feature f = new Feature(Metric.valueOf(metric.toString()), aspect, Config.numEdges, Config.weighting, Context.NONE, score);
                    try{
                        double siblingScore = partialMatrix.getValue(chunk.chunkID, siblingTermID, f);    
                        if (siblingScore != -1){
                            siblingMatchScores.add(siblingScore);
                            siblingMatchIDs.add(siblingTermID);
                        }  
                    }
                    catch(Exception e){
                        // Most likely here because one of the sibling terms is deprecated and isn't in the term list because it has no label.
                        // Do nothing here for now.
                        // logger.info("problem looking up this chunk or term in the partial matrix. " + siblingTermID);
                    }
                }
                
                // Add the feature value to the feature vector.
                if (!siblingMatchScores.isEmpty()){
                    score = FeatureEquations.eqn6(siblingMatchIDs, siblingMatchScores);
                }
                vector.add(new Feature(Metric.valueOf(metric.toString()), aspect, Config.numEdges, Config.weighting, Context.SIBLING, score));
            }
        }
        return vector;
    }
    
    
    
    
    /**
     * Produces a vector of feature values based off of context given by the path to the root.
     * Make sure these are getting an weight of 0.00 if the term isn't found in the IC for 
     * ontologies. Check to make sure that doesn't happen anyway though.
     * @param chunk
     * @param term
     * @param partialMatrix
     * @return 
     */
    public static FeatureVector getRootPathContextVector(Chunk chunk, OntologyTerm term, FeatureArray partialMatrix){
        
        FeatureVector vector = new FeatureVector(chunk.chunkID, term.termID);
        for (Metric metric : Config.contextFunctions){
            for (Aspect aspect: Config.contextAspects){
                
                double score = -1;
                List<String> parentTermIDs = term.inheritedNodes;
                List<Double> parentMatchScores = new ArrayList<>();
                List<String> parentMatchIDs = new ArrayList<>();

                for (String parentTermID: parentTermIDs){
                     Feature f = new Feature(Metric.valueOf(metric.toString()), aspect, Config.numEdges, Config.weighting, Context.NONE, score);
                     double parentScore = partialMatrix.getValue(chunk.chunkID, parentTermID, f);
                     if (parentScore != -1){
                         parentMatchScores.add(parentScore);
                         parentMatchIDs.add(parentTermID);
                     }  
                }

                // Add the feature values to the feature vector.
                if (parentMatchScores.size() > 0){
                    score = FeatureEquations.eqn5(parentMatchIDs, parentMatchScores);
                }
                vector.add(new Feature(Metric.valueOf(metric.toString()), aspect, Config.numEdges, Config.weighting, Context.ROOTPATH, score));
            }
        }
        return vector;
    }
    
    
    


}

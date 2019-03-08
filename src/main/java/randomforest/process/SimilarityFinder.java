package randomforest.process;


import config.Config;
import structure.SimilarityStore;
import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.lexical_db.data.Concept;
import edu.cmu.lti.ws4j.Relatedness;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.HirstStOnge;
import edu.cmu.lti.ws4j.impl.JiangConrath;
import edu.cmu.lti.ws4j.impl.LeacockChodorow;
import edu.cmu.lti.ws4j.impl.Lesk;
import edu.cmu.lti.ws4j.impl.Lin;
import edu.cmu.lti.ws4j.impl.Path;
import edu.cmu.lti.ws4j.impl.Resnik;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import enums.Metric;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import static main.Main.logger;
import nlp.Stemmer;
import org.apache.commons.text.similarity.CosineDistance;
import org.apache.commons.text.similarity.JaccardSimilarity;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.apache.commons.text.similarity.LongestCommonSubsequenceDistance;
import org.apache.commons.text.similarity.SimilarityScore;


public class SimilarityFinder {
    
    private final ILexicalDatabase db;
    private final RelatednessCalculator rcWP;
    private final RelatednessCalculator rcLin;
    private final RelatednessCalculator rcPath;
    private final RelatednessCalculator rcLesk;
    private final RelatednessCalculator rcResnik;
    private final RelatednessCalculator rcLCH;
    private final RelatednessCalculator rcHSO;
    private final RelatednessCalculator rcJC;
    
    private final SimilarityScore  ssCD;
    private final SimilarityScore  ssJWD;
    private final SimilarityScore  ssLD;
    private final SimilarityScore  ssJS;
    private final SimilarityScore  ssLCSD;
    
    private final Map metricNames;
    
    private SimilarityStore store;
    private Stemmer stemmer;

    
    
    public SimilarityFinder(){
        
        
        // Semantic metrics.
        db = new NictWordNet();
        rcWP = new WuPalmer(db);
        rcLin = new Lin(db);
        rcPath = new Path(db);
        rcLesk = new Lesk(db);
        rcResnik = new Resnik(db);
        rcLCH= new LeacockChodorow(db);
        rcHSO = new HirstStOnge(db);
        rcJC = new JiangConrath(db);

        // Syntactic metrics.
        ssCD = new CosineDistance();
        ssJWD = new JaroWinklerDistance();
        ssLD = new LevenshteinDistance();
        ssJS = new JaccardSimilarity();
        ssLCSD = new LongestCommonSubsequenceDistance();
        
        // Create the hashmap of metric names associating them with their objects.
        metricNames = new HashMap<>();
        metricNames.put(Metric.WU_PALMER,rcWP);
        metricNames.put(Metric.LIN,rcLin);
        metricNames.put(Metric.PATH,rcPath);
        metricNames.put(Metric.LESK,rcLesk);
        metricNames.put(Metric.RESNIK,rcResnik);
        metricNames.put(Metric.LEACOCK_CHODOROW,rcLCH);
        metricNames.put(Metric.HIRST_ST_ONGE,rcHSO);
        metricNames.put(Metric.JIANG_CONRATH,rcJC);
        metricNames.put(Metric.COSINE_DIST, ssCD);
        metricNames.put(Metric.JAROWINKLER_DIST, ssJWD);
        metricNames.put(Metric.LEVENSCHTEIN_DIST, ssLD);
        metricNames.put(Metric.JACCARD_SIM, ssJS);
        metricNames.put(Metric.LCS_DIST, ssLCSD);
        
        stemmer = new Stemmer();
        
    }
    
    
    
    
    public void swapSaveStore(SimilarityStore newStore) throws IOException{
        // Save new stuff if there was already a store open then swap it out.
        if (store != null){
            store.save();
        }
        store = newStore;
    }
    
    
    
    
    // Handles being passed a generic similarity or distance function, figures out the subtype.
    public double getSimilarity(Metric metric, String word1, String word2) throws IOException{
        
        if (Metric.isSemanticMetric(metric)){
            return getSemanticSimilarity(metric, word1, word2);
        }
        else {
            return getSyntacticSimilarity(metric, word1, word2);
        }
        
    }
    
    
    
    
   
    private double getSemanticSimilarity(Metric metric, String word1, String word2) throws IOException{
        if (Config.useSaveStore){
            return getSemanticSimilarityWithStore(metric, word1, word2);
        }
        else {
            return getSemanticSimilarityWithoutStore(metric, word1, word2);
        }
    }
   

    private double getSyntacticSimilarity(Metric metric, String word1, String word2) throws IOException{
        if (Config.useSaveStore){
            return getSyntacticSimilarityWithStore(metric, word1, word2);
        }
        else {
            return getSyntacticSimilarityWithoutStore(metric, word1, word2);
        }
    }
    

    
    
    
    

    private double getSemanticSimilarityWithStore(Metric metric, String word1, String word2) throws IOException{
        double simScore = -1;
        try{
            return store.get(word1, word2);
        }
        catch(Exception e){
            try{
                simScore = getMaxSimilarity(metric, word1, word2);
                store.add(word1, word2, simScore);
                return simScore;
            }
            catch(Exception e2){
                Object[] data = {word1,word2,metric.toString(),e2.getMessage()};
                logger.info(String.format("problem finding similarity of %s,%s with %s: %s",data));
                return simScore;
            }
        }
    }
   

    private double getSyntacticSimilarityWithStore(Metric metric, String word1, String word2) throws IOException{
        double simScore = -1;
        switch(metric){
            // Distance metrics that return a Double.
            case JAROWINKLER_DIST:
            case COSINE_DIST:
            case JACCARD_SIM: 
                try{
                    return store.get(word1, word2);
                }
                catch(Exception e){
                    SimilarityScore scorer = (SimilarityScore) metricNames.get(metric);
                    try{
                        simScore = (double) scorer.apply(word1, word2);
                        store.add(word1, word2, simScore);
                        return simScore;
                    }
                    catch(Exception e2){
                        Object[] data = {word1,word2,metric.toString(),e2.getMessage()};
                        logger.info(String.format("problem finding similarity of %s,%s with %s: %s",data));
                        return simScore;
                    }
                }  
            
            // Distance metrics that return an Integer.
            case LCS_DIST:
            case LEVENSCHTEIN_DIST: 
                try{
                    return store.get(word1, word2);
                }
                catch(Exception e){
                    SimilarityScore scorer = (SimilarityScore) metricNames.get(metric);
                    try{
                        Integer tempInt = (Integer) scorer.apply(word1, word2);
                        simScore = tempInt.doubleValue();
                        store.add(word1, word2, simScore);
                        return simScore;
                    }
                    catch(Exception e2){
                        Object[] data = {word1,word2,metric.toString(),e2.getMessage()};
                        logger.info(String.format("problem finding similarity of %s,%s with %s: %s",data));
                        return simScore;
                    }
                }  
        }        
        return simScore;
    }
    
    
   
    private double getSemanticSimilarityWithoutStore(Metric metric, String word1, String word2) throws IOException{
        double simScore = -1;
        try{
            simScore = getMaxSimilarity(metric, word1, word2);
            return simScore;
        }
        catch(Exception e){
            Object[] data = {word1,word2,metric.toString(),e.getMessage()};
            logger.info(String.format("problem finding similarity of %s,%s with %s: %s",data));
            return simScore;
        }
    }
   

    private double getSyntacticSimilarityWithoutStore(Metric metric, String word1, String word2) throws IOException{
        double simScore = -1;
        SimilarityScore scorer = (SimilarityScore) metricNames.get(metric);
        switch(metric){
            // Distance metrics that return a Double.
            case JAROWINKLER_DIST:
            case COSINE_DIST:
            case JACCARD_SIM: 
                //SimilarityScore scorer = (SimilarityScore) metricNames.get(metric);
                try{
                    simScore = (double) scorer.apply(word1, word2);
                    return simScore;
                }
                catch(Exception e){
                    Object[] data = {word1,word2,metric.toString(),e.getMessage()};
                    logger.info(String.format("problem finding similarity of %s,%s with %s: %s",data));
                    return simScore;
                }
            // Distance metrics that return an Integer.
            case LCS_DIST:
            case LEVENSCHTEIN_DIST: 
                //SimilarityScore scorer = (SimilarityScore) metricNames.get(metric);
                try{
                    Integer tempInt = (Integer) scorer.apply(word1, word2);
                    simScore = tempInt.doubleValue();
                    return simScore;
                }
                catch(Exception e2){
                    Object[] data = {word1,word2,metric.toString(),e2.getMessage()};
                    logger.info(String.format("problem finding similarity of %s,%s with %s: %s",data));
                    return simScore;
                }
        }        
        return simScore;
    }
    

    
    
    
    
    private double getMaxSimilarity(Metric metric, String word1, String word2){
        
        // Look at potential alternate forms of the words.
        HashSet<String> word1Forms = new HashSet<>();
        HashSet<String> word2Forms = new HashSet<>();
        word1Forms.add(word1);
        word2Forms.add(word2);
        if (Config.useStemmer){
            word1Forms.add(stemmer.stem(word1));
            word2Forms.add(stemmer.stem(word2));
        }
        
        // Find the maximum applicable similarity.
        WS4JConfiguration.getInstance().setMFS(true);
        RelatednessCalculator rc = (RelatednessCalculator) metricNames.get(metric);        
        double maxScore = -1.00;
        List<POS[]> posPairs = rc.getPOSPairs();
        // Iterate through the possible forms of the words, can be more than one if stemmer applied.
        for (String word1Form: word1Forms){
            for (String word2Form: word2Forms){
                for (POS[] posPair: posPairs){
                    List<Concept> synsets1 = (List<Concept>) db.getAllConcepts(word1, posPair[0].toString());
                    List<Concept> synsets2 = (List<Concept>) db.getAllConcepts(word2, posPair[1].toString());
                    for (Concept synset1: synsets1){
                        for (Concept synset2: synsets2){
                            Relatedness relatedness = rc.calcRelatednessOfSynset(synset1, synset2);
                            double score = relatedness.getScore();
                            if (score > maxScore){
                                maxScore = score;
                            }
                        }
                    }
                }
            }
        }
        return maxScore;
    }

    
    
    
    
}

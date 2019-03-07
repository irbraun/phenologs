/*
 * Ian Braun
 * irbraun@iastate.edu
 * term-mapping 
 */
package nlp_algs;

import composer.Modifier;
import composer.Term;
import config.Config;
import enums.Ontology;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import static main.Main.logger;
import nlp.MyAnnotation;
import structure.Chunk;
import structure.OntologyTerm;
import text.Text;

/**
 *
 * @author irbraun
 */
public class MaxEnt_Model {
    
    private Maxent m;
    private final MaxEnt_FeatureList featuresObj;
    private final HashSet<String> used;
    private final HashMap<String,Integer> termToYMap;
    private final HashMap<Integer,String> yToTermMap;
    private int numTrainingChunks;
    private final int maxIter;
    private final HashMap<String,Integer> countTermTotal;
    private final HashSet<String> termIDsObservedInTraining;
    
    // testing isolating the ontologies.
    private final boolean useSingleOntology;
    private Ontology ontology;
    
    
    
    
    
    
    
    public MaxEnt_Model(int maxIter, Ontology o){
        featuresObj = new MaxEnt_FeatureList();
        used = new HashSet<>();
        termToYMap = new HashMap<>();
        yToTermMap = new HashMap<>();
        this.maxIter = maxIter;
        countTermTotal = new HashMap<>();
        termIDsObservedInTraining = new HashSet<>();
        useSingleOntology = true;
        ontology = o;
    }
    
    
    
    
    
    
    /*
    public MaxEnt_Model(int maxIter){
        featuresObj = new MaxEnt_FeatureList();
        used = new HashSet<>();
        termToYMap = new HashMap<>();
        yToTermMap = new HashMap<>();
        this.maxIter = maxIter;
        countTermTotal = new HashMap<>();
        termIDsObservedInTraining = new HashSet<>();
        useSingleOntology = false;
    }
    */
    
    
    
    
    public void train(Text text, List<Chunk> chunks) throws FileNotFoundException, Exception{
        
        numTrainingChunks = chunks.size();
        
        int yCtr = 0;
        
        // Extract features.
        //logger.info("extracting features");
        int numInstances = 0;
        for (Chunk chunk: chunks){
            MyAnnotation annotations = Modifier.getAnnotation(chunk);
            for (String termID: text.getCuratedEQStatementFromAtomID(chunk.chunkID).getAllTermIDs()){
                
                
                // see below. (1)
                if (useSingleOntology && utils.Util.inferOntology(termID).equals(ontology)){
                    
                    
                    
                    // is finding all these terms, but the 
                    //logger.info("found an applicable term");
                
                    // Found another instance (pair of some ontology class with a chunk of text).
                    numInstances++;

                    // See what features are present in this pairing, depending on whether lemmatization is used.
                    List<String> words = new ArrayList<>();
                    if (Config.useLemma){
                        annotations = Modifier.getAnnotation(chunk);
                        words = annotations.lemmas;
                    }
                    else{
                        words = chunk.getBagValues();
                    }

                    // Create all the relevant features.                
                    for (String w: words){
                        createFeature(w, termID);
                    }

                    // Add this to the ontology classes we can predict on.
                    if (!termToYMap.keySet().contains(termID)){
                        termToYMap.put(termID, yCtr);
                        yToTermMap.put(yCtr, termID);
                        yCtr++;
                    }

                    // Count the frequeny of terms to get priors.
                    int count = countTermTotal.getOrDefault(termID, 0);
                    count++;
                    countTermTotal.put(termID, count);

                    // Remember that this term was observed during training.
                    termIDsObservedInTraining.add(termID);
                
                }
                
                // have to add else for when we want all the ontologies
                //{
                
                
                
                //}
                
            }
        }

        // Prune the features if necessary.
        featuresObj.pruneFeatureList(Config.maxFeaturesPerTerm);
        
        // Create a training matrix.
        logger.info("creating training matrix");
        int[][] x = new int[numInstances][featuresObj.getSize()];
        int[] y = new int[numInstances];
        int instanceIdx=0;
        for (Chunk chunk: chunks){
            for (String termID: text.getCuratedEQStatementFromAtomID(chunk.chunkID).getAllTermIDs()){
                
                
                // has to be the the exact same check as above to get same list of curated terms. (2)
                if (useSingleOntology && utils.Util.inferOntology(termID).equals(ontology)){
                
                
                    List<MaxEnt_Feature> featureList = featuresObj.getFeatureList();
                    for (int j=0; j<featureList.size(); j++){
                        x[instanceIdx][j] = featureList.get(j).fires(chunk.getBagValues(), termID);
                    }
                    y[instanceIdx] = termToYMap.get(termID);
                    instanceIdx++;
                    
                }    
                 // have to add else for when we want all the ontologies
                //{
                
                
                
                //}
                
            }
        }
        
        // Training the model and learning the weights.
        double tol = Config.maxentTol;
        //double tol = 1E-5; //(default)
        //double tol = 1E-3; //(large)
        //double tol = 1E-4; //(small)
        
        logger.info("training");
        m = new Maxent(featuresObj.getSize(), x, y, 0.00, tol, maxIter);
        logger.info("done training");
        
        
  
        printWeights(new ArrayList<>(termIDsObservedInTraining));
        logger.info("done writing weights");
        
    }
    
    
    

    
    
    
    /**
     * Create new feature for this word and term pairing if not already created.
     * @param w
     * @param termID 
     */
    private void createFeature(String w, String termID){
        
        // This word and term have already been used to make a feature.
        String key = String.format("%s:%s", w, termID);
        if (used.contains(key)){
            featuresObj.getFeature(key).increaseInfo();
        }
        else {
            MaxEnt_Feature f = new MaxEnt_Feature(w, termID);
            featuresObj.addFeature(f);
            used.add(key);
        }
    }
    
    
    

    
    
    private double getProb(int[] x, int y) throws Exception{
        if (x.length != featuresObj.getSize()){
            throw new Exception("wrong dimensionality of the instance");
        }
        double prob = m.predict(x,y);
        if (Config.usePrior){
            String termID = yToTermMap.get(y);
            double prior = (double) (countTermTotal.getOrDefault(termID,0)+1) / (double) numTrainingChunks;
            return prior*prob;
        }
        return prob;
    }
    
    
    
    
    
    public double getProb(List<String> words, String termID) throws Exception{
        
        List<MaxEnt_Feature> featureList = featuresObj.getFeatureList();
        int[] x = new int[featureList.size()];
        
        
        int ctr=0;
        
        
        for (int j=0; j<featureList.size(); j++){
            x[j] = featureList.get(j).fires(words, termID);
            
            
            if (x[j]!=0){
                ctr++;
            }
            
            
        }
        
        

        
        
        
        
        
        
        
        /*
        String s = words.stream().collect(Collectors.joining(" "));
        System.out.println(String.format("The vector for %s | %s is",s,termID));
        for (int i=0; i<x.length; i++){
            System.out.print(x[i] + ",");
        }
        System.out.println();
        */
        int y=-1;
        double prob;
        if (termToYMap.keySet().contains(termID)){
            y = termToYMap.get(termID);
            prob = getProb(x,y);
        }
        else {
            prob = -100.00;
        }
        
        
        // how many features were non-zero.
        logger.info(String.format("%s out of %s features fired for %s",ctr,featureList.size(),termID));
        logger.info(String.format("prob for y=%s is %s",y,prob));
        
        
        
        
        
        return prob;

    }
    
    
    
   
    
    
    
    
    
    
    
    
    
    
    public List<Term> getBestKTerms(List<OntologyTerm> allTerms, Chunk chunk, int k) throws Exception{
        
        
        logger.info(String.format("--------------chunk %s---------------",chunk.chunkID));
        
        
        
        
        
        
        
        // Check whether want to use lemmatization or not.
        List<String> words = new ArrayList<>();
        if (Config.useLemma){
            MyAnnotation annotations = Modifier.getAnnotation(chunk);
            words = annotations.lemmas;
        }
        else{
            words = chunk.getBagValues();
        }
        
        // Create the feature matrices for this input then predict on it.
        // Only check the terms that have been observed during training because we 
        // already know the probability for the others will be zero.
        List<Term> terms = new ArrayList<>();
        List<Result> results = new ArrayList<>();
        for (OntologyTerm t: allTerms){
            if (termIDsObservedInTraining.contains(t.termID)){
                
                //logger.info(String.format("%s was observed in training, getting prob for it",t.termID));
                
                Result r = new Result();
                r.termID = t.termID;
                r.prob = getProb(words, t.termID);
                results.add(r);
            }
        }
        Collections.sort(results, new ResultComparator());
        if (!results.isEmpty()){
            for (int idx=0; idx<k; idx++){
                terms.add(new Term(results.get(idx).termID, results.get(idx).prob, utils.Util.inferOntology(results.get(idx).termID)));
            }
        }
        return terms;
    }
    
    
    
    
  
    /**
     * Input list of IDs has to be the terms used in training that have weights when those
     * are the target classes. Issue with weights not being adjusted because of imbalance
     * in the classes present in the training data. Is the termToYMap getting the correct
     * rows of the weights matrix in the smile maxent object. 
     * @param trainingTermIDs
     * @throws FileNotFoundException
     * @throws Exception 
     */
    public void printWeights(List<String> trainingTermIDs) throws FileNotFoundException, Exception{
        
        
        File wFile = new File("/work/dillpicl/irbraun/term-mapping/alpha2/nlp/me/matrix.csv");
        PrintWriter mWriter = new PrintWriter(wFile);
        
        List<MaxEnt_Feature> fs = featuresObj.getFeatureList();
        double[][] table = new double[fs.size()+1][trainingTermIDs.size()];   //why is this not the right size???

        // populate the table.
        int col=0;
        for (String id: trainingTermIDs){
            int y = termToYMap.get(id);
            double[] ws = m.getWeights(y);
            
            if (ws.length != fs.size()){
                //System.out.println(ws.length);
                //System.out.println(fs.size());
                //throw new Exception();
            }
            
            int row=0;
            for (double w: ws){
                table[row][col] = w;
                row++;
            }
            col++;
        }
        
        // print header only.
        StringBuilder sbH = new StringBuilder();
        for (String t: trainingTermIDs){
            sbH.append(t);
            sbH.append(",");
        }
        mWriter.println(sbH.toString());
        
        // print out the table.
        for (int r=0; r<fs.size(); r++){
            StringBuilder sb = new StringBuilder();
            sb.append(fs.get(r).featureTermID);
            sb.append(" (and) ");
            sb.append(fs.get(r).featureWord);
            sb.append(",");
            for (int c=0; c<trainingTermIDs.size(); c++){
                sb.append(String.valueOf(table[r][c]));
                sb.append(",");
            }
            mWriter.println(sb.toString());
        }
        
        mWriter.close();
    }
    
    
    
    
    
    private static class Result{
        public double prob;
        public String termID;
    }
    
    
    
    static class ResultComparator implements Comparator<Result>{
        @Override
        public int compare(Result r1, Result r2) {    
            if (r1.prob < r2.prob){
                return 1;
            }
            else if (r1.prob > r2.prob){
                return -1;
            }
            return 0;
        }
    }
  
    
    
    
    
    
    
    
}

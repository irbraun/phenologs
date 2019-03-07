/*
 * Ian Braun
 * irbraun@iastate.edu
 * term-mapping 
 */
package nlp;

import composer.EQStatement;
import composer.Term;
import config.Config;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import enums.Metric;
import enums.Ontology;
import enums.Role;
import enums.Aspect;
import enums.TextDatatype;
import infocontent.InfoContent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import static main.Main.logger;
import main.SimilarityFinder;
import ontology.Onto;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import structure.Chunk;
import structure.OntologyTerm;
import text.Text;
import uk.ac.ebi.brain.error.ClassExpressionException;
import uk.ac.ebi.brain.error.NewOntologyException;



/**
 * Parser that handles both term-mass assignment and extracting features and distributions from the
 * text data run through the core NLP pipeline.
 * @author irbraun
 */
public class Parser {
    
  
    
    private final Text text;
    private final HashMap<Ontology,Onto> ontoObjects;
    //private final Weighting wts;
    private final SimilarityFinder finder;
    private Bayesian bayes;
    private ArrayList<Bayesian> bayesList;
    private MyAnnotations annots;
    
    
    
    private final HashMap<Integer,List<String>> heatmap;
    
    
   
    
    
    public Parser() throws SQLException, OWLOntologyCreationException, NewOntologyException, ClassExpressionException, IOException, Exception{
        
        CoreNLP.setup();
        
        text = new Text();
        ontoObjects = new HashMap<>();        
        ontoObjects.put(Ontology.PATO, new Onto(Config.ontologyPaths.get(Ontology.PATO)));
        ontoObjects.put(Ontology.PO, new Onto(Config.ontologyPaths.get(Ontology.PO)));
        ontoObjects.put(Ontology.GO, new Onto(Config.ontologyPaths.get(Ontology.GO)));
        ontoObjects.put(Ontology.CHEBI, new Onto(Config.ontologyPaths.get(Ontology.CHEBI)));

        /*
        wts = new Weighting();
        wts.addOntology(Ontology.PATO);
        wts.addOntology(Ontology.PO);
        wts.addOntology(Ontology.GO);
        wts.addOntology(Ontology.CHEBI);
        */
        
        finder = new SimilarityFinder();
        heatmap = new HashMap<>();
        
        
        
        
        
        
        
    }
    
    
    
    
    
    
    
    
    public void loadAnnotations(List<Chunk> chunks){
        annots = annotate(chunks);
    }
    
    
    
    
     
    public void loadDistributions(String path) throws IOException, ClassNotFoundException{
        
        // Previous version for single type of distributions.
        //bayes = new Bayesian(path, Config.distributionsSerialName);
        //bayes.populateCounts();
        
        // Version that creates a number of different distribution objects based on different numbers of bins.
        bayesList = new ArrayList<>();
        bayes = new Bayesian(path, Config.distributionsSerialName);
        int minBins = 3;
        int maxBins = 5;
        for(int bins=minBins; bins<=maxBins; bins++){
            bayesList.add(new Bayesian(path, Config.distributionsSerialName));
            int idx = bins-minBins;
            bayesList.get(idx).populateCounts(bins);
        }
        
    }
    
    
    
    
    
   
    // Creates a distributions object for the dependency features and then outputs it to files.
    public void learnDistributions(List<Chunk> chunks, String loopTag) throws Exception{
        annots = annotate(chunks);
        bayes = new Bayesian();
        for (Chunk chunk: chunks){
            
            logger.info(chunk.chunkID);
            
            EQStatement eq = text.getCuratedEQStatementFromAtomID(chunk.chunkID);
            for (MyAnnotation annot: annots.getAnnotations(chunk)){
                HashMap<List<Role>,Double[]> depFeatureValues = getDependencyFeatures(eq, annot.dependencyGraph, annot.posTags, bayes, chunk.chunkID);
                for (List<Role> rolePair: depFeatureValues.keySet()){
                    double direction = depFeatureValues.get(rolePair)[0];
                    double length = depFeatureValues.get(rolePair)[1];
                    HashMap<DepFeatureCategories,Double> featureMap = new HashMap<>();
                    featureMap.put(DepFeatureCategories.DIRECTION, direction);
                    featureMap.put(DepFeatureCategories.LENGTH, length);
                    bayes.addFeatures(featureMap, rolePair);
                }
            }
        }
        bayes.summarize(Config.distributionsPath, loopTag);
        bayes.writeToTextFiles(Config.distributionsPath, loopTag);
        bayes.writeToSerializedFile(Config.distributionsPath, loopTag);
    }
    
    
    
   
    
    
    
    
    
    /**
     * Runs the core NLP pipeline on all the passed in text data.
     * As this is written there is no limit on the number of annotations that can be generated for a single text chunk.
     * Is this a problem? Make sure it's known what the graphs for those annotations look like so that the sentence
     * extractor is working the way it should be.
     * @param chunks
     * @return 
     */
    private MyAnnotations annotate(List<Chunk> chunks){
        MyAnnotations annotations = new MyAnnotations();
        for (Chunk chunk: chunks){
            ArrayList<String> sentences = extractSentences(chunk);
            for (String sentence: sentences){
                CoreDocument doc = new CoreDocument(sentence); 
                CoreNLP.getPipeline().annotate(doc);
                for (CoreSentence s: doc.sentences()){
                    annotations.addAnnotation(chunk, new MyAnnotation(s));
                }
            }
        }
        return annotations;
    }
    
    
    
    
    
    /**
     * Notes.
     * The maximum only gets updated at all when there is at least one feature available.
     * No normalization is currently done with respect to how many features are available, check this.
     * The maximum is there because more than one parse could be available for a given chunk.
     * If at least one of the parses had some available features, the max of those should be returned, even if its zero.
     * If none of the parses had any features available at all, -1 is returned to indicate no information.
     * @param eq
     * @param chunk
     * @return
     * @throws Exception 
     */
    public double getProbability(EQStatement eq, Chunk chunk) throws Exception{

        double maxProduct = 0.00;
        boolean atleastOneFeature = false;
        for (MyAnnotation annot: annots.getAnnotations(chunk)){
            double product = 1.00;
            HashMap<List<Role>,Double[]> depFeatureValues = getDependencyFeatures(eq, annot.dependencyGraph, annot.posTags, bayes, chunk.chunkID);
            List<Double> featureValueProbabilities = new ArrayList<>();
            
            
            for (List<Role> rolePair: depFeatureValues.keySet()){
                double direction = depFeatureValues.get(rolePair)[0];
                double length = depFeatureValues.get(rolePair)[1];
                
                // Check if there was an error in finding the features.
                if (direction >= 0.0 && length >= 0.0){
                    double directionProb = bayes.getProbability(rolePair, DepFeatureCategories.DIRECTION, direction);
                    double lengthProb = bayes.getProbability(rolePair, DepFeatureCategories.LENGTH, length);
                    featureValueProbabilities.add(directionProb);
                    featureValueProbabilities.add(lengthProb);
                }
            }
            // Check if any features were available for this EQ statement and this annotation of this chunk.
            if (featureValueProbabilities.size()>0){
                atleastOneFeature = true;
                for (double prob: featureValueProbabilities){
                    product = (double)product*prob;
                }
                if (product>=maxProduct){
                    maxProduct = product;
                }
            }
        }        
        if (atleastOneFeature){
            return maxProduct;
        }
        else {
            return -1.0;
        }
    }
    
    
    // overloaded version that supports the multiple distribution finding function by allowing different dist to be provided.
    public double getProbability(EQStatement eq, Chunk chunk, Bayesian someDist) throws Exception{

        double maxProduct = 0.00;
        boolean atleastOneFeature = false;
        for (MyAnnotation annot: annots.getAnnotations(chunk)){
            double product = 1.00;
            HashMap<List<Role>,Double[]> depFeatureValues = getDependencyFeatures(eq, annot.dependencyGraph, annot.posTags, someDist, chunk.chunkID);
            List<Double> featureValueProbabilities = new ArrayList<>();
            
            
            for (List<Role> rolePair: depFeatureValues.keySet()){
                double direction = depFeatureValues.get(rolePair)[0];
                double length = depFeatureValues.get(rolePair)[1];
                
                // Check if there was an error in finding the features.
                if (direction >= 0.0 && length >= 0.0){
                    double directionProb = someDist.getProbability(rolePair, DepFeatureCategories.DIRECTION, direction);
                    double lengthProb = someDist.getProbability(rolePair, DepFeatureCategories.LENGTH, length);
                    featureValueProbabilities.add(directionProb);
                    featureValueProbabilities.add(lengthProb);
                }
            }
            // Check if any features were available for this EQ statement and this annotation of this chunk.
            if (featureValueProbabilities.size()>0){
                atleastOneFeature = true;
                for (double prob: featureValueProbabilities){
                    product = (double)product*prob;
                }
                if (product>=maxProduct){
                    maxProduct = product;
                }
            }
        }        
        if (atleastOneFeature){
            return maxProduct;
        }
        else {
            return -1.0;
        }
    }
    public double getProbabilityNoDirection(EQStatement eq, Chunk chunk, Bayesian someDist) throws Exception{

        double maxProduct = 0.00;
        boolean atleastOneFeature = false;
        for (MyAnnotation annot: annots.getAnnotations(chunk)){
            double product = 1.00;
            HashMap<List<Role>,Double[]> depFeatureValues = getDependencyFeatures(eq, annot.dependencyGraph, annot.posTags, someDist, chunk.chunkID);
            List<Double> featureValueProbabilities = new ArrayList<>();

            for (List<Role> rolePair: depFeatureValues.keySet()){
                double length = depFeatureValues.get(rolePair)[1];
                
                // Check if there was an error in finding the features.
                if (length >= 0.0){
                    double lengthProb = someDist.getProbability(rolePair, DepFeatureCategories.LENGTH, length);
                    featureValueProbabilities.add(lengthProb);
                }
            }
            // Check if any features were available for this EQ statement and this annotation of this chunk.
            if (featureValueProbabilities.size()>0){
                atleastOneFeature = true;
                for (double prob: featureValueProbabilities){
                    product = (double)product*prob;
                }
                if (product>=maxProduct){
                    maxProduct = product;
                }
            }
        }        
        if (atleastOneFeature){
            return maxProduct;
        }
        else {
            return -1.0;
        }
    }
    
    
    
    
    
    
    
    // version that finds multiple probabilities using different methods.
    public ArrayList<Double> getProbabilities(EQStatement eq, Chunk chunk) throws Exception {
        ArrayList<Double> probs = new ArrayList<>();
        /* Only want to include one of these that uses the direction ratio.
        for (Bayesian dist: bayesList){
            probs.add(getProbability(eq, chunk, dist));
        }
        */
        
        // Distribution with 4 bins, with directions.
        probs.add(getProbability(eq, chunk, bayesList.get(1)));
        
        // Distribution with 3,4,5 bins, with no directions.
        for (Bayesian dist: bayesList){
            probs.add(getProbabilityNoDirection(eq, chunk, dist));
        }
        return probs;
    }    
        
    
    
    
    
    
    
    
    
    
    
    
    

        

    
    
    
    private HashMap<List<Role>,Double[]> getDependencyFeatures(EQStatement eq, SemanticGraph dG, List<String> posTags, Bayesian dist, int chunkID) throws IOException, Exception{
        
        // Note these error codes are not checked for until right before they are added to the growing lists.
        Double[] error1 = {-1.0,-1.0}; // At least one of the componenets was not present in the EQ statement. 
        Double[] error2 = {-2.0,-2.0}; // There is no undirected path connecting these two tokens in the graph.
        Double[] error3 = {-3.0,-3.0}; // The target and source are the same token, direction is meaningless.
        
        HashMap<Role,Integer> roleIndexMap = new HashMap<>();
        HashMap<Role,IndexedWord> roleIdxTokenMap = new HashMap<>();
        HashMap<List<Role>,Double[]> returnMap = new HashMap<>();

        // Look at the term mass distributions of all the ontology terms in the curated EQ statement.
        for (Term term: eq.termChain){
            try{
                Role role = term.role;
                OntologyTerm ontologyTerm = ontoObjects.get(utils.Util.inferOntology(term.id)).getTermFromTermID(term.id);
                
                //double[] termMassDistribution = assignTermMass(wts, ontologyTerm, dG, finder);
                double[] termMassDistribution = assignTermMassWithPOS(ontologyTerm, dG, posTags, finder, chunkID);
                
                
                
                /*
                PROBLEM
                massIndex is currently being returned as 0 if a location couldn't be found.
                
                */
               
                
                int massIndex = resolveMassIndices(termMassDistribution);
                
                // Working on the problem.
                if (massIndex == 0){
                    logger.info("PROBLEM");
                }
                else {
                    roleIndexMap.put(role, massIndex);
                }
                
                
                //roleIndexMap.put(role, massIndex); 
            }
            catch(NullPointerException e){
                System.out.println(term.id);
            }
        }

        // Components that aren't mapped to any tokens in the graph are are null indexed words.
        for (Role role: Role.values()){
            roleIdxTokenMap.put(role, getIndexedWord(dG, role, roleIndexMap));
        }

        // Iterate through all the pairs of components whose dependencies we want to look at.
        for (List<Role> rolePair: dist.getDependencyRolePairs()){
            
            // The two tokens that we want to look at the dependency betweenn.
            IndexedWord token1 = roleIdxTokenMap.get(rolePair.get(0));
            IndexedWord token2 = roleIdxTokenMap.get(rolePair.get(1));
            if (token1==null || token2==null){
                returnMap.put(rolePair, error1);
            }
            
            else {
                double[] dirAndLen = getDirectionAndLength(dG, token1, token2);
                if (dirAndLen==null){
                    returnMap.put(rolePair, error2);
                }
                else{
                    double direction = dirAndLen[0];
                    double length = dirAndLen[1];
                    
                    // Not using error 3 anymore, instead when there is target/source overlap treat it as length 0 and unique direction (>1).
                    if (length==0){
                        Double[] returnValues = {1.5, length};
                        returnMap.put(rolePair, returnValues);
                    }
                    /* This was how 
                    if (length==0){
                        returnMap.put(rolePair, error3);
                    }
                    */
                    else {
                        Double[] returnValues = {direction, length};
                        returnMap.put(rolePair, returnValues);
                    }
                }
            }
        }
        return returnMap;
    }
    
    
    
    
    
   
        
    /**
     * Distribute the mass of an ontology term among tokens in the dependency graph.
     * This is the first initial attempt at one that uses the variable importance measures taken from the candidate
     * term mapping step. The potential problem with doing it this way is that the the variable importance should 
     * actually be specific to individual instances rather than the forest level importance of the features. The R
     * implementation doesn't support this, would have to either write the prediction method myself and 
     * @param wts Object containing the metrics, term aspects, and their associated weights for all ontology.
     * @param term The ontology term.
     * @param dG The dependencies graph.
     * @param finder The similarity finder that calculates word to word similarity and distances with each metric.
     * @return
     * @throws IOException 
     */
    private double[] assignTermMass(Weighting wts, OntologyTerm term, SemanticGraph dG, SimilarityFinder finder) throws IOException{
        
        //TODO Account for the similarity metrics that are distance based not similarity based, just add the check and find reciprocal.
        
        Ontology ontology = utils.Util.inferOntology(term.termID);
        
        double[] finalVotes = new double[dG.size()];
        
        // Iterate through the individual words in each aspect of the ontology term.
        for (Aspect aspect: wts.getAspects(ontology)){
            for (String termWord: term.getAllWords(aspect)){
                double[] thisWordsVotes = new double[dG.size()];
                for (Metric metric: wts.getMetrics(ontology)){
                    
                    // Find the contribution of this metric to each vote.
                    double[] thisWordsVotesWithThisMetric = new double[dG.size()];
                    double sum = 0.000;
                    for (int i=0; i<dG.size(); i++){
                        
                        
                        
                        // TODO fix this, trying something here
                        String token;
                        try{
                            token = dG.getNodeByIndex(i+1).originalText();
                            
                        }
                        catch (Exception e){
                            int idxProb = i+1;
                            logger.info("Problem with getting node with index " + idxProb);
                            token = "aaaaa";
                        }
                          
                        
                        
                        
                        // TODO use a stemmer. This should actually be moved to the similarity finder methods I think.
                        Stemmer s = new Stemmer();
                        //token = s.stem(token);
                        //termWord = s.stem(termWord);
                        
                        double similarity = finder.getSimilarity(metric, termWord, token);
                        // Check that a valid similarity was found.
                        if (similarity == -1.000){
                            similarity = 0.000;
                        }
                        // Weight by the information content of the ontology word in the flattened ontology.
                        double ic = InfoContent.getICofWordInOntology(utils.Util.inferOntology(term.termID),termWord);
                        
                        // TODO check about this.
                        //similarity = similarity * (double)ic;
                        
                        sum = sum + similarity;
                        thisWordsVotesWithThisMetric[i] = similarity;
                    }
                         
                    // Compress the contribution of this metric to a single vote distributed across all words, then weight by metric importance.
                    if (sum != 0.000){
                        for (int i=0; i<dG.size(); i++){
                            thisWordsVotes[i] = thisWordsVotes[i] + ((thisWordsVotesWithThisMetric[i] / (double)sum) * (double)wts.getMetricWeight(ontology, metric));  
                        }
                    } 
                }
                
                // Update the final votes to reflect the contribution of this ontology word weighted with respect to the term aspect.
                for (int i=0; i<dG.size(); i++){
                    //finalVotes[i] = finalVotes[i] + (double)thisWordsVotes[i] * (double)wts.getAspectWeight(ontology, aspect);
                    finalVotes[i] = finalVotes[i] + (double)thisWordsVotes[i];
                }
            }
        }
        
        finalVotes = utils.Util.normalize(finalVotes);
        return finalVotes;
    }
    
    
    
    
    
    
    // Testing this method out, change this.
    /*
    public void writeHeatMaps() throws FileNotFoundException{
        for (Map.Entry<Integer,List<String>> entry: heatmap.entrySet()){
            File outputFile = new File(String.format("%schunk_%s_mapping.csv", Config.heatmapsPath, entry.getKey().toString()));
            PrintWriter writer = new PrintWriter(outputFile);
            for (String row: entry.getValue()){
                writer.println(row);
            }
            writer.close();            
        }
    }
    */
    
    
    
    
    
    
    // current version
    private double[] assignTermMassWithPOS(OntologyTerm term, SemanticGraph dG, List<String> posTags, SimilarityFinder finder, int chunkID) throws IOException{
        
        // What is the ontology that this term is from?
        Ontology ontology = utils.Util.inferOntology(term.termID);
        double[] finalVotes = new double[dG.size()];
        
        // Check semantic syntactic similarity to the term label.
        for (int i=0; i<dG.size(); i++){
            double[] localVotes = new double[dG.size()];
            String token = dG.getNodeByIndex(i+1).originalText();
            double maxSimilarity = 0.00;
            // If the term is for an entity then only nouns get to vote.
            if ((!ontology.equals(Ontology.PATO) && posTags.get(i).contains("NN")) || ontology.equals(Ontology.PATO)){
                for (String termWord: term.getAllWords(Aspect.LABEL)){ 
                    if (CoreNLP.isNotStopWord(token) && CoreNLP.isNotStopWord(termWord)){
                        double similarity = finder.getSimilarity(Metric.LEVENSCHTEIN_DIST, token, termWord);
                        if (similarity >= maxSimilarity){
                            maxSimilarity = similarity;
                        }
                    }
                }
            }
            localVotes[i] = maxSimilarity;
            double diffFromMedian = maxSimilarity - getMedianValue(localVotes);
            double diffFromMin = maxSimilarity - getMinValue(localVotes);
            finalVotes[i] = finalVotes[i] + diffFromMin;
        }
        
        
        
        // Check each token for semantic similarity to the term label or syntactic similarity to the synonyms;
        for (int i=0; i<dG.size(); i++){
            double[] localVotes = new double[dG.size()];
            String token = dG.getNodeByIndex(i+1).originalText();
            double maxSimilarity = 0.00;
            if ((!ontology.equals(Ontology.PATO) && posTags.get(i).contains("NN")) || ontology.equals(Ontology.PATO)){
                for (String termWord: term.getAllWords(Aspect.LABEL)){
                    if (CoreNLP.isNotStopWord(token) && CoreNLP.isNotStopWord(termWord)){
                        double similarity = finder.getSimilarity(Metric.LEVENSCHTEIN_DIST, token, termWord);
                        if (similarity >= maxSimilarity){
                            maxSimilarity = similarity;
                        }
                    }
                }
                for (String termWord: term.getAllSynonymWords()){
                    if (CoreNLP.isNotStopWord(token) && CoreNLP.isNotStopWord(termWord)){
                        double similarity = finder.getSimilarity(Metric.PATH, token, termWord);
                        if (similarity >= maxSimilarity){
                            maxSimilarity = similarity;
                        }
                    }
                }
            }
            localVotes[i] = maxSimilarity;
            double diffFromMedian = maxSimilarity - getMedianValue(localVotes);
            double diffFromMin = maxSimilarity - getMinValue(localVotes);
            finalVotes[i] = finalVotes[i] + diffFromMin;
        }
        
        
        // Check each token for any similarity to the description or semantic similarity to the synonyms.
        for (int i=0; i<dG.size(); i++){
            double[] localVotes = new double[dG.size()];
            String token = dG.getNodeByIndex(i+1).originalText();
            double maxSimilarity = 0.00;
            if ((!ontology.equals(Ontology.PATO) && posTags.get(i).contains("NN")) || ontology.equals(Ontology.PATO)){
                for (String termWord: term.getAllWords(Aspect.DESCRIPTION)){
                    if (CoreNLP.isNotStopWord(token) && CoreNLP.isNotStopWord(termWord)){
                        double similarity = finder.getSimilarity(Metric.PATH, token, termWord);
                        if (similarity >= maxSimilarity){
                            maxSimilarity = similarity;
                        }
                    }
                }
                for (String termWord: term.getAllWords(Aspect.DESCRIPTION)){
                    if (CoreNLP.isNotStopWord(token) && CoreNLP.isNotStopWord(termWord)){
                        double similarity = finder.getSimilarity(Metric.LEVENSCHTEIN_DIST, token, termWord);
                        if (similarity >= maxSimilarity){
                            maxSimilarity = similarity;
                        }
                    }
                }
                for (String termWord: term.getAllSynonymWords()){
                    if (CoreNLP.isNotStopWord(token) && CoreNLP.isNotStopWord(termWord)){
                        double similarity = finder.getSimilarity(Metric.LEVENSCHTEIN_DIST, token, termWord);
                        if (similarity >= maxSimilarity){
                            maxSimilarity = similarity;
                        }
                    }
                }
            }
            localVotes[i] = maxSimilarity;
            double diffFromMedian = maxSimilarity - getMedianValue(localVotes);
            double diffFromMin = maxSimilarity - getMinValue(localVotes);
            finalVotes[i] = finalVotes[i] + diffFromMin;
        }
       
        finalVotes = utils.Util.normalize(finalVotes);
        return finalVotes;
    }
    
    
    
    
    
    
    
    
    
    
    

    /**
     * Fuzzy matching for mapping ontology terms to locations in a dependency graph.
     * Ignores weighting information from the variable importance (local or forest-level) from
     * the candidate term finding step.
     * @param term
     * @param dG
     * @param finder
     * @return
     * @throws IOException 
     */
    private double[] assignTermMassFixedOLD(OntologyTerm term, SemanticGraph dG, SimilarityFinder finder, int chunkID) throws IOException{
        
        
        // Things that need to be added to this:
        // 1. stopword removal (done)
        // 2. POS appropriate assignment? That could be really difficult depending on the structure. (not done yet)
        
        // Fix efficiency for checking for stopwords when running on full dataset.
        
        
        // Information not currently used.
        // This would be important if allowing terms from different ontologies to match different parts-of-speech?
        Ontology ontology = utils.Util.inferOntology(term.termID);
        double[] finalVotes = new double[dG.size()];
        
        // Check each token for syntactic similarity to the term label.
        for (int i=0; i<dG.size(); i++){
            double[] localVotes = new double[dG.size()];
            String token = dG.getNodeByIndex(i+1).originalText();
            double maxSimilarity = 0.00;
            for (String termWord: term.getAllWords(Aspect.LABEL)){ 
                if (CoreNLP.isNotStopWord(token) && CoreNLP.isNotStopWord(termWord)){
                    double similarity = finder.getSimilarity(Metric.JACCARD_SIM, token, termWord);
                    if (similarity >= maxSimilarity){
                        maxSimilarity = similarity;
                    }
                }
            }
            localVotes[i] = maxSimilarity;
            double diffFromMedian = maxSimilarity - getMedianValue(localVotes);
            double diffFromMin = maxSimilarity - getMinValue(localVotes);
            finalVotes[i] = finalVotes[i] + diffFromMin;
        }
        
        // Check each token for semantic similarity to the term label or syntactic similarity to the synonyms;
        for (int i=0; i<dG.size(); i++){
            double[] localVotes = new double[dG.size()];
            String token = dG.getNodeByIndex(i+1).originalText();
            double maxSimilarity = 0.00;
            for (String termWord: term.getAllWords(Aspect.LABEL)){
                if (CoreNLP.isNotStopWord(token) && CoreNLP.isNotStopWord(termWord)){
                    double similarity = finder.getSimilarity(Metric.JACCARD_SIM, token, termWord);
                    if (similarity >= maxSimilarity){
                        maxSimilarity = similarity;
                    }
                }
            }
            for (String termWord: term.getAllSynonymWords()){
                if (CoreNLP.isNotStopWord(token) && CoreNLP.isNotStopWord(termWord)){
                    double similarity = finder.getSimilarity(Metric.PATH, token, termWord);
                    if (similarity >= maxSimilarity){
                        maxSimilarity = similarity;
                    }
                }
            }
            localVotes[i] = maxSimilarity;
            double diffFromMedian = maxSimilarity - getMedianValue(localVotes);
            double diffFromMin = maxSimilarity - getMinValue(localVotes);
            finalVotes[i] = finalVotes[i] + diffFromMin;
        }
        
        
        // Check each token for any similarity to the description or semantic similarity to the synonyms.
        for (int i=0; i<dG.size(); i++){
            double[] localVotes = new double[dG.size()];
            String token = dG.getNodeByIndex(i+1).originalText();
            double maxSimilarity = 0.00;
            for (String termWord: term.getAllWords(Aspect.DESCRIPTION)){
                if (CoreNLP.isNotStopWord(token) && CoreNLP.isNotStopWord(termWord)){
                    double similarity = finder.getSimilarity(Metric.PATH, token, termWord);
                    if (similarity >= maxSimilarity){
                        maxSimilarity = similarity;
                    }
                }
            }
            for (String termWord: term.getAllWords(Aspect.DESCRIPTION)){
                if (CoreNLP.isNotStopWord(token) && CoreNLP.isNotStopWord(termWord)){
                    double similarity = finder.getSimilarity(Metric.JACCARD_SIM, token, termWord);
                    if (similarity >= maxSimilarity){
                        maxSimilarity = similarity;
                    }
                }
            }
            for (String termWord: term.getAllSynonymWords()){
                if (CoreNLP.isNotStopWord(token) && CoreNLP.isNotStopWord(termWord)){
                    double similarity = finder.getSimilarity(Metric.JACCARD_SIM, token, termWord);
                    if (similarity >= maxSimilarity){
                        maxSimilarity = similarity;
                    }
                }
            }
            localVotes[i] = maxSimilarity;
            double diffFromMedian = maxSimilarity - getMedianValue(localVotes);
            double diffFromMin = maxSimilarity - getMinValue(localVotes);
            finalVotes[i] = finalVotes[i] + diffFromMin;
        }
        
        
        finalVotes = utils.Util.normalize(finalVotes);
        
        
        // Values for heatmap
        String termID = term.termID;
        String termLabel = term.label;
        StringBuilder heatmapRowBldr = new StringBuilder();
        heatmapRowBldr.append(String.format("%s,%s,", termID, termLabel));
        for (int i=0; i<dG.size(); i++){
            String value = String.format("%.2f",finalVotes[i]);
            heatmapRowBldr.append(String.format("%s,",value));
        }
        String heatMapRow = heatmapRowBldr.toString();
        
        
        if (heatmap.containsKey(chunkID)){
            heatmap.get(chunkID).add(heatMapRow);
        }
        else {
            heatmap.put(chunkID, new ArrayList<>());
            
            StringBuilder wordsBldr = new StringBuilder();
            wordsBldr.append("ID,Label,");
            for (int i=0; i<dG.size(); i++){
                wordsBldr.append(String.format("%s,", dG.getNodeByIndex(i+1).originalText()));
            }
            
            heatmap.get(chunkID).add(wordsBldr.toString());    
            heatmap.get(chunkID).add(heatMapRow);
        }
        
        return finalVotes;
         
        
    }
    
    
    
    
    private double getMedianValue(double[] arr){
        Arrays.sort(arr);
        double median;
        if (arr.length % 2 == 0)
            median = ((double)arr[arr.length/2] + (double)arr[arr.length/2 - 1])/2;
        else
            median = (double) arr[arr.length/2];
        return median;
    }
    
    private double getMinValue(double[] arr){
        Arrays.sort(arr);
        return arr[0];
    }
    
    
    
    
    
    /**
     * Looking at the shortest path between words 1 and 2 using edges in the dependency graph.
     * Returns null if and only if there was no undirected path found between the two words.
     * Returns {0,0} if and only if the two tokens (target and source) are the same, otherwise
     * the length will always be 0 but the direction could still be 0.
     * The way this method is written right now, forward edges are from the token1 to token2 and 
     * backwards edges are from token1 to token2. The direction is a number between 0 and 1 which
     * represents the fraction of the total edges that are forward. So a direction of 1 indicates 
     * that token1 is the source and token2 is the target for all the dependency edges on the path.
     * In these graphs the target is dependent on the source. 
     * So a direction near 1 indicates that token2 is mainly dependent on token 1.
     * 
     * @param dG
     * @param token1
     * @param token2
     * @return
     * @throws Exception 
     */
    private double[] getDirectionAndLength(SemanticGraph dG, IndexedWord token1, IndexedWord token2) throws Exception{
        
        // Try to find a shortest undirected path between the two tokens.
        List<IndexedWord> nodeList;
        try{
            nodeList = dG.getShortestUndirectedPathNodes(token1, token2);
        }
        catch(Exception e){
            return null;
        }
       
        // Check that there are atleast two nodes along the path  
        if (nodeList.size()>=2){
            // Iterate through the edges that make up this shortest dependency path.
            int forward = 0;
            int backward = 0;
            double direction;
            for (int i=0; i<nodeList.size()-1; i++){
                // Find this particular edge.
                IndexedWord node1 = nodeList.get(i);
                IndexedWord node2 = nodeList.get(i+1);
                boolean okay = false;
                try{
                    List<SemanticGraphEdge> edges = dG.getShortestDirectedPathEdges(node1,node2);
                    if (edges.size()==1){
                        forward++;
                        okay = true;
                    }
                    // debugging
                    else {
                        System.out.println("instead of length 1, its " + edges.size());
                    }    
                }
                catch(NullPointerException e){
                    List<SemanticGraphEdge> edges = dG.getShortestDirectedPathEdges(node2, node1);
                    if (edges.size()==1){
                        backward++;
                        okay = true;
                    }
                    // debugging
                    else {
                        System.out.println("instead of length 1, its " + edges.size());
                    }
                }
                if (!okay){
                    throw new Exception();
                }
            }
            direction = (double)forward / (double)(forward+backward);
            double[] result = {direction, forward+backward};
            return result;
        }
        else {
            double[] result = {0, 0};
            return result;
        }
    } 
    
    
    
    
    
    
    
    
    // TODO update this to account for the fact that term mass should be spread over multiple tokens,
    // a mapping to multipe locations like that could be valid.
    
    // Currently returns 0 if no max was found.
    // Why would this happen?
    private int resolveMassIndices(double[] termMassDistribution){
        
        // Resolve to singe token or set of tokens.
        int maxIndex = -1;
        double maxValue = -1.00;
        for (int i=0; i<termMassDistribution.length; i++){
            if (termMassDistribution[i] >= maxValue){
                maxValue = termMassDistribution[i];
                maxIndex = i;
            }
        }
        maxIndex = maxIndex+1;
        return maxIndex;
    }
    
    
    
    
    
    private IndexedWord getIndexedWord(SemanticGraph dG, Role role, HashMap<Role,Integer> roleIndexMap){
        if (roleIndexMap.containsKey(role)){
            return dG.getNodeByIndex(roleIndexMap.get(role));
        }
        else {
            return null;
        }       
    }
    
    
    
    
    
    private ArrayList<String> extractSentences(Chunk chunk){
        ArrayList<String> sentences = new ArrayList<>();
        if (chunk.textType == TextDatatype.PHENE){
            sentences.add(chunk.getRawText());
        }
        else if (chunk.textType == TextDatatype.PHENOTYPE){
            sentences.addAll(Arrays.asList(chunk.getRawText().split(";")));
        }
        return sentences;  
    }
    
    
    
    
}

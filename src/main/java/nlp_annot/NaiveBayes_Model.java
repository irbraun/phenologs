
package nlp_annot;

import composer.Modifier;
import composer.Term;
import config.Config;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import static main.Main.logger;
import nlp.MyAnnotation;
import objects.Chunk;
import objects.OntologyTerm;
import text.Text;






public class NaiveBayes_Model {
    
    

    
   
     
    private final HashMap<String,HashMap<String,Integer>> countWordsByTerm;
    private final HashMap<String,Integer> countTermTotal;
    private int wordVocabSize;
    private int lemmaVocabSize;
    private int numChunks;
    

    public NaiveBayes_Model() throws SQLException{
        countWordsByTerm = new HashMap<>();
        countTermTotal = new HashMap<>();
    }
        
    
    
    
    public void train(Text text, List<Chunk> chunks, double threshold) throws SQLException, FileNotFoundException, IOException, Exception{
        
        
        // Build the mapping of synonyms from the w2v results file.
        HashMap<String,ArrayList<String>> w2vs = new HashMap<>();
        
        // For this model, what use_embeddings means is to add words to the bag of words for each chunk.
        // Order doesn't matter here (unlike with the string-based ones so just stick them on to the chunks).
        // Add all the synonyms that surpass the threshold of similarity to the existing word in question.
        if (Config.useEmbeddings){
            System.out.println("embeddings are being used");
            File file = new File("/Users/irbraun/NetBeansProjects/term-mapping/path/data/allpairs.txt");
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    String w1 = parts[0];
                    String w2 = parts[1];
                    double similarity = Double.valueOf(parts[2]);
                    if (similarity>=threshold){
                        ArrayList<String> a = w2vs.getOrDefault(w1, new ArrayList<>());
                        a.add(w2);
                        w2vs.put(w1, a);
                        ArrayList<String> b = w2vs.getOrDefault(w2, new ArrayList<>());
                        b.add(w1);
                        w2vs.put(w2, b);
                    }
                }
            }
            catch (Exception e){
                System.out.println("problem with building the dictionary");
                logger.info("problem with building the dictionary");
            }
        }
        System.out.println("After reading in dict " + (w2vs.keySet().size()/2) + " were added due to threshold="+threshold);
        
        int totalWordsInModel=0;
        
        numChunks = chunks.size();
        HashSet<String> uniqueWords = new HashSet<>();
        HashSet<String> uniqueLemmas = new HashSet<>();
        for (Chunk chunk: chunks){

            ArrayList<String> additionalTokens = new ArrayList<>();
            
            // Check if lemmatization should be done before updating the word frequences.
            List<String> tokens = new ArrayList<>();
            if (Config.useLemmas){
                MyAnnotation annotations = Modifier.getAnnotation(chunk);
                tokens = annotations.lemmas;
                // new bit
                for (String s: tokens){
                    additionalTokens.addAll(w2vs.getOrDefault(s, new ArrayList<>()));
                }
            }
            else {
                tokens = chunk.getBagValues();
                // new bit
                for (String s: tokens){
                    additionalTokens.addAll(w2vs.getOrDefault(s, new ArrayList<>()));
                }
            }
            
            // new bit
            tokens.addAll(additionalTokens);
            
            totalWordsInModel += tokens.size();
            
            
           
            
            // The size of the vocabulary reflects lemmatization or not.
            uniqueWords.addAll(chunk.getBagValues());
            uniqueLemmas.addAll(tokens);

           
            List<String> termIDs = text.getCuratedEQStatementFromAtomID(chunk.chunkID).getAllTermIDs();
            for (String token: tokens){
                for (String termID: termIDs){
                    // Update the counts for this word and this term class.
                    HashMap<String,Integer> updatedMap = countWordsByTerm.getOrDefault(termID, new HashMap<>());
                    int count = updatedMap.getOrDefault(token, 0);
                    count++;
                    updatedMap.put(token, count);
                    countWordsByTerm.put(termID, updatedMap);       
                }
            }
            for (String termID: termIDs){
                int count = countTermTotal.getOrDefault(termID, 0);
                count++;
                countTermTotal.put(termID, count);
            }
        }
        
        totalWordsInModel++;
        
        // If lemmatization is not used then the number of lemmas reported is just the number of words.
        wordVocabSize = uniqueWords.size();
        lemmaVocabSize = uniqueLemmas.size();
        logger.info(String.format("there were %s unique words in the training data", wordVocabSize));
        logger.info(String.format("there were %s unique lemmas in the training data", lemmaVocabSize));
        
        
        System.out.println("The total number of words (vocabulary size of the model) was " + totalWordsInModel);
        
    }
    

    
    
    
    
    
    private double getLogProbAddOne(String word, String termID){       
        double probWgivenT;
        if (countWordsByTerm.containsKey(termID)){
            probWgivenT = (double) (countWordsByTerm.get(termID).getOrDefault(word,0)+1) / (double) (countTermTotal.getOrDefault(termID,0)+wordVocabSize);
        }
        else {
            probWgivenT = (double) (1) / (double) (wordVocabSize);
        }
        return Math.log(probWgivenT);
    }
    
    
    
    
    
    public double getLogProb(List<String> words, String termID){
        double sum;
        if (Config.usePrior){
            double probT = (double) (countTermTotal.getOrDefault(termID,0)+1) / (double) numChunks;
            sum = Math.log(probT);
        }
        else {
            sum = 0.00;
        }

        for (String w: words){
            sum += getLogProbAddOne(w, termID);
        }
        return sum;
    }
    
    
    
    
    
    public List<Term> getBestKTerms(List<OntologyTerm> allTerms, Chunk chunk, int k){
        List<Term> terms = new ArrayList<>();
        List<Result> results = new ArrayList<>();
        
        // Only do this modification if lemmatization is used.
        List<String> chunkWords;
        if (Config.useLemmas){
            MyAnnotation annotations = Modifier.getAnnotation(chunk);
            chunkWords = annotations.lemmas;
        }
        else {
            chunkWords = chunk.getBagValues();
        }

        for (OntologyTerm t: allTerms){
            Result r = new Result();
            r.termID = t.termID;
            r.prob = getLogProb(chunkWords, t.termID);
            results.add(r);
        }
        Collections.sort(results, new ResultComparator());
        for (int idx=0; idx<k; idx++){
            terms.add(new Term(results.get(idx).termID, results.get(idx).prob, utils.Utils.inferOntology(results.get(idx).termID)));
        }
        return terms;
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

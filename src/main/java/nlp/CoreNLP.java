/*
 * Ian Braun
 * irbraun@iastate.edu
 * term-mapping 
 */
package nlp;

import config.Config;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;
import java.util.Scanner;

/**
 *
 * @author irbraun
 */
public class CoreNLP {
 
    private static StanfordCoreNLP pipeline;
    private static HashSet<String> stopwords; 
    
    public static void setup() throws FileNotFoundException, IOException{
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,parse,depparse");
        props.setProperty("coref.algorithm", "neural");
        pipeline = new StanfordCoreNLP(props);
        readInStopWords();
    }
    
    
    public static StanfordCoreNLP getPipeline(){
        return pipeline;
    }
    
    
    
    
    
    
    
    
    
    
    private static void readInStopWords() throws FileNotFoundException, IOException{
        File f = new File(Config.stopWordsPath);
        stopwords = new HashSet<>();
        Scanner scanner = new Scanner(f);
        while (scanner.hasNextLine()){
            String line = scanner.nextLine();
            String stopword = line.trim();
            stopwords.add(stopword);
        }
        scanner.close();
    }
    
    
    public static boolean isNotStopWord(String word){
        return !stopwords.contains(word);
    }
    
    public static ArrayList<String> removeStopWords(ArrayList<String> words){
        ArrayList<String> wordsWithoutStopWords = new ArrayList<>();
        for (String word: words){
            if (!stopwords.contains(word)){
                wordsWithoutStopWords.add(word);
            }
        }
        return wordsWithoutStopWords;
    }
    
    public static HashSet<String> removeStopWords(HashSet<String> words){
        HashSet<String> wordsWithoutStopWords = new HashSet<>();
        for (String word: words){
            if (!stopwords.contains(word)){
                wordsWithoutStopWords.add(word);
            }
        }
        return wordsWithoutStopWords;
    }
    
    
    
    
    
    public static ArrayList<String> removeSingleCharWords(ArrayList<String> words){
         ArrayList<String> wordsWithoutStopWords = new ArrayList<>();
        for (String word: words){
            if (word.length() >= 2){
                wordsWithoutStopWords.add(word);
            }
        }
        return wordsWithoutStopWords;
    } 
}

/*
 * Ian Braun
 * irbraun@iastate.edu
 * term-mapping 
 */
package nlp;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.trees.Tree;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores the different data structure obtained by the core NLP pipeline for a particular sentence.
 * @author irbraun
 */
public class MyAnnotation {
    
    public CoreSentence sentence;
    public Tree constituencyTree;
    public SemanticGraph dependencyGraph;
    public List<String> posTags;
    public List<String> lemmas;
    public List<Token> tokens;
    
    
    
    
    
    public MyAnnotation(CoreSentence sentence){
        Sentence simpleSentence = new Sentence(sentence.coreMap());
        constituencyTree = sentence.constituencyParse();
        dependencyGraph = sentence.dependencyParse();
        posTags = sentence.posTags();
        tokens = new ArrayList<>();
        lemmas = simpleSentence.lemmas();
        for(int idx=1; idx<=dependencyGraph.size(); idx++){
            String graphNode = dependencyGraph.getNodeByIndex(idx).originalText();
            Token token = new Token();
            token.idxWord = dependencyGraph.getNodeByIndex(idx);
            token.nodeText = token.idxWord.originalText();
            token.posTag = posTags.get(idx-1);
            token.posIndex = idx-1;
            token.depIndex = idx;
            token.lemma = simpleSentence.lemma(idx-1);
            tokens.add(token);
        }
        
        
    }
    
    public static class Token{
        public IndexedWord idxWord;
        public String nodeText;
        public String posTag;
        public String lemma;
        public int posIndex;
        public int depIndex;
    }
    
    
    
   
    
 
}

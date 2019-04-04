package structure;


import config.Config;
import enums.Species;
import enums.TextDatatype;
import java.util.ArrayList;
import nlp.CoreNLP;


public class Chunk {
    
    private final ArrayList<Word> bagOfWords;
    private final String rawText;
    public final int chunkID;
    public final TextDatatype textType;
    public final Species species;
    public final String geneIdentifier;
    
    /* No longer used, species and gene identifer are always specified for any text data type.
    public Chunk(int chunkID, TextDatatype textType, String rawText){
        this.chunkID = chunkID;
        this.textType = textType;
        this.rawText = rawText;
        this.species = Species.UNKNOWN;
        this.bagOfWords = populateBagOfWords(rawText);
        this.geneIdentifier = null;
    }
    */

    /* No longer used, species and gene identifer are always specified for any text data type.
    public Chunk(int chunkID, TextDatatype textType, String rawText, Species species){
        this.chunkID = chunkID;
        this.textType = textType;
        this.rawText = rawText;
        this.species = species;
        this.bagOfWords = populateBagOfWords(rawText);
        this.geneIdentifier = null;
    }
    */
    
    public Chunk(int chunkID, TextDatatype textType, String rawText, Species species, String geneIdentifier){
        this.chunkID = chunkID;
        this.textType = textType;
        this.rawText = rawText;
        this.species = species;
        this.bagOfWords = populateBagOfWords(rawText);
        this.geneIdentifier = geneIdentifier;
    }
    
    
    
    private ArrayList<Word> populateBagOfWords(String text){
        ArrayList<Word> words = new ArrayList<>();
        for (String word: text.toLowerCase().trim().split("\\s+|_|-|,")){
            if (!word.equals("")){
                if ((Config.removeStopWords && CoreNLP.isNotStopWord(word)) || !Config.removeStopWords){
                    words.add(new Word(word,"default_tag"));
                }
            }
        }
        return words;
    }
    
    
    

    public ArrayList<Word> getBagOfWords(){
        return bagOfWords;
    }
    
    public ArrayList<String> getBagValues(){
        ArrayList<String> bagValues = new ArrayList<>();
        for (Word w: bagOfWords){
            bagValues.add(w.value);
        }
        return bagValues;
    }
    
    public String getRawText(){
        return rawText;
    }
    
    
    
    

    
}

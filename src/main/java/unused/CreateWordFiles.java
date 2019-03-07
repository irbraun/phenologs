/*
 * Ian Braun
 * irbraun@iastate.edu
 * term-mapping 
 */
package unused;

import config.Config;
import enums.TextDatatype;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import main.Partitions;
import structure.Chunk;
import text.Text;

/**
 *
 * @author irbraun
 */
public class CreateWordFiles {
    
   
    /**
     * Generate a text file containing all the words which are in the complete text 
     * data to be run. The format of the text file is just one line per word, without
     * duplicates where words are mentioned more than once in the data. These words
     * will include whatever preprocessing is done to the text chunks like removing 
     * stopwords or lemmatizing or stemming and making all lowercase.
     * @param path
     * @throws SQLException
     * @throws FileNotFoundException 
     */
    /*
    public static void create(String path) throws SQLException, FileNotFoundException, Exception{
        Text text = new Text();
        Partitions parts = new Partitions(text, Config.typePartitions);
        List<Chunk> chunks = parts.getChunksInPartitionRangeInclusive(0, Config.numPartitions-1, text.getAllAtomChunks());
        String filename = path;
        File file = new File(filename);
        PrintWriter wordsPrinter = new PrintWriter(file);
        HashSet<String> words = new HashSet<>();
        for (Chunk c: chunks){
            for (String word: c.getBagValues()){
                words.add(word);
            }
        }
        for (String word: words){
            wordsPrinter.println(word);
        }
        wordsPrinter.close();
    }
    */
    
    
    
    
}

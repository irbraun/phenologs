/*
 * Ian Braun
 * irbraun@iastate.edu
 * term-mapping 
 */
package text;

import config.Config;
import enums.TextDatatype;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import static main.Main.logger;
import main.Partitions;
import structure.Chunk;

/**
 *
 * @author irbraun
 */
public class TextUtils {
    
    
    
    
    /**
     * Generate a text file containing all the words which are in the complete text 
     * data to be run. The format of the text file is just one line per word, without
     * duplicates where words are mentioned more than once in the data. These words
     * will include whatever preprocessing is done to the text chunks like removing 
     * stopwords or lemmatizing or stemming and making all lowercase.
     * @param path
     * @throws SQLException
     * @throws FileNotFoundException
     * @throws Exception 
     */
    public static void createWordFiles(String path) throws SQLException, FileNotFoundException, Exception{
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
    
    
    
    
    
    /**
     * Generate one text file for each chunk. Uses the naming scheme of 
     * [chunk ID].txt for all of the files. The string passed in is the 
     * head directory where the of the individual text files should be put.
     * This is useful for the external tools that take text files as input
     * and then produce annotations for a single file, allows for keeping
     * the chunks separate during this process.
     * @param dir
     * @param threshold
     * @throws SQLException
     * @throws FileNotFoundException 
     */
    public static void createTextFilesForAllChunks(String dir, double threshold, TextDatatype format) throws SQLException, FileNotFoundException, Exception{
        // Read in the synonym mappings that are the results of the word embedding process.
        HashMap<String,ArrayList<String>> w2vs = new HashMap<>();
        if (Config.useEmbeddings){
            File file = new File("/Users/irbraun/NetBeansProjects/term-mapping/path/data/allpairs.txt");
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    String w1 = parts[0];
                    String w2 = parts[1];
                    double similarity = Double.valueOf(parts[2]);
                    if (similarity >= threshold){
                        ArrayList<String> a = w2vs.getOrDefault(w1, new ArrayList<>());
                        a.add(w2);
                        w2vs.put(w1, a);
                        ArrayList<String> b = w2vs.getOrDefault(w2, new ArrayList<>());
                        b.add(w1);
                        w2vs.put(w2, b);
                    }
                }
                System.out.println("done reading in dict");
            }
            catch (Exception e){
                logger.info("file containing word embedding results could not be read");
            }
            
            Text text = new Text();
            List<Chunk> chunks;
            switch(format){
            case PHENE: 
                chunks = text.getAllAtomChunks();
                break;
            case PHENOTYPE:
                chunks = text.getAllPhenotypeChunks();
                break;
            case SPLIT_PHENOTYPE:
                chunks = text.getAllSplitPhenotypeChunks();
                break;
            default:
                throw new Exception();
            }

            for (Chunk c: chunks){
                File outputFile = new File(String.format("%s%s.txt",dir,c.chunkID));
                PrintWriter writer = new PrintWriter(outputFile);
                writer.println(c.getRawText());

                // Append all other variations that could be possible using the allowed synonyms at this threshold value.
                for (int w=0; w<c.getBagValues().size(); w++){
                    String word = c.getBagValues().get(w);
                    ArrayList<String> synonyms = w2vs.getOrDefault(word, new ArrayList<>());
                    for (int i=0; i<synonyms.size(); i++){
                        StringBuilder sb = new StringBuilder();
                        for (int j=0; j<c.getBagValues().size(); j++){
                            if (j!=w){
                                sb.append(c.getBagValues().get(j));
                                sb.append(" ");
                            }
                            else {
                                sb.append(synonyms.get(i));
                                sb.append(" ");
                            }
                        }
                        writer.println(sb.toString());
                    }
                }
                writer.close();
            }
        }
        
        // Ignore the results of the word embedding process completely.
        else{ 
            Text text = new Text();
            List<Chunk> chunks = text.getAllAtomChunks();
            for (Chunk c: chunks){
                File outputFile = new File(String.format("%s%s.txt",dir,c.chunkID));
                PrintWriter writer = new PrintWriter(outputFile);
                writer.print(c.getRawText());
                writer.close();
            }
        }        
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}

/*
 * Ian Braun
 * irbraun@iastate.edu
 * term-mapping 
 */
package locus;

import config.Config;
import enums.Species;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import structure.Chunk;
import text.Text;

/**
 *
 * @author irbraun
 */
public class LocusSubsets {
    
    public static void find_subsets() throws SQLException, FileNotFoundException, IOException, Exception{

        Text text = new Text();
            
        HashMap<String,ArrayList<String>> locusToSubsetMap = new HashMap<>();

        Reader in = new FileReader(Config.subsetsInputPath);
        Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
        for (CSVRecord record: records){

            // Get the categorization information for this locus.
            String locus = record.get(0);
            String group = record.get(5);
            String class_ = record.get(6);
            String subsets = record.get(7);
            
            // Sanity check over the file that defines the gene categories.
            if (locusToSubsetMap.containsKey(locus)){
                System.out.println(String.format("%s has more than one entry in the subset file", locus));
                throw new Exception();
            }
            
            // Generate a list of subsets that this locus belongs to and remember them.
            String[] subsetsArray = subsets.split(",");
            ArrayList<String> subsetsList = new ArrayList<>(Arrays.asList(subsetsArray));
            subsetsList.replaceAll(String::trim);
            subsetsList.replaceAll(new LocusClean());
            locusToSubsetMap.put(locus,subsetsList);
            
        }
        in.close();
        
        // Sanity check to make sure all Arabidopsis gene identifiers in the dataset mapped to subsets specified in this file.
        int ctrNotMapped = 0;
        int ctrMapped = 0;
        for (Chunk c: text.getAllPhenotypeChunks()){
            if (c.species.equals(Species.ARABIDOPSIS_THALIANA)){
                ArrayList<String> s = locusToSubsetMap.get(c.geneIdentifier);
                if (s==null){
                    ctrNotMapped++;
                }
                else{
                    ctrMapped++;
                }
            }
        }
        System.out.println(String.format("Of the %s Arabidopsis gene identifiers in the data, %s were mapped to subsets",ctrNotMapped+ctrMapped,ctrMapped));
        if (ctrNotMapped!=0){
            throw new Exception();
        }
        
        // Remember which text text chunks (phenotypes) belong to which subsets.
        HashMap<String,ArrayList<Integer>> subsetToChunkIDMap = new HashMap<>();
        String dtype = Config.format;
        for (Chunk c: text.getAllChunksOfDType(dtype)){
            // The default case handled here is when the gene identifer was not mapped to any subsets (true for all non-Arabidopsis genes).
            ArrayList<String> subsetsForThisChunk = locusToSubsetMap.getOrDefault(c.geneIdentifier, new ArrayList<>());
            for (String subset: subsetsForThisChunk){
                ArrayList<Integer> chunkIDs = subsetToChunkIDMap.getOrDefault(subset, new ArrayList<>());
                chunkIDs.add(c.chunkID);
                subsetToChunkIDMap.put(subset, chunkIDs);
            }
        }
        
        // Produce a file which species which chunk IDs go with which subsets.
        File outf = new File(Config.subsetsOutputPath);
        PrintWriter writer = new PrintWriter(outf);
        String header = "chunk,subset";
        writer.println(header);
        for (String subset: subsetToChunkIDMap.keySet()){
            ArrayList<Integer> chunkIDs = subsetToChunkIDMap.get(subset);
            for (Integer chunkID: chunkIDs){
                writer.println(String.format("%s,%s",chunkID,subset));
            }
        }
        writer.close();
        
    }
}

class LocusClean implements UnaryOperator<String>
{
    @Override
    public String apply(String rawSubsetStr) {
        Pattern pattern = Pattern.compile("[a-zA-Z]{3}");
        Matcher matcher = pattern.matcher(rawSubsetStr);
        matcher.find();
        String cleanedSubsetStr = matcher.group();
        return cleanedSubsetStr;          
    }
}





package text;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;


public class ReadValues {
    
   
    private final HashMap<String,Double> similarityValuesMap;
    private final String DELIM = ":";
    
    public ReadValues(String nonzeroSimilaritiesFilePath) throws FileNotFoundException, IOException{
        similarityValuesMap = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(nonzeroSimilaritiesFilePath));
        String line;
        while ((line = reader.readLine()) != null)
        {
            String[] lineValues = line.split("\\s+");
            String geneID1 = lineValues[0];
            String geneID2 = lineValues[1];
            Double similarity = Double.valueOf(lineValues[2]);
            String keyVersion1 = String.format("%s%s%s", geneID1, DELIM, geneID2);
            String keyVersion2 = String.format("%s%s%s", geneID2, DELIM, geneID1);
            similarityValuesMap.put(keyVersion1, similarity);
            similarityValuesMap.put(keyVersion2, similarity);
        }
        reader.close();   
    }
    
    public double getSimilarity(String geneID1, String geneID2){
        String key = String.format("%s%s%s", geneID1, DELIM, geneID2);
        double similarity = similarityValuesMap.getOrDefault(key, 0.000);
        return similarity;
    }
}

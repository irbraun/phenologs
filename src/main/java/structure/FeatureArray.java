package structure;

import index.PairIndices;
import index.FeatureIndices;
import config.Config;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;


public class FeatureArray {
    
    private final int numPairs;
    private final int numFeatures;
    private final double[][] featureTable;  
    private final HashMap<Integer, Attributes> attributesMap;
    private final ArrayList<Integer> usedPairIndices;
    
    
    public FeatureArray(){
        attributesMap = new HashMap<>();
        usedPairIndices = new ArrayList<>();
        numPairs = PairIndices.getSize();
        numFeatures = FeatureIndices.getSize();
        featureTable = new double[numPairs][numFeatures];
    }
    
    
    public void addVector(FeatureVector vector){
        int pairIndex = PairIndices.getIndex(vector.chunkID, vector.termID);
        for (Feature feature: vector.features){
            int featureIndex = FeatureIndices.getIndex(feature);
            featureTable[pairIndex][featureIndex] = feature.value;
        }
    }
    
    public void addAttributes(Attributes attrib){
        int pairIndex = PairIndices.getIndex(attrib.chunkID, attrib.termID);
        attributesMap.put(pairIndex, attrib);
        usedPairIndices.add(pairIndex);
    }
    
   
    public double getValue(int chunkID, String termID, Feature feature){
        int pairIndex = PairIndices.getIndex(chunkID, termID);
        int featureIndex = FeatureIndices.getIndex(feature);
        return featureTable[pairIndex][featureIndex];
    }
    
    
    public void writeData(int partition, boolean testingPart) throws FileNotFoundException{   
        
        // Removing this part that separates the output into two different folders based on whether it was undersampled or not.
        /*
        String innerFolder;
        if (testingPart){
            innerFolder = "full/";
        }
        else {
            innerFolder = "undersampled/";
        }
        Object[] data = {Config.csvPath, innerFolder, Config.csvName, partition};
        File outputFile = new File(String.format("%s%s%s.%s.csv", data));
        */
        
        
        // Open an output file for the feature array.
        Object[] data = {Config.csvPath, Config.csvName, partition};
        File outputFile = new File(String.format("%s%s.%s.csv", data));
        
        // Headers
        try (PrintWriter writer = new PrintWriter(outputFile)) {
            // Headers
            writer.print("chunk,term,");
            for (int i=0; i<featureTable[0].length; i++){
                writer.print(FeatureIndices.getAbbreviation(i)+",");
            }
            // When adding other attributions, have to modify this line.
            writer.println("response,hF1,hJac,role,partition");
            // Body
            for (Integer pairIdx: usedPairIndices){
                Attributes attrib = attributesMap.get(pairIdx);
                writer.print(attrib.chunkID + "," + attrib.termID + ",");
                // Features
                for (int i=0; i<featureTable[0].length; i++){
                    writer.print(String.format("%.3f", featureTable[pairIdx][i]) + ",");
                }
                // Target class
                if (attrib.match){
                    writer.print("1,");
                }
                else{
                    writer.print("0,");
                }
                // Other attributes. When adding other attributes, have to modify these two lines to match above.
                Object[] attributeNames = {attrib.hF1, attrib.hJac, attrib.role, attrib.partition};
                writer.println(String.format("%.3f,%.3f,%s,%s", attributeNames));
            }
            writer.close();
        }
    }
    
     
}

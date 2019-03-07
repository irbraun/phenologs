package index;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PairIndices {
    
    
    private static Map<List<String>, Integer> map;
    private static int pairNumber = 0;
    
    
    public PairIndices(){
        map = new HashMap<>();
    }
    
    
    public static void add(int chunkID, String termID){
        List pairKey = Arrays.asList(String.valueOf(chunkID), termID);
        map.put(pairKey, pairNumber);
        pairNumber++;
    }
    
    public static int getIndex(int chunkID, String termID){
        List pairKey = Arrays.asList(String.valueOf(chunkID), termID);
        return map.getOrDefault(pairKey, -1);
    }
    
    
    
    
    /*
    public static void add(String chunkID, String termID){
        List pairKey = Arrays.asList(chunkID, termID);
        map.put(pairKey, pairNumber);
        pairNumber++;
    }
    
    
    public static int getIndex(String chunkID, String termID){
        List pairKey = Arrays.asList(chunkID, termID);
        return map.getOrDefault(pairKey, -1);
        //return map.get(pairKey);
    }
    */
    
    
    
    public static int getSize(){
        return pairNumber;
    }
    
    public static Collection<Integer> getAll(){
        return map.values();
    }
    
    public static void clear(){
        pairNumber = 0;
        map.clear();
    }
    
    
}

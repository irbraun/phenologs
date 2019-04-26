

package nlp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import objects.Chunk;

/**
 * Note: Class doesn't differentiate between IDs for phenotype descriptions and atomized statements, is
 * only meant to be used for a single type of input text data.
 * @author irbraun
 */



public class MyAnnotations {
    
    HashMap<Integer,List<MyAnnotation>> chunkToAnnotMap;
    
    
    public MyAnnotations(){
        chunkToAnnotMap = new HashMap<>();
    }
    
    
    public void addAnnotation(Chunk chunk, MyAnnotation annot){
        if (chunkToAnnotMap.containsKey(chunk.chunkID)){
            chunkToAnnotMap.get(chunk.chunkID).add(annot);
        }
        else {
            chunkToAnnotMap.put(chunk.chunkID, new ArrayList<>());
            chunkToAnnotMap.get(chunk.chunkID).add(annot);
        }
    }
    
    
    public List<MyAnnotation> getAnnotations(Chunk chunk){
        return chunkToAnnotMap.get(chunk.chunkID);
    }
    
    
    public List<MyAnnotation> getAnnotations(Integer chunkID){
        return chunkToAnnotMap.get(chunkID);
    }
    
    
    
    
    
}

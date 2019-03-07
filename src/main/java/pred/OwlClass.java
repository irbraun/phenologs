/*
 * Ian Braun
 * irbraun@iastate.edu
 * term-mapping 
 */
package pred;

import java.util.HashSet;

/**
 *
 * @author irbraun
 */
public class OwlClass {
    
    
    public int chunkID;
    public String termID;
    public double value;
    public String category;
    public String role;      // fix this later to use the enum.
    public double similarity;
    public HashSet<String> nodes;
    
            
    public OwlClass(){
        
    }
    
    
    
    
    
    
}

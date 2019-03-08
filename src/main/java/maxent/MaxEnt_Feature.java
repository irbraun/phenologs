/*
 * Ian Braun
 * irbraun@iastate.edu
 * term-mapping 
 */
package maxent;

import java.util.List;

/**
 *
 * @author irbraun
 */
class MaxEnt_Feature {
 
    public final String featureWord;
    public final String featureTermID;
    private int infoCounter;
    
    public MaxEnt_Feature(String w, String termID){
        featureWord = w;
        featureTermID = termID;
        infoCounter = 1;
    }
    
    public int fires(String s, String termID){
        if(termID.equals(featureTermID) && s.contains(featureWord)){
            return 1;
        }
        return 0;
    }
    
    public int fires(List<String> s, String termID){
        if(termID.equals(featureTermID) && s.contains(featureWord)){
            return 1;
        }
        return 0;
    }
    
    public int getInfo(){
        return infoCounter;
    }
    
    public void increaseInfo(){
        infoCounter++;
    }
    
}

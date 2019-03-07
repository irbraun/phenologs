/*
 * Ian Braun
 * irbraun@iastate.edu
 * term-mapping 
 */
package nlp_algs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import static main.Main.logger;

/**
 *
 * @author irbraun
 */
public class MaxEnt_FeatureList {
    
    private final List<MaxEnt_Feature> featureList;
    private final HashMap<String,MaxEnt_Feature> bothToFeatureMap;
    private final HashMap<String,List<MaxEnt_Feature>> termIDToFeaturesMap;
    
    
    
    public MaxEnt_FeatureList(){
        featureList = new ArrayList<>();
        bothToFeatureMap = new HashMap<>();
        termIDToFeaturesMap = new HashMap<>();
    }
    
    public void addFeature(MaxEnt_Feature f){
        featureList.add(f);
        bothToFeatureMap.put(String.format("%s:%s",f.featureWord,f.featureTermID),f);
        ArrayList<MaxEnt_Feature> a = (ArrayList<MaxEnt_Feature>) termIDToFeaturesMap.getOrDefault(f.featureTermID, new ArrayList<>());
        a.add(f);
        termIDToFeaturesMap.put(f.featureTermID,a);
    }
    
    
    public MaxEnt_Feature getFeature(String key){
        return bothToFeatureMap.get(key);
    }
    
    public int getSize(){
        return featureList.size();
    }
    
    
    
    
    
    /**
     * Only retain the k most informative features that pertain to each
     * ontology term that was observed in the data. Note that only the 
     * list of features is modified as a result of this function being 
     * run, the other dictionaries are not touched and wont reflect the
     * changes made to the number of features.
     * @param k 
     */   
    public void pruneFeatureList(int k){
        int numFeaturesBefore = featureList.size();
        featureList.clear();
        List<String> termIDs = new ArrayList<>(termIDToFeaturesMap.keySet());
        for (String termID: termIDs){
            List<MaxEnt_Feature> l = termIDToFeaturesMap.get(termID);
            Collections.sort(l, new MaxEntFeatureComparator());
            List<MaxEnt_Feature> lPruned = l.subList(0, Math.min(k, l.size()));
            featureList.addAll(lPruned);
        }
        int numFeaturesAfter = featureList.size();
        logger.info(String.format("The number of features was reduced from %s to %s", numFeaturesBefore, numFeaturesAfter));
    }
    

    static class MaxEntFeatureComparator implements Comparator<MaxEnt_Feature>{
        @Override
        public int compare(MaxEnt_Feature f1, MaxEnt_Feature f2) {    
            if (f1.getInfo() < f2.getInfo()){
                return 1;
            }
            else if (f1.getInfo() > f2.getInfo()){
                return -1;
            }
            return 0;
        }
    }
    
    
    
    public List<MaxEnt_Feature> getFeatureList(){
        return featureList;
    }

    
    
    
    
    
    
    
    
}
